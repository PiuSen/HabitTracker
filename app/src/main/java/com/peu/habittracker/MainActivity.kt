package com.peu.habittracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.peu.habittracker.navigation.NavGraph
import com.peu.habittracker.ui.theme.HabitTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HabitTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(navController = navController)
                }

            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HabitTrackerTheme {
        Greeting("Android")
    }
}

//// ============================================================================
//// PART 1: BUILD CONFIGURATION
//// ============================================================================
//
//// build.gradle.kts (Project level)
//plugins {
//    id("com.android.application") version "8.2.0" apply false
//    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
//    id("com.google.dagger.hilt.android") version "2.48" apply false
//    id("com.google.devtools.ksp") version "1.9.20-1.0.14" apply false
//}
//
//// build.gradle.kts (App level)
//plugins {
//    id("com.android.application")
//    id("org.jetbrains.kotlin.android")
//    id("com.google.dagger.hilt.android")
//    id("com.google.devtools.ksp")
//}
//
//android {
//    namespace = "com.example.habittracker"
//    compileSdk = 34
//
//    defaultConfig {
//        applicationId = "com.example.habittracker"
//        minSdk = 26
//        targetSdk = 34
//        versionCode = 1
//        versionName = "1.0"
//    }
//
//    buildFeatures {
//        compose = true
//    }
//
//    composeOptions {
//        kotlinCompilerExtensionVersion = "1.5.4"
//    }
//
//    kotlinOptions {
//        jvmTarget = "17"
//    }
//}
//
//dependencies {
//    // Compose
//    val composeBom = platform("androidx.compose:compose-bom:2023.10.01")
//    implementation(composeBom)
//    implementation("androidx.compose.ui:ui")
//    implementation("androidx.compose.ui:ui-graphics")
//    implementation("androidx.compose.ui:ui-tooling-preview")
//    implementation("androidx.compose.material3:material3")
//    implementation("androidx.activity:activity-compose:1.8.2")
//    implementation("androidx.navigation:navigation-compose:2.7.6")
//
//    // Room
//    implementation("androidx.room:room-runtime:2.6.1")
//    implementation("androidx.room:room-ktx:2.6.1")
//    ksp("androidx.room:room-compiler:2.6.1")
//
//    // DataStore
//    implementation("androidx.datastore:datastore-preferences:1.0.0")
//
//    // WorkManager
//    implementation("androidx.work:work-runtime-ktx:2.9.0")
//
//    // Hilt
//    implementation("com.google.dagger:hilt-android:2.48")
//    ksp("com.google.dagger:hilt-compiler:2.48")
//    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
//    implementation("androidx.hilt:hilt-work:1.1.0")
//    ksp("androidx.hilt:hilt-compiler:1.1.0")
//
//    // Lifecycle
//    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
//    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
//}
//
//// ============================================================================
//// PART 2: DATA LAYER - ENTITIES & DATABASE
//// ============================================================================
//
//// data/local/entity/Habit.kt
//package com.example.habittracker.data.local.entity
//
//import androidx.room.Entity
//import androidx.room.PrimaryKey
//
//@Entity(tableName = "habits")
//data class Habit(
//    @PrimaryKey(autoGenerate = true)
//    val id: Long = 0,
//    val name: String,
//    val description: String = "",
//    val color: Int,
//    val icon: String = "🎯",
//    val createdAt: Long = System.currentTimeMillis(),
//    val currentStreak: Int = 0,
//    val longestStreak: Int = 0,
//    val totalCompletions: Int = 0
//)
//
//// data/local/entity/HabitCompletion.kt
//package com.example.habittracker.data.local.entity
//
//import androidx.room.Entity
//import androidx.room.PrimaryKey
//import androidx.room.ForeignKey
//import androidx.room.Index
//
//@Entity(
//    tableName = "habit_completions",
//    foreignKeys = [
//        ForeignKey(
//            entity = Habit::class,
//            parentColumns = ["id"],
//            childColumns = ["habitId"],
//            onDelete = ForeignKey.CASCADE
//        )
//    ],
//    indices = [Index("habitId"), Index("date")]
//)
//data class HabitCompletion(
//    @PrimaryKey(autoGenerate = true)
//    val id: Long = 0,
//    val habitId: Long,
//    val date: String, // Format: "yyyy-MM-dd"
//    val completed: Boolean = true,
//    val completedAt: Long = System.currentTimeMillis(),
//    val note: String = ""
//)
//
//// data/local/dao/HabitDao.kt
//package com.example.habittracker.data.local.dao
//
//import androidx.room.*
//import com.example.habittracker.data.local.entity.Habit
//import com.example.habittracker.data.local.entity.HabitCompletion
//import kotlinx.coroutines.flow.Flow
//
//@Dao
//interface HabitDao {
//    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
//    fun getAllHabits(): Flow<List<Habit>>
//
//    @Query("SELECT * FROM habits WHERE id = :habitId")
//    fun getHabitById(habitId: Long): Flow<Habit?>
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertHabit(habit: Habit): Long
//
//    @Update
//    suspend fun updateHabit(habit: Habit)
//
//    @Delete
//    suspend fun deleteHabit(habit: Habit)
//
//    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND date = :date")
//    suspend fun getCompletion(habitId: Long, date: String): HabitCompletion?
//
//    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId ORDER BY date DESC")
//    fun getCompletionsForHabit(habitId: Long): Flow<List<HabitCompletion>>
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertCompletion(completion: HabitCompletion)
//
//    @Delete
//    suspend fun deleteCompletion(completion: HabitCompletion)
//
//    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND date >= :startDate AND date <= :endDate")
//    suspend fun getCompletionsInRange(habitId: Long, startDate: String, endDate: String): List<HabitCompletion>
//}
//
//// data/local/HabitDatabase.kt
//package com.example.habittracker.data.local
//
//import androidx.room.Database
//import androidx.room.RoomDatabase
//import com.example.habittracker.data.local.dao.HabitDao
//import com.example.habittracker.data.local.entity.Habit
//import com.example.habittracker.data.local.entity.HabitCompletion
//
//@Database(
//    entities = [Habit::class, HabitCompletion::class],
//    version = 1,
//    exportSchema = false
//)
//abstract class HabitDatabase : RoomDatabase() {
//    abstract fun habitDao(): HabitDao
//}
//
//// ============================================================================
//// PART 3: DATA LAYER - REPOSITORY
//// ============================================================================
//
//// domain/repository/HabitRepository.kt
//package com.example.habittracker.domain.repository
//
//import com.example.habittracker.data.local.entity.Habit
//import com.example.habittracker.data.local.entity.HabitCompletion
//import kotlinx.coroutines.flow.Flow
//
//interface HabitRepository {
//    fun getAllHabits(): Flow<List<Habit>>
//    fun getHabitById(habitId: Long): Flow<Habit?>
//    suspend fun insertHabit(habit: Habit): Long
//    suspend fun updateHabit(habit: Habit)
//    suspend fun deleteHabit(habit: Habit)
//    suspend fun toggleHabitCompletion(habitId: Long, date: String)
//    suspend fun isHabitCompletedOnDate(habitId: Long, date: String): Boolean
//    fun getCompletionsForHabit(habitId: Long): Flow<List<HabitCompletion>>
//    suspend fun getCompletionsInRange(habitId: Long, startDate: String, endDate: String): List<HabitCompletion>
//}
//
//// data/repository/HabitRepositoryImpl.kt
//package com.example.habittracker.data.repository
//
//import com.example.habittracker.data.local.dao.HabitDao
//import com.example.habittracker.data.local.entity.Habit
//import com.example.habittracker.data.local.entity.HabitCompletion
//import com.example.habittracker.domain.repository.HabitRepository
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.first
//import java.time.LocalDate
//import java.time.format.DateTimeFormatter
//import javax.inject.Inject
//
//class HabitRepositoryImpl @Inject constructor(
//    private val habitDao: HabitDao
//) : HabitRepository {
//
//    override fun getAllHabits(): Flow<List<Habit>> = habitDao.getAllHabits()
//
//    override fun getHabitById(habitId: Long): Flow<Habit?> = habitDao.getHabitById(habitId)
//
//    override suspend fun insertHabit(habit: Habit): Long = habitDao.insertHabit(habit)
//
//    override suspend fun updateHabit(habit: Habit) = habitDao.updateHabit(habit)
//
//    override suspend fun deleteHabit(habit: Habit) = habitDao.deleteHabit(habit)
//
//    override suspend fun toggleHabitCompletion(habitId: Long, date: String) {
//        val existing = habitDao.getCompletion(habitId, date)
//
//        if (existing != null) {
//            habitDao.deleteCompletion(existing)
//            updateStreakOnUncomplete(habitId)
//        } else {
//            val completion = HabitCompletion(habitId = habitId, date = date)
//            habitDao.insertCompletion(completion)
//            updateStreakOnComplete(habitId, date)
//        }
//    }
//
//    override suspend fun isHabitCompletedOnDate(habitId: Long, date: String): Boolean {
//        return habitDao.getCompletion(habitId, date) != null
//    }
//
//    override fun getCompletionsForHabit(habitId: Long): Flow<List<HabitCompletion>> {
//        return habitDao.getCompletionsForHabit(habitId)
//    }
//
//    override suspend fun getCompletionsInRange(
//        habitId: Long,
//        startDate: String,
//        endDate: String
//    ): List<HabitCompletion> {
//        return habitDao.getCompletionsInRange(habitId, startDate, endDate)
//    }
//
//    private suspend fun updateStreakOnComplete(habitId: Long, dateStr: String) {
//        val habit = habitDao.getHabitById(habitId).first() ?: return
//        val completions = habitDao.getCompletionsForHabit(habitId).first()
//            .sortedByDescending { it.date }
//
//        val currentStreak = calculateCurrentStreak(completions, dateStr)
//        val longestStreak = maxOf(habit.longestStreak, currentStreak)
//
//        habitDao.updateHabit(
//            habit.copy(
//                currentStreak = currentStreak,
//                longestStreak = longestStreak,
//                totalCompletions = habit.totalCompletions + 1
//            )
//        )
//    }
//
//    private suspend fun updateStreakOnUncomplete(habitId: Long) {
//        val habit = habitDao.getHabitById(habitId).first() ?: return
//        val completions = habitDao.getCompletionsForHabit(habitId).first()
//            .sortedByDescending { it.date }
//
//        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
//        val currentStreak = calculateCurrentStreak(completions, today)
//
//        habitDao.updateHabit(
//            habit.copy(
//                currentStreak = currentStreak,
//                totalCompletions = maxOf(0, habit.totalCompletions - 1)
//            )
//        )
//    }
//
//    private fun calculateCurrentStreak(completions: List<HabitCompletion>, fromDate: String): Int {
//        if (completions.isEmpty()) return 0
//
//        var streak = 0
//        var currentDate = LocalDate.parse(fromDate)
//        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
//
//        val completionDates = completions.map { it.date }.toSet()
//
//        while (completionDates.contains(currentDate.format(formatter))) {
//            streak++
//            currentDate = currentDate.minusDays(1)
//        }
//
//        return streak
//    }
//}
//
//// ============================================================================
//// PART 4: DEPENDENCY INJECTION
//// ============================================================================
//
//// di/DatabaseModule.kt
//package com.example.habittracker.di
//
//import android.content.Context
//import androidx.room.Room
//import com.example.habittracker.data.local.HabitDatabase
//import com.example.habittracker.data.local.dao.HabitDao
//import dagger.Module
//import dagger.Provides
//import dagger.hilt.InstallIn
//import dagger.hilt.android.qualifiers.ApplicationContext
//import dagger.hilt.components.SingletonComponent
//import javax.inject.Singleton
//
//@Module
//@InstallIn(SingletonComponent::class)
//object DatabaseModule {
//
//    @Provides
//    @Singleton
//    fun provideHabitDatabase(@ApplicationContext context: Context): HabitDatabase {
//        return Room.databaseBuilder(
//            context,
//            HabitDatabase::class.java,
//            "habit_database"
//        ).build()
//    }
//
//    @Provides
//    fun provideHabitDao(database: HabitDatabase): HabitDao {
//        return database.habitDao()
//    }
//}
//
//// di/RepositoryModule.kt
//package com.example.habittracker.di
//
//import com.example.habittracker.data.repository.HabitRepositoryImpl
//import com.example.habittracker.domain.repository.HabitRepository
//import dagger.Binds
//import dagger.Module
//import dagger.hilt.InstallIn
//import dagger.hilt.components.SingletonComponent
//import javax.inject.Singleton
//
//@Module
//@InstallIn(SingletonComponent::class)
//abstract class RepositoryModule {
//
//    @Binds
//    @Singleton
//    abstract fun bindHabitRepository(
//        habitRepositoryImpl: HabitRepositoryImpl
//    ): HabitRepository
//}
//
//// ============================================================================
//// PART 5: PRESENTATION LAYER - UI STATE & VIEWMODEL
//// ============================================================================
//
//// presentation/home/HomeViewModel.kt
//package com.example.habittracker.presentation.home
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.habittracker.data.local.entity.Habit
//import com.example.habittracker.domain.repository.HabitRepository
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.flow.*
//import kotlinx.coroutines.launch
//import java.time.LocalDate
//import java.time.format.DateTimeFormatter
//import javax.inject.Inject
//
//data class HabitWithStatus(
//    val habit: Habit,
//    val isCompletedToday: Boolean
//)
//
//sealed class HomeUiState {
//    object Loading : HomeUiState()
//    data class Success(val habits: List<HabitWithStatus>) : HomeUiState()
//    data class Error(val message: String) : HomeUiState()
//}
//
//@HiltViewModel
//class HomeViewModel @Inject constructor(
//    private val repository: HabitRepository
//) : ViewModel() {
//
//    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
//    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
//
//    private val _deletedHabit = MutableStateFlow<Habit?>(null)
//    val deletedHabit: StateFlow<Habit?> = _deletedHabit.asStateFlow()
//
//    init {
//        loadHabits()
//    }
//
//    private fun loadHabits() {
//        viewModelScope.launch {
//            repository.getAllHabits()
//                .catch { e ->
//                    _uiState.value = HomeUiState.Error(e.message ?: "Unknown error")
//                }
//                .collect { habits ->
//                    val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
//                    val habitsWithStatus = habits.map { habit ->
//                        val isCompleted = repository.isHabitCompletedOnDate(habit.id, today)
//                        HabitWithStatus(habit, isCompleted)
//                    }
//                    _uiState.value = HomeUiState.Success(habitsWithStatus)
//                }
//        }
//    }
//
//    fun toggleHabitCompletion(habitId: Long) {
//        viewModelScope.launch {
//            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
//            repository.toggleHabitCompletion(habitId, today)
//        }
//    }
//
//    fun deleteHabit(habit: Habit) {
//        viewModelScope.launch {
//            _deletedHabit.value = habit
//            repository.deleteHabit(habit)
//        }
//    }
//
//    fun undoDelete() {
//        viewModelScope.launch {
//            _deletedHabit.value?.let { habit ->
//                repository.insertHabit(habit)
//                _deletedHabit.value = null
//            }
//        }
//    }
//}
//
//// presentation/addhabit/AddHabitViewModel.kt
//package com.example.habittracker.presentation.addhabit
//
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.toArgb
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.habittracker.data.local.entity.Habit
//import com.example.habittracker.domain.repository.HabitRepository
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//data class AddHabitState(
//    val name: String = "",
//    val description: String = "",
//    val selectedColor: Color = Color(0xFF6200EE),
//    val selectedIcon: String = "🎯",
//    val isLoading: Boolean = false,
//    val error: String? = null
//)
//
//@HiltViewModel
//class AddHabitViewModel @Inject constructor(
//    private val repository: HabitRepository
//) : ViewModel() {
//
//    private val _state = MutableStateFlow(AddHabitState())
//    val state: StateFlow<AddHabitState> = _state.asStateFlow()
//
//    fun onNameChange(name: String) {
//        _state.value = _state.value.copy(name = name)
//    }
//
//    fun onDescriptionChange(description: String) {
//        _state.value = _state.value.copy(description = description)
//    }
//
//    fun onColorSelect(color: Color) {
//        _state.value = _state.value.copy(selectedColor = color)
//    }
//
//    fun onIconSelect(icon: String) {
//        _state.value = _state.value.copy(selectedIcon = icon)
//    }
//
//    fun saveHabit(onSuccess: () -> Unit) {
//        val currentState = _state.value
//
//        if (currentState.name.isBlank()) {
//            _state.value = currentState.copy(error = "Habit name cannot be empty")
//            return
//        }
//
//        viewModelScope.launch {
//            _state.value = currentState.copy(isLoading = true, error = null)
//
//            try {
//                val habit = Habit(
//                    name = currentState.name.trim(),
//                    description = currentState.description.trim(),
//                    color = currentState.selectedColor.toArgb(),
//                    icon = currentState.selectedIcon
//                )
//
//                repository.insertHabit(habit)
//                onSuccess()
//            } catch (e: Exception) {
//                _state.value = currentState.copy(
//                    isLoading = false,
//                    error = e.message ?: "Failed to save habit"
//                )
//            }
//        }
//    }
//}
//
//// ============================================================================
//// PART 6: UI COMPONENTS - HOME SCREEN
//// ============================================================================
//
//// presentation/home/HomeScreen.kt
//package com.example.habittracker.presentation.home
//
//import androidx.compose.animation.*
//import androidx.compose.animation.core.*
//import androidx.compose.foundation.ExperimentalFoundationApi
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Add
//import androidx.compose.material.icons.filled.Delete
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.draw.scale
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.hilt.navigation.compose.hiltViewModel
//import androidx.compose.material3.DismissDirection
//import androidx.compose.material3.DismissValue
//import androidx.compose.material3.SwipeToDismiss
//import androidx.compose.material3.rememberDismissState
//
//@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
//@Composable
//fun HomeScreen(
//    onNavigateToAddHabit: () -> Unit,
//    onNavigateToDetail: (Long) -> Unit,
//    viewModel: HomeViewModel = hiltViewModel()
//) {
//    val uiState by viewModel.uiState.collectAsState()
//    val deletedHabit by viewModel.deletedHabit.collectAsState()
//    val snackbarHostState = remember { SnackbarHostState() }
//
//    LaunchedEffect(deletedHabit) {
//        deletedHabit?.let { habit ->
//            val result = snackbarHostState.showSnackbar(
//                message = "${habit.name} deleted",
//                actionLabel = "Undo",
//                duration = SnackbarDuration.Short
//            )
//            if (result == SnackbarResult.ActionPerformed) {
//                viewModel.undoDelete()
//            }
//        }
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = {
//                    Text(
//                        "My Habits",
//                        style = MaterialTheme.typography.headlineMedium,
//                        fontWeight = FontWeight.Bold
//                    )
//                },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = MaterialTheme.colorScheme.primaryContainer
//                )
//            )
//        },
//        floatingActionButton = {
//            FloatingActionButton(
//                onClick = onNavigateToAddHabit,
//                containerColor = MaterialTheme.colorScheme.primary
//            ) {
//                Icon(Icons.Default.Add, "Add Habit")
//            }
//        },
//        snackbarHost = { SnackbarHost(snackbarHostState) }
//    ) { padding ->
//        when (val state = uiState) {
//            is HomeUiState.Loading -> {
//                Box(
//                    modifier = Modifier.fillMaxSize(),
//                    contentAlignment = Alignment.Center
//                ) {
//                    CircularProgressIndicator()
//                }
//            }
//            is HomeUiState.Success -> {
//                if (state.habits.isEmpty()) {
//                    EmptyState(modifier = Modifier.padding(padding))
//                } else {
//                    LazyColumn(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .padding(padding),
//                        contentPadding = PaddingValues(16.dp),
//                        verticalArrangement = Arrangement.spacedBy(12.dp)
//                    ) {
//                        items(
//                            items = state.habits,
//                            key = { it.habit.id }
//                        ) { habitWithStatus ->
//                            SwipeToDeleteHabitItem(
//                                habitWithStatus = habitWithStatus,
//                                onToggle = { viewModel.toggleHabitCompletion(it) },
//                                onClick = { onNavigateToDetail(it) },
//                                onDelete = { viewModel.deleteHabit(it) },
//                                modifier = Modifier.animateItemPlacement()
//                            )
//                        }
//                    }
//                }
//            }
//            is HomeUiState.Error -> {
//                Box(
//                    modifier = Modifier.fillMaxSize(),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Text(
//                        text = state.message,
//                        color = MaterialTheme.colorScheme.error
//                    )
//                }
//            }
//        }
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun SwipeToDeleteHabitItem(
//    habitWithStatus: HabitWithStatus,
//    onToggle: (Long) -> Unit,
//    onClick: (Long) -> Unit,
//    onDelete: (Habit) -> Unit,
//    modifier: Modifier = Modifier
//) {
//    val dismissState = rememberDismissState(
//        confirmValueChange = {
//            if (it == DismissValue.DismissedToStart) {
//                onDelete(habitWithStatus.habit)
//                true
//            } else {
//                false
//            }
//        }
//    )
//
//    SwipeToDismiss(
//        state = dismissState,
//        modifier = modifier,
//        directions = setOf(DismissDirection.EndToStart),
//        background = {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .clip(RoundedCornerShape(16.dp))
//                    .background(MaterialTheme.colorScheme.errorContainer)
//                    .padding(horizontal = 20.dp),
//                contentAlignment = Alignment.CenterEnd
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Delete,
//                    contentDescription = "Delete",
//                    tint = MaterialTheme.colorScheme.onErrorContainer
//                )
//            }
//        },
//        dismissContent = {
//            HabitItem(
//                habitWithStatus = habitWithStatus,
//                onToggle = onToggle,
//                onClick = onClick
//            )
//        }
//    )
//}
//
//@Composable
//fun HabitItem(
//    habitWithStatus: HabitWithStatus,
//    onToggle: (Long) -> Unit,
//    onClick: (Long) -> Unit
//) {
//    val habit = habitWithStatus.habit
//    val isCompleted = habitWithStatus.isCompletedToday
//
//    var checkboxChecked by remember { mutableStateOf(isCompleted) }
//
//    LaunchedEffect(isCompleted) {
//        checkboxChecked = isCompleted
//    }
//
//    val scale by animateFloatAsState(
//        targetValue = if (checkboxChecked) 0.95f else 1f,
//        animationSpec = spring(
//            dampingRatio = Spring.DampingRatioMediumBouncy,
//            stiffness = Spring.StiffnessLow
//        ),
//        label = "scale"
//    )
//
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .scale(scale)
//            .clickable { onClick(habit.id) },
//        colors = CardDefaults.cardColors(
//            containerColor = MaterialTheme.colorScheme.surfaceVariant
//        ),
//        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
//        shape = RoundedCornerShape(16.dp)
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Row(
//                horizontalArrangement = Arrangement.spacedBy(12.dp),
//                verticalAlignment = Alignment.CenterVertically,
//                modifier = Modifier.weight(1f)
//            ) {
//                Box(
//                    modifier = Modifier
//                        .size(48.dp)
//                        .clip(CircleShape)
//                        .background(Color(habit.color)),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Text(
//                        text = habit.icon,
//                        style = MaterialTheme.typography.headlineSmall
//                    )
//                }
//
//                Column {
//                    Text(
//                        text = habit.name,
//                        style = MaterialTheme.typography.titleMedium,
//                        fontWeight = FontWeight.SemiBold
//                    )
//                    Row(
//                        horizontalArrangement = Arrangement.spacedBy(16.dp),
//                        modifier = Modifier.padding(top = 4.dp)
//                    ) {
//                        Text(
//                            text = "🔥 ${habit.currentStreak}",
//                            style = MaterialTheme.typography.bodySmall,
//                            color = MaterialTheme.colorScheme.onSurfaceVariant
//                        )
//                        Text(
//                            text = "✅ ${habit.totalCompletions}",
//                            style = MaterialTheme.typography.bodySmall,
//                            color = MaterialTheme.colorScheme.onSurfaceVariant
//                        )
//                    }
//                }
//            }
//
//            AnimatedCheckbox(
//                checked = checkboxChecked,
//                onCheckedChange = {
//                    checkboxChecked = it
//                    onToggle(habit.id)
//                }
//            )
//        }
//    }
//}
//
//@Composable
//fun AnimatedCheckbox(
//    checked: Boolean,
//    onCheckedChange: (Boolean) -> Unit
//) {
//    Checkbox(
//        checked = checked,
//        onCheckedChange = onCheckedChange,
//        colors = CheckboxDefaults.colors(
//            checkedColor = MaterialTheme.colorScheme.primary,
//            uncheckedColor = MaterialTheme.colorScheme.outline
//        )
//    )
//}
//
//@Composable
//fun EmptyState(modifier: Modifier = Modifier) {
//    Box(
//        modifier = modifier.fillMaxSize(),
//        contentAlignment = Alignment.Center
//    ) {
//        Column(
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            Text(
//                text = "🎯",
//                style = MaterialTheme.typography.displayLarge
//            )
//            Text(
//                text = "No habits yet",
//                style = MaterialTheme.typography.headlineSmall,
//                fontWeight = FontWeight.Bold
//            )
//            Text(
//                text = "Tap + to create your first habit",
//                style = MaterialTheme.typography.bodyLarge,
//                color = MaterialTheme.colorScheme.onSurfaceVariant
//            )
//        }
//    }
//}
//
//// ============================================================================
//// PART 7: UI COMPONENTS - ADD HABIT SCREEN
//// ============================================================================
//
//// presentation/addhabit/AddHabitScreen.kt
//package com.example.habittracker.presentation.addhabit
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.grid.GridCells
//import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
//import androidx.compose.foundation.lazy.grid.items
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.Check
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.hilt.navigation.compose.hiltViewModel
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun AddHabitScreen(
//    onNavigateBack: () -> Unit,
//    viewModel: AddHabitViewModel = hiltViewModel()
//) {
//    val state by viewModel.state.collectAsState()
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Create Habit") },
//                navigationIcon = {
//                    IconButton(onClick = onNavigateBack) {
//                        Icon(Icons.Default.ArrowBack, "Back")
//                    }
//                }
//            )
//        },
//        floatingActionButton = {
//            FloatingActionButton(
//                onClick = { viewModel.saveHabit(onNavigateBack) },
//                containerColor = MaterialTheme.colorScheme.primary
//            ) {
//                Icon(Icons.Default.Check, "Save")
//            }
//        }
//    ) { padding ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(padding)
//                .padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(24.dp)
//        ) {
//            OutlinedTextField(
//                value = state.name,
//                onValueChange = viewModel::onNameChange,
//                label = { Text("Habit Name") },
//                placeholder = { Text("e.g., Morning Exercise") },
//                modifier = Modifier.fillMaxWidth(),
//                isError = state.error != null,
//                supportingText = state.error?.let { { Text(it) } }
//            )
//
//            OutlinedTextField(
//                value = state.description,
//                onValueChange = viewModel::onDescriptionChange,
//                label = { Text("Description (Optional)") },
//                placeholder = { Text("Why is this habit important?") },
//                modifier = Modifier.fillMaxWidth(),
//                minLines = 3
//            )
//
//            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
//                Text(
//                    text = "Choose Icon",
//                    style = MaterialTheme.typography.titleMedium,
//                    fontWeight = FontWeight.SemiBold
//                )
//
//                IconSelector(
//                    selectedIcon = state.selectedIcon,
//                    onIconSelect = viewModel::onIconSelect
//                )
//            }
//
//            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
//                Text(
//                    text = "Choose Color",
//                    style = MaterialTheme.typography.titleMedium,
//                    fontWeight = FontWeight.SemiBold
//                )
//
//                ColorSelector(
//                    selectedColor = state.selectedColor,
//                    onColorSelect = viewModel::onColorSelect
//                )
//            }
//        }
//    }
//}
//
//@Composable
//fun IconSelector(
//    selectedIcon: String,
//    onIconSelect: (String) -> Unit
//) {
//    val icons = listOf(
//        "🎯", "💪", "📚", "🧘", "🏃", "💻",
//        "🎨", "🎵", "✍️", "🌱", "💧", "🍎",
//        "😴", "🧠", "❤️", "⭐", "🔥", "🌟"
//    )
//
//    LazyVerticalGrid(
//        columns = GridCells.Fixed(6),
//        horizontalArrangement = Arrangement.spacedBy(8.dp),
//        verticalArrangement = Arrangement.spacedBy(8.dp),
//        modifier = Modifier.height(120.dp)
//    ) {
//        items(icons) { icon ->
//            IconItem(
//                icon = icon,
//                isSelected = icon == selectedIcon,
//                onClick = { onIconSelect(icon) }
//            )
//        }
//    }
//}
//
//@Composable
//fun IconItem(
//    icon: String,
//    isSelected: Boolean,
//    onClick: () -> Unit
//) {
//    Box(
//        modifier = Modifier
//            .size(48.dp)
//            .clip(CircleShape)
//            .background(
//                if (isSelected) MaterialTheme.colorScheme.primaryContainer
//                else MaterialTheme.colorScheme.surface
//            )
//            .border(
//                width = 2.dp,
//                color = if (isSelected) MaterialTheme.colorScheme.primary
//                else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
//                shape = CircleShape
//            )
//            .clickable(onClick = onClick),
//        contentAlignment = Alignment.Center
//    ) {
//        Text(
//            text = icon,
//            style = MaterialTheme.typography.headlineSmall
//        )
//    }
//}
//
//@Composable
//fun ColorSelector(
//    selectedColor: Color,
//    onColorSelect: (Color) -> Unit
//) {
//    val colors = listOf(
//        Color(0xFF6200EE), Color(0xFFFF6B6B), Color(0xFF4ECDC4),
//        Color(0xFFFFA726), Color(0xFF66BB6A), Color(0xFFAB47BC),
//        Color(0xFF42A5F5), Color(0xFFEF5350), Color(0xFF26A69A),
//        Color(0xFFFFCA28), Color(0xFF7E57C2), Color(0xFF29B6F6)
//    )
//
//    LazyVerticalGrid(
//        columns = GridCells.Fixed(6),
//        horizontalArrangement = Arrangement.spacedBy(12.dp),
//        verticalArrangement = Arrangement.spacedBy(12.dp),
//        modifier = Modifier.height(100.dp)
//    ) {
//        items(colors) { color ->
//            ColorItem(
//                color = color,
//                isSelected = color == selectedColor,
//                onClick = { onColorSelect(color) }
//            )
//        }
//    }
//}
//
//@Composable
//fun ColorItem(
//    color: Color,
//    isSelected: Boolean,
//    onClick: () -> Unit
//) {
//    Box(
//        modifier = Modifier
//            .size(48.dp)
//            .clip(CircleShape)
//            .background(color)
//            .border(
//                width = if (isSelected) 3.dp else 0.dp,
//                color = MaterialTheme.colorScheme.onSurface,
//                shape = CircleShape
//            )
//            .clickable(onClick = onClick),
//        contentAlignment = Alignment.Center
//    ) {
//        if (isSelected) {
//            Icon(
//                imageVector = Icons.Default.Check,
//                contentDescription = "Selected",
//                tint = Color.White
//            )
//        }
//    }
//}
//
//// ============================================================================
//// PART 8: NAVIGATION
//// ============================================================================
//
//// navigation/NavGraph.kt
//package com.example.habittracker.navigation
//
//import androidx.compose.runtime.Composable
//import androidx.navigation.NavHostController
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import com.example.habittracker.presentation.addhabit.AddHabitScreen
//import com.example.habittracker.presentation.home.HomeScreen
//
//sealed class Screen(val route: String) {
//    object Home : Screen("home")
//    object AddHabit : Screen("add_habit")
//    object HabitDetail : Screen("habit_detail/{habitId}") {
//        fun createRoute(habitId: Long) = "habit_detail/$habitId"
//    }
//}
//
//@Composable
//fun NavGraph(navController: NavHostController) {
//    NavHost(
//        navController = navController,
//        startDestination = Screen.Home.route
//    ) {
//        composable(Screen.Home.route) {
//            HomeScreen(
//                onNavigateToAddHabit = {
//                    navController.navigate(Screen.AddHabit.route)
//                },
//                onNavigateToDetail = { habitId ->
//                    navController.navigate(Screen.HabitDetail.createRoute(habitId))
//                }
//            )
//        }
//
//        composable(Screen.AddHabit.route) {
//            AddHabitScreen(
//                onNavigateBack = { navController.popBackStack() }
//            )
//        }
//    }
//}
//
//// ============================================================================
//// PART 9: MAIN APPLICATION
//// ============================================================================
//
//// MainActivity.kt
//package com.example.habittracker
//
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Surface
//import androidx.compose.ui.Modifier
//import androidx.navigation.compose.rememberNavController
//import com.example.habittracker.navigation.NavGraph
//import com.example.habittracker.ui.theme.HabitTrackerTheme
//import dagger.hilt.android.AndroidEntryPoint
//
//@AndroidEntryPoint
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            HabitTrackerTheme {
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
//                ) {
//                    val navController = rememberNavController()
//                    NavGraph(navController = navController)
//                }
//            }
//        }
//    }
//}
//
//// HabitTrackerApplication.kt
//package com.example.habittracker
//
//import android.app.Application
//import dagger.hilt.android.HiltAndroidApp
//
//@HiltAndroidApp
//class HabitTrackerApplication : Application()
//
//// ============================================================================
//// PART 10: THEME
//// ============================================================================
//
//// ui/theme/Color.kt
//package com.example.habittracker.ui.theme
//
//import androidx.compose.ui.graphics.Color
//
//val Purple80 = Color(0xFFD0BCFF)
//val PurpleGrey80 = Color(0xFFCCC2DC)
//val Pink80 = Color(0xFFEFB8C8)
//
//val Purple40 = Color(0xFF6650a4)
//val PurpleGrey40 = Color(0xFF625b71)
//val Pink40 = Color(0xFF7D5260)
//
//// ui/theme/Theme.kt
//package com.example.habittracker.ui.theme
//
//import android.app.Activity
//import androidx.compose.foundation.isSystemInDarkTheme
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.darkColorScheme
//import androidx.compose.material3.lightColorScheme
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.SideEffect
//import androidx.compose.ui.graphics.toArgb
//import androidx.compose.ui.platform.LocalView
//import androidx.core.view.WindowCompat
//
//private val DarkColorScheme = darkColorScheme(
//    primary = Purple80,
//    secondary = PurpleGrey80,
//    tertiary = Pink80
//)
//
//private val LightColorScheme = lightColorScheme(
//    primary = Purple40,
//    secondary = PurpleGrey40,
//    tertiary = Pink40
//)
//
//@Composable
//fun HabitTrackerTheme(
//    darkTheme: Boolean = isSystemInDarkTheme(),
//    content: @Composable () -> Unit
//) {
//    val colorScheme = when {
//        darkTheme -> DarkColorScheme
//        else -> LightColorScheme
//    }
//
//    val view = LocalView.current
//    if (!view.isInEditMode) {
//        SideEffect {
//            val window = (view.context as Activity).window
//            window.statusBarColor = colorScheme.primary.toArgb()
//            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
//        }
//    }
//
//    MaterialTheme(
//        colorScheme = colorScheme,
//        typography = Typography,
//        content = content
//    )
//}
//
//// ui/theme/Type.kt
//package com.example.habittracker.ui.theme
//
//import androidx.compose.material3.Typography
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.font.FontFamily
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.sp
//
//val Typography = Typography(
//    bodyLarge = TextStyle(
//        fontFamily = FontFamily.Default,
//        fontWeight = FontWeight.Normal,
//        fontSize = 16.sp,
//        lineHeight = 24.sp,
//        letterSpacing = 0.5.sp
//    )
//)
//
//// ============================================================================
//// PART 11: ANDROID MANIFEST
//// ============================================================================
//
//// AndroidManifest.xml
//<?xml version="1.0" encoding="utf-8"?>
//<manifest xmlns:android="http://schemas.android.com/apk/res/android">
//
//    <application
//        android:name=".HabitTrackerApplication"
//        android:allowBackup="true"
//        android:icon="@mipmap/ic_launcher"
//        android:label="Habit Tracker"
//        android:roundIcon="@mipmap/ic_launcher_round"
//        android:supportsRtl="true"
//        android:theme="@style/Theme.HabitTracker">
//        <activity
//            android:name=".MainActivity"
//            android:exported="true"
//            android:theme="@style/Theme.HabitTracker">
//            <intent-filter>
//                <action android:name="android.intent.action.MAIN" />
//                <category android:name="android.intent.category.LAUNCHER" />
//            </intent-filter>
//        </activity>
//    </application>
//
//</manifest>
//
//// ============================================================================
//// SETUP INSTRUCTIONS
//// ============================================================================
//
///*
//SETUP INSTRUCTIONS:
//
//1. Create a new Android Studio project (Empty Activity)
//
//2. Copy all the dependency configurations from the build files above
//
//3. Create the package structure:
//   - com.example.habittracker
//   - com.example.habittracker.data.local.dao
//   - com.example.habittracker.data.local.entity
//   - com.example.habittracker.data.repository
//   - com.example.habittracker.domain.repository
//   - com.example.habittracker.di
//   - com.example.habittracker.presentation.home
//   - com.example.habittracker.presentation.addhabit
//   - com.example.habittracker.navigation
//   - com.example.habittracker.ui.theme
//
//4. Copy each class/file into its respective package
//
//5. Sync Gradle and build the project
//
//6. Run the app!
//
//FEATURES INCLUDED:
//✅ Room Database with Foreign Keys
//✅ Repository Pattern with Clean Architecture
//✅ Hilt Dependency Injection
//✅ MVVM with StateFlow
//✅ Jetpack Compose UI with Material 3
//✅ Swipe-to-Delete with Undo
//✅ Animated Checkbox completion
//✅ Streak tracking (current & longest)
//✅ Total completions counter
//✅ Icon & Color picker
//✅ Navigation Compose
//✅ Reactive UI updates with Flow
//
//NEXT STEPS TO ENHANCE:
//- Add WorkManager notifications
//- Create custom Canvas charts for statistics
//- Add detailed habit view with calendar
//- Implement weekly/monthly statistics
//- Add habit categories/tags
//- Create home screen widget
//*/