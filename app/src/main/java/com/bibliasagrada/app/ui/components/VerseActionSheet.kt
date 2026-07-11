package com.bibliasagrada.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FormatColorReset
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.bibliasagrada.app.data.model.Verse
import com.bibliasagrada.app.ui.theme.LocalHighlightColors

@Composable
fun VerseActionSheet(
    verse: Verse,
    isFavorite: Boolean,
    currentHighlight: String?,
    currentNote: String,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onToggleFavorite: () -> Unit,
    onSetHighlight: (String?) -> Unit,
    onSaveNote: (String) -> Unit,
    onClose: () -> Unit
) {
    var noteText by remember(currentNote) { mutableStateOf(currentNote) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
            .navigationBarsPadding()
            .imePadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(verse.reference, style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = onClose) { Icon(Icons.Filled.Close, contentDescription = "Fechar") }
        }
        Spacer(Modifier.height(4.dp))
        Text(verse.text, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ActionIcon(Icons.Filled.ContentCopy, "Copiar", onClick = onCopy)
            ActionIcon(Icons.Filled.Share, "Compartilhar", onClick = onShare)
            ActionIcon(
                if (isFavorite) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                "Favorito",
                onClick = onToggleFavorite
            )
        }

        Spacer(Modifier.height(16.dp))
        Text("Marcar com cor", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(8.dp))
        val highlightColors = LocalHighlightColors.current
        Row(verticalAlignment = Alignment.CenterVertically) {
            highlightColors.forEach { (name, color) ->
                val selected = currentHighlight == name
                androidx.compose.material3.Surface(
                    color = color,
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(end = 10.dp)
                        .size(if (selected) 34.dp else 28.dp)
                        .clickable { onSetHighlight(if (selected) null else name) }
                ) {}
            }
            if (currentHighlight != null) {
                IconButton(onClick = { onSetHighlight(null) }) {
                    Icon(Icons.Filled.FormatColorReset, contentDescription = "Remover marcação")
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.EditNote, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text("Minha nota", style = MaterialTheme.typography.labelLarge)
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = noteText,
            onValueChange = { noteText = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Escreva uma reflexão sobre este versículo...") },
            minLines = 2
        )
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { onSaveNote(noteText); onClose() }) {
                Text("Salvar nota")
            }
        }
    }
}

@Composable
private fun ActionIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        IconButton(onClick = onClick) { Icon(icon, contentDescription = label) }
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}
