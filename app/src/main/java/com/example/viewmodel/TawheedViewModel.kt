package com.example.viewmodel

import android.app.Application
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.MainActivity
import com.example.model.ArabicFontType
import com.example.model.BracketStyle
import com.example.model.DhikrItem
import com.example.model.DisplayTheme
import com.example.widget.TawheedWidgetProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class TawheedUiState(
    val phrases: List<DhikrItem> = listOf(
        DhikrItem(
            id = 0,
            arabicText = "لا إله إلا الله",
            virtue = "أفضل الذكر وخير ما قال النبيون، كلمة التوحيد وحصن المؤمن",
            translation = "There is no god but Allah"
        ),
        DhikrItem(
            id = 1,
            arabicText = "محمد رسول الله",
            virtue = "شهادة الرسالة ومفتاح الاقتداء والرحمة المهداة للعالمين ﷺ",
            translation = "Muhammad is the Messenger of Allah"
        )
    ),
    val currentIndex: Int = 0,
    val intervalSeconds: Int = 5,
    val isAutoSwitching: Boolean = true,
    val progressRemainingRatio: Float = 1.0f,
    val secondsRemaining: Float = 5.0f,
    val selectedFont: ArabicFontType = ArabicFontType.AMIRI,
    val fontSizeSp: Float = 42f,
    val bracketStyle: BracketStyle = BracketStyle.ROUND,
    val selectedTheme: DisplayTheme = DisplayTheme.PURE_WHITE,
    val hapticEnabled: Boolean = true,
    val keepScreenOn: Boolean = true,
    val showVirtue: Boolean = true,
    val isFullscreen: Boolean = false,
    val showHijriDate: Boolean = true,
    val hijriAdjustmentDays: Int = 0,
    val clockFontSizeSp: Float = 36f,
    val dateFontSizeSp: Float = 14f,
    val counterFirst: Int = 0,
    val counterSecond: Int = 0,
    val showWidgetDialog: Boolean = false,
    val showSettingsSheet: Boolean = false
) {
    val currentItem: DhikrItem
        get() = phrases.getOrElse(currentIndex) { phrases.first() }

    val formattedPhrase: String
        get() = bracketStyle.exampleFormat(currentItem.arabicText)

    val currentCounter: Int
        get() = if (currentIndex == 0) counterFirst else counterSecond
}

class TawheedViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(
        TawheedUiState(
            currentIndex = prefs.getInt(KEY_CURRENT_INDEX, 0).coerceIn(0, 1),
            intervalSeconds = prefs.getInt(KEY_INTERVAL_SECONDS, 5).coerceIn(2, 300),
            isAutoSwitching = prefs.getBoolean(KEY_AUTO_SWITCHING, true),
            selectedFont = try {
                ArabicFontType.valueOf(prefs.getString(KEY_FONT_TYPE, ArabicFontType.AMIRI.name) ?: ArabicFontType.AMIRI.name)
            } catch (e: Exception) {
                ArabicFontType.AMIRI
            },
            fontSizeSp = prefs.getFloat(KEY_FONT_SIZE, 42f).coerceIn(26f, 60f),
            bracketStyle = try {
                BracketStyle.valueOf(prefs.getString(KEY_BRACKET_STYLE, BracketStyle.ROUND.name) ?: BracketStyle.ROUND.name)
            } catch (e: Exception) {
                BracketStyle.ROUND
            },
            selectedTheme = try {
                DisplayTheme.valueOf(prefs.getString(KEY_DISPLAY_THEME, DisplayTheme.PURE_WHITE.name) ?: DisplayTheme.PURE_WHITE.name)
            } catch (e: Exception) {
                DisplayTheme.PURE_WHITE
            },
            hapticEnabled = prefs.getBoolean(KEY_HAPTIC, true),
            keepScreenOn = prefs.getBoolean(KEY_KEEP_SCREEN_ON, true),
            showVirtue = prefs.getBoolean(KEY_SHOW_VIRTUE, true),
            showHijriDate = prefs.getBoolean(KEY_SHOW_HIJRI_DATE, true),
            hijriAdjustmentDays = prefs.getInt(KEY_HIJRI_ADJUSTMENT, 0),
            clockFontSizeSp = prefs.getFloat(KEY_CLOCK_FONT_SIZE, 36f).coerceIn(20f, 54f),
            dateFontSizeSp = prefs.getFloat(KEY_DATE_FONT_SIZE, 14f).coerceIn(10f, 22f),
            counterFirst = prefs.getInt(KEY_COUNTER_FIRST, 0),
            counterSecond = prefs.getInt(KEY_COUNTER_SECOND, 0)
        )
    )
    val uiState: StateFlow<TawheedUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        startTimer()
        TawheedWidgetProvider.scheduleNextSwitch(application)
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var elapsedMillis = 0L
            val tickStepMillis = 100L

            while (isActive) {
                val state = _uiState.value
                val totalMillis = (state.intervalSeconds * 1000L).coerceAtLeast(1000L)

                if (state.isAutoSwitching) {
                    elapsedMillis += tickStepMillis
                    val remainingMillis = (totalMillis - elapsedMillis).coerceAtLeast(0L)
                    val ratio = (remainingMillis.toFloat() / totalMillis.toFloat()).coerceIn(0f, 1f)
                    val secondsLeft = remainingMillis / 1000f

                    _uiState.update {
                        it.copy(
                            progressRemainingRatio = ratio,
                            secondsRemaining = secondsLeft
                        )
                    }

                    if (elapsedMillis >= totalMillis) {
                        elapsedMillis = 0L
                        switchToNextPhrase(userInitiated = false)
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            progressRemainingRatio = 1.0f,
                            secondsRemaining = state.intervalSeconds.toFloat()
                        )
                    }
                }
                delay(tickStepMillis)
            }
        }
    }

    private fun switchToNextPhrase(userInitiated: Boolean) {
        val nextIndex = if (_uiState.value.currentIndex == 0) 1 else 0
        setPhraseIndex(nextIndex)
        if (_uiState.value.hapticEnabled) {
            triggerGentleHaptic()
        }
    }

    fun nextPhrase() {
        switchToNextPhrase(userInitiated = true)
        resetTimerElapsed()
    }

    fun previousPhrase() {
        val prevIndex = if (_uiState.value.currentIndex == 0) 1 else 0
        setPhraseIndex(prevIndex)
        if (_uiState.value.hapticEnabled) {
            triggerGentleHaptic()
        }
        resetTimerElapsed()
    }

    fun selectPhrase(index: Int) {
        setPhraseIndex(index.coerceIn(0, 1))
        resetTimerElapsed()
    }

    private fun setPhraseIndex(index: Int) {
        _uiState.update { it.copy(currentIndex = index) }
        prefs.edit().putInt(KEY_CURRENT_INDEX, index).apply()
        TawheedWidgetProvider.notifyWidgetUpdate(getApplication())
    }

    fun toggleAutoSwitch() {
        _uiState.update {
            val newState = !it.isAutoSwitching
            prefs.edit().putBoolean(KEY_AUTO_SWITCHING, newState).apply()
            it.copy(isAutoSwitching = newState)
        }
        resetTimerElapsed()
        TawheedWidgetProvider.scheduleNextSwitch(getApplication())
    }

    fun setInterval(seconds: Int) {
        val safeSeconds = seconds.coerceIn(2, 300)
        _uiState.update {
            it.copy(
                intervalSeconds = safeSeconds,
                secondsRemaining = safeSeconds.toFloat(),
                progressRemainingRatio = 1.0f
            )
        }
        prefs.edit().putInt(KEY_INTERVAL_SECONDS, safeSeconds).apply()
        resetTimerElapsed()
        TawheedWidgetProvider.scheduleNextSwitch(getApplication())
    }

    fun setFont(fontType: ArabicFontType) {
        _uiState.update { it.copy(selectedFont = fontType) }
        prefs.edit().putString(KEY_FONT_TYPE, fontType.name).apply()
        TawheedWidgetProvider.notifyWidgetUpdate(getApplication())
    }

    fun setFontSize(sizeSp: Float) {
        val safeSize = sizeSp.coerceIn(24f, 64f)
        _uiState.update { it.copy(fontSizeSp = safeSize) }
        prefs.edit().putFloat(KEY_FONT_SIZE, safeSize).apply()
        TawheedWidgetProvider.notifyWidgetUpdate(getApplication())
    }

    fun setBracketStyle(style: BracketStyle) {
        _uiState.update { it.copy(bracketStyle = style) }
        val widgetStyleKey = when (style) {
            BracketStyle.ROUND -> "round"
            BracketStyle.QURANIC -> "quranic"
            BracketStyle.SQUARE -> "square"
            BracketStyle.NONE -> "none"
        }
        prefs.edit().putString(KEY_BRACKET_STYLE, widgetStyleKey).apply()
        TawheedWidgetProvider.notifyWidgetUpdate(getApplication())
    }

    fun setDisplayTheme(theme: DisplayTheme) {
        _uiState.update { it.copy(selectedTheme = theme) }
        prefs.edit().putString(KEY_DISPLAY_THEME, theme.name).apply()
    }

    fun toggleHaptic() {
        _uiState.update {
            val newHaptic = !it.hapticEnabled
            prefs.edit().putBoolean(KEY_HAPTIC, newHaptic).apply()
            it.copy(hapticEnabled = newHaptic)
        }
    }

    fun toggleKeepScreenOn() {
        _uiState.update {
            val newKeep = !it.keepScreenOn
            prefs.edit().putBoolean(KEY_KEEP_SCREEN_ON, newKeep).apply()
            it.copy(keepScreenOn = newKeep)
        }
    }

    fun toggleShowVirtue() {
        _uiState.update {
            val newShow = !it.showVirtue
            prefs.edit().putBoolean(KEY_SHOW_VIRTUE, newShow).apply()
            it.copy(showVirtue = newShow)
        }
    }

    fun toggleFullscreen() {
        _uiState.update { it.copy(isFullscreen = !it.isFullscreen) }
    }

    fun incrementCounter() {
        val state = _uiState.value
        if (state.currentIndex == 0) {
            val newCount = state.counterFirst + 1
            _uiState.update { it.copy(counterFirst = newCount) }
            prefs.edit().putInt(KEY_COUNTER_FIRST, newCount).apply()
        } else {
            val newCount = state.counterSecond + 1
            _uiState.update { it.copy(counterSecond = newCount) }
            prefs.edit().putInt(KEY_COUNTER_SECOND, newCount).apply()
        }
        if (_uiState.value.hapticEnabled) {
            triggerGentleHaptic()
        }
    }

    fun resetCounter() {
        val state = _uiState.value
        if (state.currentIndex == 0) {
            _uiState.update { it.copy(counterFirst = 0) }
            prefs.edit().putInt(KEY_COUNTER_FIRST, 0).apply()
        } else {
            _uiState.update { it.copy(counterSecond = 0) }
            prefs.edit().putInt(KEY_COUNTER_SECOND, 0).apply()
        }
    }

    fun toggleShowHijriDate() {
        _uiState.update {
            val newVal = !it.showHijriDate
            prefs.edit().putBoolean(KEY_SHOW_HIJRI_DATE, newVal).apply()
            it.copy(showHijriDate = newVal)
        }
        TawheedWidgetProvider.notifyWidgetUpdate(getApplication())
    }

    fun setHijriAdjustment(days: Int) {
        val clamped = days.coerceIn(-2, 2)
        _uiState.update {
            prefs.edit().putInt(KEY_HIJRI_ADJUSTMENT, clamped).apply()
            it.copy(hijriAdjustmentDays = clamped)
        }
        TawheedWidgetProvider.notifyWidgetUpdate(getApplication())
    }

    fun setClockFontSize(sizeSp: Float) {
        val safeSize = sizeSp.coerceIn(20f, 54f)
        _uiState.update { it.copy(clockFontSizeSp = safeSize) }
        prefs.edit().putFloat(KEY_CLOCK_FONT_SIZE, safeSize).apply()
        TawheedWidgetProvider.notifyWidgetUpdate(getApplication())
    }

    fun setDateFontSize(sizeSp: Float) {
        val safeSize = sizeSp.coerceIn(10f, 22f)
        _uiState.update { it.copy(dateFontSizeSp = safeSize) }
        prefs.edit().putFloat(KEY_DATE_FONT_SIZE, safeSize).apply()
        TawheedWidgetProvider.notifyWidgetUpdate(getApplication())
    }

    fun resetAllCounters() {
        _uiState.update { it.copy(counterFirst = 0, counterSecond = 0) }
        prefs.edit().putInt(KEY_COUNTER_FIRST, 0).putInt(KEY_COUNTER_SECOND, 0).apply()
    }

    fun setShowWidgetDialog(show: Boolean) {
        _uiState.update { it.copy(showWidgetDialog = show) }
    }

    fun setShowSettingsSheet(show: Boolean) {
        _uiState.update { it.copy(showSettingsSheet = show) }
    }

    fun pinWidgetToHomeScreen(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val appWidgetManager = context.getSystemService(AppWidgetManager::class.java)
            val myProvider = ComponentName(context, TawheedWidgetProvider::class.java)

            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                val successIntent = Intent(context, MainActivity::class.java)
                val successPendingIntent = PendingIntent.getActivity(
                    context,
                    9001,
                    successIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                return appWidgetManager.requestPinAppWidget(myProvider, null, successPendingIntent)
            }
        }
        return false
    }

    private fun resetTimerElapsed() {
        _uiState.update {
            it.copy(
                progressRemainingRatio = 1.0f,
                secondsRemaining = it.intervalSeconds.toFloat()
            )
        }
    }

    private fun triggerGentleHaptic() {
        try {
            val context = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(20)
                }
            }
        } catch (_: Exception) {}
    }

    companion object {
        const val PREFS_NAME = "tawheed_prefs"
        const val KEY_CURRENT_INDEX = "current_phrase_index"
        const val KEY_INTERVAL_SECONDS = "interval_seconds"
        const val KEY_AUTO_SWITCHING = "auto_switching"
        const val KEY_FONT_TYPE = "font_type"
        const val KEY_FONT_SIZE = "font_size"
        const val KEY_BRACKET_STYLE = "bracket_style"
        const val KEY_DISPLAY_THEME = "display_theme"
        const val KEY_HAPTIC = "haptic_enabled"
        const val KEY_KEEP_SCREEN_ON = "keep_screen_on"
        const val KEY_SHOW_VIRTUE = "show_virtue"
        const val KEY_COUNTER_FIRST = "counter_first"
        const val KEY_COUNTER_SECOND = "counter_second"
        const val KEY_SHOW_HIJRI_DATE = "show_hijri_date"
        const val KEY_HIJRI_ADJUSTMENT = "hijri_adjustment"
        const val KEY_CLOCK_FONT_SIZE = "clock_font_size"
        const val KEY_DATE_FONT_SIZE = "date_font_size"
    }
}
