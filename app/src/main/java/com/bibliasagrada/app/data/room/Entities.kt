package com.bibliasagrada.app.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites", primaryKeys = ["bookId", "chapter", "verse"])
data class FavoriteEntity(
    val bookId: Int,
    val chapter: Int,
    val verse: Int,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "highlights", primaryKeys = ["bookId", "chapter", "verse"])
data class HighlightEntity(
    val bookId: Int,
    val chapter: Int,
    val verse: Int,
    val color: String, // nome da cor: amarelo, azul, verde, vermelho, roxo
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Int,
    val chapter: Int,
    val verse: Int, // primeiro versículo do intervalo
    val verseEnd: Int = verse, // último versículo do intervalo (igual a "verse" para notas de 1 versículo só)
    val text: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "history", primaryKeys = ["bookId", "chapter"])
data class HistoryEntity(
    val bookId: Int,
    val chapter: Int,
    val lastReadAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Int,
    val chapter: Int,
    val name: String,
    /** Nome da cor (mesma paleta usada nos destaques de versículo), ou null = cor padrão do tema. */
    val color: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reading_progress")
data class ReadingProgressEntity(
    @PrimaryKey val id: Int = 0, // única linha, sempre id=0
    val bookId: Int,
    val chapter: Int,
    val verse: Int
)

/**
 * Tag de estudo (ex.: "Salvação", "Fé", "Oração"), com nome único e cor
 * própria (mesma paleta usada nos destaques e marcadores). Os versículos
 * marcados com essa tag ficam na tabela [VerseTagEntity], numa relação
 * muitos-para-muitos: um versículo pode ter várias tags, e uma tag pode
 * estar em vários versículos.
 */
@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val color: String, // nome da cor: amarelo, azul, verde, vermelho, roxo
    val createdAt: Long = System.currentTimeMillis()
)

/** Associação entre uma tag e um versículo marcado com ela. */
@Entity(tableName = "verse_tags", primaryKeys = ["tagId", "bookId", "chapter", "verse"])
data class VerseTagEntity(
    val tagId: Long,
    val bookId: Int,
    val chapter: Int,
    val verse: Int,
    val addedAt: Long = System.currentTimeMillis()
)
