package com.bibliasagrada.app.data.repository

/** Uma referência de versículo usada na Caixinha de Promessas. */
data class PromiseRef(val bookId: Int, val chapter: Int, val verse: Int)

/**
 * Lista com uma seleção de versículos de promessas/conforto bastante conhecidos,
 * usada pela "Caixinha de Promessas". Os ids de livro correspondem à ordem
 * padrão da Bíblia usada no banco (1 = Gênesis ... 66 = Apocalipse).
 */
object PromisesCatalog {
    val ALL = listOf(
        PromiseRef(43, 3, 16),   // João 3:16
        PromiseRef(50, 4, 13),   // Filipenses 4:13
        PromiseRef(24, 29, 11),  // Jeremias 29:11
        PromiseRef(19, 23, 1),   // Salmos 23:1
        PromiseRef(23, 41, 10),  // Isaías 41:10
        PromiseRef(45, 8, 28),   // Romanos 8:28
        PromiseRef(20, 3, 5),    // Provérbios 3:5
        PromiseRef(20, 3, 6),    // Provérbios 3:6
        PromiseRef(40, 11, 28),  // Mateus 11:28
        PromiseRef(19, 46, 1),   // Salmos 46:1
        PromiseRef(55, 1, 7),    // 2 Timóteo 1:7
        PromiseRef(23, 40, 31),  // Isaías 40:31
        PromiseRef(58, 11, 1),   // Hebreus 11:1
        PromiseRef(50, 4, 6),    // Filipenses 4:6
        PromiseRef(50, 4, 7),    // Filipenses 4:7
        PromiseRef(19, 34, 18),  // Salmos 34:18
        PromiseRef(60, 5, 7),    // 1 Pedro 5:7
        PromiseRef(45, 15, 13),  // Romanos 15:13
        PromiseRef(24, 17, 7),   // Jeremias 17:7
        PromiseRef(19, 27, 1),   // Salmos 27:1
        PromiseRef(43, 14, 27),  // João 14:27
        PromiseRef(48, 6, 9),    // Gálatas 6:9
        PromiseRef(58, 13, 5),   // Hebreus 13:5
        PromiseRef(19, 55, 22),  // Salmos 55:22
        PromiseRef(23, 26, 3),   // Isaías 26:3
        PromiseRef(66, 21, 4),   // Apocalipse 21:4
        PromiseRef(45, 10, 9),   // Romanos 10:9
        PromiseRef(49, 2, 8),    // Efésios 2:8
        PromiseRef(19, 91, 1),   // Salmos 91:1
        PromiseRef(19, 121, 1)   // Salmos 121:1
    )

    fun random(excluding: PromiseRef? = null): PromiseRef {
        if (ALL.size <= 1) return ALL.first()
        var candidate: PromiseRef
        do {
            candidate = ALL.random()
        } while (candidate == excluding)
        return candidate
    }
}
