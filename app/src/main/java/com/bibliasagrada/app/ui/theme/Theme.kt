package com.bibliasagrada.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import com.bibliasagrada.app.data.repository.ColorPalette
import com.bibliasagrada.app.data.repository.ThemeMode

private val GoldLightColors = lightColorScheme(
    primary = GoldLightPrimary,
    onPrimary = GoldLightOnPrimary,
    secondary = GoldLightSecondary,
    background = GoldLightBackground,
    onBackground = GoldLightOnBackground,
    surface = GoldLightSurface,
    onSurface = GoldLightOnBackground,
    surfaceVariant = GoldLightSurfaceVariant,
    outline = GoldLightOutline
)

private val GoldDarkColors = darkColorScheme(
    primary = GoldDarkPrimary,
    onPrimary = GoldDarkOnPrimary,
    secondary = GoldDarkSecondary,
    background = GoldDarkBackground,
    onBackground = GoldDarkOnBackground,
    surface = GoldDarkSurface,
    onSurface = GoldDarkOnBackground,
    surfaceVariant = GoldDarkSurfaceVariant,
    outline = GoldDarkOutline
)

private val MonoLightColors = lightColorScheme(
    primary = MonoLightPrimary,
    onPrimary = MonoLightOnPrimary,
    secondary = MonoLightSecondary,
    background = MonoLightBackground,
    onBackground = MonoLightOnBackground,
    surface = MonoLightSurface,
    onSurface = MonoLightOnBackground,
    surfaceVariant = MonoLightSurfaceVariant,
    outline = MonoLightOutline
)

private val MonoDarkColors = darkColorScheme(
    primary = MonoDarkPrimary,
    onPrimary = MonoDarkOnPrimary,
    secondary = MonoDarkSecondary,
    background = MonoDarkBackground,
    onBackground = MonoDarkOnBackground,
    surface = MonoDarkSurface,
    onSurface = MonoDarkOnBackground,
    surfaceVariant = MonoDarkSurfaceVariant,
    outline = MonoDarkOutline
)

private val RoseLightColors = lightColorScheme(
    primary = RoseLightPrimary,
    onPrimary = RoseLightOnPrimary,
    secondary = RoseLightSecondary,
    background = RoseLightBackground,
    onBackground = RoseLightOnBackground,
    surface = RoseLightSurface,
    onSurface = RoseLightOnBackground,
    surfaceVariant = RoseLightSurfaceVariant,
    outline = RoseLightOutline
)

private val RoseDarkColors = darkColorScheme(
    primary = RoseDarkPrimary,
    onPrimary = RoseDarkOnPrimary,
    secondary = RoseDarkSecondary,
    background = RoseDarkBackground,
    onBackground = RoseDarkOnBackground,
    surface = RoseDarkSurface,
    onSurface = RoseDarkOnBackground,
    surfaceVariant = RoseDarkSurfaceVariant,
    outline = RoseDarkOutline
)

/** Indica, em qualquer ponto da árvore de composição, se o tema escuro está ativo. */
val LocalIsDarkTheme = compositionLocalOf { false }

/** Paleta de cores atual — usada por telas que precisam de um detalhe visual específico da paleta (ex.: gradiente do tema rosa). */
val LocalColorPalette = compositionLocalOf { ColorPalette.DOURADO }

/** Cores de marcação (highlight) adequadas ao tema atual (claro ou escuro). */
val LocalHighlightColors = compositionLocalOf<Map<String, Color>> { LightHighlightColors }

@Composable
fun BibliaSagradaTheme(
    themeMode: ThemeMode = ThemeMode.SISTEMA,
    colorPalette: ColorPalette = ColorPalette.DOURADO,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.CLARO -> false
        ThemeMode.ESCURO -> true
        ThemeMode.SISTEMA -> isSystemInDarkTheme()
    }

    val colorScheme = when (colorPalette) {
        ColorPalette.DOURADO -> if (darkTheme) GoldDarkColors else GoldLightColors
        ColorPalette.PRETO_BRANCO -> if (darkTheme) MonoDarkColors else MonoLightColors
        ColorPalette.ROSA -> if (darkTheme) RoseDarkColors else RoseLightColors
    }

    val highlightColors = if (darkTheme) DarkHighlightColors else LightHighlightColors

    CompositionLocalProvider(
        LocalIsDarkTheme provides darkTheme,
        LocalColorPalette provides colorPalette,
        LocalHighlightColors provides highlightColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
