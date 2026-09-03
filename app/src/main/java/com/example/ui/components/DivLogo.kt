package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DivCyan
import com.example.ui.theme.DivGradientBrand
import com.example.ui.theme.DivGradientPurpleCyan
import com.example.ui.theme.DivPink
import com.example.ui.theme.DivPurple
import com.example.ui.theme.DivPurpleLight

@Composable
fun DivLogo(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    showTagline: Boolean = false,
    showSubtitle: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "logo_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Row(
        modifier = modifier.testTag("div_song_ai_logo"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Icon Box with Neon Soundwave & Note Ring
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF1E1038), Color(0xFF0F0F1A))
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.sweepGradient(
                        listOf(DivPurple, DivPink, DivCyan, DivPurple)
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(size * 0.85f)) {
                // Outer subtle ring
                drawCircle(
                    color = DivPurple.copy(alpha = glowAlpha * 0.4f),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "Music Note",
                    tint = DivPink,
                    modifier = Modifier.size(size * 0.48f)
                )
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = "Sound Wave",
                    tint = DivCyan,
                    modifier = Modifier.size(size * 0.45f)
                )
            }
        }

        Column {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "D",
                    fontWeight = FontWeight.Black,
                    fontSize = (size.value * 0.55).sp,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "I",
                    fontWeight = FontWeight.Black,
                    fontSize = (size.value * 0.55).sp,
                    color = DivPink,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "V",
                    fontWeight = FontWeight.Black,
                    fontSize = (size.value * 0.55).sp,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
                if (showSubtitle) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SONG AI",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = (size.value * 0.42).sp,
                        color = DivCyan,
                        letterSpacing = 2.sp
                    )
                }
            }
            if (showTagline) {
                Text(
                    text = "YOUR SONG. YOUR SOUND. YOUR STUDIO.",
                    fontWeight = FontWeight.Bold,
                    fontSize = 8.sp,
                    color = Color(0xFFA0A0C8),
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
