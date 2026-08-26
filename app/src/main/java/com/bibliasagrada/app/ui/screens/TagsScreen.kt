package com.bibliasagrada.app.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bibliasagrada.app.data.model.Verse
import com.bibliasagrada.app.data.repository.BibleRepository
import com.bibliasagrada.app.data.room.TagEntity
import com.bibliasagrada.app.ui.theme.LocalHighlightColors
import kotlinx.coroutines.launch

/**
 * Lista todas as tags de estudo criadas, cada uma com sua cor e a quantidade
 * de versículos marcados. Ao tocar numa tag, expande e mostra os versículos
 * — tocar num versículo abre o leitor naquele ponto; dá para remover a tag
 * de um versículo específico, ou apagar a tag inteira (com confirmação).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagsScreen(
    repository: BibleRepository,
    onBack: () -> Unit,
    onOpenReader: (Int, Int, Int) -> Unit
) {
    val scope = rememberCoroutineScope()
    val tags by repository.observeAllTags().collectAsState(initial = emptyList())
    val tagCounts by repository.observeTagCounts().collectAsState(initial = emptyMap())
    val highlightColors = LocalHighlightColors.current

    var expandedTagId by remember { mutableStateOf<Long?>(null) }
    var versesForExpanded by remember { mutableStateOf<List<Verse>>(emptyList()) }
    var tagToDelete by remember { mutableStateOf<TagEntity?>(null) }
    var tagToEdit by remember { mutableStateOf<TagEntity?>(null) }

    LaunchedEffect(expandedTagId) {
        val id = expandedTagId
        if (id == null) {
            versesForExpanded = emptyList()
            return@LaunchedEffect
        }
        val refs = repository.getVersesForTag(id)
        versesForExpanded = refs.mapNotNull { repository.getVerse(it.bookId, it.chapter, it.verse) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tags de estudo") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar") }
                }
            )
        }
    ) { padding ->
        if (tags.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.Sell,
                message = "Você ainda não criou nenhuma tag.\nDurante a leitura, toque num versículo e use " +
                    "\"Adicionar tag\" para marcar temas como \"Salvação\" ou \"Fé\" e encontrá-los aqui depois.",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(tags, key = { it.id }) { tag ->
                    val isExpanded = expandedTagId == tag.id
                    val count = tagCounts[tag.id] ?: 0
                    val tagColor = highlightColors[tag.color] ?: MaterialTheme.colorScheme.primary

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .animateContentSize(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    expandedTagId = if (isExpanded) null else tag.id
                                }
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .padding(end = 8.dp)
                                            .size(12.dp)
                                            .background(tagColor, CircleShape)
                                    )
                                    Text(
                                        tag.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    Text(
                                        " · $count",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { tagToEdit = tag }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Editar tag \"${tag.name}\"")
                                }
                                IconButton(onClick = { tagToDelete = tag }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Apagar tag \"${tag.name}\"")
                                }
                                Icon(
                                    if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                    contentDescription = if (isExpanded) "Recolher" else "Expandir"
                                )
                            }

                            if (isExpanded) {
                                Column(modifier = Modifier.padding(top = 10.dp)) {
                                    if (count == 0) {
                                        Text(
                                            "Nenhum versículo marcado com esta tag ainda.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else {
                                        versesForExpanded.forEach { verse ->
                                            TagVerseRow(
                                                verse = verse,
                                                onOpen = { onOpenReader(verse.bookId, verse.chapter, verse.verse) },
                                                onRemove = {
                                                    scope.launch {
                                                        repository.removeVerseTag(tag.id, verse.bookId, verse.chapter, verse.verse)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    tagToDelete?.let { tag ->
        AlertDialog(
            onDismissRequest = { tagToDelete = null },
            title = { Text("Apagar tag \"${tag.name}\"?") },
            text = { Text("Isso remove a tag de todos os versículos marcados com ela. Os versículos em si não são afetados.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { repository.deleteTag(tag.id) }
                    if (expandedTagId == tag.id) expandedTagId = null
                    tagToDelete = null
                }) { Text("Apagar") }
            },
            dismissButton = {
                TextButton(onClick = { tagToDelete = null }) { Text("Cancelar") }
            }
        )
    }

    tagToEdit?.let { tag ->
        EditTagDialog(
            tag = tag,
            onDismiss = { tagToEdit = null },
            onSave = { newName, newColor ->
                scope.launch { repository.updateTag(tag, newName, newColor) }
                tagToEdit = null
            }
        )
    }
}

@Composable
private fun TagVerseRow(
    verse: Verse,
    onOpen: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onOpen)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(verse.reference, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text(
                verse.text,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(onClick = onRemove) {
            Icon(Icons.Filled.Delete, contentDescription = "Remover esta tag deste versículo")
        }
    }
}

/** Diálogo simples para renomear uma tag e/ou trocar sua cor. */
@Composable
private fun EditTagDialog(
    tag: TagEntity,
    onDismiss: () -> Unit,
    onSave: (newName: String, newColor: String) -> Unit
) {
    var name by remember(tag) { mutableStateOf(tag.name) }
    var color by remember(tag) { mutableStateOf(tag.color) }
    val highlightColors = LocalHighlightColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar tag") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "Cor",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    highlightColors.forEach { (colorName, swatch) ->
                        val isSelected = color == colorName
                        Box(
                            modifier = Modifier
                                .padding(end = 10.dp)
                                .size(if (isSelected) 32.dp else 26.dp)
                                .background(swatch, CircleShape)
                                .clickable { color = colorName }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = { onSave(name.trim(), color) }
            ) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
