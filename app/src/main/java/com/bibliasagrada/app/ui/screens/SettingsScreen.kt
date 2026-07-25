package com.bibliasagrada.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bibliasagrada.app.data.repository.BibleRepository
import com.bibliasagrada.app.data.repository.ColorPalette
import com.bibliasagrada.app.data.repository.ThemeMode
import com.bibliasagrada.app.ui.theme.readingTextStyle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    repository: BibleRepository,
    onBack: () -> Unit,
    onBookmarks: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val fontScale by repository.prefs.fontScale.collectAsState(initial = 1.0f)
    val themeMode by repository.prefs.themeMode.collectAsState(initial = ThemeMode.SISTEMA)
    val colorPalette by repository.prefs.colorPalette.collectAsState(initial = ColorPalette.DOURADO)
    val soundEnabled by repository.prefs.pageTurnSoundEnabled.collectAsState(initial = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurações") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar") }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(20.dp)) {
            Text("Tamanho da fonte", style = MaterialTheme.typography.titleMedium)
            Text(
                "No princípio criou Deus os céus e a terra.",
                style = readingTextStyle(fontScale),
                modifier = Modifier.padding(vertical = 12.dp)
            )
            Slider(
                value = fontScale,
                onValueChange = { newValue ->
                    scope.launch { repository.prefs.setFontScale(newValue) }
                },
                valueRange = 0.7f..1.8f
            )

            Text("Aparência", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 24.dp))
            ThemeOption("Claro", ThemeMode.CLARO, themeMode) {
                scope.launch { repository.prefs.setThemeMode(it) }
            }
            ThemeOption("Escuro", ThemeMode.ESCURO, themeMode) {
                scope.launch { repository.prefs.setThemeMode(it) }
            }
            ThemeOption("Seguir o sistema", ThemeMode.SISTEMA, themeMode) {
                scope.launch { repository.prefs.setThemeMode(it) }
            }

            Text("Cor do tema", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 24.dp))
            PaletteOption("Dourado (padrão)", ColorPalette.DOURADO, colorPalette) {
                scope.launch { repository.prefs.setColorPalette(it) }
            }
            PaletteOption("Preto e branco (clássico)", ColorPalette.PRETO_BRANCO, colorPalette) {
                scope.launch { repository.prefs.setColorPalette(it) }
            }
            PaletteOption("Bíblia para mulheres (rosa antigo)", ColorPalette.ROSA, colorPalette) {
                scope.launch { repository.prefs.setColorPalette(it) }
            }

            Text("Leitura", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 24.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Som ao virar a página")
                Switch(
                    checked = soundEnabled,
                    onCheckedChange = { scope.launch { repository.prefs.setPageTurnSoundEnabled(it) } }
                )
            }

            Text("Marcadores", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onBookmarks)
                    .padding(vertical = 10.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Bookmarks,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Text("Ver todos os marcadores")
                }
                Icon(Icons.Filled.ChevronRight, contentDescription = null)
            }
        }
    }
}

@Composable
private fun ThemeOption(
    label: String,
    mode: ThemeMode,
    current: ThemeMode,
    onSelect: (ThemeMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = current == mode, onClick = { onSelect(mode) })
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = current == mode, onClick = { onSelect(mode) })
        Text(label, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
private fun PaletteOption(
    label: String,
    palette: ColorPalette,
    current: ColorPalette,
    onSelect: (ColorPalette) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = current == palette, onClick = { onSelect(palette) })
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = current == palette, onClick = { onSelect(palette) })
        Text(label, modifier = Modifier.padding(start = 8.dp))
    }
}
