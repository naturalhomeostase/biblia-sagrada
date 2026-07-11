package com.bibliasagrada.app.data.model

/**
 * Representa uma versão/tradução da Bíblia que pode ser usada no app.
 *
 * [builtIn] = true significa que o arquivo já vem embutido nos assets do APK
 * (é o caso da Bíblia Livre — BLIVRE). Traduções não embutidas são baixadas
 * em tempo de execução a partir de [downloadUrl] e salvas no armazenamento
 * interno do app como [fileName].
 *
 * IMPORTANTE: o arquivo baixado precisa ser um banco SQLite com o mesmo
 * esquema usado pelo BibleDatabaseHelper (tabelas `books`, `verses` e,
 * idealmente, o índice `verses_fts`). Veja o comentário em
 * TranslationsCatalog.kt sobre como preencher as URLs de download.
 */
data class Translation(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val fileName: String,
    val downloadUrl: String?,
    val builtIn: Boolean
)
