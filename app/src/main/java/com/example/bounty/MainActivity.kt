package com.example.bounty

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bounty.data.Bounty
import com.example.bounty.data.BountyDatabase
import com.example.bounty.data.BountyRepository
import com.example.bounty.ui.BountyViewModel
import com.example.bounty.ui.components.FlowLayout
import com.example.bounty.ui.components.ProgressRing
import com.example.bounty.ui.components.TagChip
import com.example.bounty.ui.components.shimmerLoading
import com.example.bounty.ui.theme.BountyTheme
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // One database instance for the app's lifetime; the repository just wraps its DAO.
        val database = BountyDatabase.getInstance(applicationContext)
        val repository = BountyRepository(database.bountyDao())

        setContent {
            BountyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val viewModel: BountyViewModel = viewModel(factory = BountyViewModel.factory(repository))
                    BountyBoardScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun BountyBoardScreen(viewModel: BountyViewModel, modifier: Modifier = Modifier) {
    val bounties by viewModel.bounties.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        // GridCells.Adaptive picks the column count from available width at runtime —
        // 1 column on a narrow phone, 2-3+ on a wider phone/landscape/tablet. No breakpoints
        // or screen-size checks written by us; Compose measures and decides every recomposition.
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 320.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isLoading) {
                // Task D: "the guild posting bounties" — shimmering skeleton shapes while
                // Room's first query is still in flight. These placeholders disappear for
                // good the moment isLoading flips false; nothing here keeps animating after.
                items(6) {
                    ShimmerBountyPlaceholder()
                }
            } else {
                items(bounties, key = { it.id }) { bounty ->
                    SwipeableBountyCard(
                        bounty = bounty,
                        onTap = { viewModel.advanceProgress(bounty) },
                        onAccept = { viewModel.acceptBounty(bounty) },
                        onAbandon = { viewModel.abandonBounty(bounty) }
                    )
                }
            }
        }
    }
}

@Composable
fun ShimmerBountyPlaceholder(modifier: Modifier = Modifier) {
    // Same shape/clip/background/padding chain as the real BountyCard, plus shimmerLoading
    // and a disabled clickable — this is the proof that the modifier composes cleanly
    // alongside padding, clip, and clickable rather than requiring special-cased ordering.
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(enabled = false, onClick = {})
            .shimmerLoading(isLoading = true)
            .padding(16.dp)
    ) {
        // Intentionally empty — the shimmering rounded rect itself is the placeholder.
    }
}

@Composable
fun SwipeableBountyCard(
    bounty: Bounty,
    onTap: () -> Unit,
    onAccept: () -> Unit,
    onAbandon: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val thresholdPx = with(density) { 120.dp.toPx() }
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var showAbandonConfirm by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth()) {
        // Revealed behind the card as it's dragged — green hints "accept", red hints "abandon".
        Row(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    when {
                        offsetX.value > 0f -> Color(0xFF66BB6A)
                        offsetX.value < 0f -> Color(0xFFE57373)
                        else -> Color.Transparent
                    }
                )
                .padding(horizontal = 20.dp),
            horizontalArrangement = if (offsetX.value >= 0f) Arrangement.Start else Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (offsetX.value != 0f) {
                Text(
                    text = if (offsetX.value > 0f) "Accept" else "Abandon",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        BountyCard(
            bounty = bounty,
            onTap = onTap,
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(bounty.id) {
                    // Raw pointer input, not SwipeToDismiss: we track the drag ourselves via
                    // Animatable so we control both the live offset and the snap-back animation.
                    detectDragGestures(
                        onDragEnd = {
                            // TEMP DEBUG: check Logcat (filter tag "BountyDrag") to see exactly
                            // which branch fires and what the raw offset/threshold values were —
                            // remove these Log.d calls once accept is confirmed working.
                            Log.d("BountyDrag", "onDragEnd offsetX=${offsetX.value} thresholdPx=$thresholdPx")
                            when {
                                offsetX.value > thresholdPx -> {
                                    Log.d("BountyDrag", "ACCEPT branch fired for ${bounty.title}")
                                    scope.launch {
                                        onAccept() // persists to Room via the ViewModel/DAO
                                        offsetX.animateTo(0f, tween(200))
                                    }
                                }
                                offsetX.value < -thresholdPx -> {
                                    Log.d("BountyDrag", "ABANDON branch fired for ${bounty.title}")
                                    showAbandonConfirm = true
                                    scope.launch { offsetX.animateTo(0f, tween(200)) }
                                }
                                else -> {
                                    Log.d("BountyDrag", "Below threshold, snapping back")
                                    // Didn't cross either threshold — smooth snap back, no jump cut.
                                    scope.launch { offsetX.animateTo(0f, tween(200)) }
                                }
                            }
                        },
                        onDragCancel = {
                            scope.launch { offsetX.animateTo(0f, tween(200)) }
                        }
                    ) { change, dragAmount ->
                        change.consume()
                        scope.launch { offsetX.snapTo(offsetX.value + dragAmount.x) }
                    }
                }
        )
    }

    if (showAbandonConfirm) {
        AlertDialog(
            onDismissRequest = { showAbandonConfirm = false },
            title = { Text("Abandon bounty?") },
            text = { Text("\"${bounty.title}\" will be removed from the board. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showAbandonConfirm = false
                    onAbandon() // deletes the row via the ViewModel/DAO; the Flow drops it from the list
                }) { Text("Abandon") }
            },
            dismissButton = {
                TextButton(onClick = { showAbandonConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun BountyCard(bounty: Bounty, onTap: () -> Unit, modifier: Modifier = Modifier) {
    // Order matters here: clip first so the ripple from clickable is bounded by the
    // rounded corners, background fills the clipped shape, clickable adds the tap
    // affordance, padding last so it only shrinks the *content* area, not the tap target.
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            // Visibly distinct background once accepted — no more relying on a small text
            // change to notice the state actually flipped.
            .background(
                color = if (bounty.isAccepted) {
                    Color(0xFFDCEDC8)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
            .clickable(onClick = onTap)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = bounty.title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "${bounty.rewardGold} gold" + if (bounty.isAccepted) " ✓ Accepted" else "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Box(modifier = Modifier.padding(top = 8.dp)) {
                FlowLayout(horizontalSpacing = 8.dp, verticalSpacing = 8.dp) {
                    bounty.tags.forEach { tag -> TagChip(text = tag) }
                }
            }
        }

        // Task C: animated Canvas ring, reads straight from Room via bounty.progress.
        // Tapping the card (see onTap above) advances progress and persists it; the ring
        // then animates from its old value to the new one as the next Flow emission arrives.
        ProgressRing(
            progress = bounty.progress,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}