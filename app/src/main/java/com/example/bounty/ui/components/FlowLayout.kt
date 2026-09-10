package com.example.bounty.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp

/**
 * Wraps [content] onto as many rows as needed, left-to-right, top-to-bottom — no Row/Column
 * nesting, no LazyRow. Reusable outside the bounty screen: just a Layout + spacing knobs.
 *
 * @param horizontalSpacing gap between chips on the same row
 * @param verticalSpacing   gap between rows
 */
@Composable
fun FlowLayout(
    modifier: Modifier = Modifier,
    horizontalSpacing: Dp = 8.dp,
    verticalSpacing: Dp = 8.dp,
    content: @Composable () -> Unit
) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        val hSpacingPx = horizontalSpacing.roundToPx()
        val vSpacingPx = verticalSpacing.roundToPx()

        // Loose constraints: each child reports its own natural (intrinsic) size, capped
        // at the row width so a single oversized chip can't blow past the parent.
        val childConstraints = Constraints(
            minWidth = 0,
            minHeight = 0,
            maxWidth = constraints.maxWidth,
            maxHeight = constraints.maxHeight
        )
        val placeables = measurables.map { it.measure(childConstraints) }

        // First pass: decide which row each child lands on and its (x, y) within the layout.
        data class Placement(val placeable: Placeable, val x: Int, val y: Int)

        val placements = mutableListOf<Placement>()
        var rowX = 0
        var rowY = 0
        var rowMaxHeight = 0
        var maxRowWidthUsed = 0

        for (placeable in placeables) {
            val wouldOverflow = rowX != 0 && rowX + placeable.width > constraints.maxWidth
            if (wouldOverflow) {
                // Wrap: commit this row's width, advance y past it + vertical spacing, reset x.
                maxRowWidthUsed = maxOf(maxRowWidthUsed, rowX - hSpacingPx)
                rowY += rowMaxHeight + vSpacingPx
                rowX = 0
                rowMaxHeight = 0
            }

            placements += Placement(placeable, rowX, rowY)
            rowX += placeable.width + hSpacingPx
            rowMaxHeight = maxOf(rowMaxHeight, placeable.height)
        }
        maxRowWidthUsed = maxOf(maxRowWidthUsed, rowX - hSpacingPx)

        val totalHeight = rowY + rowMaxHeight
        val totalWidth = if (constraints.hasBoundedWidth) constraints.maxWidth else maxRowWidthUsed.coerceAtLeast(0)

        layout(totalWidth, totalHeight.coerceAtLeast(0)) {
            placements.forEach { (placeable, x, y) ->
                placeable.placeRelative(x, y)
            }
        }
    }
}

/** A single tag chip, styled to sit inside [FlowLayout]. */
@Composable
fun TagChip(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 12.dp, vertical = 4.dp)
    )
}