package com.bibliasagrada.app.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.bibliasagrada.app.data.model.Verse
import com.bibliasagrada.app.data.repository.BibleRepository
import com.bibliasagrada.app.data.repository.PromiseRef
import com.bibliasagrada.app.data.repository.PromisesCatalog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromisesScreen(
    repository: BibleRepository,
    onBack: () -> Unit,
    onOpenReader: (Int, Int, Int) -> Unit
) {
    val context = LocalContext.current
    var opened by remember { mutableStateOf(false) }
    var current by remember { mutableStateOf<PromiseRef?>(null) }
    var verse by remember { mutableStateOf<Verse?>(null) }

    LaunchedEffect(current) {
        val ref = current
        if (ref != null) verse = repository.getVerse(ref.bookId, ref.chapter, ref.verse)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Caixinha de Promessas") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar") }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = opened,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "promise-box"
            ) { isOpen ->
                if (!isOpen) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clickable {
                                    opened = true
                                    if (current == null) {
                                        current = PromisesCatalog.random()
                                    }
                                },
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.height(48.dp)
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "Toque para receber uma promessa",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Um versículo de conforto e esperança, escolhido para você agora.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                } else {
                    val v = verse
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                if (v != null) {
                                    Text(
                                        "\u201C${v.text}\u201D",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontStyle = FontStyle.Italic
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    Text(
                                        v.reference,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Text("Carregando…", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(onClick = {
                                v?.let {
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, "\"${it.text}\" — ${it.reference}")
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, null))
                                }
                            }) {
                                Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                                Text("Compartilhar")
                            }
                            OutlinedButton(onClick = {
                                v?.let { onOpenReader(it.bookId, it.chapter, it.verse) }
                            }) {
                                Icon(Icons.Filled.MenuBook, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                                Text("Ler no contexto")
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = {
                            current = PromisesCatalog.random(excluding = current)
                        }) {
                            Text("Nova promessa")
                        }
                    }
                }
            }
        }
    }
}
