package com.example.bounty.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * A standalone, reusable shimmer effect. Composes cleanly with padding/clip/clickable —
 * order it wherever in the modifier chain you'd order a decorative draw modifier.
 *
 * The animation only exists while [isLoading] is true: [rememberInfiniteTransition] is called
 * conditionally inside [composed], so the moment [isLoading] flips false, Compose disposes that
 * subtree — the infinite animation's underlying coroutine is cancelled, not just hidden. Nothing
 * keeps running in the background after loading finishes.
 */
fun Modifier.shimmerLoading(isLoading: Boolean): Modifier = composed {
    if (!isLoading) return@composed this

    val transition = rememberInfiniteTransition(label = "shimmerLoading")
    val translateAnim = transition.animateFloat(
        initialValue = -400f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.3f),
        Color.LightGray.copy(alpha = 0.9f),
        Color.LightGray.copy(alpha = 0.3f)
    )

    this.drawWithContent {
        drawContent()
        // BlendMode.SrcAtop composites the gradient only over pixels drawContent already drew —
        // so the shimmer stays inside whatever shape (rounded card, chip, etc.) is being drawn,
        // rather than painting a rectangle across the whole layout bounds.
        drawRect(
            brush = Brush.linearGradient(
                colors = shimmerColors,
                start = Offset(translateAnim.value, 0f),
                end = Offset(translateAnim.value + 300f, size.height)
            ),
            blendMode = BlendMode.SrcAtop
        )
    }
}