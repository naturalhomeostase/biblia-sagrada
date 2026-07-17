package com.bibliasagrada.app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.bibliasagrada.app.data.model.Verse
import com.bibliasagrada.app.data.repository.BibleRepository

/**
 * Primeira tela ao abrir o app: mostra o versículo do dia (um só, sem opção
 * de trocar) e um botão para seguir para a Bíblia.
 */
@Composable
fun DailyVerseScreen(
    repository: BibleRepository,
    onContinue: () -> Unit
) {
    var verse by remember { mutableStateOf<Verse?>(null) }

    LaunchedEffect(Unit) {
        verse = repository.getDailyVerse()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        val v = verse
        if (v == null) {
            CircularProgressIndicator()
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Versículo do dia",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    "\u201C${v.text}\u201D",
                    style = MaterialTheme.typography.headlineSmall.copy(fontStyle = FontStyle.Italic),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(top = 24.dp)
                )
                Text(
                    v.reference,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Button(onClick = onContinue, modifier = Modifier.fillMaxWidth().padding(top = 40.dp)) {
                    Text("Continuar")
                }
            }
        }
    }
}
