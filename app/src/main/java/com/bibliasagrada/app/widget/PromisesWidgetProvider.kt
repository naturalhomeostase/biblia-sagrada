package com.bibliasagrada.app.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.bibliasagrada.app.MainActivity
import com.bibliasagrada.app.R
import com.bibliasagrada.app.data.repository.BibleRepository
import com.bibliasagrada.app.data.repository.PromisesCatalog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Widget de tela inicial para a Caixinha de Promessas. Mostra um versículo de
 * promessa aleatório, que muda sozinho de tempos em tempos e também pode ser
 * trocado na hora tocando no ícone de atualizar. Tocar no restante do widget
 * abre o app.
 *
 * (Intencionalmente NÃO existe um widget para o "Versículo do dia" — a ideia
 * é que, para ver o versículo do dia, a pessoa precise abrir o app de verdade.)
 */
class PromisesWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) refreshWidget(context, appWidgetManager, id)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, PromisesWidgetProvider::class.java))
            for (id in ids) refreshWidget(context, appWidgetManager, id)
        }
    }

    companion object {
        const val ACTION_REFRESH = "com.bibliasagrada.app.widget.ACTION_REFRESH"

        fun refreshWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            CoroutineScope(Dispatchers.IO).launch {
                val repository = BibleRepository.getInstance(context)
                val ref = PromisesCatalog.random()
                val verse = repository.getVerse(ref.bookId, ref.chapter, ref.verse)

                val views = RemoteViews(context.packageName, R.layout.widget_promises)
                if (verse != null) {
                    views.setTextViewText(R.id.widget_verse_text, "\u201C${verse.text}\u201D")
                    views.setTextViewText(R.id.widget_verse_ref, verse.reference)
                }

                // Tocar no widget abre o app.
                val openAppIntent = Intent(context, MainActivity::class.java)
                val openAppPendingIntent = android.app.PendingIntent.getActivity(
                    context, appWidgetId, openAppIntent,
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent)

                // Tocar no ícone de atualizar troca a promessa na hora.
                val refreshIntent = Intent(context, PromisesWidgetProvider::class.java).apply {
                    action = ACTION_REFRESH
                }
                val refreshPendingIntent = android.app.PendingIntent.getBroadcast(
                    context, appWidgetId, refreshIntent,
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_refresh, refreshPendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}
