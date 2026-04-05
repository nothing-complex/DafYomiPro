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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dafyomi.pro.domain.DafData
import com.dafyomi.pro.ui.theme.LocalDafColors
import com.dafyomi.pro.ui.theme.ThemeMode
import kotlin.math.sin
import kotlin.random.Random

// ============================================================================
// MINIMALIST FLAT DESIGN - SAND, STONE & SKY
// ============================================================================

@Composable
fun DafScreen(
    viewModel: DafViewModel,
    onThemeModeChange: (ThemeMode) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val dafColors = LocalDafColors.current
    var showSettings by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(dafColors.gradientBrush)
    ) {
        // Background animation (non-interactive - draws behind)
        BackgroundAnimation(dafColors = dafColors)

        // Settings icon - separate Box to ensure touch works
        SettingsIcon(
            dafColors = dafColors,
            onClick = { showSettings = true }
        )

        when {
            state.isLoading -> {
                LoadingState(dafColors)
            }
            state.error != null -> {
                ErrorState(state.error ?: "", dafColors)
            }
            state.daf != null -> {
                DafContent(daf = state.daf!!, dafColors = dafColors)
            }
            else -> {
                LoadingState(dafColors)
            }
        }

        // Settings dialog
        if (showSettings) {
            SettingsDialog(
                dafColors = dafColors,
                onDismiss = { showSettings = false },
                onThemeModeChange = onThemeModeChange
            )
        }
    }
}

@Composable
private fun SettingsIcon(
    dafColors: com.dafyomi.pro.ui.theme.DafColors,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        // Use text symbol for hamburger - more reliable touch
        Text(
            text = "☰",
            fontSize = 24.sp,
            color = dafColors.textSecondary,
            modifier = Modifier
                .size(44.dp)
                .clickable { onClick() }
        )
    }
}

@Composable
private fun SettingsDialog(
    dafColors: com.dafyomi.pro.ui.theme.DafColors,
    onDismiss: () -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit
) {
    var currentTheme by remember { mutableStateOf(ThemeMode.AUTO) }

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
                .clickable(enabled = false) {} // Prevent dismiss on content click
        ) {
            Text(
                text = "Settings",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = dafColors.textPrimary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Appearance",
                fontSize = 12.sp,
                fontWeight = FontWeight.W500,
                color = dafColors.textSecondary,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            ThemeOption(
                label = "Off (Light)",
                isSelected = currentTheme == ThemeMode.OFF,
                dafColors = dafColors,
                onClick = {
                    currentTheme = ThemeMode.OFF
                    onThemeModeChange(ThemeMode.OFF)
                }
            )

            ThemeOption(
                label = "On (Dark)",
                isSelected = currentTheme == ThemeMode.ON,
                dafColors = dafColors,
                onClick = {
                    currentTheme = ThemeMode.ON
                    onThemeModeChange(ThemeMode.ON)
                }
            )

            ThemeOption(
                label = "Auto (time-based)",
                isSelected = currentTheme == ThemeMode.AUTO,
                dafColors = dafColors,
                onClick = {
                    currentTheme = ThemeMode.AUTO
                    onThemeModeChange(ThemeMode.AUTO)
                }
            )

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
private fun LoadingState(dafColors: com.dafyomi.pro.ui.theme.DafColors) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = dafColors.sky,
            strokeWidth = 2.dp
        )
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
// MAIN CONTENT - Flat, Section-Based Layout
// ============================================================================

@Composable
private fun DafContent(daf: DafData, dafColors: com.dafyomi.pro.ui.theme.DafColors) {
    var showEnglish by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)  // Reduced from 40dp per research
    ) {
        Spacer(modifier = Modifier.height(24.dp))  // Reduced - status bar handled in MainActivity

        // Section 1: Current Daf (large typography focus)
        DafSection(daf = daf, dafColors = dafColors)

        Spacer(modifier = Modifier.height(32.dp))  // Reduced from 40dp

        // 1px divider
        HorizontalDivider(dafColors)

        Spacer(modifier = Modifier.height(32.dp))  // Reduced from 40dp

        // Section 2: Hebrew text with toggle for English
        SummarySection(
            daf = daf,
            dafColors = dafColors,
            showEnglish = showEnglish,
            onToggleLanguage = { showEnglish = showEnglish.not() }
        )

        Spacer(modifier = Modifier.height(32.dp))  // Reduced from 40dp

        // 1px divider
        HorizontalDivider(dafColors)

        Spacer(modifier = Modifier.height(32.dp))  // Reduced from 40dp

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
// BACKGROUND ANIMATION - Subtle sand dunes / stars
// ============================================================================

@Composable
private fun BackgroundAnimation(dafColors: com.dafyomi.pro.ui.theme.DafColors) {
    val infiniteTransition = rememberInfiniteTransition(label = "background")

    // Slow oscillation for sand dunes (light mode) or star twinkle (dark mode)
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase"
    )

    // Secondary slower phase for depth
    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase2"
    )

    // Use pointerInput to ensure touches pass through to elements below
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        if (dafColors.isDark) {
            // Dark mode: subtle twinkling stars (very faint)
            val starCount = 25
            val random = Random(42) // Fixed seed for consistent star positions
            repeat(starCount) {
                val x = random.nextFloat() * width
                val y = random.nextFloat() * height * 0.6f // Stars in upper 60%
                val baseAlpha = 0.15f + random.nextFloat() * 0.2f
                val twinkleSpeed = 0.3f + random.nextFloat() * 0.3f

                // Use sin for gentle twinkle
                val twinkle = (sin((phase * 8 + it) * twinkleSpeed) + 1f) / 2f
                val alpha = baseAlpha * (0.4f + twinkle * 0.6f)

                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = 0.8f + random.nextFloat() * 1.2f,
                    center = Offset(x, y)
                )
            }
        } else {
            // Light mode: sand dunes at bottom third (visible but subtle)
            val duneHeight = height * 0.33f  // Bottom third
            val waveAmplitude = height * 0.03f

            // Dune 1 (back layer)
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
            drawPath(
                path = path1,
                color = dafColors.sand.copy(alpha = 0.25f)  // Subtle sand tone
            )

            // Dune 2 (middle layer)
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
            drawPath(
                path = path2,
                color = dafColors.stone.copy(alpha = 0.2f)  // Stone tone
            )

            // Dune 3 (front layer)
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
            drawPath(
                path = path3,
                color = dafColors.stoneMuted.copy(alpha = 0.18f)  // Darker stone
            )
        }
    }
}

// ============================================================================
// DAF SECTION - Typography as the hero
// ============================================================================

@Composable
private fun DafSection(daf: DafData, dafColors: com.dafyomi.pro.ui.theme.DafColors) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Hebrew name - Display typography (refined size per research)
        Text(
            text = daf.masechet.hebrew,
            fontSize = 44.sp,  // Reduced from 48sp for elegance
            fontWeight = FontWeight.Light,
            fontFamily = FontFamily.Serif,
            color = dafColors.textPrimary,
            textAlign = TextAlign.Center,
            letterSpacing = 0.5.sp  // Reduced for tighter Hebrew
        )

        Spacer(modifier = Modifier.height(6.dp))

        // English transliteration
        Text(
            text = daf.masechet.transliteration,
            fontSize = 13.sp,  // Slightly smaller
            fontWeight = FontWeight.Normal,
            color = dafColors.textSecondary,
            textAlign = TextAlign.Center,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(28.dp))  // Reduced spacing

        // Daf number - PROMINENT (refined size)
        Text(
            text = "${daf.dafNumber}",
            fontSize = 64.sp,  // Reduced from 72sp
            fontWeight = FontWeight.Light,
            color = dafColors.sky,
            textAlign = TextAlign.Center,
            letterSpacing = (-1.5).sp  // Adjusted for balance
        )

        Text(
            text = "of ${daf.masechet.dafCount}",
            fontSize = 13.sp,  // Slightly smaller
            fontWeight = FontWeight.Normal,
            color = dafColors.textSecondary,
            textAlign = TextAlign.Center,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Cycle progress
        Text(
            text = "Day ${daf.cycleDay} of 2,711",
            fontSize = 11.sp,  // Smaller, more subtle
            fontWeight = FontWeight.Normal,
            color = dafColors.textSecondary,
            textAlign = TextAlign.Center,
            letterSpacing = 0.3.sp
        )
    }
}

// ============================================================================
// TEXT SECTION - Actual Hebrew text from Sefaria with proper typography
// ============================================================================

@Composable
private fun SummarySection(
    daf: DafData,
    dafColors: com.dafyomi.pro.ui.theme.DafColors,
    showEnglish: Boolean,
    onToggleLanguage: () -> Unit
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
            fontSize = 18.sp,  // Slightly reduced per research
            fontWeight = FontWeight.Normal,
            fontFamily = FontFamily.Serif,
            color = dafColors.textPrimary,
            lineHeight = 30.sp,  // 1.67x ratio for Hebrew readability
            textAlign = TextAlign.Start,
            letterSpacing = 0.2.sp,  // Slightly tighter
            modifier = Modifier.clickable { onToggleLanguage() }
        )
    }
}

// ============================================================================
// SHARE SECTION - Flat button
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

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Label
        Text(
            text = "SHARE TODAY'S DAF",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = dafColors.textSecondary,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Flat share button - subtle border, minimalist style
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = dafColors.divider,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
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
                        // Silently fail
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

                // Share icon (arrow)
                Canvas(modifier = Modifier.size(20.dp, 20.dp)) {
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(4f, 10f)
                        lineTo(16f, 10f)
                        lineTo(11f, 5f)
                        moveTo(16f, 10f)
                        lineTo(11f, 15f)
                    }
                    drawPath(path, dafColors.skyDeep, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
                }
            }
        }
    }
}
