package com.example.bounty.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A circular objective-progress ring, replacing the default progress bar.
 *
 * [progress] should come straight from Room (via the ViewModel) — this composable never
 * mutates it. Whenever [progress] changes to a new value (e.g. after a Flow re-emission
 * following a write), [LaunchedEffect] retargets the underlying [Animatable] so the ring
 * tweens smoothly from its old value to the new one instead of jumping.
 */
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    ringSize: Dp = 48.dp,
    strokeWidth: Dp = 5.dp,
    lowColor: Color = Color(0xFFE57373),
    highColor: Color = Color(0xFF66BB6A)
) {
    val animatedProgress = remember { Animatable(progress.coerceIn(0f, 1f)) }

    LaunchedEffect(progress) {
        animatedProgress.animateTo(
            targetValue = progress.coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 500)
        )
    }

    // Color transitions along with the same animated value, so it eases toward highColor
    // as the ring fills rather than snapping the instant progress crosses some threshold.
    val ringColor = lerp(lowColor, highColor, animatedProgress.value)

    Box(modifier = modifier.size(ringSize), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(ringSize)) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)

            // Faint full-circle track so the ring's total extent is always visible.
            drawArc(
                color = ringColor.copy(alpha = 0.2f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = stroke
            )
            // The actual progress arc, sized to the animated (not raw) fraction.
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress.value,
                useCenter = false,
                style = stroke
            )
        }
        Text(
            text = "${(animatedProgress.value * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall
        )
    }
}