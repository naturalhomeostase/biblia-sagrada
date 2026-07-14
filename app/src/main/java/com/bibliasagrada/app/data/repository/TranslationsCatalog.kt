package com.bibliasagrada.app.data.repository

import com.bibliasagrada.app.data.model.Translation

/**
 * Catálogo das traduções que o app conhece.
 *
 * A Bíblia Livre (BLIVRE) já vem embutida no app.
 *
 * As demais traduções (ex.: "Bíblia Fiel") NÃO são baixadas automaticamente
 * pelo app — por questão de direitos autorais, o app não hospeda nem
 * redistribui esses textos. Em vez disso, a tela de Traduções oferece um
 * botão para o próprio usuário baixar o arquivo de uma fonte externa (fora
 * do app) e depois importar esse arquivo manualmente — a responsabilidade
 * pelo conteúdo e pelos direitos de uso é de quem faz esse download.
 * Veja TranslationImporter.kt para o código que lê o arquivo importado.
 */
object TranslationsCatalog {

    val BLIVRE = Translation(
        id = "blivre",
        displayName = "Bíblia Livre (BLIVRE)",
        subtitle = "Incluída no app — funciona offline",
        fileName = "biblia.db",
        downloadUrl = null,
        builtIn = true
    )

    val FIEL = Translation(
        id = "fiel",
        displayName = "Bíblia Fiel (ACF)",
        subtitle = "Baixe fora do app e importe aqui",
        fileName = "biblia_fiel.db",
        downloadUrl = null,
        builtIn = false
    )

    val NVLH = Translation(
        id = "nvlh",
        displayName = "NVLH",
        subtitle = "Baixe fora do app e importe aqui",
        fileName = "biblia_nvlh.db",
        downloadUrl = null,
        builtIn = false
    )

    val all: List<Translation> = listOf(BLIVRE, FIEL, NVLH)

    fun byFileName(fileName: String): Translation = all.firstOrNull { it.fileName == fileName } ?: BLIVRE

    /** Página onde o usuário pode baixar traduções adicionais em formato JSON, por conta própria. */
    const val EXTERNAL_SOURCE_URL = "https://github.com/thiagobodruk/biblia"
}
