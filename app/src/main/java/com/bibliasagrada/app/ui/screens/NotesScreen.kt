package com.bibliasagrada.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bibliasagrada.app.data.repository.BibleRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    repository: BibleRepository,
    onBack: () -> Unit,
    onOpenReader: (Int, Int, Int) -> Unit
) {
    val notes by repository.observeAllNotes().collectAsState(initial = emptyList())
    var referenceOf by remember { mutableStateOf<Map<Long, String>>(emptyMap()) }

    LaunchedEffect(notes) {
        val map = mutableMapOf<Long, String>()
        notes.forEach { note ->
            repository.getVerse(note.bookId, note.chapter, note.verse)?.let { startVerse ->
                map[note.id] = if (note.verseEnd > note.verse) {
                    "${startVerse.bookName} ${note.chapter}:${note.verse}-${note.verseEnd}"
                } else {
                    startVerse.reference
                }
            }
        }
        referenceOf = map
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Minhas notas") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar") }
                }
            )
        }
    ) { padding ->
        if (notes.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.EditNote,
                message = "Você ainda não escreveu notas.\nToque em um versículo durante a leitura para anotar suas reflexões.",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(notes, key = { it.id }) { note ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenReader(note.bookId, note.chapter, note.verse) }
                            .padding(vertical = 10.dp)
                    ) {
                        Text(
                            referenceOf[note.id] ?: "",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(note.text, style = MaterialTheme.typography.bodyMedium)
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}
