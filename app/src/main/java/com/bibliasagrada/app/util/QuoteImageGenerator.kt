package com.bibliasagrada.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.bibliasagrada.app.data.model.Verse
import java.io.File
import java.io.FileOutputStream

/**
 * Gera uma imagem estilo "quote card" com um ou mais versículos, pronta para
 * compartilhar (WhatsApp, Instagram, etc). Desenhada diretamente com
 * android.graphics (em vez de capturar a UI do Compose), para funcionar em
 * qualquer versão do Compose sem depender de APIs mais novas de captura.
 */
object QuoteImageGenerator {

    private const val WIDTH = 1080
    private const val PADDING = 90f
    /** Limite de versículos numa única imagem, para a imagem não ficar gigante nem lenta de gerar. */
    const val MAX_VERSES_PER_IMAGE = 25

    /**
     * Cores usadas na imagem, para combinar com o tema que a pessoa está usando no app
     * (dourado, preto e branco, ou rosé — claro ou escuro). Todos os valores em formato
     * de cor Android (Int ARGB), o que combina naturalmente com `.toArgb()` de uma
     * `androidx.compose.ui.graphics.Color`.
     */
    data class Theme(
        val background: Int,
        val accent: Int,
        val text: Int,
        val footer: Int
    )

    // Usado apenas se nenhum tema for informado (fallback dourado escuro, cor original do app).
    private val DEFAULT_THEME = Theme(
        background = Color.parseColor("#20140B"),
        accent = Color.parseColor("#D7B37B"),
        text = Color.parseColor("#F3ECE0"),
        footer = Color.parseColor("#9C8B6F")
    )

    /**
     * Gera a imagem e devolve um content:// URI pronto para usar num Intent.ACTION_SEND.
     * [verses] deve estar em ordem de leitura (ex.: vários versículos seguidos de um capítulo).
     */
    fun generateAndShare(
        context: Context,
        verses: List<Verse>,
        theme: Theme = DEFAULT_THEME,
        appName: String = "Bíblia Sagrada para Todos",
        appLink: String = "https://t.me/BibliaSagradaparatodos"
    ): android.net.Uri {
        val dir = File(context.cacheDir, "shared_images").apply { mkdirs() }
        // Limpa imagens compartilhadas antigas (mais de 1 hora) para não deixar
        // arquivos acumulando à toa no armazenamento do app.
        val oneHourAgo = System.currentTimeMillis() - 60 * 60 * 1000
        dir.listFiles()?.forEach { old -> if (old.lastModified() < oneHourAgo) old.delete() }

        val bitmap = generateBitmap(verses.take(MAX_VERSES_PER_IMAGE), theme, appName, appLink)
        val file = File(dir, "citacao_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        bitmap.recycle()
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    /**
     * Gera a imagem e salva direto na galeria do celular (álbum "Bíblia Sagrada"),
     * usando a API moderna (MediaStore) no Android 10+, e o caminho de arquivo
     * tradicional em versões mais antigas.
     */
    fun saveToGallery(
        context: Context,
        verses: List<Verse>,
        theme: Theme = DEFAULT_THEME,
        appName: String = "Bíblia Sagrada para Todos",
        appLink: String = "https://t.me/BibliaSagradaparatodos"
    ): Boolean {
        val bitmap = generateBitmap(verses.take(MAX_VERSES_PER_IMAGE), theme, appName, appLink)
        val fileName = "biblia_sagrada_${System.currentTimeMillis()}.png"
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                val values = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Bíblia Sagrada")
                }
                val uri = context.contentResolver.insert(
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
                ) ?: return false
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                } ?: return false
            } else {
                @Suppress("DEPRECATION")
                val picturesDir = android.os.Environment.getExternalStoragePublicDirectory(
                    android.os.Environment.DIRECTORY_PICTURES
                )
                val albumDir = File(picturesDir, "Bíblia Sagrada").apply { mkdirs() }
                val file = File(albumDir, fileName)
                FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
                android.media.MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf("image/png"), null)
            }
            true
        } catch (e: Exception) {
            false
        } finally {
            bitmap.recycle()
        }
    }

    private fun generateBitmap(verses: List<Verse>, theme: Theme, appName: String, appLink: String): Bitmap {
        val quoteText = verses.joinToString(" ") { it.text.trim() }
        val reference = referenceFor(verses)

        val quotePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.text
            textSize = 46f
            typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
        }
        val refPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.accent
            textSize = 38f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        val footerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.footer
            textSize = 28f
            typeface = Typeface.DEFAULT
        }
        val quoteMarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.accent
            textSize = 130f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            alpha = 140
        }

        val textWidth = (WIDTH - PADDING * 2).toInt()
        val quoteLayout = buildStaticLayout("\u201C$quoteText\u201D", quotePaint, textWidth)
        val refLayout = buildStaticLayout(reference, refPaint, textWidth)

        val topBlock = 210f // espaço para a aspas decorativa + respiro
        val spacingAfterQuote = 40f
        val spacingBeforeFooter = 70f
        val footerHeight = 110f

        val height = (topBlock + quoteLayout.height + spacingAfterQuote + refLayout.height +
            spacingBeforeFooter + footerHeight + PADDING).toInt()

        val bitmap = Bitmap.createBitmap(WIDTH, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(theme.background)

        // Moldura sutil na cor de destaque do tema
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.accent
            style = Paint.Style.STROKE
            strokeWidth = 3f
            alpha = 120
        }
        canvas.drawRect(24f, 24f, WIDTH - 24f, height - 24f, borderPaint)

        // Aspas decorativa
        canvas.drawText("\u201C", PADDING - 10f, 165f, quoteMarkPaint)

        // Texto do versículo
        canvas.save()
        canvas.translate(PADDING, topBlock)
        quoteLayout.draw(canvas)
        canvas.restore()

        // Referência (livro capítulo:versículo)
        canvas.save()
        canvas.translate(PADDING, topBlock + quoteLayout.height + spacingAfterQuote)
        refLayout.draw(canvas)
        canvas.restore()

        // Rodapé: nome do app + link
        val footerY = topBlock + quoteLayout.height + spacingAfterQuote + refLayout.height + spacingBeforeFooter
        val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.accent
            alpha = 90
            strokeWidth = 2f
        }
        canvas.drawLine(PADDING, footerY, WIDTH - PADDING, footerY, dividerPaint)
        canvas.drawText(appName, PADDING, footerY + 45f, footerPaint)
        canvas.drawText(appLink, PADDING, footerY + 80f, footerPaint)

        return bitmap
    }

    private fun buildStaticLayout(text: String, paint: TextPaint, width: Int): StaticLayout {
        return StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(10f, 1.15f)
            .setIncludePad(false)
            .build()
    }

    private fun referenceFor(verses: List<Verse>): String {
        if (verses.isEmpty()) return ""
        val first = verses.first()
        val last = verses.last()
        return if (verses.size == 1 || first.verse == last.verse) {
            first.reference
        } else if (first.bookId == last.bookId && first.chapter == last.chapter) {
            "${first.bookName} ${first.chapter}:${first.verse}-${last.verse}"
        } else {
            "${first.reference} — ${last.reference}"
        }
    }
}
