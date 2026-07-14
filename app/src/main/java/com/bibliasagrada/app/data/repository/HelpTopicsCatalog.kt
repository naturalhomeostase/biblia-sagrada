package com.bibliasagrada.app.data.repository

/** Referência de versículo usada no catálogo de "Encontre Ajuda". */
data class HelpVerseRef(val bookId: Int, val chapter: Int, val verse: Int)

data class HelpTopic(
    val title: String,
    val category: String,
    val verses: List<HelpVerseRef>
)

/**
 * Catálogo próprio de "Encontre Ajuda na Bíblia": tópicos de situações e
 * sentimentos do dia a dia, cada um com alguns versículos relacionados.
 * Curadoria e seleção próprias do app (não é uma cópia de nenhum material
 * de terceiros) — a ideia de organizar passagens por tema de vida é um
 * formato clássico e comum a diversas edições da Bíblia, mas os tópicos,
 * títulos e a seleção de versículos aqui foram escolhidos especificamente
 * para este app.
 */
object HelpTopicsCatalog {
    val ALL = listOf(
        HelpTopic("Enfrentando o medo", "Situações da vida", listOf(
            HelpVerseRef(19, 27, 1), HelpVerseRef(23, 41, 10), HelpVerseRef(60, 5, 7)
        )),
        HelpTopic("Ansiedade e preocupação", "Situações da vida", listOf(
            HelpVerseRef(50, 4, 6), HelpVerseRef(50, 4, 7), HelpVerseRef(40, 6, 34)
        )),
        HelpTopic("Luto e perda de um ente querido", "Situações da vida", listOf(
            HelpVerseRef(19, 23, 4), HelpVerseRef(43, 14, 1), HelpVerseRef(66, 21, 4)
        )),
        HelpTopic("Doença e sofrimento físico", "Situações da vida", listOf(
            HelpVerseRef(19, 41, 3), HelpVerseRef(59, 5, 15), HelpVerseRef(23, 53, 5)
        )),
        HelpTopic("Solidão", "Situações da vida", listOf(
            HelpVerseRef(5, 31, 6), HelpVerseRef(19, 68, 6), HelpVerseRef(58, 13, 5)
        )),
        HelpTopic("Tomando decisões difíceis", "Situações da vida", listOf(
            HelpVerseRef(20, 3, 5), HelpVerseRef(20, 3, 6), HelpVerseRef(59, 1, 5)
        )),
        HelpTopic("Aprendendo a perdoar", "Situações da vida", listOf(
            HelpVerseRef(49, 4, 32), HelpVerseRef(40, 6, 14), HelpVerseRef(51, 3, 13)
        )),
        HelpTopic("Casamento", "Situações da vida", listOf(
            HelpVerseRef(1, 2, 24), HelpVerseRef(49, 5, 25), HelpVerseRef(21, 4, 12)
        )),
        HelpTopic("Criação dos filhos", "Situações da vida", listOf(
            HelpVerseRef(20, 22, 6), HelpVerseRef(49, 6, 4), HelpVerseRef(5, 6, 6)
        )),
        HelpTopic("Dificuldades financeiras", "Situações da vida", listOf(
            HelpVerseRef(50, 4, 19), HelpVerseRef(58, 13, 5), HelpVerseRef(20, 3, 9)
        )),
        HelpTopic("Perda de emprego", "Situações da vida", listOf(
            HelpVerseRef(24, 29, 11), HelpVerseRef(45, 8, 28), HelpVerseRef(50, 4, 13)
        )),
        HelpTopic("Começando um novo trabalho", "Situações da vida", listOf(
            HelpVerseRef(20, 16, 3), HelpVerseRef(51, 3, 23), HelpVerseRef(45, 12, 11)
        )),
        HelpTopic("Estudos e novos desafios", "Situações da vida", listOf(
            HelpVerseRef(20, 1, 7), HelpVerseRef(59, 1, 5), HelpVerseRef(45, 12, 2)
        )),
        HelpTopic("Chegando a uma nova fase da vida", "Situações da vida", listOf(
            HelpVerseRef(21, 3, 1), HelpVerseRef(23, 43, 19), HelpVerseRef(47, 5, 17)
        )),
        HelpTopic("Amizade", "Situações da vida", listOf(
            HelpVerseRef(20, 17, 17), HelpVerseRef(21, 4, 9), HelpVerseRef(21, 4, 10)
        )),
        HelpTopic("Servindo e liderando outros", "Situações da vida", listOf(
            HelpVerseRef(41, 10, 45), HelpVerseRef(45, 12, 8), HelpVerseRef(60, 5, 2)
        )),
        HelpTopic("Cuidando dos pais ou de idosos", "Situações da vida", listOf(
            HelpVerseRef(20, 23, 22), HelpVerseRef(54, 5, 4), HelpVerseRef(19, 71, 9)
        )),
        HelpTopic("Divórcio ou separação", "Situações da vida", listOf(
            HelpVerseRef(19, 34, 18), HelpVerseRef(23, 54, 10), HelpVerseRef(47, 1, 3)
        )),
        HelpTopic("Buscando a vontade de Deus", "Situações da vida", listOf(
            HelpVerseRef(20, 3, 6), HelpVerseRef(45, 12, 2), HelpVerseRef(59, 1, 5)
        )),
        HelpTopic("Buscando a salvação", "Situações da vida", listOf(
            HelpVerseRef(43, 3, 16), HelpVerseRef(45, 10, 9), HelpVerseRef(49, 2, 8)
        )),
        HelpTopic("Sentindo medo", "Sentimentos e emoções", listOf(
            HelpVerseRef(19, 56, 3), HelpVerseRef(23, 41, 13), HelpVerseRef(41, 4, 40)
        )),
        HelpTopic("Sentindo ansiedade", "Sentimentos e emoções", listOf(
            HelpVerseRef(60, 5, 7), HelpVerseRef(19, 94, 19), HelpVerseRef(50, 4, 6)
        )),
        HelpTopic("Sentindo tristeza ou desânimo profundo", "Sentimentos e emoções", listOf(
            HelpVerseRef(19, 34, 18), HelpVerseRef(19, 42, 11), HelpVerseRef(23, 61, 3)
        )),
        HelpTopic("Sentindo raiva", "Sentimentos e emoções", listOf(
            HelpVerseRef(49, 4, 26), HelpVerseRef(49, 4, 31), HelpVerseRef(59, 1, 19)
        )),
        HelpTopic("Sentindo desânimo", "Sentimentos e emoções", listOf(
            HelpVerseRef(19, 42, 5), HelpVerseRef(45, 15, 13), HelpVerseRef(47, 4, 16)
        )),
        HelpTopic("Tendo dúvidas na fé", "Sentimentos e emoções", listOf(
            HelpVerseRef(41, 9, 24), HelpVerseRef(58, 11, 1), HelpVerseRef(43, 20, 29)
        )),
        HelpTopic("Sentindo insegurança", "Sentimentos e emoções", listOf(
            HelpVerseRef(19, 139, 14), HelpVerseRef(50, 4, 13), HelpVerseRef(62, 4, 18)
        )),
        HelpTopic("Sentindo inveja", "Sentimentos e emoções", listOf(
            HelpVerseRef(20, 14, 30), HelpVerseRef(59, 3, 16), HelpVerseRef(48, 5, 26)
        )),
        HelpTopic("Sentindo culpa", "Sentimentos e emoções", listOf(
            HelpVerseRef(19, 32, 5), HelpVerseRef(62, 1, 9), HelpVerseRef(45, 8, 1)
        )),
        HelpTopic("Sentindo gratidão", "Sentimentos e emoções", listOf(
            HelpVerseRef(19, 100, 4), HelpVerseRef(52, 5, 18), HelpVerseRef(51, 3, 17)
        )),
        HelpTopic("Buscando paz interior", "Sentimentos e emoções", listOf(
            HelpVerseRef(43, 14, 27), HelpVerseRef(50, 4, 7), HelpVerseRef(23, 26, 3)
        )),
        HelpTopic("Lidando com a impaciência", "Sentimentos e emoções", listOf(
            HelpVerseRef(19, 37, 7), HelpVerseRef(59, 5, 7), HelpVerseRef(45, 12, 12)
        )),
        HelpTopic("Lidando com o orgulho", "Sentimentos e emoções", listOf(
            HelpVerseRef(20, 16, 18), HelpVerseRef(59, 4, 6), HelpVerseRef(50, 2, 3)
        )),
        HelpTopic("Preocupação com o futuro", "Sentimentos e emoções", listOf(
            HelpVerseRef(24, 29, 11), HelpVerseRef(19, 32, 8), HelpVerseRef(58, 13, 8)
        )),
        HelpTopic("Buscando força e coragem", "Sentimentos e emoções", listOf(
            HelpVerseRef(6, 1, 9), HelpVerseRef(23, 40, 31), HelpVerseRef(50, 4, 13)
        ))
    )

    val categories: List<String> get() = ALL.map { it.category }.distinct()
}
