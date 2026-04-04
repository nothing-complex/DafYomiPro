package com.dafyomi.pro.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dafyomi.pro.R
import com.dafyomi.pro.domain.DafData
import com.dafyomi.pro.ui.theme.Gold
import com.dafyomi.pro.ui.theme.LightGold
import com.dafyomi.pro.ui.theme.Primary
import com.dafyomi.pro.ui.theme.ProgressTrack
import com.dafyomi.pro.ui.theme.Transliteration

@Composable
fun DafScreen(viewModel: DafViewModel) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDF8F3))
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Primary
            )
        } else if (state.error != null) {
            val errorMessage = state.error?.ifEmpty { "An unknown error occurred" } ?: "An unknown error occurred"
            Text(
                text = errorMessage,
                color = Color.Red,
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (state.daf != null) {
            DafContent(daf = state.daf!!)
        } else {
            Text(
                text = "Loading...",
                color = Color.Gray,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun DafContent(daf: DafData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        ProgressIndicator(
            cycleDay = daf.cycleDay,
            totalDays = 2711,
            percent = daf.cyclePercent
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${daf.cycleDay} of 2,711",
                    fontSize = 14.sp,
                    color = Transliteration,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = daf.masechet.hebrew,
                    fontSize = 48.sp,
                    fontFamily = FontFamily.Serif,
                    color = Color(0xFF2D2A26),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = daf.masechet.transliteration,
                    fontSize = 20.sp,
                    color = Primary,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = daf.masechet.pronunciation,
                    fontSize = 14.sp,
                    color = Transliteration,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Daf ${daf.dafNumber}",
                    fontSize = 18.sp,
                    color = Color(0xFF5C5550),
                    fontWeight = FontWeight.Normal
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Gold)
                    )
                    Text(
                        text = "  Summary",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = daf.summary,
                    fontSize = 16.sp,
                    lineHeight = 26.sp,
                    color = Color(0xFF3D3A36),
                    textAlign = TextAlign.Start
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        ShareCard(daf = daf)

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ProgressIndicator(
    cycleDay: Int,
    totalDays: Int,
    percent: Float
) {
    var animatedPercent by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(percent) {
        animatedPercent = percent
    }

    val animatedValue by animateFloatAsState(
        targetValue = animatedPercent,
        animationSpec = tween(durationMillis = 1500),
        label = "progress"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(120.dp)
        ) {
            Canvas(modifier = Modifier.size(120.dp)) {
                val strokeWidth = 8.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2

                drawCircle(
                    color = ProgressTrack,
                    radius = radius,
                    style = Stroke(width = strokeWidth)
                )

                drawArc(
                    color = Gold,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedValue,
                    useCenter = false,
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(size.width - strokeWidth, size.height - strokeWidth),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${(animatedValue * 100).toInt()}%",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Cycle Progress",
            fontSize = 12.sp,
            color = Transliteration
        )
    }
}

@Composable
private fun ShareCard(daf: DafData) {
    val context = LocalContext.current

    val shareText = buildString {
        append("Today's Daf Yomi: ")
        append(daf.masechet.transliteration)
        append(" ")
        append(daf.dafNumber)
        append("\n")
        append("\"")
        append(daf.summary)
        append("\"")
        append("\n\n")
        append("Cycle day ${daf.cycleDay} of 2,711 (${(daf.cyclePercent * 100).toInt()}%)")
        append("\n")
        append("Shared via Daf Yomi Pro")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LightGold),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Share Today's Daf",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary
                )
                Text(
                    text = "Share with friends & family",
                    fontSize = 12.sp,
                    color = Transliteration
                )
            }

            IconButton(
                onClick = {
                    try {
                        val intent = android.content.Intent().apply {
                            action = android.content.Intent.ACTION_SEND
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(android.content.Intent.createChooser(intent, "Share via"))
                    } catch (e: Exception) {
                        // Silently fail if no app can handle share intent
                        // This prevents crash on devices without share capability
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Primary)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_share),
                    contentDescription = "Share",
                    tint = Color.White
                )
            }
        }
    }
}
