package com.bibliasagrada.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.FormatColorReset
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.unit.dp
import com.bibliasagrada.app.data.model.Verse
import com.bibliasagrada.app.data.room.TagEntity
import com.bibliasagrada.app.ui.theme.LocalHighlightColors

/**
 * Menu de ações para um ou mais versículos selecionados.
 *
 * Quando [verses] tem mais de um item (seleção múltipla), o favoritar fica
 * escondido (não faz sentido para vários versículos de uma vez). As demais
 * ações (copiar, compartilhar, gerar imagem, marcar com cor, e também a
 * nota) funcionam normalmente com todos os versículos selecionados juntos —
 * a nota fica associada ao intervalo inteiro (do primeiro ao último
 * versículo selecionado).
 */
@Composable
fun VerseActionSheet(
    verses: List<Verse>,
    isFavorite: Boolean,
    currentHighlight: String?,
    currentNote: String,
    currentTags: List<TagEntity>,
    allTags: List<TagEntity>,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onShareImage: () -> Unit,
    onSaveImage: () -> Unit,
    onToggleFavorite: () -> Unit,
    onSetHighlight: (String?) -> Unit,
    onPartialHighlightClick: () -> Unit,
    onToggleTag: (TagEntity) -> Unit,
    onCreateTagClick: () -> Unit,
    onSaveNote: (String) -> Unit,
    onClose: () -> Unit
) {
    val isSingle = verses.size == 1
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
            Text(
                if (isSingle) verses.first().reference else "${verses.size} versículos selecionados",
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(onClick = onClose) { Icon(Icons.Filled.Close, contentDescription = "Fechar") }
        }
        Spacer(Modifier.height(4.dp))
        if (isSingle) {
            Text(verses.first().text, style = MaterialTheme.typography.bodyLarge)
        } else {
            Text(
                verses.joinToString("  ") { it.shortReference },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ActionIcon(Icons.Filled.ContentCopy, "Copiar", onClick = onCopy)
            ActionIcon(Icons.Filled.Share, "Enviar", onClick = onShare)
            ActionIcon(Icons.Filled.Image, "Imagem", onClick = onShareImage)
            ActionIcon(Icons.Filled.Download, "Salvar", onClick = onSaveImage)
            if (isSingle) {
                ActionIcon(
                    if (isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                    "Favorito",
                    onClick = onToggleFavorite
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Marcar com cor", style = MaterialTheme.typography.labelLarge)
            if (currentHighlight != null) {
                TextButton(onClick = { onSetHighlight(null) }, modifier = Modifier.padding(start = 8.dp)) {
                    Icon(Icons.Filled.FormatColorReset, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Remover marcação", modifier = Modifier.padding(start = 4.dp))
                }
            }
        }
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
        }
        if (isSingle) {
            Spacer(Modifier.height(4.dp))
            TextButton(onClick = onPartialHighlightClick, modifier = Modifier.padding(start = 0.dp)) {
                Icon(Icons.Filled.Colorize, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("Realçar só um trecho...", modifier = Modifier.padding(start = 6.dp))
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Sell, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text(
                if (isSingle) "Tags de estudo" else "Tags de estudo (para estes ${verses.size} versículos)",
                style = MaterialTheme.typography.labelLarge
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val currentTagIds = remember(currentTags) { currentTags.map { it.id }.toSet() }
            allTags.forEach { tag ->
                val isChosen = tag.id in currentTagIds
                val tagColor = highlightColors[tag.color] ?: MaterialTheme.colorScheme.secondaryContainer
                FilterChip(
                    selected = isChosen,
                    onClick = { onToggleTag(tag) },
                    label = { Text(tag.name) },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .padding(start = 2.dp)
                                .size(10.dp)
                                .background(tagColor, CircleShape)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(),
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            AssistChip(
                onClick = onCreateTagClick,
                label = { Text("Nova tag") },
                leadingIcon = {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                },
                colors = AssistChipDefaults.assistChipColors()
            )
        }

        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.EditNote, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text(
                if (isSingle) "Minha nota" else "Minha nota (para estes ${verses.size} versículos)",
                style = MaterialTheme.typography.labelLarge
            )
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = noteText,
            onValueChange = { noteText = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(if (isSingle) "Escreva uma reflexão sobre este versículo..." else "Escreva uma reflexão sobre esta passagem...")
            },
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
        Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 1)
    }
}
