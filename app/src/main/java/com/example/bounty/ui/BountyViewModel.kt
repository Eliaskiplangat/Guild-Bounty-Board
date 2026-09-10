package com.example.bounty.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bounty.data.Bounty
import com.example.bounty.data.BountyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BountyViewModel(private val repository: BountyRepository) : ViewModel() {

    val bounties: StateFlow<List<Bounty>> = repository.bounties
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // True until the very first emission from Room's Flow arrives. Task D reads this
    // to drive the shimmer, then it flips false permanently — no re-triggering on later updates.
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        viewModelScope.launch { repository.seedIfEmpty() }
        viewModelScope.launch {
            repository.bounties.first()
            _isLoading.value = false
        }
    }

    /** Task C: bump progress by a fixed step and clamp — this is the only place clamping happens. */
    fun advanceProgress(bounty: Bounty) {
        viewModelScope.launch {
            val next = (bounty.progress + 0.2f).coerceIn(0f, 1f)
            repository.update(bounty.copy(progress = next))
        }
    }

    /** Task B: drag-right commit — flips the accepted flag, row stays on the board. */
    fun acceptBounty(bounty: Bounty) {
        viewModelScope.launch { repository.update(bounty.copy(isAccepted = true)) }
    }

    /** Task B: drag-left commit, after the confirmation dialog. */
    fun abandonBounty(bounty: Bounty) {
        viewModelScope.launch { repository.delete(bounty) }
    }

    companion object {
        fun factory(repository: BountyRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    BountyViewModel(repository) as T
            }
    }
}