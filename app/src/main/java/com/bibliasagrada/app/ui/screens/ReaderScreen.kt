package com.bibliasagrada.app.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Remove
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import com.bibliasagrada.app.R
import com.bibliasagrada.app.data.model.ChapterRef
import com.bibliasagrada.app.data.model.Verse
import com.bibliasagrada.app.data.repository.BibleRepository
import com.bibliasagrada.app.data.repository.ColorPalette
import com.bibliasagrada.app.data.repository.ThemeMode
import com.bibliasagrada.app.data.room.NoteEntity
import com.bibliasagrada.app.data.room.TagEntity
import com.bibliasagrada.app.ui.components.VerseActionSheet
import com.bibliasagrada.app.ui.theme.LocalColorPalette
import com.bibliasagrada.app.ui.theme.LocalHighlightColors
import com.bibliasagrada.app.ui.theme.readingTextStyle
import com.bibliasagrada.app.util.QuoteImageGenerator
import kotlinx.coroutines.launch

private const val MIN_FONT_SCALE = 0.7f
private const val MAX_FONT_SCALE = 1.8f

private fun shareVersesAsImage(context: android.content.Context, verses: List<Verse>, theme: QuoteImageGenerator.Theme) {
    if (verses.isEmpty()) return
    val uri = QuoteImageGenerator.generateAndShare(context, verses, theme)
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(sendIntent, null))
}

/** Lê as cores do tema atual (dourado/preto e branco/rosé, claro/escuro) para
 *  a imagem de citação sair com a mesma cara do app no momento em que a
 *  pessoa compartilha. */
@Composable
private fun currentQuoteImageTheme(): QuoteImageGenerator.Theme {
    val colorScheme = MaterialTheme.colorScheme
    return QuoteImageGenerator.Theme(
        background = colorScheme.background.toArgb(),
        accent = colorScheme.primary.toArgb(),
        text = colorScheme.onBackground.toArgb(),
        footer = colorScheme.secondary.toArgb()
    )
}

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
    val quoteImageTheme = currentQuoteImageTheme()

    // Pedido de permissão só é necessário no Android 9 ou anterior — a
    // partir do Android 10 (scoped storage), salvar na galeria não exige
    // permissão nenhuma.
    var pendingSaveVerses by remember { mutableStateOf<List<Verse>?>(null) }
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val verses = pendingSaveVerses
        pendingSaveVerses = null
        if (granted && verses != null) {
            val ok = QuoteImageGenerator.saveToGallery(context, verses, quoteImageTheme)
            Toast.makeText(
                context,
                if (ok) "Imagem salva na galeria" else "Não foi possível salvar a imagem",
                Toast.LENGTH_SHORT
            ).show()
        } else if (!granted) {
            Toast.makeText(context, "Permissão necessária para salvar na galeria", Toast.LENGTH_SHORT).show()
        }
    }

    fun saveVerseImage(verses: List<Verse>) {
        val needsPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        if (needsPermission) {
            pendingSaveVerses = verses
            storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            val ok = QuoteImageGenerator.saveToGallery(context, verses, quoteImageTheme)
            Toast.makeText(
                context,
                if (ok) "Imagem salva na galeria" else "Não foi possível salvar a imagem",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    var chapterRefs by remember { mutableStateOf<List<ChapterRef>>(emptyList()) }
    var startPage by remember { mutableStateOf<Int?>(null) }
    val fontScale by repository.prefs.fontScale.collectAsState(initial = 1.0f)
    val soundEnabled by repository.prefs.pageTurnSoundEnabled.collectAsState(initial = true)

    // Som de "página virando" — carregado uma vez e liberado ao sair da tela.
    val soundPool = remember {
        SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()
    }
    var pageTurnSoundId by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        pageTurnSoundId = soundPool.load(context, R.raw.page_turn, 1)
    }
    DisposableEffect(Unit) {
        onDispose { soundPool.release() }
    }

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
    var selectedVerses by remember { mutableStateOf<List<Verse>?>(null) }
    var showBookmarkDialog by remember { mutableStateOf(false) }
    var showQuickSettings by remember { mutableStateOf(false) }
    var previousPage by remember { mutableStateOf(startPageValue) }

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

    val colorPalette = LocalColorPalette.current

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
                        containerColor = if (colorPalette == ColorPalette.ROSA) {
                            Color.Transparent
                        } else {
                            MaterialTheme.colorScheme.background
                        }
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
                        IconButton(onClick = { showQuickSettings = true }) {
                            Text(
                                "A",
                                style = MaterialTheme.typography.titleMedium,
                                color = LocalContentColor.current
                            )
                        }
                    }
                )
            }
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
                    onVerseClick = { verse -> selectedVerses = listOf(verse) },
                    onFontScaleChange = { newScale ->
                        scope.launch { repository.prefs.setFontScale(newScale.coerceIn(MIN_FONT_SCALE, MAX_FONT_SCALE)) }
                    },
                    onShareVerses = { verses -> selectedVerses = verses }
                )
            }

            // Setinhas nas bordas: além de indicar visualmente que dá para
            // arrastar, também funcionam como botão, para quem preferir tocar
            // em vez de arrastar.
            if (pagerState.currentPage > 0) {
                Icon(
                    Icons.Filled.ChevronLeft,
                    contentDescription = "Capítulo anterior",
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 2.dp)
                        .size(32.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                        }
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
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        }
                )
            }
        }
    }

    // Registra histórico/progresso e toca o som de página virando sempre que
    // o capítulo exibido mudar de verdade (não na primeira composição).
    LaunchedEffect(pagerState.currentPage, chapterRefs) {
        val ref = chapterRefs.getOrNull(pagerState.currentPage) ?: return@LaunchedEffect
        repository.recordHistory(ref.bookId, ref.chapter)
        repository.saveReadingProgress(ref.bookId, ref.chapter, 1)
        if (pagerState.currentPage != previousPage) {
            if (soundEnabled && pageTurnSoundId != 0) {
                soundPool.play(pageTurnSoundId, 0.22f, 0.22f, 1, 0, 0.95f)
            }
            previousPage = pagerState.currentPage
        }
    }

    if (showQuickSettings) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(onDismissRequest = { showQuickSettings = false }, sheetState = sheetState) {
            QuickSettingsSheet(repository = repository, fontScale = fontScale)
        }
    }

    if (showBookmarkDialog && currentRef != null) {
        val allBookmarks by repository.observeBookmarks().collectAsState(initial = emptyList())
        val allBookmarkNames = remember(allBookmarks) {
            allBookmarks.map { it.name }.filter { it.isNotBlank() }.distinct().sorted()
        }
        BookmarkDialog(
            isBookmarked = isChapterBookmarked,
            existingNames = bookmarksInChapter.map { it.name },
            allBookmarkNames = allBookmarkNames,
            getColorForName = { n -> repository.getBookmarkColorForName(n) },
            onDismiss = { showBookmarkDialog = false },
            onSave = { newName, selectedExisting, newColor ->
                scope.launch {
                    // Cada rótulo já existente marcado mantém a própria cor lembrada
                    // (uma cor por rótulo, não uma cor só pra tudo que for salvo agora).
                    selectedExisting.forEach { existingName ->
                        val color = repository.getBookmarkColorForName(existingName)
                        repository.addBookmark(currentRef.bookId, currentRef.chapter, existingName, color)
                    }
                    if (newName.isNotBlank()) {
                        repository.addBookmark(currentRef.bookId, currentRef.chapter, newName, newColor)
                    } else if (selectedExisting.isEmpty()) {
                        // Nada selecionado e nenhum nome novo: marca a página sem nome.
                        repository.addBookmark(currentRef.bookId, currentRef.chapter, "", newColor)
                    }
                }
                showBookmarkDialog = false
            },
            onRemoveAll = {
                scope.launch { repository.removeBookmarksForChapter(currentRef.bookId, currentRef.chapter) }
                showBookmarkDialog = false
            }
        )
    }

    selectedVerses?.let { verses ->
        // skipPartiallyExpanded = true: a caixa já abre totalmente expandida,
        // mostrando de cara as cores de marcação e a caixa de notas — sem
        // precisar arrastar pra cima (quem não conhece o app não vai adivinhar
        // isso sozinho).
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val singleVerse = verses.singleOrNull()
        // Intervalo coberto pela seleção atual — para uma nota que abrange
        // vários versículos, guardamos o primeiro e o último selecionados.
        val verseStart = remember(verses) { verses.minOf { it.verse } }
        val verseEnd = remember(verses) { verses.maxOf { it.verse } }
        var isFavorite by remember(verses) { mutableStateOf(false) }
        var highlight by remember(verses) { mutableStateOf<String?>(null) }
        var noteText by remember(verses) { mutableStateOf("") }
        var existingNote by remember(verses) { mutableStateOf<NoteEntity?>(null) }
        var showTagDialog by remember(verses) { mutableStateOf(false) }
        // Tags mostradas na caixa: só quando é 1 versículo só (para vários
        // versículos, cada um pode ter um conjunto diferente de tags já
        // aplicadas, então a lista fica vazia — mas "Adicionar tag" continua
        // aplicando a tag escolhida/criada a todos os selecionados de uma vez).
        val tagsFlow = remember(verses) {
            singleVerse?.let { v -> repository.observeTagsForVerse(v.bookId, v.chapter, v.verse) }
                ?: kotlinx.coroutines.flow.flowOf(emptyList())
        }
        val currentTags by tagsFlow.collectAsState(initial = emptyList())

        LaunchedEffect(verses) {
            if (singleVerse != null) {
                isFavorite = repository.isFavorite(singleVerse.bookId, singleVerse.chapter, singleVerse.verse)
                highlight = repository.getHighlightForVerse(singleVerse.bookId, singleVerse.chapter, singleVerse.verse)
            }
            // A nota é buscada pelo primeiro versículo selecionado — encontra a
            // mesma nota tanto para 1 versículo quanto para um intervalo.
            existingNote = repository.getNoteForVerse(verses.first().bookId, verses.first().chapter, verseStart)
            noteText = existingNote?.text ?: ""
        }

        val combinedText = verses.joinToString(" ") { it.text.trim() }
        val combinedReference = if (singleVerse != null) singleVerse.reference else verses.joinToString(", ") { it.shortReference }

        ModalBottomSheet(onDismissRequest = { selectedVerses = null }, sheetState = sheetState) {
            VerseActionSheet(
                verses = verses,
                isFavorite = isFavorite,
                currentHighlight = highlight,
                currentNote = noteText,
                currentTags = currentTags,
                onCopy = {
                    clipboard.setText(AnnotatedString("$combinedText ($combinedReference)"))
                },
                onShare = {
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "\"$combinedText\" — $combinedReference")
                    }
                    context.startActivity(Intent.createChooser(sendIntent, null))
                },
                onShareImage = { shareVersesAsImage(context, verses, quoteImageTheme) },
                onSaveImage = { saveVerseImage(verses) },
                onToggleFavorite = {
                    singleVerse?.let { v ->
                        scope.launch {
                            repository.toggleFavorite(v.bookId, v.chapter, v.verse)
                            isFavorite = !isFavorite
                        }
                    }
                },
                onSetHighlight = { color ->
                    highlight = color
                    scope.launch {
                        verses.forEach { v -> repository.setHighlight(v.bookId, v.chapter, v.verse, color) }
                    }
                },
                onRemoveTag = { tag ->
                    singleVerse?.let { v ->
                        scope.launch { repository.removeVerseTag(tag.id, v.bookId, v.chapter, v.verse) }
                    }
                },
                onAddTagClick = { showTagDialog = true },
                onSaveNote = { text ->
                    scope.launch {
                        repository.saveNote(existingNote, verses.first().bookId, verses.first().chapter, verseStart, verseEnd, text)
                    }
                },
                onClose = { selectedVerses = null }
            )
        }

        if (showTagDialog) {
            val allTags by repository.observeAllTags().collectAsState(initial = emptyList())
            val alreadyAppliedNames = remember(currentTags) { currentTags.map { it.name } }
            TagDialog(
                allTags = allTags,
                alreadyAppliedNames = alreadyAppliedNames,
                onDismiss = { showTagDialog = false },
                onSave = { newName, selectedExistingIds, newColor ->
                    scope.launch {
                        selectedExistingIds.forEach { tagId -> repository.addTagToVerses(tagId, verses) }
                        if (newName.isNotBlank()) {
                            val tagId = repository.getOrCreateTag(newName, newColor)
                            repository.addTagToVerses(tagId, verses)
                        }
                    }
                    showTagDialog = false
                }
            )
        }
    }
}

/** Diálogo para marcar esta página com um ou mais rótulos: pode selecionar
 *  vários rótulos já existentes ao mesmo tempo (cada um mantém sua própria
 *  cor lembrada), e/ou criar um rótulo novo com uma cor à sua escolha. */
@Composable
private fun BookmarkDialog(
    isBookmarked: Boolean,
    existingNames: List<String>,
    allBookmarkNames: List<String>,
    getColorForName: suspend (String) -> String?,
    onDismiss: () -> Unit,
    onSave: (newName: String, selectedExisting: Set<String>, newColor: String?) -> Unit,
    onRemoveAll: () -> Unit
) {
    var newName by remember { mutableStateOf("") }
    var selectedExisting by remember { mutableStateOf(setOf<String>()) }
    var selectedColor by remember { mutableStateOf<String?>(null) }
    var lastAutoFilledName by remember { mutableStateOf("") }
    val highlightColors = LocalHighlightColors.current

    // Selecionáveis: rótulos já existentes, exceto os que já estão nesta página.
    val selectableNames = remember(allBookmarkNames, existingNames) {
        allBookmarkNames.filter { it !in existingNames }
    }

    // A cor escolhida aqui vale só para o rótulo NOVO (os já existentes mantêm
    // a própria cor lembrada). Se o texto digitado bater com um nome já usado
    // antes, repete a cor daquele nome — a menos que você escolha outra cor
    // manualmente depois disso.
    LaunchedEffect(newName) {
        if (newName.isNotBlank() && newName != lastAutoFilledName) {
            lastAutoFilledName = newName
            getColorForName(newName)?.let { rememberedColor -> selectedColor = rememberedColor }
        }
    }

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
                        "Você pode adicionar mais rótulos abaixo, ou remover todos os deste capítulo.",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // 1) Usar um ou mais rótulos já existentes
                if (selectableNames.isNotEmpty()) {
                    Text(
                        "1. Usar rótulos já existentes (pode escolher vários)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = if (isBookmarked) 16.dp else 4.dp, bottom = 6.dp)
                    )
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                    ) {
                        items(selectableNames) { existing ->
                            val isChosen = existing in selectedExisting
                            androidx.compose.material3.FilterChip(
                                selected = isChosen,
                                onClick = {
                                    selectedExisting = if (isChosen) {
                                        selectedExisting - existing
                                    } else {
                                        selectedExisting + existing
                                    }
                                },
                                label = { Text(existing) }
                            )
                        }
                    }
                }

                // 2) Criar um novo rótulo
                Text(
                    "2. Ou criar um novo rótulo",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
                )
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    placeholder = { Text("Ex.: Estudo bíblico, Oração...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 3) Cor do rótulo novo, se quiser
                Text(
                    "3. Cor do rótulo novo (se quiser)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    highlightColors.forEach { (colorName, color) ->
                        val isSelected = selectedColor == colorName
                        Box(
                            modifier = Modifier
                                .padding(end = 10.dp)
                                .size(if (isSelected) 32.dp else 26.dp)
                                .background(color, androidx.compose.foundation.shape.CircleShape)
                                .clickable { selectedColor = if (isSelected) null else colorName }
                        )
                    }
                }
                Text(
                    "Rótulos já existentes mantêm a própria cor automaticamente. Sem cor " +
                        "escolhida, ou com rótulos de cores diferentes no mesmo capítulo, a fita " +
                        "aparece na cor do tema do app.",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(newName.trim(), selectedExisting, selectedColor) }
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

/** Diálogo para aplicar tags de estudo (ex.: "Salvação", "Fé") ao(s)
 *  versículo(s) selecionado(s): pode escolher várias tags já existentes de
 *  uma vez, e/ou criar uma tag nova com uma cor à sua escolha (mesma paleta
 *  usada nos marcadores e destaques). */
@Composable
private fun TagDialog(
    allTags: List<TagEntity>,
    alreadyAppliedNames: List<String>,
    onDismiss: () -> Unit,
    onSave: (newName: String, selectedExistingIds: Set<Long>, newColor: String) -> Unit
) {
    var newName by remember { mutableStateOf("") }
    var selectedExisting by remember { mutableStateOf(setOf<Long>()) }
    var selectedColor by remember { mutableStateOf<String?>(null) }
    val highlightColors = LocalHighlightColors.current
    val defaultColor = remember(highlightColors) { highlightColors.keys.first() }

    // Selecionáveis: tags já existentes, exceto as que já estão neste versículo.
    val selectableTags = remember(allTags, alreadyAppliedNames) {
        allTags.filter { it.name !in alreadyAppliedNames }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tags de estudo") },
        text = {
            Column {
                // 1) Usar uma ou mais tags já existentes
                if (selectableTags.isNotEmpty()) {
                    Text(
                        "1. Usar tags já existentes (pode escolher várias)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                    ) {
                        items(selectableTags) { tag ->
                            val isChosen = tag.id in selectedExisting
                            androidx.compose.material3.FilterChip(
                                selected = isChosen,
                                onClick = {
                                    selectedExisting = if (isChosen) {
                                        selectedExisting - tag.id
                                    } else {
                                        selectedExisting + tag.id
                                    }
                                },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(
                                                highlightColors[tag.color] ?: MaterialTheme.colorScheme.primary,
                                                androidx.compose.foundation.shape.CircleShape
                                            )
                                    )
                                },
                                label = { Text(tag.name) }
                            )
                        }
                    }
                }

                // 2) Criar uma tag nova
                Text(
                    "2. Ou criar uma tag nova",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = if (selectableTags.isNotEmpty()) 16.dp else 4.dp, bottom = 6.dp)
                )
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    placeholder = { Text("Ex.: Salvação, Fé, Oração...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 3) Cor da tag nova
                Text(
                    "3. Cor da tag nova",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    highlightColors.forEach { (colorName, color) ->
                        val isSelected = (selectedColor ?: defaultColor) == colorName
                        Box(
                            modifier = Modifier
                                .padding(end = 10.dp)
                                .size(if (isSelected) 32.dp else 26.dp)
                                .background(color, androidx.compose.foundation.shape.CircleShape)
                                .clickable { selectedColor = colorName }
                        )
                    }
                }
                Text(
                    "Cada tag guarda sua própria cor, para reconhecer o tema de relance " +
                        "ao rolar a página. Tags já existentes mantêm a cor que já têm.",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = newName.isNotBlank() || selectedExisting.isNotEmpty(),
                onClick = { onSave(newName.trim(), selectedExisting, selectedColor ?: defaultColor) }
            ) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}


/** Pequena fita no canto superior indicando que esta página tem um marcador salvo. */
@Composable
private fun BookmarkRibbon(tint: Color, modifier: Modifier = Modifier) {
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
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
    }
}

/** Barra flutuante que aparece quando um ou mais versículos estão selecionados. */
@Composable
private fun SelectionBar(
    count: Int,
    onCancel: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCancel) {
                Icon(Icons.Filled.Close, contentDescription = "Cancelar seleção")
            }
            Text(
                if (count == 1) "1 versículo selecionado" else "$count versículos selecionados",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            Button(onClick = onShare, modifier = Modifier.padding(start = 8.dp)) {
                Icon(Icons.Filled.MoreVert, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                Text("Ações")
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChapterPage(
    repository: BibleRepository,
    ref: ChapterRef,
    fontScale: Float,
    scrollToVerse: Int?,
    onVerseClick: (Verse) -> Unit,
    onFontScaleChange: (Float) -> Unit,
    onShareVerses: (List<Verse>) -> Unit
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

    // Seleção de vários versículos (toque e segure para começar), para
    // compartilhar um trecho maior que um único versículo como imagem.
    var selectedNumbers by remember(ref) { mutableStateOf(setOf<Int>()) }

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
                val isSelected = selectedNumbers.contains(verse.verse)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {
                                if (selectedNumbers.isNotEmpty()) {
                                    selectedNumbers = if (isSelected) {
                                        selectedNumbers - verse.verse
                                    } else if (selectedNumbers.size < QuoteImageGenerator.MAX_VERSES_PER_IMAGE) {
                                        selectedNumbers + verse.verse
                                    } else {
                                        selectedNumbers
                                    }
                                } else {
                                    onVerseClick(verse)
                                }
                            },
                            onLongClick = {
                                if (selectedNumbers.size < QuoteImageGenerator.MAX_VERSES_PER_IMAGE) {
                                    selectedNumbers = selectedNumbers + verse.verse
                                }
                            }
                        )
                        .then(
                            if (isSelected) Modifier.background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
                                RoundedCornerShape(4.dp)
                            ) else Modifier
                        )
                        .padding(vertical = 4.dp, horizontal = 4.dp)
                ) {
                    Text(
                        text = "${verse.verse} ",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    // getPathForRange (usado na versão anterior) é o mesmo mecanismo da
                    // SELEÇÃO de texto, que sempre estende o retângulo até a margem da
                    // linha quando o texto continua na linha de baixo — não é o que
                    // queremos aqui. Em vez disso, calculamos manualmente um retângulo
                    // por linha usando os limites reais dos caracteres (getBoundingBox):
                    // do início ao último caractere visível de cada linha (sem contar
                    // espaço em branco final), com altura igual à caixa do próprio
                    // caractere — não à altura cheia da linha (que inclui o
                    // entrelinhamento/leading, e por isso "grudava" nas linhas vizinhas).
                    var textLayoutResult by remember(verse) { mutableStateOf<TextLayoutResult?>(null) }
                    Text(
                        text = verse.text,
                        style = readingTextStyle(liveFontScale),
                        onTextLayout = { textLayoutResult = it },
                        modifier = Modifier.drawBehind {
                            if (bgColor != null && !isSelected) {
                                textLayoutResult?.let { layout ->
                                    for (lineIndex in 0 until layout.lineCount) {
                                        val lineStart = layout.getLineStart(lineIndex)
                                        val lineEnd = layout.getLineEnd(lineIndex, visibleEnd = true)
                                        if (lineEnd <= lineStart) continue // linha em branco
                                        val firstBox = layout.getBoundingBox(lineStart)
                                        val lastBox = layout.getBoundingBox(lineEnd - 1)
                                        val top = minOf(firstBox.top, lastBox.top)
                                        val bottom = maxOf(firstBox.bottom, lastBox.bottom)
                                        drawRect(
                                            color = bgColor,
                                            topLeft = Offset(firstBox.left, top),
                                            size = Size(lastBox.right - firstBox.left, bottom - top)
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }

        if (bookmarks.isNotEmpty() && selectedNumbers.isEmpty()) {
            BookmarkRibbon(
                tint = if (bookmarks.map { it.color }.distinct().size == 1) {
                    bookmarks.first().color?.let { highlightColors[it] } ?: MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }

        if (selectedNumbers.isNotEmpty()) {
            SelectionBar(
                count = selectedNumbers.size,
                onCancel = { selectedNumbers = emptySet() },
                onShare = {
                    val chosen = verses.filter { selectedNumbers.contains(it.verse) }
                        .sortedBy { it.verse }
                    onShareVerses(chosen)
                    selectedNumbers = emptySet()
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

/** Painel rápido de acesso: tema, tamanho de fonte e som — sem precisar sair da leitura. */
@Composable
private fun QuickSettingsSheet(repository: BibleRepository, fontScale: Float) {
    val scope = rememberCoroutineScope()
    val themeMode by repository.prefs.themeMode.collectAsState(initial = ThemeMode.SISTEMA)
    val colorPalette by repository.prefs.colorPalette.collectAsState(initial = ColorPalette.DOURADO)
    val soundEnabled by repository.prefs.pageTurnSoundEnabled.collectAsState(initial = true)

    Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
        Text("Tamanho da fonte", style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
            Text("A", style = MaterialTheme.typography.labelMedium)
            Slider(
                value = fontScale,
                onValueChange = { scope.launch { repository.prefs.setFontScale(it) } },
                valueRange = MIN_FONT_SCALE..MAX_FONT_SCALE,
                modifier = Modifier.weight(1f).padding(horizontal = 10.dp)
            )
            Text("A", style = MaterialTheme.typography.titleLarge)
        }

        Text("Modo", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 20.dp))
        QuickThemeOption("Claro", ThemeMode.CLARO, themeMode) { scope.launch { repository.prefs.setThemeMode(it) } }
        QuickThemeOption("Escuro", ThemeMode.ESCURO, themeMode) { scope.launch { repository.prefs.setThemeMode(it) } }
        QuickThemeOption("Seguir o sistema", ThemeMode.SISTEMA, themeMode) { scope.launch { repository.prefs.setThemeMode(it) } }

        Text("Cor do tema", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 20.dp))
        QuickPaletteOption("Dourado", ColorPalette.DOURADO, colorPalette) { scope.launch { repository.prefs.setColorPalette(it) } }
        QuickPaletteOption("Preto e branco", ColorPalette.PRETO_BRANCO, colorPalette) { scope.launch { repository.prefs.setColorPalette(it) } }
        QuickPaletteOption("Bíblia para mulheres (rosé)", ColorPalette.ROSA, colorPalette) { scope.launch { repository.prefs.setColorPalette(it) } }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Som ao virar a página", style = MaterialTheme.typography.titleMedium)
            Switch(
                checked = soundEnabled,
                onCheckedChange = { scope.launch { repository.prefs.setPageTurnSoundEnabled(it) } }
            )
        }
    }
}

@Composable
private fun QuickThemeOption(label: String, mode: ThemeMode, current: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = current == mode, onClick = { onSelect(mode) })
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = current == mode, onClick = { onSelect(mode) })
        Text(label, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
private fun QuickPaletteOption(label: String, palette: ColorPalette, current: ColorPalette, onSelect: (ColorPalette) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = current == palette, onClick = { onSelect(palette) })
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = current == palette, onClick = { onSelect(palette) })
        Text(label, modifier = Modifier.padding(start = 8.dp))
    }
}
