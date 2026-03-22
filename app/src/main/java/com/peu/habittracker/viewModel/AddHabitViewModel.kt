package com.peu.habittracker.viewModel

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.peu.habittracker.db.Habit
import com.peu.habittracker.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddHabitState(
    val name: String = "",
    val description: String = "",
    val selectedColor: Color = Color(0xFF6200EE),
    val selectedIcon: String = "🎯",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddHabitViewModel @Inject constructor(
    private val repository: HabitRepository
) : ViewModel()
{

    private val _state = MutableStateFlow(AddHabitState())
    val state: StateFlow<AddHabitState> = _state.asStateFlow()

    fun onNameChange(name: String) {
        _state.value = _state.value.copy(name = name)
    }

    fun onDescriptionChange(description: String) {
        _state.value = _state.value.copy(description = description)
    }

    fun onColorSelect(color: Color) {
        _state.value = _state.value.copy(selectedColor = color)
    }

    fun onIconSelect(icon: String) {
        _state.value = _state.value.copy(selectedIcon = icon)
    }

    fun saveHabit(onSuccess: () -> Unit) {
        val currentState = _state.value

        if (currentState.name.isBlank()) {
            _state.value = currentState.copy(error = "Habit name cannot be empty")
            return
        }

        viewModelScope.launch {
            _state.value = currentState.copy(isLoading = true, error = null)

            try {
                val habit = Habit(
                    name = currentState.name.trim(),
                    description = currentState.description.trim(),
                    color = currentState.selectedColor.toArgb(),
                    icon = currentState.selectedIcon
                )

                repository.insertHabit(habit)
                onSuccess()
            } catch (e: Exception) {
                _state.value = currentState.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to save habit"
                )
            }
        }
    }
}
