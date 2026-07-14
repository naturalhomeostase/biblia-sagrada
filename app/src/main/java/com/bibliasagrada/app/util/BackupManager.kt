package com.bibliasagrada.app.util

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Backup e restauração dos dados do usuário (favoritos, notas, destaques,
 * marcadores e histórico) usando o seletor de arquivos do próprio Android
 * (Storage Access Framework) em vez de uma integração direta com a API do
 * Google Drive.
 *
 * Por quê esse caminho, e não a API do Google Drive diretamente?
 * Integrar a API do Google Drive de verdade exigiria criar um projeto no
 * Google Cloud Console, configurar OAuth, registrar a impressão digital
 * (SHA-1) da chave de assinatura, e nesse caso também passar pelo processo
 * de verificação do Google — trabalho extra sem necessidade. Usando o
 * seletor de arquivos do Android, o app do Google Drive (se instalado e
 * logado no celular) já aparece como opção de salvar/abrir automaticamente,
 * com o mesmo resultado prático para o usuário, sem nenhuma configuração
 * externa e sem o app pedir permissão de acesso à conta Google.
 *
 * O backup é salvo como um .zip contendo o banco de dados (e os arquivos
 * auxiliares -wal/-shm do SQLite, se existirem) — isso evita qualquer risco
 * de perder as últimas gravações, mesmo que o checkpoint do WAL não tenha
 * limpado tudo por algum motivo.
 */
object BackupManager {

    private val DB_FILE_NAMES = listOf("user_data.db", "user_data.db-wal", "user_data.db-shm")

    /** Empacota o banco de dados do usuário (e arquivos auxiliares) num .zip no destino escolhido. */
    suspend fun export(context: Context, destination: Uri) = withContext(Dispatchers.IO) {
        val dbDir = context.getDatabasePath("user_data.db").parentFile
        context.contentResolver.openOutputStream(destination)?.use { out ->
            ZipOutputStream(out).use { zip ->
                for (name in DB_FILE_NAMES) {
                    val file = File(dbDir, name)
                    if (!file.exists()) continue
                    zip.putNextEntry(ZipEntry(name))
                    FileInputStream(file).use { input -> input.copyTo(zip) }
                    zip.closeEntry()
                }
            }
        } ?: throw IllegalStateException("Não foi possível abrir o destino do backup.")
    }

    /**
     * Restaura o banco de dados do usuário a partir de um .zip gerado por [export].
     * Extrai primeiro para arquivos temporários e só troca os arquivos de verdade
     * no final — assim, se a restauração falhar no meio do caminho por qualquer
     * motivo (arquivo corrompido, etc.), os dados atuais do usuário continuam
     * intactos em vez de serem apagados antes da hora.
     *
     * IMPORTANTE: o banco precisa estar fechado (ver [com.bibliasagrada.app.data.repository.BibleRepository.closeUserDatabaseForRestore])
     * antes de chamar esta função, e reaberto (`reopenUserDatabaseAfterRestore`) depois.
     */
    suspend fun import(context: Context, source: Uri) = withContext(Dispatchers.IO) {
        val dbFile = context.getDatabasePath("user_data.db")
        val dbDir = dbFile.parentFile ?: throw IllegalStateException("Pasta do banco de dados não encontrada.")

        val opened = context.contentResolver.openInputStream(source)
            ?: throw IllegalStateException("Não foi possível abrir o arquivo de backup escolhido.")

        val tempFiles = mutableListOf<File>()
        var restoredMainFile = false
        try {
            opened.use { input ->
                ZipInputStream(input).use { zip ->
                    var entry = zip.nextEntry
                    while (entry != null) {
                        if (DB_FILE_NAMES.contains(entry.name)) {
                            val temp = File(dbDir, "${entry.name}.restoring")
                            temp.outputStream().use { out -> zip.copyTo(out) }
                            tempFiles.add(temp)
                            if (entry.name == "user_data.db") restoredMainFile = true
                        }
                        zip.closeEntry()
                        entry = zip.nextEntry
                    }
                }
            }

            if (!restoredMainFile) {
                throw IllegalStateException("Esse arquivo não parece ser um backup válido do app.")
            }

            // Só agora, com o backup já validado e extraído com sucesso, troca os
            // arquivos de verdade — o momento mais curto possível sem dados.
            for (name in DB_FILE_NAMES) File(dbDir, name).delete()
            for (temp in tempFiles) {
                val finalName = temp.name.removeSuffix(".restoring")
                temp.renameTo(File(dbDir, finalName))
            }
        } finally {
            // Limpa qualquer temporário que tenha sobrado (ex.: se algo falhou no meio).
            tempFiles.forEach { if (it.exists()) it.delete() }
        }
    }
}

