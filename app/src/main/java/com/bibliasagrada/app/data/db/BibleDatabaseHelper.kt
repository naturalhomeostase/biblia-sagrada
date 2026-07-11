package com.bibliasagrada.app.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.bibliasagrada.app.data.model.Book
import com.bibliasagrada.app.data.model.SearchResult
import com.bibliasagrada.app.data.model.Verse
import java.io.File
import java.io.FileOutputStream
import java.text.Normalizer

/**
 * Acesso de leitura ao banco SQLite de uma tradução da Bíblia.
 *
 * A tradução embutida (Bíblia Livre) vem nos assets do APK e é copiada para o
 * armazenamento interno na primeira execução. Traduções adicionais, baixadas
 * pelo usuário (ver TranslationDownloadManager), já chegam prontas nesse mesmo
 * diretório e só precisam ser abertas.
 */
class BibleDatabaseHelper private constructor(dbFile: File) {

    private val db: SQLiteDatabase = SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY)

    /** Fecha a conexão com o banco. Deve ser chamado ao trocar de tradução. */
    fun close() {
        db.close()
    }

    fun getBooks(): List<Book> {
        val list = mutableListOf<Book>()
        db.rawQuery("SELECT id, code, name, abbrev, testament, chapter_count FROM books ORDER BY id", null).use { c ->
            while (c.moveToNext()) {
                list.add(
                    Book(
                        id = c.getInt(0),
                        code = c.getString(1),
                        name = c.getString(2),
                        abbrev = c.getString(3),
                        testament = c.getString(4),
                        chapterCount = c.getInt(5)
                    )
                )
            }
        }
        return list
    }

    fun getBook(bookId: Int): Book? {
        db.rawQuery(
            "SELECT id, code, name, abbrev, testament, chapter_count FROM books WHERE id=?",
            arrayOf(bookId.toString())
        ).use { c ->
            if (c.moveToFirst()) {
                return Book(
                    id = c.getInt(0), code = c.getString(1), name = c.getString(2),
                    abbrev = c.getString(3), testament = c.getString(4), chapterCount = c.getInt(5)
                )
            }
        }
        return null
    }

    fun getVerseCountInChapter(bookId: Int, chapter: Int): Int {
        db.rawQuery(
            "SELECT COUNT(*) FROM verses WHERE book_id=? AND chapter=?",
            arrayOf(bookId.toString(), chapter.toString())
        ).use { c ->
            if (c.moveToFirst()) return c.getInt(0)
        }
        return 0
    }

    fun getChapter(bookId: Int, chapter: Int): List<Verse> {
        val list = mutableListOf<Verse>()
        db.rawQuery(
            """SELECT v.id, v.book_id, b.name, b.abbrev, v.chapter, v.verse, v.text
               FROM verses v JOIN books b ON b.id = v.book_id
               WHERE v.book_id=? AND v.chapter=? ORDER BY v.verse""",
            arrayOf(bookId.toString(), chapter.toString())
        ).use { c ->
            while (c.moveToNext()) {
                list.add(
                    Verse(
                        id = c.getLong(0), bookId = c.getInt(1), bookName = c.getString(2),
                        bookAbbrev = c.getString(3), chapter = c.getInt(4), verse = c.getInt(5),
                        text = c.getString(6)
                    )
                )
            }
        }
        return list
    }

    fun getVerse(bookId: Int, chapter: Int, verse: Int): Verse? {
        db.rawQuery(
            """SELECT v.id, v.book_id, b.name, b.abbrev, v.chapter, v.verse, v.text
               FROM verses v JOIN books b ON b.id = v.book_id
               WHERE v.book_id=? AND v.chapter=? AND v.verse=?""",
            arrayOf(bookId.toString(), chapter.toString(), verse.toString())
        ).use { c ->
            if (c.moveToFirst()) {
                return Verse(
                    id = c.getLong(0), bookId = c.getInt(1), bookName = c.getString(2),
                    bookAbbrev = c.getString(3), chapter = c.getInt(4), verse = c.getInt(5),
                    text = c.getString(6)
                )
            }
        }
        return null
    }

    fun getVerseById(id: Long): Verse? {
        db.rawQuery(
            """SELECT v.id, v.book_id, b.name, b.abbrev, v.chapter, v.verse, v.text
               FROM verses v JOIN books b ON b.id = v.book_id WHERE v.id=?""",
            arrayOf(id.toString())
        ).use { c ->
            if (c.moveToFirst()) {
                return Verse(
                    id = c.getLong(0), bookId = c.getInt(1), bookName = c.getString(2),
                    bookAbbrev = c.getString(3), chapter = c.getInt(4), verse = c.getInt(5),
                    text = c.getString(6)
                )
            }
        }
        return null
    }

    /** Busca por palavra ou expressão (usa índice FTS4; cai para LIKE se a expressão FTS falhar). */
    fun searchWords(query: String, testamentFilter: String? = null): List<SearchResult> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()

        val results = mutableListOf<SearchResult>()
        val ftsQuery = trimmed.split(Regex("\\s+")).joinToString(" ") { "$it*" }
        val testamentClause = if (testamentFilter != null) "AND b.testament=?" else ""
        val args = if (testamentFilter != null) arrayOf(ftsQuery, testamentFilter) else arrayOf(ftsQuery)

        try {
            db.rawQuery(
                """SELECT v.id, v.book_id, b.name, b.abbrev, v.chapter, v.verse, v.text
                   FROM verses_fts f
                   JOIN verses v ON v.id = f.rowid
                   JOIN books b ON b.id = v.book_id
                   WHERE f.text MATCH ? $testamentClause
                   LIMIT 200""",
                args
            ).use { c ->
                while (c.moveToNext()) {
                    val verse = Verse(
                        id = c.getLong(0), bookId = c.getInt(1), bookName = c.getString(2),
                        bookAbbrev = c.getString(3), chapter = c.getInt(4), verse = c.getInt(5),
                        text = c.getString(6)
                    )
                    results.add(SearchResult(verse, buildSnippet(verse.text, trimmed)))
                }
            }
        } catch (e: Exception) {
            // Expressão FTS inválida (ex: caracteres especiais) -> busca aproximada com LIKE
            return searchApproximate(trimmed, testamentFilter)
        }

        if (results.isEmpty()) {
            return searchApproximate(trimmed, testamentFilter)
        }
        return results
    }

    /** Busca aproximada: tolera erros de digitação e não exige a palavra exata. */
    private fun searchApproximate(query: String, testamentFilter: String?): List<SearchResult> {
        val normalizedQuery = normalize(query)
        val words = normalizedQuery.split(Regex("\\s+")).filter { it.length >= 3 }
        if (words.isEmpty()) return emptyList()

        val results = mutableListOf<SearchResult>()
        val testamentClause = if (testamentFilter != null) "WHERE b.testament=?" else ""
        val args: Array<String> = if (testamentFilter != null) arrayOf(testamentFilter) else emptyArray()

        db.rawQuery(
            """SELECT v.id, v.book_id, b.name, b.abbrev, v.chapter, v.verse, v.text
               FROM verses v JOIN books b ON b.id=v.book_id $testamentClause""",
            args
        ).use { c ->
            while (c.moveToNext()) {
                val text = c.getString(6)
                val normalizedText = normalize(text)
                val matchCount = words.count { normalizedText.contains(it) }
                if (matchCount > 0) {
                    val verse = Verse(
                        id = c.getLong(0), bookId = c.getInt(1), bookName = c.getString(2),
                        bookAbbrev = c.getString(3), chapter = c.getInt(4), verse = c.getInt(5),
                        text = text
                    )
                    results.add(SearchResult(verse, buildSnippet(text, query)))
                }
                if (results.size >= 200) break
            }
        }
        return results
    }

    /** Interpreta referências digitadas livremente, ex: "joão 3:16", "jo 3.16", "1 cor 13". */
    fun searchByReference(query: String): Verse? {
        val m = Regex("""^\s*([1-3]?\s*[a-zA-ZçÇãÃõÕáÁéÉíÍóÓúÚâÂêÊôÔ.]+)\s*(\d+)?\s*[:.,]?\s*(\d+)?\s*$""")
            .find(query.trim()) ?: return null
        val (bookPart, chapterPart, versePart) = m.destructured
        val normalizedBookPart = normalize(bookPart.trim())

        val books = getBooks()
        val matchedBook = books.firstOrNull { normalize(it.name) == normalizedBookPart }
            ?: books.firstOrNull { normalize(it.abbrev) == normalizedBookPart }
            ?: books.firstOrNull { normalize(it.name).startsWith(normalizedBookPart) }
            ?: books.firstOrNull { normalize(it.name).contains(normalizedBookPart) }
            ?: return null

        val chapter = chapterPart.toIntOrNull() ?: 1
        val verse = versePart.toIntOrNull() ?: 1
        return getVerse(matchedBook.id, chapter, verse)
            ?: getChapter(matchedBook.id, chapter).firstOrNull()
    }

    private fun buildSnippet(text: String, query: String): String {
        return if (text.length > 140) text.take(140) + "…" else text
    }

    private fun normalize(s: String): String {
        val temp = Normalizer.normalize(s.lowercase(), Normalizer.Form.NFD)
        return temp.replace(Regex("\\p{M}"), "").trim()
    }

    companion object {
        /**
         * Abre a tradução informada por [fileName]. Se [isBuiltIn] for verdadeiro
         * e o arquivo ainda não existir no armazenamento interno, ele é copiado
         * dos assets do APK (mesmo nome de arquivo). Traduções baixadas
         * (isBuiltIn = false) precisam já existir nesse caminho — quem baixa é o
         * TranslationDownloadManager.
         */
        fun open(context: Context, fileName: String, isBuiltIn: Boolean): BibleDatabaseHelper {
            val dbFile: File = context.getDatabasePath(fileName)
            if (isBuiltIn && !dbFile.exists()) {
                dbFile.parentFile?.mkdirs()
                context.assets.open(fileName).use { input ->
                    FileOutputStream(dbFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }
            if (!dbFile.exists()) {
                throw IllegalStateException("Arquivo de tradução não encontrado: $fileName")
            }
            return BibleDatabaseHelper(dbFile)
        }
    }
}
