package com.peu.habittracker.viewModel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.peu.habittracker.db.Habit
import com.peu.habittracker.db.HabitCompletion
import com.peu.habittracker.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HabitRepository
) : ViewModel()
{

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _deletedHabit = MutableStateFlow<Habit?>(null)
    val deletedHabit: StateFlow<Habit?> = _deletedHabit.asStateFlow()

    init {
        loadHabits()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadHabits() {
        viewModelScope.launch {
            repository.getAllHabits()
                .catch { e ->
                    _uiState.value = HomeUiState.Error(e.message ?: "Unknown error")
                }
                .collect { habits ->
                    val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                    val habitsWithStatus = habits.map { habit ->
                        val isCompleted = repository.isHabitCompletedOnDate(habit.id, today)
                        HabitWithStatus(habit, isCompleted)
                    }
                    _uiState.value = HomeUiState.Success(habitsWithStatus)
                }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun toggleHabitCompletion(habitId: Long) {
        viewModelScope.launch {
            val today = LocalDate.now()
                .format(DateTimeFormatter.ISO_LOCAL_DATE)

            repository.toggleHabitCompletion(habitId, today)
        }
    }
    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            _deletedHabit.value = habit
            repository.deleteHabit(habit)
        }
    }

    fun undoDelete() {
        viewModelScope.launch {
            _deletedHabit.value?.let { habit ->
                repository.insertHabit(habit)
                _deletedHabit.value = null
            }
        }
    }
}


data class HabitWithStatus(
    val habit: Habit,
    val isCompletedToday: Boolean
)

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val habits: List<HabitWithStatus>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}