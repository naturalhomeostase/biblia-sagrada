package com.bibliasagrada.app.data.repository

import com.bibliasagrada.app.data.model.Translation

/**
 * Catálogo das traduções que o app conhece.
 *
 * A Bíblia Livre (BLIVRE) já vem embutida no app.
 *
 * As demais traduções NÃO são baixadas automaticamente pelo app — por
 * questão de direitos autorais, o app não hospeda nem redistribui esses
 * textos. Em vez disso, a tela de Traduções oferece um botão para o próprio
 * usuário baixar o arquivo de uma fonte externa (fora do app) e depois
 * importar esse arquivo manualmente — a responsabilidade pelo conteúdo e
 * pelos direitos de uso é de quem faz esse download.
 *
 * "Bíblia Fiel (ACF)": a Almeida Corrigida Fiel tem direitos reservados da
 * Sociedade Bíblica Trinitariana — cuidado extra recomendado (ver conversa
 * anterior sobre isso).
 * "Almeida 1911": a reimpressão de 1911 da tradução de João Ferreira de
 * Almeida é de domínio público pela idade; o arquivo específico da API
 * getBible é distribuído sob licença GPL — a mais tranquila das opções aqui.
 * "OpenBible": projeto com traduções de domínio público / geradas
 * automaticamente.
 *
 * Veja TranslationImporter.kt para o código que lê o arquivo importado (ele
 * reconhece automaticamente os três formatos: thiagobodruk/biblia, getBible
 * e openbible).
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

    val ALMEIDA_1911 = Translation(
        id = "almeida_1911",
        displayName = "Almeida 1911",
        subtitle = "Baixe fora do app e importe aqui",
        fileName = "biblia_almeida_1911.db",
        downloadUrl = null,
        builtIn = false
    )

    val OPEN_BIBLE = Translation(
        id = "open_bible",
        displayName = "OpenBible (PT)",
        subtitle = "Baixe fora do app e importe aqui",
        fileName = "biblia_openbible.db",
        downloadUrl = null,
        builtIn = false
    )

    /** Link direto para o arquivo .sqlite da NTLH na última release do projeto
     *  damarals/biblias (coletânea de bíblias em formatos abertos, MIT — mas
     *  atenção: a licença MIT cobre só o código/toolkit; o texto da NTLH em si
     *  pertence à SBB, como o próprio README do projeto deixa claro). */
    const val EXTERNAL_SOURCE_URL_NTLH = "https://github.com/damarals/biblias/releases/latest/download/NTLH.sqlite"

    /**
     * NTLH (Nova Tradução na Linguagem de Hoje): direitos reservados da SBB
     * (Sociedade Bíblica do Brasil). O app NÃO baixa nem hospeda o texto —
     * o botão "Baixar no GitHub" só abre a página de release do projeto
     * damarals/biblias no navegador, para o próprio usuário baixar o arquivo
     * .sqlite por conta própria; depois ele importa esse arquivo aqui do
     * mesmo jeito que as outras traduções. Ver TranslationImporter.kt: esse
     * .sqlite usa o schema do OpenLP (tabelas "book"/"verse"), reconhecido
     * automaticamente e convertido para o schema interno do app.
     */
    val NTLH = Translation(
        id = "ntlh",
        displayName = "Nova Tradução na Linguagem de Hoje (NTLH)",
        subtitle = "Baixe fora do app e importe aqui",
        fileName = "biblia_ntlh.db",
        downloadUrl = null,
        builtIn = false,
        sourceUrl = EXTERNAL_SOURCE_URL_NTLH
    )

    val all: List<Translation> = listOf(BLIVRE, FIEL, ALMEIDA_1911, OPEN_BIBLE, NTLH)

    fun byFileName(fileName: String): Translation = all.firstOrNull { it.fileName == fileName } ?: BLIVRE

    /** Fontes externas onde dá para baixar traduções adicionais, por conta própria. */
    const val EXTERNAL_SOURCE_URL = "https://github.com/thiagobodruk/biblia"
    const val EXTERNAL_SOURCE_URL_GETBIBLE = "https://api.getbible.net/v2/almeida.json"
    const val EXTERNAL_SOURCE_URL_OPENBIBLE = "https://github.com/churchstudio-org/openbible"
}
