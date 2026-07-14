package com.bibliasagrada.app.util

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.File

/**
 * Importa um arquivo de tradução escolhido pelo próprio usuário (via seletor
 * de arquivos do Android) para dentro do app.
 *
 * IMPORTANTE sobre direitos autorais: o app não baixa nem hospeda textos
 * bíblicos de terceiros — quem baixa o arquivo original (de onde quer que
 * seja) e decide importá-lo aqui é o próprio usuário, que deve verificar se
 * tem o direito de usar aquele conteúdo. Este código só faz a conversão
 * técnica do arquivo escolhido para o formato que o app consegue ler.
 *
 * Aceita dois formatos:
 *  - Um arquivo .db (SQLite) já no formato usado pelo app (tabelas `books`
 *    e `verses`) — nesse caso só copia o arquivo.
 *  - Um arquivo .json no formato usado pelo projeto
 *    github.com/thiagobodruk/biblia (uma lista com um objeto por livro,
 *    cada um com "chapters": uma lista de listas de textos de versículo) —
 *    nesse caso, converte para SQLite no próprio celular.
 */
object TranslationImporter {

    sealed class ImportResult {
        data object Success : ImportResult()
        data class Failure(val message: String) : ImportResult()
    }

    private data class BookMeta(
        val id: Int, val code: String, val name: String,
        val abbrev: String, val testament: String, val chapterCount: Int
    )

    // Mesma tabela de livros usada pela tradução embutida (BLIVRE), para os
    // ids continuarem batendo entre traduções diferentes.
    private val BOOKS = listOf(
        BookMeta(1, "GEN", "Gênesis", "Gn", "AT", 50), BookMeta(2, "EXO", "Êxodo", "Êx", "AT", 40),
        BookMeta(3, "LEV", "Levítico", "Lv", "AT", 27), BookMeta(4, "NUM", "Números", "Nm", "AT", 36),
        BookMeta(5, "DEU", "Deuteronômio", "Dt", "AT", 34), BookMeta(6, "JOS", "Josué", "Js", "AT", 24),
        BookMeta(7, "JDG", "Juízes", "Jz", "AT", 21), BookMeta(8, "RUT", "Rute", "Rt", "AT", 4),
        BookMeta(9, "1SA", "1 Samuel", "1Sm", "AT", 31), BookMeta(10, "2SA", "2 Samuel", "2Sm", "AT", 24),
        BookMeta(11, "1KI", "1 Reis", "1Rs", "AT", 22), BookMeta(12, "2KI", "2 Reis", "2Rs", "AT", 25),
        BookMeta(13, "1CH", "1 Crônicas", "1Cr", "AT", 29), BookMeta(14, "2CH", "2 Crônicas", "2Cr", "AT", 36),
        BookMeta(15, "EZR", "Esdras", "Ed", "AT", 10), BookMeta(16, "NEH", "Neemias", "Ne", "AT", 13),
        BookMeta(17, "EST", "Ester", "Et", "AT", 10), BookMeta(18, "JOB", "Jó", "Jó", "AT", 42),
        BookMeta(19, "PSA", "Salmos", "Sl", "AT", 150), BookMeta(20, "PRO", "Provérbios", "Pv", "AT", 31),
        BookMeta(21, "ECC", "Eclesiastes", "Ec", "AT", 12), BookMeta(22, "SOL", "Cantares", "Ct", "AT", 8),
        BookMeta(23, "ISA", "Isaías", "Is", "AT", 66), BookMeta(24, "JER", "Jeremias", "Jr", "AT", 52),
        BookMeta(25, "LAM", "Lamentações", "Lm", "AT", 5), BookMeta(26, "EZE", "Ezequiel", "Ez", "AT", 48),
        BookMeta(27, "DAN", "Daniel", "Dn", "AT", 12), BookMeta(28, "HOS", "Oséias", "Os", "AT", 14),
        BookMeta(29, "JOE", "Joel", "Jl", "AT", 3), BookMeta(30, "AMO", "Amós", "Am", "AT", 9),
        BookMeta(31, "OBA", "Obadias", "Ob", "AT", 1), BookMeta(32, "JON", "Jonas", "Jn", "AT", 4),
        BookMeta(33, "MIC", "Miquéias", "Mq", "AT", 7), BookMeta(34, "NAH", "Naum", "Na", "AT", 3),
        BookMeta(35, "HAB", "Habacuque", "Hc", "AT", 3), BookMeta(36, "ZEP", "Sofonias", "Sf", "AT", 3),
        BookMeta(37, "HAG", "Ageu", "Ag", "AT", 2), BookMeta(38, "ZEC", "Zacarias", "Zc", "AT", 14),
        BookMeta(39, "MAL", "Malaquias", "Ml", "AT", 4), BookMeta(40, "MAT", "Mateus", "Mt", "NT", 28),
        BookMeta(41, "MAR", "Marcos", "Mc", "NT", 16), BookMeta(42, "LUK", "Lucas", "Lc", "NT", 24),
        BookMeta(43, "JOH", "João", "Jo", "NT", 21), BookMeta(44, "ACT", "Atos", "At", "NT", 28),
        BookMeta(45, "ROM", "Romanos", "Rm", "NT", 16), BookMeta(46, "1CO", "1 Coríntios", "1Co", "NT", 16),
        BookMeta(47, "2CO", "2 Coríntios", "2Co", "NT", 13), BookMeta(48, "GAL", "Gálatas", "Gl", "NT", 6),
        BookMeta(49, "EPH", "Efésios", "Ef", "NT", 6), BookMeta(50, "PHI", "Filipenses", "Fp", "NT", 4),
        BookMeta(51, "COL", "Colossenses", "Cl", "NT", 4), BookMeta(52, "1TH", "1 Tessalonicenses", "1Ts", "NT", 5),
        BookMeta(53, "2TH", "2 Tessalonicenses", "2Ts", "NT", 3), BookMeta(54, "1TI", "1 Timóteo", "1Tm", "NT", 6),
        BookMeta(55, "2TI", "2 Timóteo", "2Tm", "NT", 4), BookMeta(56, "TIT", "Tito", "Tt", "NT", 3),
        BookMeta(57, "PHM", "Filemom", "Fm", "NT", 1), BookMeta(58, "HEB", "Hebreus", "Hb", "NT", 13),
        BookMeta(59, "JAM", "Tiago", "Tg", "NT", 5), BookMeta(60, "1PE", "1 Pedro", "1Pe", "NT", 5),
        BookMeta(61, "2PE", "2 Pedro", "2Pe", "NT", 3), BookMeta(62, "1JO", "1 João", "1Jo", "NT", 5),
        BookMeta(63, "2JO", "2 João", "2Jo", "NT", 1), BookMeta(64, "3JO", "3 João", "3Jo", "NT", 1),
        BookMeta(65, "JUD", "Judas", "Jd", "NT", 1), BookMeta(66, "REV", "Apocalipse", "Ap", "NT", 22)
    )

    suspend fun importFromUri(context: Context, uri: Uri, targetFileName: String): ImportResult =
        withContext(Dispatchers.IO) {
            try {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: return@withContext ImportResult.Failure("Não foi possível abrir o arquivo escolhido.")

                // Arquivo SQLite começa sempre com essa assinatura de 16 bytes.
                val isSqlite = bytes.size > 16 && String(bytes, 0, 16, Charsets.US_ASCII) == "SQLite format 3\u0000"

                if (isSqlite) {
                    copyRawDb(context, bytes, targetFileName)
                } else {
                    val text = String(bytes, Charsets.UTF_8).trim('\uFEFF', ' ', '\n', '\r', '\t')
                    if (!text.startsWith("[")) {
                        return@withContext ImportResult.Failure(
                            "Formato não reconhecido. Escolha um arquivo .db do app, ou um .json no formato do projeto thiagobodruk/biblia."
                        )
                    }
                    convertJsonToDb(text, targetFileName, context)
                }
            } catch (e: Exception) {
                ImportResult.Failure(e.message ?: "Erro desconhecido ao importar.")
            }
        }

    private fun copyRawDb(context: Context, bytes: ByteArray, targetFileName: String): ImportResult {
        val destFile = context.getDatabasePath(targetFileName)
        destFile.parentFile?.mkdirs()
        val tempFile = File(destFile.parentFile, "$targetFileName.importing")
        tempFile.writeBytes(bytes)
        if (destFile.exists()) destFile.delete()
        tempFile.renameTo(destFile)
        return ImportResult.Success
    }

    private fun convertJsonToDb(jsonText: String, targetFileName: String, context: Context): ImportResult {
        val jsonArray = JSONArray(jsonText)
        if (jsonArray.length() != 66) {
            return ImportResult.Failure(
                "O arquivo tem ${jsonArray.length()} livros; eram esperados 66. Verifique se é o arquivo certo."
            )
        }

        val destFile = context.getDatabasePath(targetFileName)
        destFile.parentFile?.mkdirs()
        val tempFile = File(destFile.parentFile, "$targetFileName.importing")
        if (tempFile.exists()) tempFile.delete()

        val db = SQLiteDatabase.openOrCreateDatabase(tempFile, null)
        try {
            db.execSQL(
                """CREATE TABLE books (
                    id INTEGER PRIMARY KEY, code TEXT NOT NULL, name TEXT NOT NULL,
                    abbrev TEXT NOT NULL, testament TEXT NOT NULL, chapter_count INTEGER NOT NULL
                )"""
            )
            db.execSQL(
                """CREATE TABLE verses (
                    id INTEGER PRIMARY KEY AUTOINCREMENT, book_id INTEGER NOT NULL,
                    chapter INTEGER NOT NULL, verse INTEGER NOT NULL, text TEXT NOT NULL
                )"""
            )

            db.beginTransaction()
            try {
                val bookStmt = db.compileStatement(
                    "INSERT INTO books (id, code, name, abbrev, testament, chapter_count) VALUES (?,?,?,?,?,?)"
                )
                for (b in BOOKS) {
                    bookStmt.bindLong(1, b.id.toLong())
                    bookStmt.bindString(2, b.code)
                    bookStmt.bindString(3, b.name)
                    bookStmt.bindString(4, b.abbrev)
                    bookStmt.bindString(5, b.testament)
                    bookStmt.bindLong(6, b.chapterCount.toLong())
                    bookStmt.executeInsert()
                    bookStmt.clearBindings()
                }

                val verseStmt = db.compileStatement(
                    "INSERT INTO verses (book_id, chapter, verse, text) VALUES (?,?,?,?)"
                )
                for (i in 0 until jsonArray.length()) {
                    val bookObj = jsonArray.getJSONObject(i)
                    val bookId = i + 1
                    val chaptersArr = bookObj.getJSONArray("chapters")
                    for (c in 0 until chaptersArr.length()) {
                        val versesArr = chaptersArr.getJSONArray(c)
                        for (v in 0 until versesArr.length()) {
                            verseStmt.bindLong(1, bookId.toLong())
                            verseStmt.bindLong(2, (c + 1).toLong())
                            verseStmt.bindLong(3, (v + 1).toLong())
                            verseStmt.bindString(4, versesArr.getString(v))
                            verseStmt.executeInsert()
                            verseStmt.clearBindings()
                        }
                    }
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }

            db.execSQL("""CREATE VIRTUAL TABLE verses_fts USING fts4(text, content="verses")""")
            db.execSQL("INSERT INTO verses_fts(verses_fts) VALUES('rebuild')")
        } finally {
            db.close()
        }

        if (destFile.exists()) destFile.delete()
        tempFile.renameTo(destFile)
        return ImportResult.Success
    }
}
