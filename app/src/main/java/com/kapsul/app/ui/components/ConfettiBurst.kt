package com.kapsul.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private class ConfettiPiece(
    val angle: Float,
    val distance: Float,
    val size: Float,
    val color: Color,
    val spin: Float,
    val delay: Float,
    val round: Boolean,
)

/**
 * Mühür kırılma anının kutlaması: merkezden fışkıran, yerçekimiyle düşen
 * ve solan konfeti parçacıkları. [Modifier] boyutu kutlamanın alanıdır.
 *
 * Yeni composed olduğunda animasyon sıfırdan başlar; 1.5 sn sonra bileşen
 * ebeveyni tarafından gizlenmelidir.
 */
@Composable
fun ConfettiBurst(modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val palette = listOf(
        scheme.secondary,
        scheme.tertiary,
        scheme.primary,
        scheme.secondaryContainer,
        scheme.primaryContainer,
        scheme.tertiaryContainer,
    )

    val pieces = remember {
        val random = Random(2026)
        List(48) { index ->
            ConfettiPiece(
                angle = random.nextFloat() * 2f * Math.PI.toFloat(),
                distance = 0.35f + random.nextFloat() * 0.65f,
                size = (5 + random.nextInt(6)).dp.value,
                color = palette[index % palette.size],
                spin = if (random.nextBoolean()) 1f else -1f,
                delay = random.nextFloat() * 0.18f,
                round = random.nextBoolean(),
            )
        }
    }

    val anim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        anim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        )
    }
    val t = anim.value

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxDistance = size.minDimension * 0.48f

        pieces.forEach { piece ->
            val local = ((t - piece.delay) / (1f - piece.delay)).coerceIn(0f, 1f)
            if (local <= 0f) return@forEach

            val travel = piece.distance * maxDistance * local
            val x = center.x + cos(piece.angle) * travel
            val y = center.y + sin(piece.angle) * travel +
                (local * local) * size.height * 0.30f
            val alpha = ((1f - local) * (local * 6f).coerceAtMost(1f)).coerceIn(0f, 1f)
            val px = piece.size.dp.toPx() * (0.6f + 0.4f * (local * 4f).coerceAtMost(1f))

            withTransform({
                rotate(degrees = piece.spin * local * 420f, pivot = Offset(x, y))
            }) {
                if (piece.round) {
                    drawCircle(
                        color = piece.color,
                        radius = px / 2f,
                        center = Offset(x, y),
                        alpha = alpha,
                    )
                } else {
                    drawRoundRect(
                        color = piece.color,
                        topLeft = Offset(x - px / 2f, y - px / 3f),
                        size = Size(px, px * 0.66f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(px * 0.2f),
                        alpha = alpha,
                    )
                }
            }
        }
    }
}
