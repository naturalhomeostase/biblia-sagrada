package com.bibliasagrada.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bibliasagrada.app.data.model.Book
import com.bibliasagrada.app.data.repository.BibleRepository
import com.bibliasagrada.app.data.repository.ColorPalette
import com.bibliasagrada.app.ui.theme.LocalColorPalette
import com.bibliasagrada.app.ui.theme.LocalHighlightColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BooksScreen(
    repository: BibleRepository,
    onOpenBook: (Int) -> Unit,
    onOpenReader: (Int, Int, Int) -> Unit,
    onSearch: () -> Unit,
    onFavorites: () -> Unit,
    onHistory: () -> Unit,
    onNotes: () -> Unit,
    onBookmarks: () -> Unit,
    onTranslations: () -> Unit,
    onPromises: () -> Unit,
    onBackup: () -> Unit,
    onHelp: () -> Unit,
    onDailyVerseReview: () -> Unit,
    onSettings: () -> Unit,
    onAbout: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var books by remember { mutableStateOf<List<Book>>(emptyList()) }
    var progress by remember { mutableStateOf<Triple<Int, Int, Int>?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }
    val pagerState = rememberPagerState(pageCount = { 2 })
    val bookmarkedBookIds by repository.observeBookmarkedBookIds().collectAsState(initial = emptySet())
    val singleBookmarkColorByBook by repository.observeSingleBookmarkColorByBook().collectAsState(initial = emptyMap())
    val colorPalette = LocalColorPalette.current
    val highlightColors = LocalHighlightColors.current

    LaunchedEffect(Unit) {
        books = repository.getBooks()
        repository.getReadingProgress()?.let {
            progress = Triple(it.bookId, it.chapter, it.verse)
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = if (colorPalette == ColorPalette.ROSA) {
                    Modifier.background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
                } else {
                    Modifier
                }
            ) {
            TopAppBar(
                title = { Text("Bíblia Sagrada") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (colorPalette == ColorPalette.ROSA) Color.Transparent else MaterialTheme.colorScheme.background
                ),
                actions = {
                    IconButton(onClick = onSearch) {
                        Icon(Icons.Filled.Search, contentDescription = "Buscar")
                    }
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Mais opções")
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        val menuIconTint = MaterialTheme.colorScheme.primary
                        DropdownMenuItem(
                            text = { Text("Favoritos") },
                            leadingIcon = { Icon(Icons.Filled.Bookmark, null, tint = menuIconTint) },
                            onClick = { menuExpanded = false; onFavorites() }
                        )
                        DropdownMenuItem(
                            text = { Text("Histórico") },
                            leadingIcon = { Icon(Icons.Filled.History, null, tint = menuIconTint) },
                            onClick = { menuExpanded = false; onHistory() }
                        )
                        DropdownMenuItem(
                            text = { Text("Marcadores") },
                            leadingIcon = { Icon(Icons.Filled.Bookmarks, null, tint = menuIconTint) },
                            onClick = { menuExpanded = false; onBookmarks() }
                        )
                        DropdownMenuItem(
                            text = { Text("Minhas notas") },
                            leadingIcon = { Icon(Icons.Filled.EditNote, null, tint = menuIconTint) },
                            onClick = { menuExpanded = false; onNotes() }
                        )
                        DropdownMenuItem(
                            text = { Text("Traduções") },
                            leadingIcon = { Icon(Icons.Filled.Translate, null, tint = menuIconTint) },
                            onClick = { menuExpanded = false; onTranslations() }
                        )
                        DropdownMenuItem(
                            text = { Text("Caixinha de Promessas") },
                            leadingIcon = { Icon(Icons.Filled.AutoAwesome, null, tint = menuIconTint) },
                            onClick = { menuExpanded = false; onPromises() }
                        )
                        DropdownMenuItem(
                            text = { Text("Encontre ajuda") },
                            leadingIcon = { Icon(Icons.Filled.Favorite, null, tint = menuIconTint) },
                            onClick = { menuExpanded = false; onHelp() }
                        )
                        DropdownMenuItem(
                            text = { Text("Rever versículo do dia") },
                            leadingIcon = { Icon(Icons.Filled.AutoAwesome, null, tint = menuIconTint) },
                            onClick = { menuExpanded = false; onDailyVerseReview() }
                        )
                        DropdownMenuItem(
                            text = { Text("Backup e restauração") },
                            leadingIcon = { Icon(Icons.Filled.Backup, null, tint = menuIconTint) },
                            onClick = { menuExpanded = false; onBackup() }
                        )
                        DropdownMenuItem(
                            text = { Text("Configurações") },
                            leadingIcon = { Icon(Icons.Filled.Settings, null, tint = menuIconTint) },
                            onClick = { menuExpanded = false; onSettings() }
                        )
                        DropdownMenuItem(
                            text = { Text("Sobre") },
                            leadingIcon = { Icon(Icons.Filled.Info, null, tint = menuIconTint) },
                            onClick = { menuExpanded = false; onAbout() }
                        )
                    }
                }
            )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            progress?.let { (bookId, chapter, verse) ->
                val bookName = books.firstOrNull { it.id == bookId }?.name ?: ""
                if (bookName.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clickable { onOpenReader(bookId, chapter, verse) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.AutoStories,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(Modifier.size(12.dp))
                            Column {
                                Text(
                                    "Continuar de onde parei",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Text(
                                    "$bookName $chapter",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }

            TabRow(selectedTabIndex = pagerState.currentPage) {
                Tab(
                    selected = pagerState.currentPage == 0,
                    onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                    text = { Text("Antigo Testamento") }
                )
                Tab(
                    selected = pagerState.currentPage == 1,
                    onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                    text = { Text("Novo Testamento") }
                )
            }

            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                val filtered = books.filter { it.testament == if (page == 0) "AT" else "NT" }
                LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                    items(filtered) { book ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenBook(book.id) }
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(book.name, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp))
                                if (bookmarkedBookIds.contains(book.id)) {
                                    val specificColorName = singleBookmarkColorByBook[book.id]
                                    val tint = specificColorName?.let { highlightColors[it] } ?: MaterialTheme.colorScheme.primary
                                    Icon(
                                        Icons.Filled.Bookmark,
                                        contentDescription = "Tem marcador",
                                        tint = tint,
                                        modifier = Modifier.padding(start = 6.dp).size(16.dp)
                                    )
                                }
                            }
                            Text(
                                "${book.chapterCount} cap.",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }
}
