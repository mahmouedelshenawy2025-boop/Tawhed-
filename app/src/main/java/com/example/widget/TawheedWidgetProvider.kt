package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R

class TawheedWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_SWITCH_PHRASE) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val currentIndex = prefs.getInt(KEY_CURRENT_PHRASE_INDEX, 0)
            val newIndex = if (currentIndex == 0) 1 else 0
            prefs.edit().putInt(KEY_CURRENT_PHRASE_INDEX, newIndex).apply()

            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, TawheedWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }

    companion object {
        const val PREFS_NAME = "tawheed_prefs"
        const val KEY_CURRENT_PHRASE_INDEX = "current_phrase_index"
        const val KEY_BRACKET_STYLE = "bracket_style"
        const val ACTION_SWITCH_PHRASE = "com.example.tawheed.ACTION_SWITCH_PHRASE"

        val PHRASES = listOf(
            "لا إله إلا الله",
            "محمد رسول الله"
        )

        fun getFormattedPhrase(context: Context, index: Int): String {
            val phrase = if (index in PHRASES.indices) PHRASES[index] else PHRASES[0]
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return when (prefs.getString(KEY_BRACKET_STYLE, "round")) {
                "round" -> "($phrase)"
                "quranic" -> "﴿ $phrase ﴾"
                "square" -> "[$phrase]"
                "none" -> phrase
                else -> "($phrase)"
            }
        }

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val currentIndex = prefs.getInt(KEY_CURRENT_PHRASE_INDEX, 0)
            val displayText = getFormattedPhrase(context, currentIndex)

            val views = RemoteViews(context.packageName, R.layout.widget_tawheed)
            views.setTextViewText(R.id.widget_text, displayText)

            // Switch button PendingIntent
            val switchIntent = Intent(context, TawheedWidgetProvider::class.java).apply {
                action = ACTION_SWITCH_PHRASE
            }
            val switchPendingIntent = PendingIntent.getBroadcast(
                context,
                1001,
                switchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_switch, switchPendingIntent)

            // Container click opens the app
            val appIntent = Intent(context, MainActivity::class.java)
            val appPendingIntent = PendingIntent.getActivity(
                context,
                1002,
                appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, appPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun notifyWidgetUpdate(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, TawheedWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }
}
