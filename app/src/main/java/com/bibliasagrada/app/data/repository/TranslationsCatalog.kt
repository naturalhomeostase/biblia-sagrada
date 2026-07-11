package com.bibliasagrada.app.data.repository

import com.bibliasagrada.app.data.model.Translation

/**
 * Catálogo das traduções que o app conhece.
 *
 * ATENÇÃO — leia antes de publicar:
 * As URLs de download abaixo (`downloadUrl`) estão como placeholder porque este
 * app não tem acesso à internet para localizar/hospedar arquivos de tradução da
 * Bíblia (e algumas traduções, como a NVI/NVT, têm direitos autorais que exigem
 * autorização da editora para redistribuição). Para ativar de fato o download de
 * "Bíblia Fiel" e "NVLH":
 *   1. Prepare um arquivo .db (SQLite) para cada tradução, com o MESMO esquema do
 *      banco atual (tabelas `books` e `verses`, e idealmente `verses_fts` para
 *      manter a busca funcionando — veja BibleDatabaseHelper.kt).
 *   2. Hospede cada arquivo .db em algum lugar acessível por HTTPS (ex.: um
 *      repositório GitHub seu, Firebase Storage, S3, etc.).
 *   3. Substitua os valores de `downloadUrl` abaixo pelas URLs reais.
 * Sem isso, a tela de "Traduções" mostra essas opções como disponíveis, mas o
 * download falhará com uma mensagem explicando que a URL não foi configurada.
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
        displayName = "Bíblia Fiel",
        subtitle = "Toque para baixar",
        fileName = "biblia_fiel.db",
        downloadUrl = null, // TODO: preencha com a URL do arquivo .db da Bíblia Fiel
        builtIn = false
    )

    val NVLH = Translation(
        id = "nvlh",
        displayName = "NVLH",
        subtitle = "Toque para baixar",
        fileName = "biblia_nvlh.db",
        downloadUrl = null, // TODO: preencha com a URL do arquivo .db da NVLH
        builtIn = false
    )

    val all: List<Translation> = listOf(BLIVRE, FIEL, NVLH)

    fun byFileName(fileName: String): Translation = all.firstOrNull { it.fileName == fileName } ?: BLIVRE
}
