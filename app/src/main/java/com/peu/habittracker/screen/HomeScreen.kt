package com.peu.habittracker.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.peu.habittracker.db.Habit
import com.peu.habittracker.viewModel.HabitWithStatus
import com.peu.habittracker.viewModel.HomeUiState
import com.peu.habittracker.viewModel.HomeViewModel

enum class HabitFilter {
    ALL, COMPLETED, PENDING, STREAK
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onNavigateToAddHabit: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val deletedHabit by viewModel.deletedHabit.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedFilter by remember { mutableStateOf(HabitFilter.ALL) }
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(deletedHabit) {
        deletedHabit?.let { habit ->
            val result = snackbarHostState.showSnackbar(
                message = "${habit.name} deleted",
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoDelete()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "My Habits",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState is HomeUiState.Success) {
                            val habits = (uiState as HomeUiState.Success).habits
                            val completedToday = habits.count { it.isCompletedToday }
                            Text(
                                "$completedToday of ${habits.size} completed today",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    IconButton(onClick = onNavigateToStatistics) {
                        Icon(Icons.Default.BarChart, "Statistics")
                    }

                    Box {
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(Icons.Default.MoreVert, "Menu")
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Settings") },
                                onClick = {
                                    showMenu = false
                                    onNavigateToSettings()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Settings, null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Export Data") },
                                onClick = {
                                    showMenu = false
                                    // TODO: Implement export
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Download, null)
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddHabit,
                containerColor = MaterialTheme.colorScheme.primary,
                icon = { Icon(Icons.Default.Add, "Add Habit") },
                text = { Text("New Habit") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            if (uiState is HomeUiState.Success) {
                FilterChipRow(
                    selectedFilter = selectedFilter,
                    onFilterChange = { selectedFilter = it },
                    habits = (uiState as HomeUiState.Success).habits
                )
            }

            // Habits List
            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is HomeUiState.Success -> {
                    val filteredHabits = when (selectedFilter) {
                        HabitFilter.ALL -> state.habits
                        HabitFilter.COMPLETED -> state.habits.filter { it.isCompletedToday }
                        HabitFilter.PENDING -> state.habits.filter { !it.isCompletedToday }
                        HabitFilter.STREAK -> state.habits.sortedByDescending { it.habit.currentStreak }
                    }

                    if (filteredHabits.isEmpty()) {
                        EmptyState(
                            filter = selectedFilter,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = filteredHabits,
                                key = { it.habit.id }
                            ) { habitWithStatus ->
                                SwipeToDeleteHabitItem(
                                    habitWithStatus = habitWithStatus,
                                    onToggle = { viewModel.toggleHabitCompletion(it) },
                                    onClick = { onNavigateToDetail(it) },
                                    onDelete = { viewModel.deleteHabit(it) },
                                    modifier = Modifier.animateItem(
                                        fadeInSpec = null,
                                        fadeOutSpec = null,
                                        placementSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        )
                                    )
                                )
                            }
                        }
                    }
                }
                is HomeUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChipRow(
    selectedFilter: HabitFilter,
    onFilterChange: (HabitFilter) -> Unit,
    habits: List<HabitWithStatus>
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),

        horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        FilterChip(
            selected = selectedFilter == HabitFilter.ALL,
            onClick = { onFilterChange(HabitFilter.ALL) },
            label = { Text("All (${habits.size})") },
            leadingIcon = if (selectedFilter == HabitFilter.ALL) {
                { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
            } else null
        )

        FilterChip(
            selected = selectedFilter == HabitFilter.COMPLETED,
            onClick = { onFilterChange(HabitFilter.COMPLETED) },
            label = {
                Text("Completed (${habits.count { it.isCompletedToday }})")
            },
            leadingIcon = if (selectedFilter == HabitFilter.COMPLETED) {
                { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
            } else null
        )

        FilterChip(
            selected = selectedFilter == HabitFilter.PENDING,
            onClick = { onFilterChange(HabitFilter.PENDING) },
            label = {
                Text("Pending (${habits.count { !it.isCompletedToday }})")
            },
            leadingIcon = if (selectedFilter == HabitFilter.PENDING) {
                { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
            } else null
        )

        FilterChip(
            selected = selectedFilter == HabitFilter.STREAK,
            onClick = { onFilterChange(HabitFilter.STREAK) },
            label = { Text("🔥 Streaks") },
            leadingIcon = if (selectedFilter == HabitFilter.STREAK) {
                { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
            } else null
        )
    }
}

@Composable
fun EmptyState(
    filter: HabitFilter,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            val (emoji, title, subtitle) = when (filter) {
                HabitFilter.ALL -> Triple(
                    "🎯",
                    "No habits yet",
                    "Tap 'New Habit' to create your first habit"
                )
                HabitFilter.COMPLETED -> Triple(
                    "✅",
                    "No completed habits today",
                    "Start checking off your habits!"
                )
                HabitFilter.PENDING -> Triple(
                    "🎉",
                    "All habits completed!",
                    "Great job! Come back tomorrow"
                )
                HabitFilter.STREAK -> Triple(
                    "🔥",
                    "No active streaks",
                    "Complete habits daily to build streaks"
                )
            }

            Text(
                text = emoji,
                style = MaterialTheme.typography.displayLarge
            )
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

