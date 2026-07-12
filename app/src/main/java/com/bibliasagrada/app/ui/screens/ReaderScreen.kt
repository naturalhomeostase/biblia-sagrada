package com.bibliasagrada.app.ui.screens

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.bibliasagrada.app.data.model.ChapterRef
import com.bibliasagrada.app.data.model.Verse
import com.bibliasagrada.app.data.repository.BibleRepository
import com.bibliasagrada.app.data.room.NoteEntity
import com.bibliasagrada.app.ui.components.VerseActionSheet
import com.bibliasagrada.app.ui.theme.LocalHighlightColors
import com.bibliasagrada.app.ui.theme.readingTextStyle
import kotlinx.coroutines.launch

private const val MIN_FONT_SCALE = 0.7f
private const val MAX_FONT_SCALE = 1.8f

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    repository: BibleRepository,
    initialBookId: Int,
    initialChapter: Int,
    initialVerse: Int,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    var chapterRefs by remember { mutableStateOf<List<ChapterRef>>(emptyList()) }
    var startPage by remember { mutableStateOf<Int?>(null) }
    val fontScale by repository.prefs.fontScale.collectAsState(initial = 1.0f)

    LaunchedEffect(Unit) {
        val refs = repository.getAllChapterRefs()
        chapterRefs = refs
        val idx = refs.indexOfFirst { it.bookId == initialBookId && it.chapter == initialChapter }
        startPage = if (idx >= 0) idx else 0
    }

    val startPageValue = startPage
    if (chapterRefs.isEmpty() || startPageValue == null) {
        // Carregando lista de capítulos
        Box(modifier = Modifier.fillMaxSize()) { }
        return
    }

    val pagerState = rememberPagerState(initialPage = startPageValue) { chapterRefs.size }
    var selectedVerse by remember { mutableStateOf<Verse?>(null) }
    var showBookmarkDialog by remember { mutableStateOf(false) }

    val currentRef = chapterRefs.getOrNull(pagerState.currentPage)
    val bookmarksInChapter by remember(currentRef) {
        currentRef?.let { repository.observeBookmarksForChapter(it.bookId, it.chapter) }
            ?: kotlinx.coroutines.flow.flowOf(emptyList())
    }.collectAsState(initial = emptyList())
    val isChapterBookmarked = bookmarksInChapter.isNotEmpty()

    fun changeFontScale(delta: Float) {
        scope.launch {
            repository.prefs.setFontScale((fontScale + delta).coerceIn(MIN_FONT_SCALE, MAX_FONT_SCALE))
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (currentRef != null) "${currentRef.bookName} ${currentRef.chapter}" else "",
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        // Linhazinha na cor do tema, para marcar visualmente o título do capítulo.
                        Box(
                            modifier = Modifier
                                .padding(top = 3.dp)
                                .size(width = 36.dp, height = 2.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(1.dp))
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { showBookmarkDialog = true }) {
                        Icon(
                            if (isChapterBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = if (isChapterBookmarked) "Remover marcador" else "Marcar esta página"
                        )
                    }
                    IconButton(
                        onClick = { changeFontScale(-0.1f) },
                        enabled = fontScale > MIN_FONT_SCALE
                    ) {
                        Icon(Icons.Filled.Remove, contentDescription = "Diminuir fonte")
                    }
                    IconButton(
                        onClick = { changeFontScale(0.1f) },
                        enabled = fontScale < MAX_FONT_SCALE
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Aumentar fonte")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val ref = chapterRefs[page]
                ChapterPage(
                    repository = repository,
                    ref = ref,
                    fontScale = fontScale,
                    scrollToVerse = if (page == startPageValue && initialVerse > 0) initialVerse else null,
                    onVerseClick = { verse -> selectedVerse = verse },
                    onFontScaleChange = { newScale ->
                        scope.launch { repository.prefs.setFontScale(newScale.coerceIn(MIN_FONT_SCALE, MAX_FONT_SCALE)) }
                    }
                )
            }

            // Setinhas sutis nas bordas, lembrando que dá para arrastar para o
            // capítulo anterior/seguinte.
            if (pagerState.currentPage > 0) {
                Icon(
                    Icons.Filled.ChevronLeft,
                    contentDescription = "Capítulo anterior",
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 2.dp)
                        .size(32.dp)
                )
            }
            if (pagerState.currentPage < chapterRefs.size - 1) {
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = "Próximo capítulo",
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 2.dp)
                        .size(32.dp)
                )
            }
        }
    }

    // Registra histórico e progresso de leitura sempre que o capítulo exibido mudar
    LaunchedEffect(pagerState.currentPage, chapterRefs) {
        val ref = chapterRefs.getOrNull(pagerState.currentPage) ?: return@LaunchedEffect
        repository.recordHistory(ref.bookId, ref.chapter)
        repository.saveReadingProgress(ref.bookId, ref.chapter, 1)
    }

    if (showBookmarkDialog && currentRef != null) {
        BookmarkDialog(
            isBookmarked = isChapterBookmarked,
            existingNames = bookmarksInChapter.map { it.name },
            onDismiss = { showBookmarkDialog = false },
            onSave = { name ->
                scope.launch { repository.addBookmark(currentRef.bookId, currentRef.chapter, name) }
                showBookmarkDialog = false
            },
            onRemoveAll = {
                scope.launch { repository.removeBookmarksForChapter(currentRef.bookId, currentRef.chapter) }
                showBookmarkDialog = false
            }
        )
    }

    selectedVerse?.let { verse ->
        val sheetState = rememberModalBottomSheetState()
        var isFavorite by remember(verse) { mutableStateOf(false) }
        var highlight by remember(verse) { mutableStateOf<String?>(null) }
        var noteText by remember(verse) { mutableStateOf("") }
        var existingNote by remember(verse) { mutableStateOf<NoteEntity?>(null) }

        LaunchedEffect(verse) {
            isFavorite = repository.isFavorite(verse.bookId, verse.chapter, verse.verse)
            existingNote = repository.getNoteForVerse(verse.bookId, verse.chapter, verse.verse)
            noteText = existingNote?.text ?: ""
        }

        ModalBottomSheet(onDismissRequest = { selectedVerse = null }, sheetState = sheetState) {
            VerseActionSheet(
                verse = verse,
                isFavorite = isFavorite,
                currentHighlight = highlight,
                currentNote = noteText,
                onCopy = {
                    clipboard.setText(AnnotatedString("${verse.text} (${verse.reference})"))
                },
                onShare = {
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "\"${verse.text}\" — ${verse.reference}")
                    }
                    context.startActivity(Intent.createChooser(sendIntent, null))
                },
                onToggleFavorite = {
                    scope.launch {
                        repository.toggleFavorite(verse.bookId, verse.chapter, verse.verse)
                        isFavorite = !isFavorite
                    }
                },
                onSetHighlight = { color ->
                    highlight = color
                    scope.launch { repository.setHighlight(verse.bookId, verse.chapter, verse.verse, color) }
                },
                onSaveNote = { text ->
                    scope.launch {
                        repository.saveNote(existingNote, verse.bookId, verse.chapter, verse.verse, text)
                    }
                },
                onClose = { selectedVerse = null }
            )
        }
    }
}

/** Diálogo para nomear um novo marcador de página, ou remover os marcadores do capítulo atual. */
@Composable
private fun BookmarkDialog(
    isBookmarked: Boolean,
    existingNames: List<String>,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onRemoveAll: () -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isBookmarked) "Página marcada" else "Marcar esta página") },
        text = {
            Column {
                if (isBookmarked) {
                    Text(
                        "Marcadores nesta página: " + existingNames.joinToString(", "),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Você pode adicionar outro nome, ou remover todos os marcadores deste capítulo.",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                } else {
                    Text(
                        "Dê um nome a este marcador para lembrar por que você salvou esta página.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Ex.: Estudo de domingo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = { onSave(name.trim()) }
            ) { Text("Salvar marcador") }
        },
        dismissButton = {
            if (isBookmarked) {
                TextButton(onClick = onRemoveAll) { Text("Remover marcadores") }
            } else {
                TextButton(onClick = onDismiss) { Text("Cancelar") }
            }
        }
    )
}

/** Pequena fita no canto superior indicando que esta página tem um marcador salvo. */
@Composable
private fun BookmarkRibbon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(top = 4.dp, end = 12.dp)
            .size(32.dp)
            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Filled.Bookmark,
            contentDescription = "Página marcada",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun ChapterPage(
    repository: BibleRepository,
    ref: ChapterRef,
    fontScale: Float,
    scrollToVerse: Int?,
    onVerseClick: (Verse) -> Unit,
    onFontScaleChange: (Float) -> Unit
) {
    var verses by remember(ref) { mutableStateOf<List<Verse>>(emptyList()) }
    val highlights by remember(ref) { repository.observeHighlightsForChapter(ref.bookId, ref.chapter) }
        .collectAsState(initial = emptyList())
    val bookmarks by remember(ref) { repository.observeBookmarksForChapter(ref.bookId, ref.chapter) }
        .collectAsState(initial = emptyList())
    val listState = rememberLazyListState()
    val highlightColors = LocalHighlightColors.current

    // Fonte "ao vivo": muda instantaneamente durante o gesto de pinça, e só é
    // salva de verdade (via onFontScaleChange) quando o usuário solta os dedos.
    var liveFontScale by remember(fontScale) { mutableStateOf(fontScale) }

    LaunchedEffect(ref) {
        verses = repository.getChapter(ref.bookId, ref.chapter)
    }

    LaunchedEffect(verses, scrollToVerse) {
        if (scrollToVerse != null && verses.isNotEmpty()) {
            val idx = verses.indexOfFirst { it.verse == scrollToVerse }
            if (idx >= 0) listState.animateScrollToItem(idx)
        }
    }

    val highlightMap = remember(highlights) { highlights.associateBy { it.verse } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            // Zoom com dois dedos (pinça) para ajustar o tamanho da fonte, do
            // mesmo jeito que se usa para dar zoom em fotos. Só reage quando
            // há 2 dedos na tela, então não atrapalha o arrastar (swipe) de
            // um dedo só que troca de capítulo.
            .pointerInput(Unit) {
                awaitEachGesture {
                    var initialDistance = 0f
                    var initialScale = 1f
                    var isScaling = false
                    awaitFirstDown(requireUnconsumed = false)
                    do {
                        val event = awaitPointerEvent()
                        val pointers = event.changes.filter { it.pressed }
                        if (pointers.size >= 2) {
                            val distance = (pointers[0].position - pointers[1].position).getDistance()
                            if (initialDistance == 0f) {
                                initialDistance = distance
                                initialScale = liveFontScale
                            } else if (distance > 0f) {
                                isScaling = true
                                liveFontScale = (initialScale * (distance / initialDistance))
                                    .coerceIn(MIN_FONT_SCALE, MAX_FONT_SCALE)
                            }
                            pointers.forEach { it.consume() }
                        } else {
                            initialDistance = 0f
                        }
                    } while (event.changes.any { it.pressed })
                    if (isScaling) onFontScaleChange(liveFontScale)
                }
            }
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
        ) {
            items(verses, key = { it.id }) { verse ->
                val highlightColorName = highlightMap[verse.verse]?.color
                val bgColor: Color? = highlightColorName?.let { highlightColors[it] }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onVerseClick(verse) }
                        .then(
                            if (bgColor != null)
                                Modifier.background(bgColor, RoundedCornerShape(4.dp))
                            else Modifier
                        )
                        .padding(vertical = 4.dp, horizontal = 4.dp)
                ) {
                    Text(
                        text = "${verse.verse} ",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = verse.text,
                        style = readingTextStyle(liveFontScale)
                    )
                }
            }
        }

        if (bookmarks.isNotEmpty()) {
            BookmarkRibbon(modifier = Modifier.align(Alignment.TopEnd))
        }
    }
}
