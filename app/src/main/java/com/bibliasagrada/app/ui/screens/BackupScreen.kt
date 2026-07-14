package com.bibliasagrada.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bibliasagrada.app.data.repository.BibleRepository
import com.bibliasagrada.app.util.BackupManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    repository: BibleRepository,
    onBack: () -> Unit,
    onRestored: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isWorking by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var pendingRestoreUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            isWorking = true
            message = null
            try {
                repository.checkpointUserDatabaseForBackup()
                BackupManager.export(context, uri)
                message = "Backup salvo com sucesso!"
            } catch (e: Exception) {
                message = "Não foi possível salvar o backup: ${e.message}"
            } finally {
                isWorking = false
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) pendingRestoreUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup e restauração") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar") }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxWidth().padding(padding).padding(20.dp)) {
            Text(
                "Guarde uma cópia dos seus favoritos, destaques, notas, marcadores e histórico de leitura. " +
                    "Ao tocar em \"Fazer backup\", escolha onde salvar — se você tiver o Google Drive instalado " +
                    "e conectado no celular, ele aparece como uma das opções de destino automaticamente.",
                style = MaterialTheme.typography.bodyMedium
            )

            Button(
                onClick = {
                    val stamp = SimpleDateFormat("yyyy-MM-dd_HHmm", Locale.getDefault()).format(Date())
                    exportLauncher.launch("biblia_sagrada_backup_$stamp.zip")
                },
                enabled = !isWorking,
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
            ) {
                Icon(Icons.Filled.CloudUpload, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("Fazer backup")
            }

            Text(
                "Restaurar um backup substitui TUDO que está salvo no app agora pelo conteúdo do arquivo escolhido.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 28.dp)
            )

            OutlinedButton(
                onClick = { importLauncher.launch(arrayOf("*/*")) },
                enabled = !isWorking,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            ) {
                Icon(Icons.Filled.CloudDownload, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("Restaurar backup")
            }

            if (isWorking) {
                Row(modifier = Modifier.padding(top = 20.dp)) {
                    CircularProgressIndicator(modifier = Modifier.padding(end = 12.dp))
                    Text("Trabalhando…", style = MaterialTheme.typography.bodyMedium)
                }
            }

            message?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 20.dp)
                )
            }
        }
    }

    pendingRestoreUri?.let { uri ->
        AlertDialog(
            onDismissRequest = { if (!isWorking) pendingRestoreUri = null },
            title = { Text("Restaurar este backup?") },
            text = {
                Text(
                    "Isso vai substituir todos os favoritos, notas, destaques, marcadores e histórico " +
                        "salvos atualmente pelo conteúdo desse arquivo. Essa ação não pode ser desfeita."
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !isWorking,
                    onClick = {
                        scope.launch {
                            isWorking = true
                            message = null
                            try {
                                repository.closeUserDatabaseForRestore()
                                BackupManager.import(context, uri)
                                repository.reopenUserDatabaseAfterRestore()
                                pendingRestoreUri = null
                                isWorking = false
                                onRestored()
                            } catch (e: Exception) {
                                repository.reopenUserDatabaseAfterRestore()
                                message = "Não foi possível restaurar: ${e.message}"
                                pendingRestoreUri = null
                                isWorking = false
                            }
                        }
                    }
                ) { Text("Restaurar") }
            },
            dismissButton = {
                TextButton(enabled = !isWorking, onClick = { pendingRestoreUri = null }) { Text("Cancelar") }
            }
        )
    }
}
