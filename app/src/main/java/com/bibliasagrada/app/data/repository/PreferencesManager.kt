package com.bibliasagrada.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

enum class ThemeMode { CLARO, ESCURO, SISTEMA }

/** Paleta de cores do app: dourada (padrão) ou preto e branco (clássica). */
enum class ColorPalette { DOURADO, PRETO_BRANCO }

class PreferencesManager(private val context: Context) {

    companion object {
        val FONT_SCALE = floatPreferencesKey("font_scale")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val COLOR_PALETTE = stringPreferencesKey("color_palette")
        val LAST_BOOK = intPreferencesKey("last_book")
        val LAST_CHAPTER = intPreferencesKey("last_chapter")
        val ACTIVE_TRANSLATION = stringPreferencesKey("active_translation")
    }

    val fontScale: Flow<Float> = context.dataStore.data.map { it[FONT_SCALE] ?: 1.0f }
    val themeMode: Flow<ThemeMode> = context.dataStore.data.map {
        ThemeMode.valueOf(it[THEME_MODE] ?: ThemeMode.SISTEMA.name)
    }
    val colorPalette: Flow<ColorPalette> = context.dataStore.data.map {
        ColorPalette.valueOf(it[COLOR_PALETTE] ?: ColorPalette.DOURADO.name)
    }
    /** Nome do arquivo .db da tradução ativa (ex.: "biblia.db"). */
    val activeTranslationFileName: Flow<String> = context.dataStore.data.map {
        it[ACTIVE_TRANSLATION] ?: "biblia.db"
    }

    suspend fun setFontScale(scale: Float) {
        context.dataStore.edit { it[FONT_SCALE] = scale.coerceIn(0.7f, 1.8f) }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[THEME_MODE] = mode.name }
    }

    suspend fun setColorPalette(palette: ColorPalette) {
        context.dataStore.edit { it[COLOR_PALETTE] = palette.name }
    }

    suspend fun setActiveTranslationFileName(fileName: String) {
        context.dataStore.edit { it[ACTIVE_TRANSLATION] = fileName }
    }
}
