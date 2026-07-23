package com.bibliasagrada.app.data.repository

import com.bibliasagrada.app.data.model.Translation

/**
 * Catálogo das traduções que o app conhece.
 *
 * A Bíblia Livre (BLIVRE) já vem embutida no app.
 *
 * As demais traduções NÃO são baixadas automaticamente pelo app — por
 * questão de direitos autorais, o app não hospeda nem redistribui esses
 * textos. Em vez disso, a tela de Traduções mostra, por tradução, um botão
 * "Baixar no GitHub" (que só abre o link no navegador) e um botão
 * "Importar arquivo" (que abre o seletor de arquivos do Android). A
 * responsabilidade pelo conteúdo e pelos direitos de uso do arquivo baixado
 * é de quem faz esse download.
 *
 * Todas as fontes abaixo vêm do projeto damarals/biblias
 * (https://github.com/damarals/biblias), uma coletânea de bíblias em
 * português em formatos abertos (Zefania, SQLite no esquema do OpenLP, e
 * JSON). A licença MIT desse projeto cobre só o código/toolkit em Python —
 * os textos em si têm seus próprios direitos, listados no README do
 * projeto:
 *  - "Bíblia Fiel (ACF)": direitos reservados da Sociedade Bíblica
 *    Trinitariana.
 *  - "Almeida Revista e Corrigida (ARC)": direitos reservados da SBB
 *    (Sociedade Bíblica do Brasil).
 *  - "Nova Versão Internacional (NVI)": direitos reservados da Biblica.
 *  - "Almeida 1911": de domínio público (marcada com † no README do
 *    damarals/biblias).
 *  - "NTLH": direitos reservados da SBB (ver TranslationsCatalog antes desta
 *    entrada ter sido expandida, e a conversa sobre isso).
 *
 * Veja TranslationImporter.kt para o código que lê o arquivo importado (ele
 * reconhece automaticamente: o schema próprio do app, o schema SQLite do
 * OpenLP usado pelo damarals/biblias, e três formatos de JSON de outros
 * projetos).
 */
object TranslationsCatalog {

    private const val DAMARALS_BASE = "https://github.com/damarals/biblias/releases/latest/download"

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
        builtIn = false,
        sourceUrl = "$DAMARALS_BASE/ACF.sqlite"
    )

    val ALMEIDA_1911 = Translation(
        id = "almeida_1911",
        displayName = "Almeida 1911",
        subtitle = "Baixe fora do app e importe aqui",
        fileName = "biblia_almeida_1911.db",
        downloadUrl = null,
        builtIn = false,
        sourceUrl = "$DAMARALS_BASE/ALM1911.sqlite"
    )

    val NVI = Translation(
        id = "nvi",
        displayName = "Nova Versão Internacional (NVI)",
        subtitle = "Baixe fora do app e importe aqui",
        fileName = "biblia_nvi.db",
        downloadUrl = null,
        builtIn = false,
        sourceUrl = "$DAMARALS_BASE/NVI.sqlite"
    )

    val ARC = Translation(
        id = "arc",
        displayName = "Almeida Revista e Corrigida (ARC)",
        subtitle = "Baixe fora do app e importe aqui",
        fileName = "biblia_arc.db",
        downloadUrl = null,
        builtIn = false,
        sourceUrl = "$DAMARALS_BASE/ARC.sqlite"
    )

    val NTLH = Translation(
        id = "ntlh",
        displayName = "Nova Tradução na Linguagem de Hoje (NTLH)",
        subtitle = "Baixe fora do app e importe aqui",
        fileName = "biblia_ntlh.db",
        downloadUrl = null,
        builtIn = false,
        sourceUrl = "$DAMARALS_BASE/NTLH.sqlite"
    )

    val all: List<Translation> = listOf(BLIVRE, FIEL, ALMEIDA_1911, NVI, ARC, NTLH)

    fun byFileName(fileName: String): Translation = all.firstOrNull { it.fileName == fileName } ?: BLIVRE

    /** Página do projeto damarals/biblias, para quem quiser ver o catálogo
     *  completo de traduções disponíveis (além das já listadas acima). */
    const val EXTERNAL_SOURCE_URL = "https://github.com/damarals/biblias"
}
