package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DivCyan
import com.example.ui.theme.DivPink
import com.example.ui.theme.DivPurple
import kotlin.math.sin

@Composable
fun WaveformVisualizer(
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    progress: Float = 0f,
    barCount: Int = 36,
    height: Dp = 48.dp,
    onSeek: ((Float) -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    // Pre-calculate pseudo-random harmonic heights for the wave pattern
    val baseHeights = remember(barCount) {
        List(barCount) { i ->
            val norm = i.toFloat() / barCount
            val h = 0.35f + 0.45f * sin(norm * 3.1415f) + 0.2f * sin(norm * 12.56f)
            h.coerceIn(0.15f, 1.0f)
        }
    }

    val activeBrush = Brush.verticalGradient(
        colors = listOf(DivPink, DivPurple, DivCyan)
    )
    val inactiveColor = Color(0xFF282845)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .pointerInput(onSeek) {
                if (onSeek != null) {
                    detectTapGestures { offset ->
                        val tappedRatio = (offset.x / size.width).coerceIn(0f, 1f)
                        onSeek(tappedRatio)
                    }
                }
            }
    ) {
        val totalWidth = size.width
        val canvasHeight = size.height
        val barWidth = (totalWidth / barCount) * 0.65f
        val gap = (totalWidth / barCount) * 0.35f

        for (i in 0 until barCount) {
            val x = i * (barWidth + gap)
            val barRatio = i.toFloat() / barCount
            val isPassed = barRatio <= progress

            // Dynamic oscillation when playing
            val dynamicScale = if (isPlaying) {
                0.8f + 0.35f * sin(phase + i * 0.4f)
            } else {
                1.0f
            }

            val barH = (canvasHeight * baseHeights[i] * dynamicScale).coerceIn(4.dp.toPx(), canvasHeight)
            val topY = (canvasHeight - barH) / 2f

            drawRoundRect(
                brush = if (isPassed) activeBrush else Brush.linearGradient(listOf(inactiveColor, inactiveColor)),
                topLeft = Offset(x, topY),
                size = Size(barWidth, barH),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}
