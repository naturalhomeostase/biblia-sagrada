package com.bibliasagrada.app.data.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE bookId=:bookId AND chapter=:chapter AND verse=:verse)")
    suspend fun isFavorite(bookId: Int, chapter: Int, verse: Int): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE bookId=:bookId AND chapter=:chapter AND verse=:verse")
    suspend fun delete(bookId: Int, chapter: Int, verse: Int)
}

@Dao
interface HighlightDao {
    @Query("SELECT * FROM highlights ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<HighlightEntity>>

    @Query("SELECT * FROM highlights WHERE bookId=:bookId AND chapter=:chapter")
    fun observeForChapter(bookId: Int, chapter: Int): Flow<List<HighlightEntity>>

    @Query("SELECT * FROM highlights WHERE bookId=:bookId AND chapter=:chapter AND verse=:verse LIMIT 1")
    suspend fun getForVerse(bookId: Int, chapter: Int, verse: Int): HighlightEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: HighlightEntity)

    @Query("DELETE FROM highlights WHERE bookId=:bookId AND chapter=:chapter AND verse=:verse")
    suspend fun delete(bookId: Int, chapter: Int, verse: Int)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<NoteEntity>>

    // Encontra uma nota cujo intervalo (verse..verseEnd) contenha o versículo
    // dado — assim, tocar em qualquer versículo dentro de uma nota de vários
    // versículos encontra a mesma nota.
    @Query("SELECT * FROM notes WHERE bookId=:bookId AND chapter=:chapter AND :verse BETWEEN verse AND verseEnd LIMIT 1")
    suspend fun getForVerse(bookId: Int, chapter: Int, verse: Int): NoteEntity?

    @Query("SELECT * FROM notes WHERE bookId=:bookId AND chapter=:chapter")
    fun observeForChapter(bookId: Int, chapter: Int): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: NoteEntity): Long

    @Update
    suspend fun update(entity: NoteEntity)

    @Delete
    suspend fun delete(entity: NoteEntity)
}

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY lastReadAt DESC LIMIT 50")
    fun observeRecent(): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: HistoryEntity)

    @Query("DELETE FROM history")
    suspend fun clear()
}

@Dao
interface ReadingProgressDao {
    @Query("SELECT * FROM reading_progress WHERE id=0 LIMIT 1")
    fun observe(): Flow<ReadingProgressEntity?>

    @Query("SELECT * FROM reading_progress WHERE id=0 LIMIT 1")
    suspend fun get(): ReadingProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entity: ReadingProgressEntity)
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE bookId=:bookId AND chapter=:chapter ORDER BY createdAt DESC")
    fun observeForChapter(bookId: Int, chapter: Int): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE bookId=:bookId ORDER BY chapter ASC")
    fun observeForBook(bookId: Int): Flow<List<BookmarkEntity>>

    /** Cor usada da última vez que esse nome de marcador foi salvo com uma cor
     *  definida — usada para pré-preencher a cor automaticamente quando o
     *  nome se repete. */
    @Query("SELECT color FROM bookmarks WHERE name=:name AND color IS NOT NULL ORDER BY createdAt DESC LIMIT 1")
    suspend fun getColorForName(name: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BookmarkEntity): Long

    @Query("DELETE FROM bookmarks WHERE id=:id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM bookmarks WHERE bookId=:bookId AND chapter=:chapter")
    suspend fun deleteForChapter(bookId: Int, chapter: Int)
}

/** Quantidade de versículos marcados com cada tag — usado para mostrar a contagem nas listas sem carregar tudo. */
data class TagCount(val tagId: Long, val count: Int)

@Dao
interface TagDao {
    @Query("SELECT * FROM tags ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun getByName(name: String): TagEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: TagEntity): Long

    @Update
    suspend fun update(entity: TagEntity)

    @Query("DELETE FROM tags WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface VerseTagDao {
    @Query(
        """
        SELECT tags.* FROM tags
        INNER JOIN verse_tags ON verse_tags.tagId = tags.id
        WHERE verse_tags.bookId = :bookId AND verse_tags.chapter = :chapter AND verse_tags.verse = :verse
        ORDER BY tags.name COLLATE NOCASE ASC
        """
    )
    fun observeTagsForVerse(bookId: Int, chapter: Int, verse: Int): Flow<List<TagEntity>>

    @Query("SELECT * FROM verse_tags WHERE tagId = :tagId ORDER BY bookId ASC, chapter ASC, verse ASC")
    fun observeVersesForTag(tagId: Long): Flow<List<VerseTagEntity>>

    @Query("SELECT * FROM verse_tags WHERE tagId = :tagId ORDER BY bookId ASC, chapter ASC, verse ASC")
    suspend fun getForTag(tagId: Long): List<VerseTagEntity>

    /** Cor de cada tag aplicada, por versículo, num capítulo inteiro — usado para desenhar
     *  o símbolo de tag (colorido) ao final de cada versículo marcado, na leitura. */
    @Query(
        """
        SELECT verse_tags.verse as verse, tags.color as color FROM verse_tags
        INNER JOIN tags ON tags.id = verse_tags.tagId
        WHERE verse_tags.bookId = :bookId AND verse_tags.chapter = :chapter
        ORDER BY verse_tags.verse ASC, tags.name COLLATE NOCASE ASC
        """
    )
    fun observeColorsForChapter(bookId: Int, chapter: Int): Flow<List<VerseTagColorRow>>

    @Query("SELECT tagId, COUNT(*) as count FROM verse_tags GROUP BY tagId")
    fun observeCounts(): Flow<List<TagCount>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: VerseTagEntity)

    @Query("DELETE FROM verse_tags WHERE tagId = :tagId AND bookId = :bookId AND chapter = :chapter AND verse = :verse")
    suspend fun delete(tagId: Long, bookId: Int, chapter: Int, verse: Int)

    @Query("DELETE FROM verse_tags WHERE tagId = :tagId")
    suspend fun deleteAllForTag(tagId: Long)
}

/** Linha crua devolvida por [VerseTagDao.observeColorsForChapter]. */
data class VerseTagColorRow(val verse: Int, val color: String)

@Dao
interface PartialHighlightDao {
    @Query("SELECT * FROM partial_highlights WHERE bookId = :bookId AND chapter = :chapter")
    fun observeForChapter(bookId: Int, chapter: Int): Flow<List<PartialHighlightEntity>>

    @Query("SELECT * FROM partial_highlights WHERE bookId = :bookId AND chapter = :chapter AND verse = :verse")
    suspend fun getForVerse(bookId: Int, chapter: Int, verse: Int): List<PartialHighlightEntity>

    @Insert
    suspend fun insert(entity: PartialHighlightEntity): Long

    @Update
    suspend fun update(entity: PartialHighlightEntity)

    @Query("DELETE FROM partial_highlights WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM partial_highlights WHERE bookId = :bookId AND chapter = :chapter AND verse = :verse")
    suspend fun deleteAllForVerse(bookId: Int, chapter: Int, verse: Int)
}
