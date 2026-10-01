package com.example.ui.screens

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.model.ArabicFontType
import com.example.model.BracketStyle
import com.example.model.DisplayTheme
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.CreamBorder
import com.example.ui.theme.CreamCard
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.IslamicGreen
import com.example.ui.theme.MarbleBackground
import com.example.ui.theme.SageBackground
import com.example.ui.theme.TextBlack
import com.example.ui.theme.TextDark
import com.example.ui.theme.WhiteBackground
import com.example.viewmodel.TawheedUiState
import com.example.viewmodel.TawheedViewModel
import android.os.Build
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun TawheedMainScreen(
    viewModel: TawheedViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Keep screen awake if configured
    DisposableEffect(state.keepScreenOn) {
        val window = (context as? Activity)?.window
        if (state.keepScreenOn) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Wrap in RTL LayoutDirection so Arabic typography and flow are natural
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .testTag("tawheed_main_screen")
        ) {
            // Background Layer
            ThemeBackground(theme = state.selectedTheme)

            if (state.isFullscreen) {
                // Fullscreen Ambient Display
                FullscreenDisplay(
                    state = state,
                    viewModel = viewModel,
                    onExitFullscreen = { viewModel.toggleFullscreen() }
                )
            } else {
                // Standard App View
                StandardDisplay(
                    state = state,
                    viewModel = viewModel
                )
            }

            // Settings Bottom Sheet
            if (state.showSettingsSheet) {
                SettingsBottomSheet(
                    state = state,
                    viewModel = viewModel,
                    onDismiss = { viewModel.setShowSettingsSheet(false) }
                )
            }

            // Widget Guide / Pinning Dialog
            if (state.showWidgetDialog) {
                WidgetInstructionsDialog(
                    onDismiss = { viewModel.setShowWidgetDialog(false) },
                    onPinWidget = {
                        val pinned = viewModel.pinWidgetToHomeScreen(context)
                        if (pinned) {
                            viewModel.setShowWidgetDialog(false)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ThemeBackground(theme: DisplayTheme) {
    // Pure spotless background without any images, gradients, or textures
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandardDisplay(
    state: TawheedUiState,
    viewModel: TawheedViewModel
) {
    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = IslamicGreen
                ),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = IslamicGreen.copy(alpha = 0.1f),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(id = R.drawable.app_launcher_icon),
                                    contentDescription = "شعار التطبيق",
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Text(
                            text = "ذكر التوحيد",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                        )
                    }
                },
                actions = {
                    // Home Screen Widget Pin button
                    IconButton(
                        onClick = { viewModel.setShowWidgetDialog(true) },
                        modifier = Modifier.testTag("btn_widget_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Widgets,
                            contentDescription = "ودجت الشاشة الرئيسية",
                            tint = IslamicGreen
                        )
                    }

                    // Fullscreen Ambient Mode button
                    IconButton(
                        onClick = { viewModel.toggleFullscreen() },
                        modifier = Modifier.testTag("btn_fullscreen_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "وضع ملء الشاشة",
                            tint = IslamicGreen
                        )
                    }

                    // Settings Sheet button
                    IconButton(
                        onClick = { viewModel.setShowSettingsSheet(true) },
                        modifier = Modifier.testTag("btn_settings_sheet")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "الإعدادات والمظهر",
                            tint = IslamicGreen
                        )
                    }
                }
            )
        },
        bottomBar = {
            // Minimal floating controls at the bottom
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Subtle auto-switch countdown bar
                if (state.isAutoSwitching) {
                    LinearProgressIndicator(
                        progress = { state.progressRemainingRatio },
                        modifier = Modifier
                            .width(140.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = IslamicGreen.copy(alpha = 0.6f),
                        trackColor = Color(0x1A000000)
                    )
                }

                // Controls row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.previousPhrase() },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0x08000000))
                            .testTag("btn_prev_phrase")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "العبارة السابقة",
                            tint = IslamicGreen
                        )
                    }

                    FilledTonalButton(
                        onClick = { viewModel.toggleAutoSwitch() },
                        shape = CircleShape,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (state.isAutoSwitching) IslamicGreen else IslamicGold,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = if (state.isAutoSwitching) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (state.isAutoSwitching) "إيقاف التبديل التلقائي" else "تشغيل التبديل التلقائي",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (state.isAutoSwitching) "تبديل تلقائي: شغال" else "تبديل تلقائي: متوقف",
                            style = MaterialTheme.typography.labelMedium.copy(fontFamily = CairoFontFamily)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.nextPhrase() },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0x08000000))
                            .testTag("btn_next_phrase")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "العبارة التالية",
                            tint = IslamicGreen
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    viewModel.incrementCounter()
                },
            contentAlignment = Alignment.Center
        ) {
            MainPhraseCard(
                state = state,
                viewModel = viewModel
            )
        }
    }
}

data class ClockDateState(
    val timeString: String = "",
    val secondsString: String = "",
    val amPmString: String = "",
    val gregorianDate: String = "",
    val hijriDate: String = ""
)

@Composable
fun rememberCurrentClockDate(hijriAdjustment: Int = 0): ClockDateState {
    var state by remember(hijriAdjustment) { mutableStateOf(calculateCurrentClockDate(hijriAdjustment)) }

    LaunchedEffect(hijriAdjustment) {
        while (true) {
            state = calculateCurrentClockDate(hijriAdjustment)
            delay(1000L)
        }
    }

    return state
}

private fun calculateCurrentClockDate(hijriAdjustment: Int = 0): ClockDateState {
    val now = Date()
    val localeAr = Locale("ar")

    // Formatter for hours:minutes in standard clean digital numerals
    val timeFmt = SimpleDateFormat("hh:mm", Locale.ENGLISH)
    val secFmt = SimpleDateFormat("ss", Locale.ENGLISH)
    val amPmFmt = SimpleDateFormat("a", localeAr)
    val dateFmt = SimpleDateFormat("EEEE، d MMMM yyyy", localeAr)

    val timeStr = timeFmt.format(now)
    val secStr = secFmt.format(now)
    val amPmStr = amPmFmt.format(now)
    val gregorianStr = dateFmt.format(now)

    var hijriStr = ""
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val localDate = java.time.LocalDate.now().plusDays(hijriAdjustment.toLong())
            val hijrahDate = java.time.chrono.HijrahDate.from(localDate)
            val hijriFormatter = java.time.format.DateTimeFormatter.ofPattern("d MMMM yyyy", localeAr)
            hijriStr = "${hijriFormatter.format(hijrahDate)} هـ"
        }
    } catch (_: Throwable) {
        hijriStr = ""
    }

    return ClockDateState(
        timeString = timeStr,
        secondsString = secStr,
        amPmString = amPmStr,
        gregorianDate = gregorianStr,
        hijriDate = hijriStr
    )
}

@Composable
fun DigitalClockView(
    timeString: String,
    secondsString: String,
    amPmString: String,
    modifier: Modifier = Modifier,
    isLarge: Boolean = false
) {
    Row(
        modifier = modifier
            .padding(
                horizontal = if (isLarge) 16.dp else 12.dp,
                vertical = if (isLarge) 8.dp else 4.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Main Time (hh:mm)
        Text(
            text = timeString,
            fontSize = if (isLarge) 50.sp else 38.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = CairoFontFamily,
            color = Color(0xFF111827),
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Seconds and AM/PM
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = ":$secondsString",
                fontSize = if (isLarge) 18.sp else 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = CairoFontFamily,
                color = IslamicGold
            )
            Text(
                text = amPmString,
                fontSize = if (isLarge) 16.sp else 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = CairoFontFamily,
                color = IslamicGreen
            )
        }
    }
}

@Composable
fun DateDisplayView(
    gregorianDate: String,
    hijriDate: String,
    showHijri: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Gregorian Date with day name
        Text(
            text = gregorianDate,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF374151)
            ),
            textAlign = TextAlign.Center
        )

        // Hijri Date if enabled
        if (showHijri && hijriDate.isNotEmpty()) {
            Text(
                text = "• $hijriDate •",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Normal,
                    color = IslamicGold
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MainPhraseCard(
    state: TawheedUiState,
    viewModel: TawheedViewModel
) {
    val clockDate = rememberCurrentClockDate(state.hijriAdjustmentDays)

    // 100% Pure Display Without Any Background - Exactly Like a Clock
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp)
            .padding(horizontal = 16.dp, vertical = 20.dp)
            .testTag("main_phrase_card"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 1. The Arabic Dhikr Text in pure solid black font - fully reactive to size and font type
        AnimatedContent(
            targetState = Triple(state.formattedPhrase, state.selectedFont, state.fontSizeSp),
            transitionSpec = {
                if (initialState.first != targetState.first) {
                    (fadeIn(animationSpec = tween(400)) + slideInHorizontally(tween(400)) { 30 })
                        .togetherWith(
                            fadeOut(animationSpec = tween(300)) + slideOutHorizontally(tween(300)) { -30 }
                        )
                } else {
                    fadeIn(animationSpec = tween(150)).togetherWith(fadeOut(animationSpec = tween(150)))
                }
            },
            label = "phrase_transition"
        ) { (phraseText, fontType, fontSize) ->
            val font = when (fontType) {
                ArabicFontType.AMIRI -> AmiriFontFamily
                ArabicFontType.CAIRO -> CairoFontFamily
                ArabicFontType.SYSTEM -> FontFamily.Default
            }

            Text(
                text = phraseText,
                color = TextBlack, // SOLID BLACK FONT as specifically requested
                fontSize = fontSize.sp,
                lineHeight = (fontSize * 1.35f).sp,
                fontFamily = font,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .testTag("displayed_arabic_phrase")
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2. Digital Clock directly under Dhikr - Pure and borderless
        DigitalClockView(
            timeString = clockDate.timeString,
            secondsString = clockDate.secondsString,
            amPmString = clockDate.amPmString
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Date directly under Clock
        DateDisplayView(
            gregorianDate = clockDate.gregorianDate,
            hijriDate = clockDate.hijriDate,
            showHijri = state.showHijriDate
        )

        // Meaning / Virtue (optional)
        AnimatedVisibility(visible = state.showVirtue) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                Text(
                    text = state.currentItem.virtue,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = CairoFontFamily,
                        color = Color(0xFF555047),
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }
}

@Composable
fun AutoSwitchTimerBar(
    state: TawheedUiState,
    viewModel: TawheedViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = CreamCard.copy(alpha = 0.85f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (state.isAutoSwitching) Color(0xFF10B981) else Color(0xFFF59E0B))
                    )
                    Text(
                        text = if (state.isAutoSwitching) "التبديل التلقائي نشط" else "التبديل التلقائي متوقف",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark
                        )
                    )
                }

                Text(
                    text = if (state.isAutoSwitching) {
                        "التبديل بعد: ${state.secondsRemaining.roundToInt()} ثوانٍ"
                    } else {
                        "كل ${state.intervalSeconds} ثانية (موقّف)"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = CairoFontFamily,
                        color = if (state.isAutoSwitching) IslamicGreen else Color(0xFFB45309)
                    )
                )
            }

            // Animated Linear Progress Indicator
            LinearProgressIndicator(
                progress = {
                    if (state.isAutoSwitching) state.progressRemainingRatio else 1.0f
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = IslamicGreen,
                trackColor = Color(0xFFE5DDCB)
            )
        }
    }
}

@Composable
fun PhraseControlsRow(
    state: TawheedUiState,
    viewModel: TawheedViewModel
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous Phrase Button
        FilledTonalButton(
            onClick = { viewModel.previousPhrase() },
            shape = CircleShape,
            modifier = Modifier
                .size(54.dp)
                .testTag("btn_prev_phrase"),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = CreamCard,
                contentColor = IslamicGreen
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "العبارة السابقة",
                modifier = Modifier.size(24.dp)
            )
        }

        // Play / Pause Auto-Switch Toggle Button
        Button(
            onClick = { viewModel.toggleAutoSwitch() },
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .height(54.dp)
                .padding(horizontal = 8.dp)
                .testTag("btn_toggle_autoswitch"),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (state.isAutoSwitching) IslamicGreen else IslamicGold,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Icon(
                    imageVector = if (state.isAutoSwitching) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = if (state.isAutoSwitching) "إيقاف مؤقت" else "تشغيل التبديل",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        // Next Phrase Button
        FilledTonalButton(
            onClick = { viewModel.nextPhrase() },
            shape = CircleShape,
            modifier = Modifier
                .size(54.dp)
                .testTag("btn_next_phrase"),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = CreamCard,
                contentColor = IslamicGreen
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "العبارة التالية",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IntervalPresetsSelector(
    currentInterval: Int,
    onSelectInterval: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = CreamCard.copy(alpha = 0.85f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "الفترة الزمنية للتبديل التلقائي:",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            )

            val presets = listOf(
                Pair(3, "3 ثوانٍ"),
                Pair(5, "5 ثوانٍ"),
                Pair(10, "10 ثوانٍ"),
                Pair(30, "30 ثانية"),
                Pair(60, "دقيقة واحدة")
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEach { (seconds, label) ->
                    val isSelected = currentInterval == seconds
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectInterval(seconds) },
                        label = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = CairoFontFamily,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IslamicGreen,
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFFF3EEDF),
                            labelColor = TextDark
                        ),
                        modifier = Modifier.testTag("interval_chip_$seconds")
                    )
                }
            }
        }
    }
}

@Composable
fun TasbeehCounterCard(
    state: TawheedUiState,
    viewModel: TawheedViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = CreamCard.copy(alpha = 0.9f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "عداد الذكر والتسبيح",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = IslamicGreen
                    )
                )

                IconButton(
                    onClick = { viewModel.resetCounter() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "إعادة ضبط العداد",
                        tint = Color(0xFF888072),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Counter for first phrase
                CounterTile(
                    title = "(لا إله إلا الله)",
                    count = state.counterFirst,
                    isActive = state.currentIndex == 0,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.selectPhrase(0) }
                )

                // Counter for second phrase
                CounterTile(
                    title = "(محمد رسول الله)",
                    count = state.counterSecond,
                    isActive = state.currentIndex == 1,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.selectPhrase(1) }
                )
            }

            // Quick increment button
            Button(
                onClick = { viewModel.incrementCounter() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("btn_increment_counter"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = IslamicGreen.copy(alpha = 0.12f),
                    contentColor = IslamicGreen
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "تسبيح للعبارة الحالية (+1)",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
fun CounterTile(
    title: String,
    count: Int,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = if (isActive) Color(0xFFEBF4EE) else Color(0xFFF7F3E9),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isActive) IslamicGreen.copy(alpha = 0.5f) else Color(0xFFE5DDD0)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = CairoFontFamily,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    color = TextBlack,
                    textAlign = TextAlign.Center
                ),
                maxLines = 1
            )
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) IslamicGreen else TextDark
                )
            )
        }
    }
}

@Composable
fun WidgetPromoBanner(
    onOpenWidgetGuide: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp)
            .clickable { onOpenWidgetGuide() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE9F3ED)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC0DEC9))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = IslamicGreen,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Widgets,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ودجت الشاشة الرئيسية للموبايل",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = IslamicGreen
                    )
                )
                Text(
                    text = "اجعل الذكر ظاهراً دائماً ع شاشة هاتفك الرئيسية",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = CairoFontFamily,
                        color = Color(0xFF2C553C)
                    )
                )
            }

            OutlinedButton(
                onClick = onOpenWidgetGuide,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = IslamicGreen
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, IslamicGreen),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "إضافة",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
fun FullscreenDisplay(
    state: TawheedUiState,
    viewModel: TawheedViewModel,
    onExitFullscreen: () -> Unit
) {
    val selectedFontFamily = when (state.selectedFont) {
        ArabicFontType.AMIRI -> AmiriFontFamily
        ArabicFontType.CAIRO -> CairoFontFamily
        ArabicFontType.SYSTEM -> FontFamily.Default
    }

    val clockDate = rememberCurrentClockDate()
    var showControlsTemporarily by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                showControlsTemporarily = !showControlsTemporarily
            }
            .padding(24.dp)
    ) {
        // Exit Fullscreen Floating Button
        IconButton(
            onClick = onExitFullscreen,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .background(Color.Black.copy(alpha = 0.1f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.FullscreenExit,
                contentDescription = "إنهاء وضع ملء الشاشة",
                tint = TextDark
            )
        }

        // Center Phrase with Digital Clock and Date
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 1. Dhikr without background - fully reactive
            AnimatedContent(
                targetState = Triple(state.formattedPhrase, state.selectedFont, state.fontSizeSp),
                transitionSpec = {
                    if (initialState.first != targetState.first) {
                        (fadeIn(animationSpec = tween(500)) + slideInHorizontally(tween(500)) { 40 })
                            .togetherWith(
                                fadeOut(animationSpec = tween(400)) + slideOutHorizontally(tween(400)) { -40 }
                            )
                    } else {
                        fadeIn(animationSpec = tween(150)).togetherWith(fadeOut(animationSpec = tween(150)))
                    }
                },
                label = "fullscreen_phrase"
            ) { (phraseText, fontType, fontSize) ->
                val font = when (fontType) {
                    ArabicFontType.AMIRI -> AmiriFontFamily
                    ArabicFontType.CAIRO -> CairoFontFamily
                    ArabicFontType.SYSTEM -> FontFamily.Default
                }
                Text(
                    text = phraseText,
                    color = TextBlack, // Rich black font
                    fontSize = (fontSize * 1.3f).coerceAtMost(64f).sp,
                    lineHeight = (fontSize * 1.6f).sp,
                    fontFamily = font,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Digital Clock under Dhikr in Fullscreen
            DigitalClockView(
                timeString = clockDate.timeString,
                secondsString = clockDate.secondsString,
                amPmString = clockDate.amPmString,
                isLarge = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Date under Clock in Fullscreen
            DateDisplayView(
                gregorianDate = clockDate.gregorianDate,
                hijriDate = clockDate.hijriDate,
                showHijri = state.showHijriDate
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Subtle countdown indicator
            if (state.isAutoSwitching) {
                LinearProgressIndicator(
                    progress = { state.progressRemainingRatio },
                    modifier = Modifier
                        .width(140.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = IslamicGreen.copy(alpha = 0.7f),
                    trackColor = Color(0x33000000)
                )
            }
        }

        // Bottom Controls Overlay
        AnimatedVisibility(
            visible = showControlsTemporarily,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = CreamCard.copy(alpha = 0.95f),
                shadowElevation = 8.dp,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.previousPhrase() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = IslamicGreen)
                    }
                    IconButton(onClick = { viewModel.toggleAutoSwitch() }) {
                        Icon(
                            if (state.isAutoSwitching) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = IslamicGreen
                        )
                    }
                    IconButton(onClick = { viewModel.nextPhrase() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = IslamicGreen)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    state: TawheedUiState,
    viewModel: TawheedViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CreamCard,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "إعدادات وتخصيص العرض",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = IslamicGreen
                    )
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                }
            }

            // 1. Arabic Font Type Selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "نوع الخط العربي:",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                )
                ArabicFontType.values().forEach { fontType ->
                    val isSelected = state.selectedFont == fontType
                    Surface(
                        onClick = { viewModel.setFont(fontType) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Color(0xFFE9F3ED) else Color(0xFFF7F3E9),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) IslamicGreen else Color(0xFFE2D7C3)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = fontType.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = if (fontType == ArabicFontType.AMIRI) AmiriFontFamily else CairoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = TextBlack
                                    )
                                )
                                Text(
                                    text = fontType.subtitle,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = CairoFontFamily,
                                        color = Color(0xFF756E63)
                                    )
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = IslamicGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 2. Bracket Style Selector (الكلام ما بين القوسين)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "شكل الأقواس المحيطة بالذكر:",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BracketStyle.values().forEach { style ->
                        val isSelected = state.bracketStyle == style
                        Surface(
                            onClick = { viewModel.setBracketStyle(style) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) IslamicGreen else Color(0xFFF7F3E9),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = style.exampleFormat("الذكر"),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = CairoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else TextDark,
                                        textAlign = TextAlign.Center
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 3. Font Size Slider
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "حجم الخط العربي:",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    )
                    Text(
                        text = "${state.fontSizeSp.toInt()} sp",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = CairoFontFamily,
                            color = IslamicGreen
                        )
                    )
                }

                Slider(
                    value = state.fontSizeSp,
                    onValueChange = { viewModel.setFontSize(it) },
                    valueRange = 26f..56f,
                    colors = SliderDefaults.colors(
                        thumbColor = IslamicGreen,
                        activeTrackColor = IslamicGreen,
                        inactiveTrackColor = Color(0xFFDDD2BD)
                    )
                )
            }

            // 4. Custom Switch Interval Slider
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "فترة التبديل الدقيقة:",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    )
                    Text(
                        text = "${state.intervalSeconds} ثانية",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = CairoFontFamily,
                            color = IslamicGreen
                        )
                    )
                }

                Slider(
                    value = state.intervalSeconds.toFloat(),
                    onValueChange = { viewModel.setInterval(it.toInt()) },
                    valueRange = 2f..60f,
                    steps = 28,
                    colors = SliderDefaults.colors(
                        thumbColor = IslamicGreen,
                        activeTrackColor = IslamicGreen,
                        inactiveTrackColor = Color(0xFFDDD2BD)
                    )
                )
            }

            // 5. Hijri Date Options & Calibration
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF7F3E9))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "إظهار التاريخ الهجري",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                        )
                        Text(
                            text = "عرض التاريخ الهجري أسفل الساعة",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = CairoFontFamily,
                                color = Color(0xFF756E63)
                            )
                        )
                    }
                    Switch(
                        checked = state.showHijriDate,
                        onCheckedChange = { viewModel.toggleShowHijriDate() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = IslamicGreen
                        )
                    )
                }

                if (state.showHijriDate) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "معايرة وضبط التاريخ الهجري (رؤية الهلال):",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = IslamicGreen
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(-2 to "-2 يوم", -1 to "-1 يوم", 0 to "مطابق (0)", 1 to "+1 يوم", 2 to "+2 يوم").forEach { (offset, label) ->
                            val isSelected = state.hijriAdjustmentDays == offset
                            Surface(
                                onClick = { viewModel.setHijriAdjustment(offset) },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) IslamicGreen else Color.White,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) IslamicGreen else Color(0xFFDDD2BD)
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = CairoFontFamily,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else TextDark,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 6. Toggles
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Haptic feedback toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "اهتزاز لطيف عند التبديل",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                color = TextDark
                            )
                        )
                        Text(
                            text = "إشعار لمسي عند تغيير العبارة",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = CairoFontFamily,
                                color = Color(0xFF756E63)
                            )
                        )
                    }
                    Switch(
                        checked = state.hapticEnabled,
                        onCheckedChange = { viewModel.toggleHaptic() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = IslamicGreen
                        )
                    )
                }

                // Keep screen awake toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "إبقاء الشاشة مضاءة دائماً",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                color = TextDark
                            )
                        )
                        Text(
                            text = "مناسب لوضع الهاتف ع المكتب أثناء القراءة",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = CairoFontFamily,
                                color = Color(0xFF756E63)
                            )
                        )
                    }
                    Switch(
                        checked = state.keepScreenOn,
                        onCheckedChange = { viewModel.toggleKeepScreenOn() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = IslamicGreen
                        )
                    )
                }

                // Show virtue text toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "إظهار فضل الشهادتين والترجمة",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                color = TextDark
                            )
                        )
                    }
                    Switch(
                        checked = state.showVirtue,
                        onCheckedChange = { viewModel.toggleShowVirtue() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = IslamicGreen
                        )
                    )
                }
            }

            // Reset counters action
            OutlinedButton(
                onClick = { viewModel.resetAllCounters() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFB91C1C)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5))
            ) {
                Text(
                    text = "تصفير جميع عدادات التسبيح",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
fun WidgetInstructionsDialog(
    onDismiss: () -> Unit,
    onPinWidget: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Widgets,
                    contentDescription = null,
                    tint = IslamicGreen
                )
                Text(
                    text = "ودجت الشاشة الرئيسية",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = IslamicGreen
                    )
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "يمكنك وضع ودجت لعرض (لا إله إلا الله) و (محمد رسول الله) مباشرة ع الشاشة الرئيسية لموبايلك بخط أسود واضح.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = CairoFontFamily,
                        color = TextDark
                    )
                )

                // Visual widget preview box
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFDFBF7),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFE0D5B8)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "معاينة الودجت ع شاشة الموبايل",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = CairoFontFamily,
                                color = IslamicGold
                            )
                        )
                        Text(
                            text = "(لا إله إلا الله)",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = AmiriFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = TextBlack
                            )
                        )
                        Text(
                            text = "اضغط للتبديل أو الفتح",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF9E927A)
                            )
                        )
                    }
                }

                Text(
                    text = "طريقة الإضافة اليدوية (إذا لم يتم التثبيت التلقائي):\n١. اخرج لشاشة الهاتف الرئيسية واضغط مطولاً في أي مساحة فارغة.\n٢. اختر «الأدوات» أو «الودجت» (Widgets).\n٣. ابحث عن «لا إله إلا الله» واسحب الودجت لشاشتك.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = CairoFontFamily,
                        color = Color(0xFF4A443A)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onPinWidget,
                colors = ButtonDefaults.buttonColors(
                    containerColor = IslamicGreen,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "إضافة الودجت للشاشة الرئيسية",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "إغلاق",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = CairoFontFamily,
                        color = Color(0xFF756E63)
                    )
                )
            }
        },
        containerColor = CreamCard,
        shape = RoundedCornerShape(24.dp)
    )
}
