package com.bibliasagrada.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.unit.dp
import com.bibliasagrada.app.data.model.Book
import com.bibliasagrada.app.data.repository.BibleRepository
import kotlinx.coroutines.launch

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

    LaunchedEffect(Unit) { books = repository.getBooks() }

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
                items(bookmarks, key = { it.id }) { bookmark ->
                    val bookName = books.firstOrNull { it.id == bookmark.bookId }?.name ?: ""
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenReader(bookmark.bookId, bookmark.chapter) }
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                bookmark.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "$bookName ${bookmark.chapter}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        IconButton(onClick = {
                            scope.launch { repository.removeBookmark(bookmark.id) }
                        }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Remover marcador")
                        }
                    }
                }
            }
        }
    }
}
