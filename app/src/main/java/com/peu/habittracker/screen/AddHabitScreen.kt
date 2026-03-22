package com.peu.habittracker.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.peu.habittracker.viewModel.AddHabitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddHabitViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Habit") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.saveHabit(onNavigateBack) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Check, "Save")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Habit Name") },
                placeholder = { Text("e.g., Morning Exercise") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.error != null,
                supportingText = state.error?.let { { Text(it) } }
            )

            OutlinedTextField(
                value = state.description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text("Description (Optional)") },
                placeholder = { Text("Why is this habit important?") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Choose Icon",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                IconSelector(
                    selectedIcon = state.selectedIcon,
                    onIconSelect = viewModel::onIconSelect
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Choose Color",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                ColorSelector(
                    selectedColor = state.selectedColor,
                    onColorSelect = viewModel::onColorSelect
                )
            }
        }
    }
}
@Composable
fun IconSelector(
    selectedIcon: String,
    onIconSelect: (String) -> Unit
) {
    val icons = listOf(
        "🎯", "💪", "📚", "🧘", "🏃", "💻",
        "🎨", "🎵", "✍️", "🌱", "💧", "🍎",
        "😴", "🧠", "❤️", "⭐", "🔥", "🌟"
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(6),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.height(120.dp)
    ) {
        items(icons) { icon ->
            IconItem(
                icon = icon,
                isSelected = icon == selectedIcon,
                onClick = { onIconSelect(icon) }
            )
        }
    }
}

@Composable
fun IconItem(
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface
            )
            .border(
                width = 2.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.headlineSmall
        )
    }
}

@Composable
fun ColorSelector(
    selectedColor: Color,
    onColorSelect: (Color) -> Unit
) {
    val colors = listOf(
        Color(0xFF6200EE), Color(0xFFFF6B6B), Color(0xFF4ECDC4),
        Color(0xFFFFA726), Color(0xFF66BB6A), Color(0xFFAB47BC),
        Color(0xFF42A5F5), Color(0xFFEF5350), Color(0xFF26A69A),
        Color(0xFFFFCA28), Color(0xFF7E57C2), Color(0xFF29B6F6)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(6),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.height(100.dp)
    ) {
        items(colors) { color ->
            ColorItem(
                color = color,
                isSelected = color == selectedColor,
                onClick = { onColorSelect(color) }
            )
        }
    }
}

@Composable
fun ColorItem(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = MaterialTheme.colorScheme.onSurface,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color.White
            )
        }
    }
}
