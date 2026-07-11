package com.bibliasagrada.app.data.model

data class Book(
    val id: Int,
    val code: String,
    val name: String,
    val abbrev: String,
    val testament: String,
    val chapterCount: Int
)

data class Verse(
    val id: Long,
    val bookId: Int,
    val bookName: String,
    val bookAbbrev: String,
    val chapter: Int,
    val verse: Int,
    val text: String
) {
    val reference: String get() = "$bookName $chapter:$verse"
    val shortReference: String get() = "$bookAbbrev $chapter:$verse"
}

data class SearchResult(
    val verse: Verse,
    val snippet: String
)

/** Representa um capítulo dentro da sequência contínua de toda a Bíblia (para navegação por gesto). */
data class ChapterRef(
    val bookId: Int,
    val chapter: Int,
    val bookName: String,
    val bookAbbrev: String
)

