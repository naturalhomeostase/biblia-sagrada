package com.bibliasagrada.app.ui.theme

import androidx.compose.ui.graphics.Color

// ======================================================================
// Paleta DOURADA (padrão do app) — tom "papel" quente / dourado
// ======================================================================

// Dourado claro
val GoldLightBackground = Color(0xFFFBF6ED)
val GoldLightSurface = Color(0xFFFBF6ED)
val GoldLightSurfaceVariant = Color(0xFFF0E7D6)
val GoldLightOnBackground = Color(0xFF2B2420)
val GoldLightPrimary = Color(0xFF7A5230)
val GoldLightOnPrimary = Color(0xFFFFFFFF)
val GoldLightSecondary = Color(0xFF8C6D46)
val GoldLightOutline = Color(0xFFD8CBB0)

// Dourado escuro
val GoldDarkBackground = Color(0xFF15130F)
val GoldDarkSurface = Color(0xFF15130F)
val GoldDarkSurfaceVariant = Color(0xFF2B2620)
val GoldDarkOnBackground = Color(0xFFEDE6D8)
val GoldDarkPrimary = Color(0xFFD7B37B)
val GoldDarkOnPrimary = Color(0xFF3A2B14)
val GoldDarkSecondary = Color(0xFFC0A379)
val GoldDarkOutline = Color(0xFF463F35)

// ======================================================================
// Paleta PRETO E BRANCO (clássica) — tons neutros, sem dourado
// ======================================================================

// Preto e branco claro
val MonoLightBackground = Color(0xFFFFFFFF)
val MonoLightSurface = Color(0xFFFFFFFF)
val MonoLightSurfaceVariant = Color(0xFFEDEDED)
val MonoLightOnBackground = Color(0xFF1A1A1A)
val MonoLightPrimary = Color(0xFF1A1A1A)
val MonoLightOnPrimary = Color(0xFFFFFFFF)
val MonoLightSecondary = Color(0xFF5C5C5C)
val MonoLightOutline = Color(0xFFD6D6D6)

// Preto e branco escuro
val MonoDarkBackground = Color(0xFF121212)
val MonoDarkSurface = Color(0xFF121212)
val MonoDarkSurfaceVariant = Color(0xFF262626)
val MonoDarkOnBackground = Color(0xFFF2F2F2)
val MonoDarkPrimary = Color(0xFFF2F2F2)
val MonoDarkOnPrimary = Color(0xFF1A1A1A)
val MonoDarkSecondary = Color(0xFFBFBFBF)
val MonoDarkOutline = Color(0xFF3D3D3D)

// ======================================================================
// Paleta "Bíblia para mulheres" — rosa antigo (dusty rose) + branco,
// um visual clean e delicado, mantendo a seriedade de uma Bíblia.
// ======================================================================

// Rosa antigo claro
val RoseLightBackground = Color(0xFFFFFBFB)
val RoseLightSurface = Color(0xFFFFFBFB)
val RoseLightSurfaceVariant = Color(0xFFF5E6E8)
val RoseLightOnBackground = Color(0xFF3A2A2D)
val RoseLightPrimary = Color(0xFFB76E79)
val RoseLightOnPrimary = Color(0xFFFFFFFF)
val RoseLightSecondary = Color(0xFF9C7A80)
val RoseLightOutline = Color(0xFFE8D3D6)

// Rosa antigo escuro
val RoseDarkBackground = Color(0xFF1A1416)
val RoseDarkSurface = Color(0xFF1A1416)
val RoseDarkSurfaceVariant = Color(0xFF2E2225)
val RoseDarkOnBackground = Color(0xFFF2E6E8)
val RoseDarkPrimary = Color(0xFFD9A5AC)
val RoseDarkOnPrimary = Color(0xFF3A2226)
val RoseDarkSecondary = Color(0xFFC4989F)
val RoseDarkOutline = Color(0xFF463539)

// ======================================================================
// Cores de marcação (highlight)
// Modo claro: tons pastel vivos, funcionam bem sob texto escuro.
// Modo escuro: tons mais escuros/dessaturados, para não ofuscar o texto
// claro nem "brigar" com ele — mantêm boa legibilidade em fundo escuro.
// ======================================================================

// Marcação — modo claro (vivas, como antes)
val HighlightAmareloLight = Color(0xFFFFE082)
val HighlightAzulLight = Color(0xFFA8D8FF)
val HighlightVerdeLight = Color(0xFFB9E4C9)
val HighlightVermelhoLight = Color(0xFFFFB3AE)
val HighlightRoxoLight = Color(0xFFDCC1F2)

// Marcação — modo escuro (tonalidades escurecidas/dessaturadas)
val HighlightAmareloDark = Color(0xFF5C4A1E)
val HighlightAzulDark = Color(0xFF264A5C)
val HighlightVerdeDark = Color(0xFF2C4A38)
val HighlightVermelhoDark = Color(0xFF5C2E2C)
val HighlightRoxoDark = Color(0xFF3E2E4F)

val LightHighlightColors = mapOf(
    "amarelo" to HighlightAmareloLight,
    "azul" to HighlightAzulLight,
    "verde" to HighlightVerdeLight,
    "vermelho" to HighlightVermelhoLight,
    "roxo" to HighlightRoxoLight
)

val DarkHighlightColors = mapOf(
    "amarelo" to HighlightAmareloDark,
    "azul" to HighlightAzulDark,
    "verde" to HighlightVerdeDark,
    "vermelho" to HighlightVermelhoDark,
    "roxo" to HighlightRoxoDark
)

/** Mantido por compatibilidade — usar [LightHighlightColors] / [DarkHighlightColors] ou [currentHighlightColors]. */
val HighlightColors = LightHighlightColors
