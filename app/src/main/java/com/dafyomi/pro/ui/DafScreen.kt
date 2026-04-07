package com.dafyomi.pro.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dafyomi.pro.domain.DafData
import com.dafyomi.pro.domain.HebrewDateFormatter
import com.dafyomi.pro.ui.theme.LocalDafColors
import com.dafyomi.pro.ui.theme.ThemeMode
import java.time.LocalDate
import kotlin.math.sin

// ============================================================================
// DAF YOMI PRO - MAIN SCREEN
// ============================================================================

@Composable
fun DafScreen(
    viewModel: DafViewModel,
    onThemeModeChange: (ThemeMode) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val dafColors = LocalDafColors.current
    var showSettings by remember { mutableStateOf(false) }
    val fontSizeMultiplier by viewModel.fontSizeMultiplier.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(dafColors.gradientBrush)
    ) {
        // Background animation (dunes for light, stars for dark)
        BackgroundAnimation(dafColors = dafColors)

        when {
            state.isLoading && state.daf == null -> {
                LoadingState(dafColors, "Loading today's daf...")
            }
            state.error != null && state.daf == null -> {
                ErrorState(state.error ?: "", dafColors)
            }
            state.daf != null -> {
                DafContent(
                    daf = state.daf!!,
                    dafColors = dafColors,
                    fontSizeMultiplier = fontSizeMultiplier
                )
            }
            else -> {
                LoadingState(dafColors, "Loading today's daf...")
            }
        }

        // Offline indicator - show when we have daf but no hebrewText (API failed)
        if (state.daf != null && state.daf!!.hebrewText == null && state.error == null) {
            OfflineIndicator(dafColors = dafColors)
        }

        // Settings dialog
        if (showSettings) {
            SettingsDialog(
                dafColors = dafColors,
                onDismiss = { showSettings = false },
                currentThemeMode = themeMode,
                onThemeModeChange = { mode ->
                    viewModel.setThemeMode(mode)
                    onThemeModeChange(mode)
                },
                fontSizeMultiplier = fontSizeMultiplier,
                onFontSizeChange = { viewModel.setFontSizeMultiplier(it) }
            )
        }

        // Settings icon - positioned at top right
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopEnd
        ) {
            SettingsIcon(
                dafColors = dafColors,
                onClick = { showSettings = true }
            )
        }
    }
}

@Composable
private fun OfflineIndicator(dafColors: com.dafyomi.pro.ui.theme.DafColors) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Offline - showing cached text",
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            color = dafColors.textSecondary.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 60.dp)
        )
    }
}

@Composable
private fun SettingsIcon(
    dafColors: com.dafyomi.pro.ui.theme.DafColors,
    onClick: () -> Unit
) {
    Text(
        text = "☰",
        fontSize = 26.sp,
        color = dafColors.textSecondary,
        modifier = Modifier
            .padding(16.dp)
            .clickable { onClick() }
    )
}

@Composable
private fun SettingsDialog(
    dafColors: com.dafyomi.pro.ui.theme.DafColors,
    onDismiss: () -> Unit,
    currentThemeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    fontSizeMultiplier: Float = 1f,
    onFontSizeChange: (Float) -> Unit = {}
) {
    var selectedTheme by remember { mutableStateOf(currentThemeMode) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(280.dp)
                .background(dafColors.background)
                .padding(24.dp)
                .clickable(enabled = false) {}
        ) {
            Text(
                text = "Settings",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = dafColors.textPrimary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "APPEARANCE",
                fontSize = 12.sp,
                fontWeight = FontWeight.W500,
                color = dafColors.textSecondary,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            ThemeOption(
                label = "Off (Light)",
                isSelected = selectedTheme == ThemeMode.OFF,
                dafColors = dafColors,
                onClick = {
                    selectedTheme = ThemeMode.OFF
                    onThemeModeChange(ThemeMode.OFF)
                }
            )

            ThemeOption(
                label = "On (Dark)",
                isSelected = selectedTheme == ThemeMode.ON,
                dafColors = dafColors,
                onClick = {
                    selectedTheme = ThemeMode.ON
                    onThemeModeChange(ThemeMode.ON)
                }
            )

            ThemeOption(
                label = "Auto (time-based)",
                isSelected = selectedTheme == ThemeMode.AUTO,
                dafColors = dafColors,
                onClick = {
                    selectedTheme = ThemeMode.AUTO
                    onThemeModeChange(ThemeMode.AUTO)
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "TEXT SIZE",
                fontSize = 12.sp,
                fontWeight = FontWeight.W500,
                color = dafColors.textSecondary,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FontSizeButton(
                    label = "A",
                    isSelected = fontSizeMultiplier == 1f,
                    dafColors = dafColors,
                    onClick = { onFontSizeChange(1f) },
                    modifier = Modifier.weight(1f)
                )
                FontSizeButton(
                    label = "A",
                    isSelected = fontSizeMultiplier == 1.2f,
                    dafColors = dafColors,
                    onClick = { onFontSizeChange(1.2f) },
                    modifier = Modifier.weight(1f),
                    scale = 1.2f
                )
                FontSizeButton(
                    label = "A",
                    isSelected = fontSizeMultiplier == 1.4f,
                    dafColors = dafColors,
                    onClick = { onFontSizeChange(1.4f) },
                    modifier = Modifier.weight(1f),
                    scale = 1.4f
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Daf Yomi Pro",
                fontSize = 11.sp,
                color = dafColors.textSecondary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun ThemeOption(
    label: String,
    isSelected: Boolean,
    dafColors: com.dafyomi.pro.ui.theme.DafColors,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            color = if (isSelected) dafColors.sky else dafColors.textPrimary
        )

        if (isSelected) {
            Canvas(modifier = Modifier.size(20.dp)) {
                drawCircle(
                    color = dafColors.sky,
                    radius = size.minDimension / 2
                )
                drawCircle(
                    color = dafColors.background,
                    radius = size.minDimension / 4
                )
            }
        }
    }
}

@Composable
private fun FontSizeButton(
    label: String,
    isSelected: Boolean,
    dafColors: com.dafyomi.pro.ui.theme.DafColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    scale: Float = 1f
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .background(
                color = if (isSelected) dafColors.sky else dafColors.backgroundSecondary,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = (14 * scale).sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            color = if (isSelected) dafColors.background else dafColors.textPrimary
        )
    }
}

@Composable
private fun LoadingState(dafColors: com.dafyomi.pro.ui.theme.DafColors, message: String = "Loading...") {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = dafColors.sky,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                color = dafColors.textSecondary
            )
        }
    }
}

@Composable
private fun ErrorState(error: String, dafColors: com.dafyomi.pro.ui.theme.DafColors) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = error.ifEmpty { "An error occurred" },
            color = dafColors.stone,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(32.dp)
        )
    }
}

// ============================================================================
// MAIN CONTENT
// ============================================================================

@Composable
private fun DafContent(daf: DafData, dafColors: com.dafyomi.pro.ui.theme.DafColors, fontSizeMultiplier: Float = 1f) {
    var showEnglish by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Section 1: Current Daf (large typography focus) - font size stays fixed
        DafSection(daf = daf, dafColors = dafColors)

        Spacer(modifier = Modifier.height(32.dp))

        // 1px divider
        HorizontalDivider(dafColors)

        Spacer(modifier = Modifier.height(32.dp))

        // Section 2: Hebrew text with toggle for English
        SummarySection(
            daf = daf,
            dafColors = dafColors,
            showEnglish = showEnglish,
            onToggleLanguage = { showEnglish = showEnglish.not() },
            fontSizeMultiplier = fontSizeMultiplier
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 1px divider
        HorizontalDivider(dafColors)

        Spacer(modifier = Modifier.height(32.dp))

        // Section 3: Share
        ShareSection(daf = daf, dafColors = dafColors)

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
private fun HorizontalDivider(dafColors: com.dafyomi.pro.ui.theme.DafColors) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(dafColors.divider)
    )
}

// ============================================================================
// BACKGROUND ANIMATION
// ============================================================================

@Composable
private fun BackgroundAnimation(dafColors: com.dafyomi.pro.ui.theme.DafColors) {
    val infiniteTransition = rememberInfiniteTransition(label = "background")

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase"
    )

    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase2"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val duneHeight = height * 0.33f
        val waveAmplitude = height * 0.025f

        // Dune 1
        val path1 = Path().apply {
            moveTo(0f, height)
            moveTo(0f, height - duneHeight * 0.6f)
            for (x in 0..width.toInt() step 10) {
                val y = height - duneHeight * 0.6f +
                        sin((x / width * 2 * Math.PI + phase * 0.5).toFloat()) * waveAmplitude
                lineTo(x.toFloat(), y)
            }
            lineTo(width, height)
            close()
        }
        drawPath(path = path1, color = dafColors.sand.copy(alpha = 0.25f))

        // Dune 2
        val path2 = Path().apply {
            moveTo(0f, height)
            for (x in 0..width.toInt() step 10) {
                val y = height - duneHeight * 0.4f +
                        sin((x / width * 3 * Math.PI + phase2 * 0.7 + 1).toFloat()) * waveAmplitude * 0.8f
                lineTo(x.toFloat(), y)
            }
            lineTo(width, height)
            close()
        }
        drawPath(path = path2, color = dafColors.stone.copy(alpha = 0.2f))

        // Dune 3
        val path3 = Path().apply {
            moveTo(0f, height)
            for (x in 0..width.toInt() step 10) {
                val y = height - duneHeight * 0.2f +
                        sin((x / width * 4 * Math.PI + (phase + phase2) * 0.3).toFloat()) * waveAmplitude * 0.6f
                lineTo(x.toFloat(), y)
            }
            lineTo(width, height)
            close()
        }
        drawPath(path = path3, color = dafColors.stoneMuted.copy(alpha = 0.18f))
    }
}

// ============================================================================
// DAF SECTION
// ============================================================================

@Composable
private fun DafSection(daf: DafData, dafColors: com.dafyomi.pro.ui.theme.DafColors) {
    val hebrewDate = remember(daf) { HebrewDateFormatter.formatHebrewDate(LocalDate.now()) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Hebrew date - with more breathing room
        if (hebrewDate.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = hebrewDate,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.Serif,
                color = dafColors.textSecondary,
                textAlign = TextAlign.Center,
                letterSpacing = 1.0.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Hebrew name - Display typography (fixed size)
        Text(
            text = daf.masechet.hebrew,
            fontSize = 44.sp,
            fontWeight = FontWeight.Light,
            fontFamily = FontFamily.Serif,
            color = dafColors.textPrimary,
            textAlign = TextAlign.Center,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        // English transliteration (fixed size)
        Text(
            text = daf.masechet.transliteration,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            color = dafColors.textSecondary,
            textAlign = TextAlign.Center,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Daf number - PROMINENT (fixed size)
        Text(
            text = "${daf.dafNumber}",
            fontSize = 64.sp,
            fontWeight = FontWeight.Light,
            color = dafColors.sky,
            textAlign = TextAlign.Center,
            letterSpacing = (-1.5).sp
        )

        Text(
            text = "of ${daf.masechet.dafCount}",
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            color = dafColors.textSecondary,
            textAlign = TextAlign.Center,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Cycle progress (fixed size)
        Text(
            text = "Day ${daf.cycleDay} of 2,711",
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            color = dafColors.textSecondary,
            textAlign = TextAlign.Center,
            letterSpacing = 0.3.sp
        )
    }
}

// ============================================================================
// TEXT SECTION
// ============================================================================

@Composable
private fun SummarySection(
    daf: DafData,
    dafColors: com.dafyomi.pro.ui.theme.DafColors,
    showEnglish: Boolean,
    onToggleLanguage: () -> Unit,
    fontSizeMultiplier: Float = 1f
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Label - "דף" meaning "Page"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "דף",
                fontSize = 12.sp,
                fontWeight = FontWeight.W500,
                color = dafColors.textSecondary,
                letterSpacing = 0.5.sp
            )

            // Tap for English toggle
            if (daf.englishText != null) {
                Text(
                    text = if (showEnglish) "tap for עברית" else "tap for English",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = dafColors.sky,
                    letterSpacing = 0.3.sp,
                    modifier = Modifier
                        .clickable { onToggleLanguage() }
                        .padding(bottom = 12.dp)
                )
            }
        }

        // Hebrew or English body text
        val displayText = if (showEnglish) {
            daf.englishText ?: daf.summary
        } else {
            daf.hebrewText ?: daf.summary
        }

        Text(
            text = displayText,
            fontSize = (18 * fontSizeMultiplier).sp,
            fontWeight = FontWeight.Normal,
            fontFamily = FontFamily.Serif,
            color = dafColors.textPrimary,
            lineHeight = (30 * fontSizeMultiplier).sp,
            textAlign = TextAlign.Start,
            letterSpacing = 0.2.sp,
            modifier = Modifier.clickable { onToggleLanguage() }
        )
    }
}

// ============================================================================
// SHARE SECTION
// ============================================================================

@Composable
private fun ShareSection(daf: DafData, dafColors: com.dafyomi.pro.ui.theme.DafColors) {
    val context = LocalContext.current

    val shareText = buildString {
        append("Today's Daf Yomi: ")
        append(daf.masechet.transliteration)
        append(" ")
        append(daf.dafNumber)
        append("\n\n")
        append("\"")
        append(daf.hebrewText ?: daf.summary)
        append("\"")
        append("\n\n")
        append("Cycle day ${daf.cycleDay} of 2,711")
        append("\n")
        append("Shared via Daf Yomi Pro")
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "SHARE TODAY'S DAF",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = dafColors.textSecondary,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = dafColors.divider,
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable {
                    try {
                        val intent = android.content.Intent().apply {
                            action = android.content.Intent.ACTION_SEND
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(android.content.Intent.createChooser(intent, "Share via"))
                    } catch (e: Exception) {
                        // Silently handle if no app can receive the share
                    }
                }
                .padding(vertical = 16.dp, horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Share with friends",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = dafColors.textPrimary
                    )
                    Text(
                        text = "Spread the Torah learning",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = dafColors.textSecondary
                    )
                }

                Canvas(modifier = Modifier.size(32.dp)) {
                    val strokeWidth = 2.5.dp.toPx()
                    drawLine(
                        color = dafColors.textSecondary,
                        start = Offset(8f, 22f),
                        end = Offset(22f, 8f),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = dafColors.textSecondary,
                        start = Offset(22f, 8f),
                        end = Offset(22f, 16f),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = dafColors.textSecondary,
                        start = Offset(22f, 8f),
                        end = Offset(14f, 8f),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}
