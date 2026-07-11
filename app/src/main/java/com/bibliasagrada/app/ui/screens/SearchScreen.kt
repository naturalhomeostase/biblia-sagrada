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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import com.bibliasagrada.app.data.model.SearchResult
import com.bibliasagrada.app.data.model.Verse
import com.bibliasagrada.app.data.repository.BibleRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    repository: BibleRepository,
    onBack: () -> Unit,
    onOpenReader: (Int, Int, Int) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var testamentFilter by remember { mutableStateOf<String?>(null) }
    var directMatch by remember { mutableStateOf<Verse?>(null) }
    var results by remember { mutableStateOf<List<SearchResult>>(emptyList()) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(query, testamentFilter) {
        if (query.trim().length < 2) {
            directMatch = null
            results = emptyList()
            return@LaunchedEffect
        }
        directMatch = repository.searchByReference(query)
        results = repository.searchWords(query, testamentFilter)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Buscar palavra ou referência...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = testamentFilter == null,
                    onClick = { testamentFilter = null },
                    label = { Text("Toda a Bíblia") }
                )
                FilterChip(
                    selected = testamentFilter == "AT",
                    onClick = { testamentFilter = "AT" },
                    label = { Text("Antigo Testamento") }
                )
                FilterChip(
                    selected = testamentFilter == "NT",
                    onClick = { testamentFilter = "NT" },
                    label = { Text("Novo Testamento") }
                )
            }

            LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                directMatch?.let { verse ->
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .clickable { onOpenReader(verse.bookId, verse.chapter, verse.verse) }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row {
                                    Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Text(
                                        "  Ir para ${verse.reference}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(verse.text, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
                items(results) { result ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onOpenReader(result.verse.bookId, result.verse.chapter, result.verse.verse)
                            }
                            .padding(vertical = 10.dp)
                    ) {
                        Text(
                            result.verse.reference,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(result.snippet, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
