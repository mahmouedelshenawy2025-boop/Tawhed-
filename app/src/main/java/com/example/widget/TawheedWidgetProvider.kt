package com.example.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.text.TextPaint
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import androidx.core.content.res.ResourcesCompat
import com.example.MainActivity
import com.example.R
import com.example.model.ArabicFontType
import java.util.Locale

class TawheedWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
        scheduleNextSwitch(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action
        if (action == ACTION_SWITCH_PHRASE || action == ACTION_AUTO_SWITCH_WIDGET) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val currentIndex = prefs.getInt(KEY_CURRENT_PHRASE_INDEX, 0)
            val newIndex = if (currentIndex == 0) 1 else 0
            prefs.edit().putInt(KEY_CURRENT_PHRASE_INDEX, newIndex).apply()

            notifyWidgetUpdate(context)
            scheduleNextSwitch(context)
        } else if (action == Intent.ACTION_BOOT_COMPLETED) {
            scheduleNextSwitch(context)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        scheduleNextSwitch(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        cancelScheduledSwitch(context)
    }

    companion object {
        const val PREFS_NAME = "tawheed_prefs"
        const val KEY_CURRENT_PHRASE_INDEX = "current_phrase_index"
        const val KEY_BRACKET_STYLE = "bracket_style"
        const val KEY_FONT_TYPE = "font_type"
        const val KEY_FONT_SIZE = "font_size"
        const val KEY_SHOW_HIJRI_DATE = "show_hijri_date"
        const val KEY_HIJRI_ADJUSTMENT = "hijri_adjustment"
        const val KEY_INTERVAL_SECONDS = "interval_seconds"
        const val KEY_AUTO_SWITCHING = "auto_switching"
        const val KEY_CLOCK_FONT_SIZE = "clock_font_size"
        const val KEY_DATE_FONT_SIZE = "date_font_size"
        const val ACTION_SWITCH_PHRASE = "com.example.tawheed.ACTION_SWITCH_PHRASE"
        const val ACTION_AUTO_SWITCH_WIDGET = "com.example.tawheed.ACTION_AUTO_SWITCH_WIDGET"

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

        fun getHijriDate(adjustmentDays: Int): String {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    val localDate = java.time.LocalDate.now().plusDays(adjustmentDays.toLong())
                    val hijrahDate = java.time.chrono.HijrahDate.from(localDate)
                    val formatter = java.time.format.DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ar"))
                    return "${formatter.format(hijrahDate)} هـ"
                } catch (_: Exception) {}
            }
            return ""
        }

        fun createPhraseBitmap(
            context: Context,
            text: String,
            fontType: ArabicFontType,
            fontSizeSp: Float
        ): Bitmap {
            val displayMetrics = context.resources.displayMetrics
            val fontSizePx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                fontSizeSp.coerceIn(20f, 48f),
                displayMetrics
            )

            val baseTypeface = when (fontType) {
                ArabicFontType.AMIRI -> {
                    try {
                        ResourcesCompat.getFont(context, R.font.amiri) ?: Typeface.DEFAULT_BOLD
                    } catch (_: Exception) {
                        Typeface.DEFAULT_BOLD
                    }
                }
                ArabicFontType.CAIRO -> {
                    try {
                        ResourcesCompat.getFont(context, R.font.cairo) ?: Typeface.DEFAULT_BOLD
                    } catch (_: Exception) {
                        Typeface.DEFAULT_BOLD
                    }
                }
                ArabicFontType.SYSTEM -> Typeface.DEFAULT_BOLD
            }

            val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                typeface = Typeface.create(baseTypeface, Typeface.BOLD)
                textSize = fontSizePx
                textAlign = Paint.Align.CENTER
                setShadowLayer(4f, 1f, 1f, 0xB0FFFFFF.toInt())
            }

            val fontMetrics = paint.fontMetrics
            val textWidth = paint.measureText(text)
            val width = (textWidth + 40f).toInt().coerceAtLeast(100)
            val textHeight = fontMetrics.bottom - fontMetrics.top
            val height = (textHeight + 24f).toInt().coerceAtLeast(50)

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val xPos = width / 2f
            val yPos = (height / 2f) - ((fontMetrics.descent + fontMetrics.ascent) / 2f)
            canvas.drawText(text, xPos, yPos, paint)

            return bitmap
        }

        fun scheduleNextSwitch(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val isAuto = prefs.getBoolean(KEY_AUTO_SWITCHING, true)
            if (!isAuto) {
                cancelScheduledSwitch(context)
                return
            }

            val intervalSec = prefs.getInt(KEY_INTERVAL_SECONDS, 5).coerceAtLeast(3)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

            val intent = Intent(context, TawheedWidgetProvider::class.java).apply {
                action = ACTION_AUTO_SWITCH_WIDGET
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                2020,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val triggerAtMillis = System.currentTimeMillis() + (intervalSec * 1000L)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } catch (_: SecurityException) {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        }

        fun cancelScheduledSwitch(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, TawheedWidgetProvider::class.java).apply {
                action = ACTION_AUTO_SWITCH_WIDGET
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                2020,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val currentIndex = prefs.getInt(KEY_CURRENT_PHRASE_INDEX, 0)
            val displayText = getFormattedPhrase(context, currentIndex)

            val fontTypeName = prefs.getString(KEY_FONT_TYPE, ArabicFontType.AMIRI.name) ?: ArabicFontType.AMIRI.name
            val fontType = try {
                ArabicFontType.valueOf(fontTypeName)
            } catch (_: Exception) {
                ArabicFontType.AMIRI
            }

            val fontSize = prefs.getFloat(KEY_FONT_SIZE, 32f).coerceIn(20f, 48f)
            val clockFontSize = prefs.getFloat(KEY_CLOCK_FONT_SIZE, 36f)
            val dateFontSize = prefs.getFloat(KEY_DATE_FONT_SIZE, 14f)

            val showHijri = prefs.getBoolean(KEY_SHOW_HIJRI_DATE, true)
            val hijriAdjustment = prefs.getInt(KEY_HIJRI_ADJUSTMENT, 0)

            val views = RemoteViews(context.packageName, R.layout.widget_tawheed)

            // 1. Render custom-font Dhikr bitmap
            try {
                val phraseBitmap = createPhraseBitmap(context, displayText, fontType, fontSize)
                views.setImageViewBitmap(R.id.widget_text_image, phraseBitmap)
                views.setViewVisibility(R.id.widget_text_image, View.VISIBLE)
                views.setViewVisibility(R.id.widget_text, View.GONE)
            } catch (_: Exception) {
                views.setTextViewText(R.id.widget_text, displayText)
                views.setViewVisibility(R.id.widget_text, View.VISIBLE)
                views.setViewVisibility(R.id.widget_text_image, View.GONE)
            }

            // 2. Dynamic Clock Size in Widget
            val widgetClockSize = (clockFontSize * 0.55f).coerceIn(14f, 32f)
            views.setTextViewTextSize(R.id.widget_clock, TypedValue.COMPLEX_UNIT_SP, widgetClockSize)

            // 3. Dynamic Date Size in Widget
            val widgetDateSize = (dateFontSize * 0.85f).coerceIn(10f, 18f)
            views.setTextViewTextSize(R.id.widget_date, TypedValue.COMPLEX_UNIT_SP, widgetDateSize)
            views.setTextViewTextSize(R.id.widget_hijri_date, TypedValue.COMPLEX_UNIT_SP, widgetDateSize)

            // 4. Hijri Date
            if (showHijri) {
                val hijriDateStr = getHijriDate(hijriAdjustment)
                if (hijriDateStr.isNotEmpty()) {
                    views.setTextViewText(R.id.widget_hijri_date, "• $hijriDateStr •")
                    views.setViewVisibility(R.id.widget_hijri_date, View.VISIBLE)
                } else {
                    views.setViewVisibility(R.id.widget_hijri_date, View.GONE)
                }
            } else {
                views.setViewVisibility(R.id.widget_hijri_date, View.GONE)
            }

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
