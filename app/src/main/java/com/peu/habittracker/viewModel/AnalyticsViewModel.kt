package com.peu.habittracker.viewModel



import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.peu.habittracker.util.HabitAnalytics
import com.peu.habittracker.db.HabitCompletion
import com.peu.habittracker.repository.HabitRepository
import com.peu.habittracker.util.AnalyticsCalculator
import com.peu.habittracker.util.OverallStatistics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AnalyticsUiState {
    object Loading : AnalyticsUiState()
    data class Success(
        val overallStats: OverallStatistics,
        val habitAnalytics: List<HabitAnalytics>
    ) : AnalyticsUiState()
    data class Error(val message: String) : AnalyticsUiState()
}

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    application: Application,
    private val repository: HabitRepository
) : AndroidViewModel(application) {

    private val calculator = AnalyticsCalculator()

    private val _uiState = MutableStateFlow<AnalyticsUiState>(AnalyticsUiState.Loading)
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadAnalytics()
    }

    private fun loadAnalytics() {
        viewModelScope.launch {
            try {
                repository.getAllHabits().collect { habits ->
                    val habitAnalyticsList = mutableListOf<HabitAnalytics>()
                    val allCompletions = mutableListOf<HabitCompletion>()

                    habits.forEach { habit ->
                        val completions = repository.getCompletionsForHabit(habit.id).first()
                        allCompletions.addAll(completions)
                        val analytics = calculator.calculateHabitAnalytics(habit, completions)
                        habitAnalyticsList.add(analytics)
                    }

                    val overallStats = calculator.calculateOverallStatistics(habits, allCompletions)

                    _uiState.value = AnalyticsUiState.Success(
                        overallStats = overallStats,
                        habitAnalytics = habitAnalyticsList.sortedByDescending { it.completionRate }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = AnalyticsUiState.Error(e.message ?: "Failed to load analytics")
            }
        }
    }
}
