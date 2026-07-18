package com.bibliasagrada.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bibliasagrada.app.data.model.Verse
import com.bibliasagrada.app.data.repository.BibleRepository

/**
 * Tela do "Versículo do dia". Tem dois modos:
 *  - [forceShow] = false (abertura normal do app): só aparece de fato uma vez
 *    por dia, depois das 6h — fora disso, passa direto para a Bíblia sem o
 *    usuário perceber.
 *  - [forceShow] = true (reaberta pelo menu, "Rever versículo do dia"): mostra
 *    sempre, sem checar nada.
 * Não existe botão literal de continuar — a tela inteira é tocável, com um
 * "continuar" discreto e em itálico no canto inferior direito como dica.
 */
@Composable
fun DailyVerseScreen(
    repository: BibleRepository,
    forceShow: Boolean = false,
    onContinue: () -> Unit
) {
    var verse by remember { mutableStateOf<Verse?>(null) }
    var shouldRender by remember { mutableStateOf(forceShow) }
    var decided by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!forceShow) {
            val show = repository.shouldAutoShowDailyVerse()
            shouldRender = show
            decided = true
            if (!show) {
                onContinue()
                return@LaunchedEffect
            }
        } else {
            decided = true
        }
        verse = repository.getDailyVerse()
        repository.markDailyVerseShownToday()
    }

    if (!decided || !shouldRender) {
        // Nada visível — ou ainda decidindo, ou já passando direto (o
        // onContinue acima já foi disparado nesse caso).
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onContinue
            )
            .padding(32.dp)
    ) {
        val v = verse
        if (v == null) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Versículo do dia",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    "\u201C${v.text}\u201D",
                    style = MaterialTheme.typography.headlineSmall.copy(fontStyle = FontStyle.Italic),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 24.dp)
                )
                Text(
                    v.reference,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            Text(
                "continuar",
                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }
}
