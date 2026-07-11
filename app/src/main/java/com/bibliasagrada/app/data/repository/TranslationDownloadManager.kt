package com.bibliasagrada.app.data.repository

import android.content.Context
import com.bibliasagrada.app.data.model.Translation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/** Resultado de uma tentativa de download de tradução. */
sealed class DownloadResult {
    data object Success : DownloadResult()
    data class Failure(val message: String) : DownloadResult()
}

object TranslationDownloadManager {

    fun isDownloaded(context: Context, translation: Translation): Boolean {
        if (translation.builtIn) return true
        return context.getDatabasePath(translation.fileName).exists()
    }

    /**
     * Baixa o arquivo .db da tradução informada, reportando progresso de 0f a 1f
     * via [onProgress] (chamado na mesma thread de IO — o chamador deve usar
     * [kotlinx.coroutines.flow.flowOn] ou trocar de thread se for atualizar UI).
     */
    suspend fun download(
        context: Context,
        translation: Translation,
        onProgress: (Float) -> Unit
    ): DownloadResult = withContext(Dispatchers.IO) {
        val url = translation.downloadUrl
        if (url.isNullOrBlank()) {
            return@withContext DownloadResult.Failure(
                "URL de download não configurada para ${translation.displayName}. " +
                    "Veja as instruções em TranslationsCatalog.kt."
            )
        }

        var connection: HttpURLConnection? = null
        try {
            connection = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 15_000
                readTimeout = 15_000
                connect()
            }

            if (connection.responseCode !in 200..299) {
                return@withContext DownloadResult.Failure(
                    "Falha ao baixar (HTTP ${connection.responseCode})."
                )
            }

            val totalBytes = connection.contentLength
            val destFile = context.getDatabasePath(translation.fileName)
            destFile.parentFile?.mkdirs()
            val tempFile = File(destFile.parentFile, "${translation.fileName}.part")

            connection.inputStream.use { input ->
                FileOutputStream(tempFile).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var bytesRead: Int
                    var totalRead = 0L
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalRead += bytesRead
                        if (totalBytes > 0) {
                            onProgress((totalRead.toFloat() / totalBytes).coerceIn(0f, 1f))
                        }
                    }
                }
            }

            if (destFile.exists()) destFile.delete()
            if (!tempFile.renameTo(destFile)) {
                return@withContext DownloadResult.Failure("Não foi possível salvar o arquivo baixado.")
            }

            onProgress(1f)
            DownloadResult.Success
        } catch (e: Exception) {
            DownloadResult.Failure(e.message ?: "Erro desconhecido ao baixar.")
        } finally {
            connection?.disconnect()
        }
    }

    fun deleteDownload(context: Context, translation: Translation) {
        if (translation.builtIn) return
        context.getDatabasePath(translation.fileName).takeIf { it.exists() }?.delete()
    }
}
