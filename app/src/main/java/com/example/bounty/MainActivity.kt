package com.example.bounty

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bounty.data.Bounty
import com.example.bounty.data.BountyDatabase
import com.example.bounty.data.BountyRepository
import com.example.bounty.ui.BountyViewModel
import com.example.bounty.ui.components.FlowLayout
import com.example.bounty.ui.components.TagChip
import com.example.bounty.ui.theme.BountyTheme
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
        if (isLoading) {
            // Placeholder for now — Task D replaces this with shimmering card outlines.
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(bounties, key = { it.id }) { bounty ->
                    BountyCard(bounty = bounty)
                }
            }
        }
    }
}

@Composable
fun BountyCard(bounty: Bounty, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Text(text = bounty.title, style = MaterialTheme.typography.titleMedium)
        Text(
            text = "${bounty.rewardGold} gold" + if (bounty.isAccepted) " • Accepted" else "",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Box(modifier = Modifier.padding(top = 8.dp)) {
            FlowLayout(horizontalSpacing = 8.dp, verticalSpacing = 8.dp) {
                bounty.tags.forEach { tag -> TagChip(text = tag) }
            }
        }

        // Plain text stand-in for now — Task C replaces this with the animated Canvas ring.
        Text(
            text = "Progress: ${(bounty.progress * 100).toInt()}%",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}