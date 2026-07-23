package com.bibliasagrada.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.bibliasagrada.app.data.repository.BibleRepository
import com.bibliasagrada.app.data.repository.TranslationDownloadManager
import com.bibliasagrada.app.data.repository.TranslationsCatalog
import com.bibliasagrada.app.util.TranslationImporter
import kotlinx.coroutines.launch

/** Igual ao GetContent padrão, mas sugere abrir já perto da pasta Downloads
 *  (funciona na maioria dos celulares com o seletor de arquivos padrão do
 *  Android; alguns fabricantes usam um seletor próprio que pode ignorar essa dica). */
private class GetContentNearDownloads : ActivityResultContracts.GetContent() {
    override fun createIntent(context: android.content.Context, input: String): Intent {
        val intent = super.createIntent(context, input)
        val downloadsUri = Uri.parse("content://com.android.externalstorage.documents/document/primary:Download")
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, downloadsUri)
        return intent
    }
}

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

    var isImporting by remember { mutableStateOf<String?>(null) } // fileName sendo importado agora
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var refreshTick by remember { mutableStateOf(0) } // força recomposição após importar/remover

    // Alvo da importação: preenchido quando o usuário toca "Importar arquivo"
    // num card específico, para sabermos para qual tradução salvar o resultado.
    var importTargetFileName by remember { mutableStateOf<String?>(null) }

    val importLauncher = rememberLauncherForActivityResult(
        GetContentNearDownloads()
    ) { uri: Uri? ->
        val targetFileName = importTargetFileName
        if (uri != null && targetFileName != null) {
            scope.launch {
                isImporting = targetFileName
                errors = errors - targetFileName
                val result = TranslationImporter.importFromUri(context, uri, targetFileName)
                isImporting = null
                when (result) {
                    is TranslationImporter.ImportResult.Success -> refreshTick++
                    is TranslationImporter.ImportResult.Failure ->
                        errors = errors + (targetFileName to result.message)
                }
            }
        }
    }

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
                    "Escolha qual tradução usar para ler a Bíblia. A Bíblia Livre já vem pronta e " +
                        "funciona 100% offline.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.padding(top = 12.dp))
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Sobre as traduções abaixo (além da Bíblia Livre)",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Este app não fornece nem hospeda essas traduções — os textos bíblicos " +
                                "pertencem às suas respectivas editoras/sociedades bíblicas. Você pode " +
                                "baixar o arquivo por conta própria numa fonte externa e depois importar " +
                                "aqui. A responsabilidade pelo conteúdo e pelos direitos de uso do arquivo " +
                                "escolhido é de quem faz o download.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                        TextButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(TranslationsCatalog.EXTERNAL_SOURCE_URL))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                            Text("Fonte: thiagobodruk/biblia (ACF, NVI...)")
                        }
                        TextButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(TranslationsCatalog.EXTERNAL_SOURCE_URL_GETBIBLE))
                                context.startActivity(intent)
                            }
                        ) {
                            Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                            Text("Fonte: getBible (Almeida 1911)")
                        }
                        TextButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(TranslationsCatalog.EXTERNAL_SOURCE_URL_OPENBIBLE))
                                context.startActivity(intent)
                            }
                        ) {
                            Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                            Text("Fonte: OpenBible")
                        }
                    }
                }
            }

            items(TranslationsCatalog.all, key = { it.id }) { translation ->
                refreshTick // leitura para participar da recomposição
                val isDownloaded = translation.builtIn ||
                    TranslationDownloadManager.isDownloaded(context, translation)
                val isActive = active.id == translation.id
                val importingThis = isImporting == translation.fileName
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
                                importingThis -> {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(modifier = Modifier.padding(end = 12.dp))
                                        Text("Importando…", style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                                !isDownloaded -> {
                                    Button(
                                        onClick = {
                                            importTargetFileName = translation.fileName
                                            importLauncher.launch("*/*")
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Filled.FileUpload, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                                        Text("Importar arquivo")
                                    }
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
                                                refreshTick++
                                            },
                                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                        ) {
                                            Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                                            Text("Remover")
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
