package com.peu.habittracker.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.peu.habittracker.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HabitDetailViewModel @Inject constructor(
    private val repository: HabitRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val habitId: Long = savedStateHandle["habitId"] ?: -1L

    val habit = repository.getHabitById(habitId)

    val completions = repository.getCompletionsForHabit(habitId)
}