

package com.peu.habittracker.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.peu.habittracker.viewModel.AddHabitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitScreen(
    habitId: Long? = null,
    onNavigateBack: () -> Unit,
    viewModel: AddHabitViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()
    var isSaving by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Create New Habit",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Preview Card
                PreviewCard(
                    icon = state.selectedIcon,
                    color = state.selectedColor,
                    name = state.name.ifBlank { "Your Habit" }
                )

                // Name Input
                ModernTextField(
                    value = state.name,
                    onValueChange = viewModel::onNameChange,
                    label = "Habit Name",
                    placeholder = "e.g., Morning Exercise, Read 30 mins",
                    isError = state.error != null,
                    errorMessage = state.error,
                    icon = Icons.Default.Edit
                )

                // Description Input
                ModernTextField(
                    value = state.description,
                    onValueChange = viewModel::onDescriptionChange,
                    label = "Description (Optional)",
                    placeholder = "Why is this habit important to you?",
                    minLines = 3,
                    icon = Icons.Default.Notes
                )

                // Icon Selector Section
                SectionHeader(
                    title = "Choose Your Icon",
                    subtitle = "Pick an emoji that represents your habit"
                )

                ModernIconSelector(
                    selectedIcon = state.selectedIcon,
                    onIconSelect = viewModel::onIconSelect
                )

                // Color Selector Section
                SectionHeader(
                    title = "Choose Your Color",
                    subtitle = "Select a color theme"
                )

                ModernColorSelector(
                    selectedColor = state.selectedColor,
                    onColorSelect = viewModel::onColorSelect
                )

                Spacer(modifier = Modifier.height(80.dp))
            }

            // Floating Action Button
            ExtendedFloatingActionButton(
                onClick = {
                    isSaving = true
                    viewModel.saveHabit {
                        isSaving = false
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .scale(if (isSaving) 0.9f else 1f),
                containerColor = state.selectedColor,
                contentColor = Color.White,
                icon = {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Check, "Save")
                    }
                },
                text = {
                    Text(
                        "Create Habit",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    }
}

@Composable
fun PreviewCard(
    icon: String,
    color: Color,
    name: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            var isAnimating by remember { mutableStateOf(false) }

            LaunchedEffect(icon) {
                isAnimating = true
                kotlinx.coroutines.delay(300)
                isAnimating = false
            }

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(if (isAnimating) 1.2f else 1f)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                color,
                                color.copy(alpha = 0.7f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.displayMedium
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Preview",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        subtitle?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isError: Boolean = false,
    errorMessage: String? = null,
    minLines: Int = 1,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            isError = isError,
            minLines = minLines,
            shape = RoundedCornerShape(16.dp),
            leadingIcon = icon?.let {
                { Icon(it, null, tint = MaterialTheme.colorScheme.primary) }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
        )

        if (isError && errorMessage != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// ============================================================================
// FIXED: Icon Selector using FlowRow instead of LazyVerticalGrid
// ============================================================================

@Composable
fun ModernIconSelector(
    selectedIcon: String,
    onIconSelect: (String) -> Unit
) {
    val iconCategories = mapOf(
        "Activities" to listOf("🎯", "💪", "🏃", "🧘", "🚴", "⚽"),
        "Learning" to listOf("📚", "✍️", "💻", "🎨", "🎵", "🧠"),
        "Health" to listOf("💧", "🍎", "😴", "🫀", "🦷", "🥗"),
        "Lifestyle" to listOf("🌱", "❤️", "⭐", "🔥", "🌟", "✨")
    )

    var selectedCategory by remember { mutableStateOf("Activities") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Category Tabs
        ScrollableTabRow(
            selectedTabIndex = iconCategories.keys.indexOf(selectedCategory),
            containerColor = Color.Transparent,
            edgePadding = 0.dp
        ) {
            iconCategories.keys.forEach { category ->
                Tab(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    text = {
                        Text(
                            category,
                            fontWeight = if (selectedCategory == category)
                                FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        // Icon Grid using FlowRow (NO LazyGrid inside scroll!)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 6
            ) {
                (iconCategories[selectedCategory] ?: emptyList()).forEach { icon ->
                    ModernIconItem(
                        icon = icon,
                        isSelected = icon == selectedIcon,
                        onClick = { onIconSelect(icon) }
                    )
                }
            }
        }
    }
}

@Composable
fun ModernIconItem(
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "icon_scale"
    )

    Box(
        modifier = Modifier
            .size(48.dp)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surface
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

// ============================================================================
// FIXED: Color Selector using FlowRow instead of LazyVerticalGrid
// ============================================================================

@Composable
fun ModernColorSelector(
    selectedColor: Color,
    onColorSelect: (Color) -> Unit
) {
    val colorPalettes = listOf(
        "Vibrant" to listOf(
            Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF673AB7),
            Color(0xFF3F51B5), Color(0xFF2196F3), Color(0xFF00BCD4)
        ),
        "Warm" to listOf(
            Color(0xFFFF5722), Color(0xFFFF6B6B), Color(0xFFFF9800),
            Color(0xFFFFA726), Color(0xFFFFCA28), Color(0xFFFFEB3B)
        ),
        "Cool" to listOf(
            Color(0xFF009688), Color(0xFF26A69A), Color(0xFF4ECDC4),
            Color(0xFF66BB6A), Color(0xFF8BC34A), Color(0xFF42A5F5)
        ),
        "Modern" to listOf(
            Color(0xFF6200EE), Color(0xFF7E57C2), Color(0xFFAB47BC),
            Color(0xFF26C6DA), Color(0xFF29B6F6), Color(0xFF5C6BC0)
        )
    )

    var selectedPalette by remember { mutableStateOf("Vibrant") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Palette Tabs
        ScrollableTabRow(
            selectedTabIndex = colorPalettes.indexOfFirst { it.first == selectedPalette },
            containerColor = Color.Transparent,
            edgePadding = 0.dp
        ) {
            colorPalettes.forEach { (palette, _) ->
                Tab(
                    selected = selectedPalette == palette,
                    onClick = { selectedPalette = palette },
                    text = {
                        Text(
                            palette,
                            fontWeight = if (selectedPalette == palette)
                                FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        // Color Grid using FlowRow (NO LazyGrid inside scroll!)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            val colors = colorPalettes.find { it.first == selectedPalette }?.second ?: emptyList()

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                maxItemsInEachRow = 6
            ) {
                colors.forEach { color ->
                    ModernColorItem(
                        color = color,
                        isSelected = color == selectedColor,
                        onClick = { onColorSelect(color) }
                    )
                }
            }
        }
    }
}

@Composable
fun ModernColorItem(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "color_scale"
    )

    Box(
        modifier = Modifier
            .size(52.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) 4.dp else 0.dp,
                color = Color.White,
                shape = CircleShape
            )
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = isSelected,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}