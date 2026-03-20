package com.theminimaldev.gymprint.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theminimaldev.gymprint.data.repository.GymRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import javax.inject.Inject

data class HomeUiState(
    val visitDates: Set<String> = emptySet(),
    val currentStreak: Int = 0,
    val monthVisits: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val gymRepository: GymRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = gymRepository.observeVisits()
        .map { visits ->
            val dateSet = visits.map { it.date }.toSortedSet()
            HomeUiState(
                visitDates = dateSet,
                currentStreak = computeStreak(dateSet),
                monthVisits = countMonthVisits(dateSet),
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState()
        )

    private fun computeStreak(sortedDates: Set<String>): Int {
        if (sortedDates.isEmpty()) return 0
        val today = LocalDate.now()
        var streak = 0
        var current = today
        while (sortedDates.contains(current.toString())) {
            streak++
            current = current.minusDays(1)
        }
        if (streak == 0) {
            // Check streak ending yesterday
            current = today.minusDays(1)
            while (sortedDates.contains(current.toString())) {
                streak++
                current = current.minusDays(1)
            }
        }
        return streak
    }

    private fun countMonthVisits(sortedDates: Set<String>): Int {
        val prefix = LocalDate.now().toString().substring(0, 7) // "yyyy-MM"
        return sortedDates.count { it.startsWith(prefix) }
    }
}
