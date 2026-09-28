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
import androidx.compose.material.icons.filled.StarBorder
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
import androidx.compose.ui.unit.dp
import com.bibliasagrada.app.data.model.Verse
import com.bibliasagrada.app.data.repository.BibleRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    repository: BibleRepository,
    onBack: () -> Unit,
    onOpenReader: (Int, Int, Int) -> Unit
) {
    val favorites by repository.observeFavorites().collectAsState(initial = emptyList())
    var verses by remember { mutableStateOf<List<Verse>>(emptyList()) }

    LaunchedEffect(favorites) {
        val list = mutableListOf<Verse>()
        for (fav in favorites) {
            repository.getVerse(fav.bookId, fav.chapter, fav.verse)?.let { list.add(it) }
        }
        verses = list
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favoritos") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar") }
                }
            )
        }
    ) { padding ->
        if (verses.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.StarBorder,
                message = "Você ainda não tem versículos favoritos.\nToque em um versículo durante a leitura para salvá-lo aqui.",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(verses, key = { it.id }) { verse ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenReader(verse.bookId, verse.chapter, verse.verse) }
                            .padding(vertical = 10.dp)
                    ) {
                        Text(verse.reference, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        Text(verse.text, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, modifier = Modifier.padding(bottom = 12.dp), tint = MaterialTheme.colorScheme.secondary)
            Text(message, textAlign = androidx.compose.ui.text.style.TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
