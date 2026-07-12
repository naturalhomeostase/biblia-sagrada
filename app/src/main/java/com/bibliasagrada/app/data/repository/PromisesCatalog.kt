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
        PromiseRef(19, 121, 1),  // Salmos 121:1

        // --- Adicionados depois, para ampliar a variedade ---
        PromiseRef(1, 28, 15),   // Gênesis 28:15
        PromiseRef(2, 14, 14),   // Êxodo 14:14
        PromiseRef(5, 31, 6),    // Deuteronômio 31:6
        PromiseRef(5, 31, 8),    // Deuteronômio 31:8
        PromiseRef(6, 1, 9),     // Josué 1:9
        PromiseRef(9, 17, 47),   // 1 Samuel 17:47
        PromiseRef(19, 16, 11),  // Salmos 16:11
        PromiseRef(19, 18, 2),   // Salmos 18:2
        PromiseRef(19, 28, 7),   // Salmos 28:7
        PromiseRef(19, 30, 5),   // Salmos 30:5
        PromiseRef(19, 32, 8),   // Salmos 32:8
        PromiseRef(19, 37, 4),   // Salmos 37:4
        PromiseRef(19, 37, 5),   // Salmos 37:5
        PromiseRef(19, 42, 11),  // Salmos 42:11
        PromiseRef(19, 56, 3),   // Salmos 56:3
        PromiseRef(19, 62, 1),   // Salmos 62:1
        PromiseRef(19, 73, 26),  // Salmos 73:26
        PromiseRef(19, 84, 11),  // Salmos 84:11
        PromiseRef(19, 94, 19),  // Salmos 94:19
        PromiseRef(19, 103, 2),  // Salmos 103:2
        PromiseRef(19, 103, 12), // Salmos 103:12
        PromiseRef(19, 107, 1),  // Salmos 107:1
        PromiseRef(19, 119, 105),// Salmos 119:105
        PromiseRef(19, 126, 5),  // Salmos 126:5
        PromiseRef(19, 138, 8),  // Salmos 138:8
        PromiseRef(19, 139, 14), // Salmos 139:14
        PromiseRef(19, 145, 18), // Salmos 145:18
        PromiseRef(19, 147, 3),  // Salmos 147:3
        PromiseRef(20, 16, 3),   // Provérbios 16:3
        PromiseRef(20, 16, 9),   // Provérbios 16:9
        PromiseRef(20, 18, 10),  // Provérbios 18:10
        PromiseRef(21, 3, 1),    // Eclesiastes 3:1
        PromiseRef(23, 12, 2),   // Isaías 12:2
        PromiseRef(23, 30, 21),  // Isaías 30:21
        PromiseRef(23, 41, 13),  // Isaías 41:13
        PromiseRef(23, 43, 2),   // Isaías 43:2
        PromiseRef(23, 54, 17),  // Isaías 54:17
        PromiseRef(23, 58, 11),  // Isaías 58:11
        PromiseRef(23, 65, 24),  // Isaías 65:24
        PromiseRef(24, 32, 17),  // Jeremias 32:17
        PromiseRef(24, 33, 3),   // Jeremias 33:3
        PromiseRef(25, 3, 22),   // Lamentações 3:22
        PromiseRef(25, 3, 23),   // Lamentações 3:23
        PromiseRef(33, 7, 7),    // Miquéias 7:7
        PromiseRef(36, 3, 17),   // Sofonias 3:17
        PromiseRef(38, 4, 6),    // Zacarias 4:6
        PromiseRef(40, 6, 33),   // Mateus 6:33
        PromiseRef(40, 7, 7),    // Mateus 7:7
        PromiseRef(40, 17, 20),  // Mateus 17:20
        PromiseRef(40, 19, 26),  // Mateus 19:26
        PromiseRef(40, 28, 20),  // Mateus 28:20
        PromiseRef(41, 11, 24),  // Marcos 11:24
        PromiseRef(42, 1, 37),   // Lucas 1:37
        PromiseRef(43, 1, 12),   // João 1:12
        PromiseRef(43, 8, 32),   // João 8:32
        PromiseRef(43, 10, 10),  // João 10:10
        PromiseRef(43, 10, 28),  // João 10:28
        PromiseRef(43, 11, 25),  // João 11:25
        PromiseRef(43, 15, 7),   // João 15:7
        PromiseRef(43, 15, 13),  // João 15:13
        PromiseRef(43, 16, 33),  // João 16:33
        PromiseRef(44, 1, 8),    // Atos 1:8
        PromiseRef(45, 5, 8),    // Romanos 5:8
        PromiseRef(45, 8, 1),    // Romanos 8:1
        PromiseRef(45, 8, 31),   // Romanos 8:31
        PromiseRef(45, 8, 38),   // Romanos 8:38
        PromiseRef(45, 8, 39),   // Romanos 8:39
        PromiseRef(45, 12, 2),   // Romanos 12:2
        PromiseRef(46, 10, 13),  // 1 Coríntios 10:13
        PromiseRef(46, 13, 4),   // 1 Coríntios 13:4
        PromiseRef(46, 13, 7),   // 1 Coríntios 13:7
        PromiseRef(46, 15, 58),  // 1 Coríntios 15:58
        PromiseRef(47, 1, 3),    // 2 Coríntios 1:3
        PromiseRef(47, 4, 16),   // 2 Coríntios 4:16
        PromiseRef(47, 5, 17),   // 2 Coríntios 5:17
        PromiseRef(47, 9, 8),    // 2 Coríntios 9:8
        PromiseRef(47, 12, 9),   // 2 Coríntios 12:9
        PromiseRef(48, 2, 20),   // Gálatas 2:20
        PromiseRef(49, 3, 20),   // Efésios 3:20
        PromiseRef(49, 6, 10),   // Efésios 6:10
        PromiseRef(50, 1, 6),    // Filipenses 1:6
        PromiseRef(50, 4, 19),   // Filipenses 4:19
        PromiseRef(51, 3, 2),    // Colossenses 3:2
        PromiseRef(54, 4, 12),   // 1 Timóteo 4:12
        PromiseRef(58, 4, 16),   // Hebreus 4:16
        PromiseRef(58, 12, 1),   // Hebreus 12:1
        PromiseRef(58, 12, 2),   // Hebreus 12:2
        PromiseRef(58, 13, 8),   // Hebreus 13:8
        PromiseRef(59, 1, 2),    // Tiago 1:2
        PromiseRef(59, 1, 5),    // Tiago 1:5
        PromiseRef(59, 4, 7),    // Tiago 4:7
        PromiseRef(59, 4, 8),    // Tiago 4:8
        PromiseRef(60, 1, 3),    // 1 Pedro 1:3
        PromiseRef(60, 2, 9),    // 1 Pedro 2:9
        PromiseRef(62, 4, 18),   // 1 João 4:18
        PromiseRef(62, 4, 19),   // 1 João 4:19
        PromiseRef(62, 5, 14)    // 1 João 5:14
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
