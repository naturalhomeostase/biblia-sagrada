package com.bibliasagrada.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.bibliasagrada.app.data.model.Book
import com.bibliasagrada.app.data.repository.BibleRepository
import com.bibliasagrada.app.ui.theme.LocalHighlightColors

/**
 * Tela de capítulos de um livro. Igual à leitura de capítulos no ReaderScreen,
 * dá para arrastar (swipe) para o lado para ir ao livro anterior/seguinte —
 * cada "página" do pager é a grade de capítulos de um livro inteiro.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChaptersScreen(
    repository: BibleRepository,
    bookId: Int,
    onBack: () -> Unit,
    onOpenChapter: (Int, Int) -> Unit // (bookId, chapter)
) {
    var books by remember { mutableStateOf<List<Book>>(emptyList()) }

    LaunchedEffect(Unit) {
        books = repository.getBooks()
    }

    if (books.isEmpty()) return // ainda carregando a lista de livros

    val initialPage = remember(books, bookId) {
        books.indexOfFirst { it.id == bookId }.let { if (it >= 0) it else 0 }
    }
    val pagerState = rememberPagerState(initialPage = initialPage) { books.size }
    val currentBook = books.getOrNull(pagerState.currentPage)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentBook?.name ?: "") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().padding(padding)
        ) { page ->
            ChapterGridPage(
                repository = repository,
                book = books[page],
                onOpenChapter = { chapter -> onOpenChapter(books[page].id, chapter) }
            )
        }
    }
}

/** Grade de capítulos de um único livro — uma "página" do pager de livros. */
@Composable
private fun ChapterGridPage(
    repository: BibleRepository,
    book: Book,
    onOpenChapter: (Int) -> Unit
) {
    val bookmarkedChapters by remember(book.id) { repository.observeBookmarksForBook(book.id) }
        .collectAsState(initial = emptyList())
    // Para cada capítulo marcado: se todos os marcadores dele têm a mesma cor
    // (ou se há só um), usa essa cor na fita; se houver 2+ marcadores/labels
    // com cores diferentes no mesmo capítulo, usa a cor padrão do tema (mesma
    // regra usada para o ícone ao lado do livro).
    val colorByChapter = remember(bookmarkedChapters) {
        bookmarkedChapters.groupBy { it.chapter }.mapValues { (_, marks) ->
            marks.map { it.color }.distinct().let { if (it.size == 1) it.first() else null }
        }
    }
    val highlightColors = LocalHighlightColors.current

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 64.dp),
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items((1..book.chapterCount).toList()) { chapter ->
            Box(
                modifier = Modifier
                    .padding(6.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onOpenChapter(chapter) },
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text("$chapter", style = MaterialTheme.typography.titleMedium)
                    }
                }
                if (colorByChapter.containsKey(chapter)) {
                    val specificColorName = colorByChapter[chapter]
                    val tint = specificColorName?.let { highlightColors[it] } ?: MaterialTheme.colorScheme.primary
                    Icon(
                        Icons.Filled.Bookmark,
                        contentDescription = "Capítulo marcado",
                        tint = tint,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 2.dp, end = 4.dp)
                            .size(16.dp)
                    )
                }
            }
        }
    }
}
