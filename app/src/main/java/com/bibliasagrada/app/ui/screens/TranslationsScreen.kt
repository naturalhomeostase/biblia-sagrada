package com.bibliasagrada.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bibliasagrada.app.data.model.Translation
import com.bibliasagrada.app.data.repository.BibleRepository
import com.bibliasagrada.app.data.repository.DownloadResult
import com.bibliasagrada.app.data.repository.TranslationDownloadManager
import com.bibliasagrada.app.data.repository.TranslationsCatalog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslationsScreen(
    repository: BibleRepository,
    onBack: () -> Unit,
    onTranslationSwitched: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val active by repository.activeTranslation.collectAsState()

    // progress[fileName] = 0f..1f enquanto baixa; null quando não está baixando
    var progress by remember { mutableStateOf<Map<String, Float>>(emptyMap()) }
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var downloadedTick by remember { mutableStateOf(0) } // força recomposição após baixar/remover

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Traduções") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar") }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Text(
                    "Escolha qual tradução usar para ler a Bíblia. A Bíblia Livre já vem " +
                        "pronta e funciona 100% offline; as demais precisam ser baixadas uma vez.",
                    style = MaterialTheme.typography.bodyMedium
                )
                androidx.compose.foundation.layout.Spacer(Modifier.padding(top = 12.dp))
            }
            items(TranslationsCatalog.all, key = { it.id }) { translation ->
                downloadedTick // leitura para participar da recomposição
                val isDownloaded = translation.builtIn ||
                    TranslationDownloadManager.isDownloaded(context, translation)
                val isActive = active.id == translation.id
                val currentProgress = progress[translation.fileName]
                val error = errors[translation.fileName]

                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(translation.displayName, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    if (isActive) "Tradução ativa" else translation.subtitle,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            if (isActive) {
                                Icon(
                                    Icons.Filled.CheckCircle,
                                    contentDescription = "Ativa",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        if (currentProgress != null) {
                            androidx.compose.foundation.layout.Spacer(Modifier.padding(top = 10.dp))
                            LinearProgressIndicator(
                                progress = currentProgress,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (error != null) {
                            Text(
                                error,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                            when {
                                !isDownloaded && currentProgress == null -> {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        Button(onClick = {
                                            scope.launch {
                                                progress = progress + (translation.fileName to 0f)
                                                errors = errors - translation.fileName
                                                val result = TranslationDownloadManager.download(
                                                    context,
                                                    translation
                                                ) { p ->
                                                    progress = progress + (translation.fileName to p)
                                                }
                                                progress = progress - translation.fileName
                                                when (result) {
                                                    is DownloadResult.Success -> downloadedTick++
                                                    is DownloadResult.Failure ->
                                                        errors = errors + (translation.fileName to result.message)
                                                }
                                            }
                                        }) {
                                            Icon(Icons.Filled.CloudDownload, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                                            Text("Baixar")
                                        }
                                    }
                                }
                                currentProgress != null -> {
                                    Text(
                                        "Baixando… ${(currentProgress * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                isDownloaded && !isActive -> {
                                    // Botões empilhados (em vez de lado a lado) para nunca
                                    // estourar a largura em telas estreitas.
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                repository.switchTranslation(translation)
                                                onTranslationSwitched()
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Usar esta tradução")
                                    }
                                    if (!translation.builtIn) {
                                        OutlinedButton(
                                            onClick = {
                                                TranslationDownloadManager.deleteDownload(context, translation)
                                                downloadedTick++
                                            },
                                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                        ) {
                                            Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                                            Text("Remover download")
                                        }
                                    }
                                }
                                else -> Unit
                            }
                        }
                    }
                }
            }
        }
    }
}
