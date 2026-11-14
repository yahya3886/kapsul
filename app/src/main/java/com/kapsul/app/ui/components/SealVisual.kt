package com.kapsul.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import kotlin.math.cos
import kotlin.math.sin

/**
 * Balmumu mühür görseli.
 *
 * - Kapalıyken: tırtıklı kenarlı balmumu mührü hafifçe sallanır (canlı his).
 * - [breaking] true: mühür büyüyerek soler — kırılma anı.
 * - Açıldıktan sonra: sade renkli daire içinde sembol kalır.
 */
@Composable
fun SealVisual(
    emoji: String,
    breaking: Boolean,
    opened: Boolean,
    modifier: Modifier = Modifier,
) {
    val motion = MaterialTheme.motionScheme
    val colorScheme = MaterialTheme.colorScheme

    val waxAlpha by animateFloatAsState(
        targetValue = if (breaking && !opened) 0f else 1f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "waxAlpha",
    )
    val waxScale by animateFloatAsState(
        targetValue = if (breaking && !opened) 1.6f else 1f,
        animationSpec = motion.defaultSpatialSpec<Float>(),
        label = "waxScale",
    )
    val boxSize by animateDpAsState(
        targetValue = if (opened) 104.dp else 184.dp,
        animationSpec = motion.defaultSpatialSpec<Dp>(),
        label = "sealSize",
    )

    val infinite = rememberInfiniteTransition(label = "sealShake")
    val shake by infinite.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "shakeAngle",
    )

    Box(
        modifier = modifier.size(boxSize),
        contentAlignment = Alignment.Center,
    ) {
        if (!opened) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
                    .graphicsLayer {
                        rotationZ = if (breaking) 0f else shake
                        scaleX = waxScale
                        scaleY = waxScale
                        alpha = waxAlpha
                    },
            ) {
                val radius = size.minDimension / 2f
                val bumpRadius = radius * 0.17f
                val bumps = 14
                val wax = colorScheme.secondary
                repeat(bumps) { i ->
                    val angle = 2.0 * Math.PI * i / bumps
                    val cx = center.x + (radius - bumpRadius * 0.55f) * cos(angle).toFloat()
                    val cy = center.y + (radius - bumpRadius * 0.55f) * sin(angle).toFloat()
                    drawCircle(color = wax, radius = bumpRadius, center = Offset(cx, cy))
                }
                drawCircle(color = wax, radius = radius, center = center)
                drawCircle(
                    color = colorScheme.onSecondary.copy(alpha = 0.25f),
                    radius = radius * 0.66f,
                    center = center,
                    style = Stroke(width = radius * 0.07f),
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .clip(CircleShape)
                    .background(colorScheme.secondaryContainer),
            )
        }

        Text(
            text = emoji,
            fontSize = if (opened) 42.sp else 54.sp,
            modifier = Modifier.graphicsLayer { alpha = waxAlpha },
        )
    }
}
