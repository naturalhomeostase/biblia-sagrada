package com.bibliasagrada.app.data.repository

import android.content.Context
import com.bibliasagrada.app.data.db.BibleDatabaseHelper
import com.bibliasagrada.app.data.model.Book
import com.bibliasagrada.app.data.model.ChapterRef
import com.bibliasagrada.app.data.model.SearchResult
import com.bibliasagrada.app.data.model.Translation
import com.bibliasagrada.app.data.model.Verse
import com.bibliasagrada.app.data.room.BookmarkEntity
import com.bibliasagrada.app.data.room.FavoriteEntity
import com.bibliasagrada.app.data.room.HighlightEntity
import com.bibliasagrada.app.data.room.HistoryEntity
import com.bibliasagrada.app.data.room.NoteEntity
import com.bibliasagrada.app.data.room.PartialHighlightEntity
import com.bibliasagrada.app.data.room.ReadingProgressEntity
import com.bibliasagrada.app.data.room.TagEntity
import com.bibliasagrada.app.data.room.UserDataDatabase
import com.bibliasagrada.app.data.room.VerseTagEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class BibleRepository(private val appContext: Context) {

    private var userDb = UserDataDatabase.getInstance(appContext)
    val prefs = PreferencesManager(appContext)

    private val initialTranslation: Translation = runBlocking {
        val savedFileName = prefs.activeTranslationFileName.first()
        val candidate = TranslationsCatalog.byFileName(savedFileName)
        if (candidate.builtIn || TranslationDownloadManager.isDownloaded(appContext, candidate)) {
            candidate
        } else {
            TranslationsCatalog.BLIVRE
        }
    }

    @Volatile
    private var bibleDb: BibleDatabaseHelper = BibleDatabaseHelper.open(
        appContext,
        initialTranslation.fileName,
        isBuiltIn = initialTranslation.builtIn
    )

    private val _activeTranslation = MutableStateFlow(initialTranslation)
    /** Tradução atualmente aberta para leitura. Observe para reagir a trocas de tradução. */
    val activeTranslation: StateFlow<Translation> = _activeTranslation.asStateFlow()

    /**
     * Troca a tradução ativa. O arquivo precisa já estar disponível localmente
     * (embutido ou já baixado — ver TranslationDownloadManager.isDownloaded).
     */
    suspend fun switchTranslation(translation: Translation) = withContext(Dispatchers.IO) {
        val newDb = BibleDatabaseHelper.open(appContext, translation.fileName, translation.builtIn)
        val old = bibleDb
        bibleDb = newDb
        old.close()
        prefs.setActiveTranslationFileName(translation.fileName)
        _activeTranslation.value = translation
    }

    // --- Leitura da Bíblia ---
    // Todas as funções abaixo tocam o SQLite bruto (BibleDatabaseHelper) de forma síncrona.
    // São expostas como `suspend` e sempre executadas em Dispatchers.IO para nunca travar
    // a main thread — foi isso que causava o ANR "Application does not have a focused window".
    suspend fun getBooks(): List<Book> = withContext(Dispatchers.IO) { bibleDb.getBooks() }
    suspend fun getBook(bookId: Int): Book? = withContext(Dispatchers.IO) { bibleDb.getBook(bookId) }
    suspend fun getChapter(bookId: Int, chapter: Int): List<Verse> =
        withContext(Dispatchers.IO) { bibleDb.getChapter(bookId, chapter) }
    suspend fun getVerse(bookId: Int, chapter: Int, verse: Int): Verse? =
        withContext(Dispatchers.IO) { bibleDb.getVerse(bookId, chapter, verse) }
    suspend fun getVerseById(id: Long): Verse? = withContext(Dispatchers.IO) { bibleDb.getVerseById(id) }
    suspend fun searchWords(query: String, testament: String? = null): List<SearchResult> =
        withContext(Dispatchers.IO) { bibleDb.searchWords(query, testament) }
    suspend fun searchByReference(query: String): Verse? =
        withContext(Dispatchers.IO) { bibleDb.searchByReference(query) }

    /** Lista contínua de todos os capítulos da Bíblia, na ordem canônica, para navegação por gesto (swipe). */
    suspend fun getAllChapterRefs(): List<ChapterRef> = withContext(Dispatchers.IO) {
        val refs = mutableListOf<ChapterRef>()
        for (book in bibleDb.getBooks()) {
            for (chapter in 1..book.chapterCount) {
                refs.add(ChapterRef(book.id, chapter, book.name, book.abbrev))
            }
        }
        refs
    }

    // --- Favoritos ---
    fun observeFavorites(): Flow<List<FavoriteEntity>> = userDb.favoriteDao().observeAll()
    suspend fun isFavorite(bookId: Int, chapter: Int, verse: Int) =
        userDb.favoriteDao().isFavorite(bookId, chapter, verse)
    suspend fun toggleFavorite(bookId: Int, chapter: Int, verse: Int) {
        if (userDb.favoriteDao().isFavorite(bookId, chapter, verse)) {
            userDb.favoriteDao().delete(bookId, chapter, verse)
        } else {
            userDb.favoriteDao().insert(FavoriteEntity(bookId, chapter, verse))
        }
    }

    // --- Destaques (highlights) ---
    fun observeHighlightsForChapter(bookId: Int, chapter: Int): Flow<List<HighlightEntity>> =
        userDb.highlightDao().observeForChapter(bookId, chapter)
    fun observeAllHighlights(): Flow<List<HighlightEntity>> = userDb.highlightDao().observeAll()
    suspend fun getHighlightForVerse(bookId: Int, chapter: Int, verse: Int): String? =
        userDb.highlightDao().getForVerse(bookId, chapter, verse)?.color
    suspend fun setHighlight(bookId: Int, chapter: Int, verse: Int, color: String?) {
        if (color == null) {
            userDb.highlightDao().delete(bookId, chapter, verse)
        } else {
            userDb.highlightDao().insert(HighlightEntity(bookId, chapter, verse, color))
        }
    }

    // --- Notas ---
    fun observeAllNotes(): Flow<List<NoteEntity>> = userDb.noteDao().observeAll()
    fun observeNotesForChapter(bookId: Int, chapter: Int): Flow<List<NoteEntity>> =
        userDb.noteDao().observeForChapter(bookId, chapter)
    /** Encontra a nota (de 1 ou vários versículos) que cobre esse versículo, se houver. */
    suspend fun getNoteForVerse(bookId: Int, chapter: Int, verse: Int): NoteEntity? =
        userDb.noteDao().getForVerse(bookId, chapter, verse)
    suspend fun saveNote(existing: NoteEntity?, bookId: Int, chapter: Int, verseStart: Int, verseEnd: Int, text: String) {
        if (text.isBlank()) {
            existing?.let { userDb.noteDao().delete(it) }
            return
        }
        if (existing != null) {
            userDb.noteDao().update(
                existing.copy(verse = verseStart, verseEnd = verseEnd, text = text, updatedAt = System.currentTimeMillis())
            )
        } else {
            userDb.noteDao().insert(
                NoteEntity(bookId = bookId, chapter = chapter, verse = verseStart, verseEnd = verseEnd, text = text)
            )
        }
    }

    // --- Histórico ---
    fun observeHistory(): Flow<List<HistoryEntity>> = userDb.historyDao().observeRecent()
    suspend fun recordHistory(bookId: Int, chapter: Int) {
        userDb.historyDao().insert(HistoryEntity(bookId, chapter))
    }

    // --- Continuar de onde parei ---
    fun observeReadingProgress(): Flow<ReadingProgressEntity?> = userDb.readingProgressDao().observe()
    suspend fun getReadingProgress(): ReadingProgressEntity? = userDb.readingProgressDao().get()
    suspend fun saveReadingProgress(bookId: Int, chapter: Int, verse: Int) {
        userDb.readingProgressDao().save(ReadingProgressEntity(0, bookId, chapter, verse))
    }

    // --- Marcadores de página (bookmarks nomeados) ---
    fun observeBookmarks(): Flow<List<BookmarkEntity>> = userDb.bookmarkDao().observeAll()
    fun observeBookmarksForChapter(bookId: Int, chapter: Int): Flow<List<BookmarkEntity>> =
        userDb.bookmarkDao().observeForChapter(bookId, chapter)
    fun observeBookmarksForBook(bookId: Int): Flow<List<BookmarkEntity>> =
        userDb.bookmarkDao().observeForBook(bookId)
    /** Ids dos livros que têm ao menos um marcador — para mostrar o ícone na lista de livros. */
    fun observeBookmarkedBookIds(): Flow<Set<Int>> =
        userDb.bookmarkDao().observeAll().map { list -> list.map { it.bookId }.toSet() }
    /** bookId -> cor do marcador, só quando o livro tem EXATAMENTE um marcador com cor definida
     *  (quando há mais de um marcador no mesmo livro, o ícone usa a cor padrão do tema). */
    fun observeSingleBookmarkColorByBook(): Flow<Map<Int, String?>> =
        userDb.bookmarkDao().observeAll().map { list ->
            list.groupBy { it.bookId }
                .filterValues { it.size == 1 }
                .mapValues { (_, bookmarks) -> bookmarks.first().color }
        }
    suspend fun addBookmark(bookId: Int, chapter: Int, name: String, color: String? = null) {
        userDb.bookmarkDao().insert(BookmarkEntity(bookId = bookId, chapter = chapter, name = name, color = color))
    }
    /** Cor lembrada para esse nome de marcador (da última vez que foi usado com cor), ou null. */
    suspend fun getBookmarkColorForName(name: String): String? =
        if (name.isBlank()) null else userDb.bookmarkDao().getColorForName(name)
    suspend fun removeBookmark(id: Long) {
        userDb.bookmarkDao().deleteById(id)
    }
    suspend fun removeBookmarksForChapter(bookId: Int, chapter: Int) {
        userDb.bookmarkDao().deleteForChapter(bookId, chapter)
    }

    // --- Tags de estudo (marcação temática de versículos, ex.: "Salvação") ---
    fun observeAllTags(): Flow<List<TagEntity>> = userDb.tagDao().observeAll()
    /** Tags do versículo, reativo — atualiza sozinho quando uma tag é adicionada/removida dele. */
    fun observeTagsForVerse(bookId: Int, chapter: Int, verse: Int): Flow<List<TagEntity>> =
        userDb.verseTagDao().observeTagsForVerse(bookId, chapter, verse)
    /** bookId/chapter/verse cobertos por cada tag, reativo — para a tela de listagem por tag. */
    fun observeVersesForTag(tagId: Long): Flow<List<VerseTagEntity>> = userDb.verseTagDao().observeVersesForTag(tagId)
    suspend fun getVersesForTag(tagId: Long): List<VerseTagEntity> = userDb.verseTagDao().getForTag(tagId)
    /** tagId -> quantidade de versículos marcados com ela. */
    fun observeTagCounts(): Flow<Map<Long, Int>> =
        userDb.verseTagDao().observeCounts().map { counts -> counts.associate { it.tagId to it.count } }
    /** número do versículo -> lista de cores das tags aplicadas a ele, para um capítulo inteiro
     *  (um versículo pode ter mais de uma tag, cada uma com sua cor) — usado para desenhar o
     *  símbolo de tag colorido ao final do versículo na leitura. */
    fun observeTagColorsForChapter(bookId: Int, chapter: Int): Flow<Map<Int, List<String>>> =
        userDb.verseTagDao().observeColorsForChapter(bookId, chapter)
            .map { rows -> rows.groupBy({ it.verse }, { it.color }) }

    /** Acha a tag pelo nome (sem diferenciar maiúsculas/minúsculas) ou cria uma nova com a cor dada. */
    suspend fun getOrCreateTag(name: String, color: String): Long {
        val trimmed = name.trim()
        userDb.tagDao().getByName(trimmed)?.let { return it.id }
        val inserted = userDb.tagDao().insert(TagEntity(name = trimmed, color = color))
        // Em corrida rara (duas criações "ao mesmo tempo" com o mesmo nome), o insert é
        // ignorado (OnConflictStrategy.IGNORE) e devolve -1 — nesse caso busca de novo.
        return if (inserted > 0) inserted else (userDb.tagDao().getByName(trimmed)?.id ?: inserted)
    }

    /** Aplica uma tag (já existente, por id) a um ou mais versículos de uma vez. */
    suspend fun addTagToVerses(tagId: Long, verses: List<Verse>) {
        verses.forEach { v ->
            userDb.verseTagDao().insert(VerseTagEntity(tagId = tagId, bookId = v.bookId, chapter = v.chapter, verse = v.verse))
        }
    }

    /** Remove uma tag de um único versículo (não apaga a tag em si, só essa associação). */
    suspend fun removeVerseTag(tagId: Long, bookId: Int, chapter: Int, verse: Int) {
        userDb.verseTagDao().delete(tagId, bookId, chapter, verse)
    }

    /** Renomeia e/ou muda a cor de uma tag. Se o novo nome já pertencer a outra
     *  tag existente, em vez de criar um nome duplicado confuso, mescla os
     *  versículos desta tag na tag já existente (e apaga esta). */
    suspend fun updateTag(tag: TagEntity, newName: String, newColor: String) {
        val trimmed = newName.trim()
        val existingWithSameName = userDb.tagDao().getByName(trimmed)
        if (existingWithSameName != null && existingWithSameName.id != tag.id) {
            val versesToMove = userDb.verseTagDao().getForTag(tag.id)
            versesToMove.forEach { ref ->
                userDb.verseTagDao().insert(ref.copy(tagId = existingWithSameName.id))
            }
            userDb.verseTagDao().deleteAllForTag(tag.id)
            userDb.tagDao().deleteById(tag.id)
            userDb.tagDao().update(existingWithSameName.copy(color = newColor))
        } else {
            userDb.tagDao().update(tag.copy(name = trimmed, color = newColor))
        }
    }

    /** Apaga a tag inteira e todas as marcações de versículos associadas a ela. */
    suspend fun deleteTag(tagId: Long) {
        userDb.verseTagDao().deleteAllForTag(tagId)
        userDb.tagDao().deleteById(tagId)
    }

    // --- Realce de apenas um trecho do versículo (diferente da marcação do versículo inteiro) ---
    fun observePartialHighlightsForChapter(bookId: Int, chapter: Int): Flow<List<PartialHighlightEntity>> =
        userDb.partialHighlightDao().observeForChapter(bookId, chapter)

    /** Realça só um trecho [start, end) do texto do versículo. Se o trecho escolhido colidir
     *  com um realce parcial já existente nesse versículo, o(s) trecho(s) antigos que se
     *  sobrepõem são removidos primeiro, para não empilhar cores confusas um sobre o outro. */
    suspend fun setPartialHighlight(bookId: Int, chapter: Int, verse: Int, start: Int, end: Int, color: String) {
        if (end <= start) return
        val existing = userDb.partialHighlightDao().getForVerse(bookId, chapter, verse)
        existing.filter { it.startOffset < end && start < it.endOffset }.forEach {
            userDb.partialHighlightDao().deleteById(it.id)
        }
        userDb.partialHighlightDao().insert(
            PartialHighlightEntity(bookId = bookId, chapter = chapter, verse = verse, startOffset = start, endOffset = end, color = color)
        )
    }

    /** Remove qualquer realce parcial que se sobreponha ao trecho [start, end), sem aplicar nenhum novo. */
    suspend fun removePartialHighlightsInRange(bookId: Int, chapter: Int, verse: Int, start: Int, end: Int) {
        if (end <= start) return
        val existing = userDb.partialHighlightDao().getForVerse(bookId, chapter, verse)
        existing.filter { it.startOffset < end && start < it.endOffset }.forEach {
            userDb.partialHighlightDao().deleteById(it.id)
        }
    }

    suspend fun removePartialHighlight(id: Long) {
        userDb.partialHighlightDao().deleteById(id)
    }

    // --- Versículo do dia ---
    /**
     * Escolhe o versículo de hoje para esta pessoa: cada instalação tem sua
     * própria "semente" (gerada uma vez, ver PreferencesManager), usada para
     * embaralhar a lista de forma diferente para cada usuário. O dia do ano
     * decide a posição dentro dessa lista embaralhada — então o mesmo
     * versículo aparece o dia inteiro, mas muda à meia-noite, e não se repete
     * dentro do ciclo (há bem mais de 365 versículos no catálogo).
     */
    suspend fun getDailyVerse(): Verse? = withContext(Dispatchers.IO) {
        val baseSeed = prefs.getOrCreateDailyVerseSeed()
        val list = DailyVersesCatalog.ALL
        val daysSinceEpoch = java.time.LocalDate.now().toEpochDay()
        val cycleLength = list.size.toLong()
        // Cada ciclo completo pelos versículos ganha uma nova embaralhada
        // (em vez de repetir a mesma ordem para sempre), então "quando todos
        // acabarem, recomeçam de forma aleatória de novo".
        val cycleNumber = daysSinceEpoch / cycleLength
        val positionInCycle = (daysSinceEpoch % cycleLength).toInt()
        val shuffleSeed = baseSeed xor cycleNumber
        val shuffled = list.shuffled(kotlin.random.Random(shuffleSeed))
        val ref = shuffled[positionInCycle]
        bibleDb.getVerse(ref.bookId, ref.chapter, ref.verse)
    }

    /** Se o "Versículo do dia" deve aparecer sozinho agora: só uma vez por dia, e só depois das 6h. */
    suspend fun shouldAutoShowDailyVerse(): Boolean {
        val now = java.time.LocalDateTime.now()
        if (now.hour < 6) return false
        val today = now.toLocalDate().toString()
        val lastShown = prefs.lastDailyVerseShownDate.first()
        return lastShown != today
    }

    suspend fun markDailyVerseShownToday() {
        val today = java.time.LocalDate.now().toString()
        prefs.setLastDailyVerseShownDate(today)
    }

    companion object {
        @Volatile private var instance: BibleRepository? = null
        fun getInstance(context: Context): BibleRepository =
            instance ?: synchronized(this) {
                instance ?: BibleRepository(context.applicationContext).also { instance = it }
            }
    }

    // --- Backup e restauração (ver util/BackupManager.kt) ---

    /** Garante que tudo que foi escrito recentemente já está no arquivo principal do banco (não só no WAL), antes de copiar para o backup. */
    suspend fun checkpointUserDatabaseForBackup() = withContext(Dispatchers.IO) {
        userDb.openHelper.writableDatabase.execSQL("PRAGMA wal_checkpoint(FULL)")
    }

    /** Fecha a conexão com o banco do usuário — necessário antes de sobrescrever o arquivo ao restaurar um backup. */
    fun closeUserDatabaseForRestore() {
        UserDataDatabase.closeAndReset()
    }

    /** Reabre o banco do usuário depois que o arquivo foi substituído (ou se a restauração falhar). */
    fun reopenUserDatabaseAfterRestore() {
        userDb = UserDataDatabase.getInstance(appContext)
    }
}
