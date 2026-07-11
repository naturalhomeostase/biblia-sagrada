package com.bibliasagrada.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Fontes do sistema: serifada para o corpo do texto bíblico (leitura confortável),
// sem serifa para interface (menus, botões, títulos)
val ReadingFontFamily = FontFamily.Serif
val UiFontFamily = FontFamily.SansSerif

val AppTypography = Typography(
    headlineSmall = TextStyle(fontFamily = UiFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
    titleLarge = TextStyle(fontFamily = UiFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
    titleMedium = TextStyle(fontFamily = UiFontFamily, fontWeight = FontWeight.Medium, fontSize = 16.sp),
    bodyLarge = TextStyle(fontFamily = ReadingFontFamily, fontWeight = FontWeight.Normal, fontSize = 18.sp, lineHeight = 28.sp),
    bodyMedium = TextStyle(fontFamily = UiFontFamily, fontWeight = FontWeight.Normal, fontSize = 15.sp),
    labelLarge = TextStyle(fontFamily = UiFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    labelMedium = TextStyle(fontFamily = UiFontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp)
)

/** Cria a tipografia do texto de leitura já escalada pelo tamanho de fonte escolhido pelo usuário. */
fun readingTextStyle(scale: Float, verseNumberInline: Boolean = true): TextStyle = TextStyle(
    fontFamily = ReadingFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = (19f * scale).sp,
    lineHeight = (30f * scale).sp
)
