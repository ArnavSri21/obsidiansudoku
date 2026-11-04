package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.GameStatsEntity
import com.example.data.model.SolvedPuzzleEntity
import com.example.data.repository.SudokuRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Locale

data class DashboardUiState(
    val totalSolved: Int = 0,
    val totalPlayed: Int = 0,
    val totalScore: Int = 0,
    val winRatePercent: Float = 0f,
    val bestTimeFormatted: String = "--:--",
    val currentStreak: Int = 0,
    val flawlessCount: Int = 0,
    val statsList: List<GameStatsEntity> = emptyList(),
    val allSolvedPuzzles: List<SolvedPuzzleEntity> = emptyList(),
    val filteredSolvedPuzzles: List<SolvedPuzzleEntity> = emptyList(),
    val selectedFilter: String = "ALL"
)

class DashboardViewModel(
    private val repository: SudokuRepository
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow("ALL")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.allStats,
        repository.allSolvedPuzzles,
        repository.userTotalScoreFlow,
        _selectedFilter
    ) { stats, solvedPuzzles, totalScore, filter ->
        val totalPlayed = stats.sumOf { it.gamesPlayed }
        val totalWon = maxOf(stats.sumOf { it.gamesWon }, solvedPuzzles.size)
        val flawless = maxOf(stats.sumOf { it.perfectGames }, solvedPuzzles.count { it.mistakes == 0 })
        val winRate = if (totalPlayed > 0) (totalWon.toFloat() / totalPlayed * 100f).coerceAtMost(100f) else (if (totalWon > 0) 100f else 0f)

        val bestSecs = solvedPuzzles.minOfOrNull { it.timeSpentSeconds }
            ?: stats.filter { it.bestTimeSeconds > 0 }.minOfOrNull { it.bestTimeSeconds }
            ?: 0L

        val bestTimeStr = if (bestSecs > 0) {
            val mins = bestSecs / 60
            val secs = bestSecs % 60
            String.format(Locale.US, "%02d:%02d", mins, secs)
        } else {
            "--:--"
        }

        val filtered = when (filter.uppercase(Locale.US)) {
            "ALL" -> solvedPuzzles
            "FLAWLESS" -> solvedPuzzles.filter { it.mistakes == 0 }
            "DAILY" -> solvedPuzzles.filter { it.isDaily }
            else -> solvedPuzzles.filter { it.difficulty.equals(filter, ignoreCase = true) }
        }

        DashboardUiState(
            totalSolved = totalWon,
            totalPlayed = maxOf(totalPlayed, totalWon),
            totalScore = totalScore,
            winRatePercent = winRate,
            bestTimeFormatted = bestTimeStr,
            currentStreak = repository.getOverallStreak(),
            flawlessCount = flawless,
            statsList = stats,
            allSolvedPuzzles = solvedPuzzles,
            filteredSolvedPuzzles = filtered,
            selectedFilter = filter
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DashboardUiState(
            totalScore = repository.getUserTotalScore(),
            currentStreak = repository.getOverallStreak()
        )
    )

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }
}
