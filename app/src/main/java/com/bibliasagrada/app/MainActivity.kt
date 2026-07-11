package com.bibliasagrada.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.bibliasagrada.app.data.repository.BibleRepository
import com.bibliasagrada.app.data.repository.ColorPalette
import com.bibliasagrada.app.data.repository.ThemeMode
import com.bibliasagrada.app.ui.navigation.AppNavGraph
import com.bibliasagrada.app.ui.theme.BibliaSagradaTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // IMPORTANTE: a criação do BibleRepository não acontece mais aqui.
        // Ela envolve copiar o banco da Bíblia dos assets (~8MB) e abrir o SQLite,
        // o que é trabalho de I/O e não pode rodar na main thread antes do
        // setContent — isso já tinha causado ANR ("Application does not have
        // a focused window") na tela preta inicial. Agora isso é feito dentro
        // de uma coroutine em Dispatchers.IO, com uma tela de carregamento
        // simples exibida enquanto isso.
        setContent {
            BibliaSagradaRoot(applicationContext)
        }
    }
}

@Composable
fun BibliaSagradaRoot(appContext: android.content.Context) {
    var repository by remember { mutableStateOf<BibleRepository?>(null) }

    LaunchedEffect(Unit) {
        repository = withContext(Dispatchers.IO) {
            BibleRepository.getInstance(appContext)
        }
    }

    val repo = repository
    if (repo == null) {
        // Tela de carregamento — evita qualquer trabalho pesado antes do primeiro frame
        MaterialTheme {
            Surface(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    } else {
        BibliaSagradaApp(repo)
    }
}

@Composable
fun BibliaSagradaApp(repository: BibleRepository) {
    val themeMode by repository.prefs.themeMode.collectAsState(initial = ThemeMode.SISTEMA)
    val colorPalette by repository.prefs.colorPalette.collectAsState(initial = ColorPalette.DOURADO)
    BibliaSagradaTheme(themeMode = themeMode, colorPalette = colorPalette) {
        Surface(modifier = Modifier.fillMaxSize()) {
            AppNavGraph(repository = repository)
        }
    }
}
