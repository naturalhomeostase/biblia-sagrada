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
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.bibliasagrada.app.data.model.Book
import com.bibliasagrada.app.data.repository.BibleRepository
import com.bibliasagrada.app.data.room.BookmarkEntity
import com.bibliasagrada.app.ui.theme.LocalHighlightColors
import kotlinx.coroutines.launch

/**
 * Marcadores agrupados por rótulo (nome). Cada grupo mostra um resumo tipo
 * "Estudo Bíblico: Gálatas 2, Romanos 6, ..." e pode ser expandido para
 * ver/abrir/remover cada capítulo individualmente.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    repository: BibleRepository,
    onBack: () -> Unit,
    onOpenReader: (Int, Int) -> Unit
) {
    val scope = rememberCoroutineScope()
    val bookmarks by repository.observeBookmarks().collectAsState(initial = emptyList())
    var books by remember { mutableStateOf<List<Book>>(emptyList()) }
    val highlightColors = LocalHighlightColors.current
    var expandedNames by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(Unit) { books = repository.getBooks() }

    fun bookName(bookId: Int) = books.firstOrNull { it.id == bookId }?.name ?: ""

    // Agrupa por nome (rótulo). Marcadores sem nome ficam juntos num grupo
    // "(sem nome)", cada um continua abrível/removível individualmente.
    val groups = remember(bookmarks) {
        bookmarks.groupBy { it.name }
            .toList()
            .sortedWith(compareBy({ it.first.isBlank() }, { it.first.lowercase() }))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Marcadores") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar") }
                }
            )
        }
    ) { padding ->
        if (bookmarks.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.Bookmarks,
                message = "Você ainda não marcou nenhuma página.\nDurante a leitura, toque no ícone de marcador " +
                    "no topo da tela para salvar o capítulo com um nome.",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(groups, key = { it.first.ifBlank { "\u0000sem_nome" } }) { (name, entries) ->
                    val isExpanded = expandedNames.contains(name)
                    // Cor do grupo: só mostra uma cor específica quando todos os
                    // marcadores desse rótulo compartilham a mesma cor escolhida.
                    val distinctColors = entries.map { it.color }.distinct()
                    val groupColorName = if (distinctColors.size == 1) distinctColors.first() else null
                    val summary = remember(entries, books) {
                        entries
                            .sortedWith(compareBy({ it.bookId }, { it.chapter }))
                            .joinToString(", ") { "${bookName(it.bookId)} ${it.chapter}" }
                    }

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
                                    expandedNames = if (isExpanded) expandedNames - name else expandedNames + name
                                }
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    groupColorName?.let { colorName ->
                                        highlightColors[colorName]?.let { swatch ->
                                            Box(
                                                modifier = Modifier
                                                    .padding(end = 8.dp)
                                                    .size(12.dp)
                                                    .background(swatch, CircleShape)
                                            )
                                        }
                                    }
                                    Text(
                                        name.ifBlank { "(sem nome)" },
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        " · ${entries.size}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                    contentDescription = if (isExpanded) "Recolher" else "Expandir"
                                )
                            }
                            Text(
                                summary,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            if (isExpanded) {
                                Column(modifier = Modifier.padding(top = 10.dp)) {
                                    entries.sortedWith(compareBy({ it.bookId }, { it.chapter }))
                                        .forEach { entry ->
                                            BookmarkEntryRow(
                                                entry = entry,
                                                bookName = bookName(entry.bookId),
                                                onOpen = { onOpenReader(entry.bookId, entry.chapter) },
                                                onDelete = { scope.launch { repository.removeBookmark(entry.id) } }
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

@Composable
private fun BookmarkEntryRow(
    entry: BookmarkEntity,
    bookName: String,
    onOpen: () -> Unit,
    onDelete: () -> Unit
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
        Text("$bookName ${entry.chapter}", style = MaterialTheme.typography.bodyMedium)
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "Remover este marcador")
        }
    }
}
