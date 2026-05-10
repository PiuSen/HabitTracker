package com.peu.habittracker.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.peu.habittracker.screen.component.CategorySelector
import com.peu.habittracker.viewModel.AddHabitViewModel
import kotlin.math.cos
import kotlin.math.sin

// ─────────────────────────────────────────────────────────────────────────────
// DESIGN TOKENS  (shared with HomeScreen)
// ─────────────────────────────────────────────────────────────────────────────
private val NightBase     = Color(0xFF0A0D1A)
private val NightSurface  = Color(0xFF111827)
private val CardDark      = Color(0xFF1A2035)
private val CardDarker    = Color(0xFF141929)
private val NeonViolet    = Color(0xFF7C6FFF)
private val NeonCyan      = Color(0xFF00D4FF)
private val NeonPink      = Color(0xFFFF6FD8)
private val NeonGreen     = Color(0xFF00F5A0)
private val NeonAmber     = Color(0xFFFFB800)
private val TextPrimary   = Color(0xFFF1F3FF)
private val TextSecondary = Color(0xFF8B90A8)
private val GlassWhite    = Color(0x14FFFFFF)
private val GlassStroke   = Color(0x26FFFFFF)

private val VioletGrad = listOf(NeonViolet, NeonCyan)
private val PinkGrad   = listOf(NeonPink, NeonViolet)
private val GreenGrad  = listOf(NeonGreen, Color(0xFF00B4DB))
private val AmberGrad  = listOf(NeonAmber, Color(0xFFFF6B00))

// ─────────────────────────────────────────────────────────────────────────────
// MAIN SCREEN
// ─────────────────────────────────────────────────────────────────────────────
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

    var currentStep by remember { mutableStateOf(0) }
    val steps = listOf("Basics", "Icon", "Color", "Schedule")

    // ✅ SAFE step progression (NO composition side-effect)
    LaunchedEffect(state.name) {
        if (currentStep == 0 && state.name.isNotBlank()) {
            currentStep = 1
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NightBase)
    ) {

        AddHabitAmbientOrbs()

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                AddHabitTopBar(
                    onBack = onNavigateBack,
                    currentStep = currentStep,
                    steps = steps
                )
            }
        ) { padding ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {

                    Spacer(Modifier.height(4.dp))

                    NeonPreviewCard(
                        icon = state.selectedIcon,
                        color = state.selectedColor,
                        name = state.name.ifBlank { "Your New Habit" }
                    )

                    StepProgressBar(currentStep = currentStep, steps = steps)

                    // ───────── STEP 0 ─────────
                    DarkSectionCard(
                        title = "01  Habit Basics",
                        subtitle = "Name & describe your habit",
                        gradient = VioletGrad,
                        isActive = currentStep >= 0
                    ) {

                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

                            NeonTextField(
                                value = state.name,
                                onValueChange = viewModel::onNameChange, // ✅ FIXED
                                label = "Habit Name",
                                placeholder = "e.g., Morning Run",
                                isError = state.error != null,
                                errorMessage = state.error,
                                icon = Icons.Default.Edit,
                                accentColor = NeonViolet
                            )

                            NeonTextField(
                                value = state.description,
                                onValueChange = viewModel::onDescriptionChange,
                                label = "Why this habit?",
                                placeholder = "Your motivation",
                                minLines = 3,
                                icon = Icons.Default.Notes,
                                accentColor = NeonCyan
                            )

                            var selectedCategoryId by remember { mutableStateOf<Long?>(null) }

                            CategorySelector(
                                selectedCategoryId = selectedCategoryId,
                                onCategorySelect = {
                                    selectedCategoryId = it?.id
                                    viewModel.onCategorySelect(it?.id)
                                }
                            )
                        }
                    }

                    // ───────── STEP 1 ─────────
                    DarkSectionCard(
                        title = "02  Choose Icon",
                        subtitle = "Pick an emoji",
                        gradient = PinkGrad,
                        isActive = currentStep >= 1
                    ) {
                        NeonIconSelector(
                            selectedIcon = state.selectedIcon,
                            onIconSelect = {
                                viewModel.onIconSelect(it)
                                currentStep = 2
                            }
                        )
                    }

                    // ───────── STEP 2 ─────────
                    DarkSectionCard(
                        title = "03  Choose Color",
                        subtitle = "Accent color",
                        gradient = GreenGrad,
                        isActive = currentStep >= 2
                    ) {
                        NeonColorSelector(
                            selectedColor = state.selectedColor,
                            onColorSelect = {
                                viewModel.onColorSelect(it)
                                currentStep = 3
                            }
                        )
                    }

                    // ───────── STEP 3 ─────────
                    DarkSectionCard(
                        title = "04  Schedule",
                        subtitle = "When?",
                        gradient = AmberGrad,
                        isActive = currentStep >= 3
                    ) {
                        FrequencySelector()
                    }
                }

                NeonSaveButton(
                    isSaving = isSaving,
                    isReady = state.name.isNotBlank(),
                    accentColor = state.selectedColor,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(24.dp),
                    onClick = {
                        isSaving = true
                        viewModel.saveHabit {
                            isSaving = false
                            onNavigateBack()
                        }
                    }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AMBIENT ORBS
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AddHabitAmbientOrbs() {
    val inf = rememberInfiniteTransition(label = "aorbs")
    val t by inf.animateFloat(
        0f, 1f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing), RepeatMode.Reverse),
        label = "at"
    )
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            brush  = Brush.radialGradient(
                listOf(NeonViolet.copy(alpha = 0.14f + t * 0.06f), Color.Transparent),
                radius = size.width * 0.5f, center = Offset(size.width * 0.9f, size.height * 0.08f)
            ),
            radius = size.width * 0.5f, center = Offset(size.width * 0.9f, size.height * 0.08f)
        )
        drawCircle(
            brush  = Brush.radialGradient(
                listOf(NeonPink.copy(alpha = 0.09f + t * 0.04f), Color.Transparent),
                radius = size.width * 0.4f, center = Offset(0f, size.height * 0.5f)
            ),
            radius = size.width * 0.4f, center = Offset(0f, size.height * 0.5f)
        )
        drawCircle(
            brush  = Brush.radialGradient(
                listOf(NeonCyan.copy(alpha = 0.07f), Color.Transparent),
                radius = size.width * 0.35f, center = Offset(size.width * 0.5f, size.height * 0.9f)
            ),
            radius = size.width * 0.35f, center = Offset(size.width * 0.5f, size.height * 0.9f)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TOP BAR
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddHabitTopBar(
    onBack: () -> Unit,
    currentStep: Int,
    steps: List<String>
) {
    Surface(color = NightBase, tonalElevation = 0.dp) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(GlassWhite)
                        .border(1.dp, GlassStroke, CircleShape)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = NeonViolet,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Create Habit",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        "Step ${currentStep + 1} of ${steps.size}  ·  ${steps[currentStep]}",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonViolet
                    )
                }
                // Completion badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = NeonViolet.copy(alpha = 0.15f),
                    modifier = Modifier.border(1.dp, NeonViolet.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                ) {
                    Text(
                        "${((currentStep + 1f) / steps.size * 100).toInt()}%",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = NeonViolet,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// STEP PROGRESS BAR
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun StepProgressBar(currentStep: Int, steps: List<String>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { idx, label ->
            val isActive   = idx == currentStep
            val isDone     = idx < currentStep
            val w by animateFloatAsState(
                if (isActive) 2.5f else 1f,
                spring(Spring.DampingRatioMediumBouncy), label = "pw$idx"
            )
            Box(
                modifier = Modifier
                    .weight(w)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        when {
                            isDone   -> Brush.horizontalGradient(VioletGrad)
                            isActive -> Brush.horizontalGradient(listOf(NeonViolet, NeonCyan))
                            else     -> Brush.horizontalGradient(listOf(GlassWhite, GlassWhite))
                        }
                    )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// NEON PREVIEW CARD
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun NeonPreviewCard(icon: String, color: Color, name: String) {
    val inf = rememberInfiniteTransition(label = "prev")
    val pulse by inf.animateFloat(
        0.92f, 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    var lastIcon by remember { mutableStateOf(icon) }
    var iconScale by remember { mutableStateOf(1f) }
    val animIconScale by animateFloatAsState(iconScale, spring(Spring.DampingRatioLowBouncy), label = "is")
    LaunchedEffect(icon) {
        if (icon != lastIcon) {
            iconScale = 0f; kotlinx.coroutines.delay(120); iconScale = 1f; lastIcon = icon
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF1B1F3B), Color(0xFF0E1225)),
                    Offset.Zero, Offset(1000f, 1000f)
                )
            )
            .border(1.dp, GlassStroke, RoundedCornerShape(28.dp))
    ) {
        // subtle dot grid
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 24.dp.toPx()
            var x = step / 2
            while (x < size.width) {
                var y = step / 2
                while (y < size.height) {
                    drawCircle(Color.White.copy(alpha = 0.04f), radius = 1.5f, center = Offset(x, y))
                    y += step
                }
                x += step
            }
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Animated icon orb
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .scale(pulse),
                contentAlignment = Alignment.Center
            ) {
                // Glow ring
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        brush  = Brush.radialGradient(
                            listOf(color.copy(alpha = 0.35f), Color.Transparent)
                        ),
                        radius = size.minDimension / 2
                    )
                }
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(color.copy(alpha = 0.9f), color.copy(alpha = 0.6f)))
                        )
                        .border(1.dp, color.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        icon.ifBlank { "✨" },
                        style = MaterialTheme.typography.displaySmall,
                        modifier = Modifier.scale(animIconScale)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = GlassWhite,
                    modifier = Modifier.border(1.dp, GlassStroke, RoundedCornerShape(20.dp))
                ) {
                    Text(
                        "Live Preview",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = color,
                    letterSpacing = (-0.5).sp,
                    maxLines = 2
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocalFireDepartment, null,
                        tint = NeonAmber, modifier = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(3.dp))
                    Text("0 day streak", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DARK SECTION CARD  (collapsible-feel, numbered)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun DarkSectionCard(
    title: String,
    subtitle: String,
    gradient: List<Color>,
    isActive: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CardDark)
            .border(
                1.dp,
                if (isActive) gradient.first().copy(alpha = 0.35f) else GlassStroke,
                RoundedCornerShape(24.dp)
            )
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // ONLY HEADER ANIMATES (safe)
            Row(
                modifier = Modifier.alpha(if (isActive) 1f else 0.6f),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Brush.linearGradient(gradient)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        title.take(2),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column {
                    Text(
                        title.drop(4),
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        subtitle,
                        color = TextSecondary
                    )
                }
            }

            // ✅ CRITICAL FIX: NO ANIMATION WRAP AROUND TEXTFIELDS
            Column {
                content()
            }
        }
    }
}
// ─────────────────────────────────────────────────────────────────────────────
// NEON TEXT FIELD
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeonTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isError: Boolean = false,
    errorMessage: String? = null,
    minLines: Int = 1,
    icon: ImageVector? = null,
    accentColor: Color = NeonViolet
) {
    var isFocused by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        isError = isError,
        minLines = minLines,
        singleLine = false,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        leadingIcon = icon?.let { { Icon(it, null) } },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = accentColor,
            unfocusedBorderColor = GlassStroke,
            cursorColor = accentColor
        )
    )

    if (isError && errorMessage != null) {
        Text(
            text = errorMessage,
            color = Color.Red
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// NEON SECTION LABEL
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun NeonSectionLabel(text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.width(3.dp).height(16.dp).clip(RoundedCornerShape(2.dp)).background(color))
        Text(text, style = MaterialTheme.typography.labelLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// NEON ICON SELECTOR
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun NeonIconSelector(selectedIcon: String, onIconSelect: (String) -> Unit) {
    val categories = linkedMapOf(
        "⚡ Active"   to listOf("🎯","💪","🏃","🧘","🚴","⚽","🏋️","🤸","🏊","🎾","🧗","🚀"),
        "📖 Learn"    to listOf("📚","✍️","💻","🎨","🎵","🧠","🔬","🎓","📝","🌍","🎭","🔭"),
        "💚 Health"   to listOf("💧","🍎","😴","🫀","🦷","🥗","🧃","🏥","🧬","🥦","🍵","🛁"),
        "✨ Life"     to listOf("🌱","❤️","⭐","🔥","🌟","✨","🏠","🌈","🦋","🌸","🎁","💰")
    )
    var selectedCat by remember { mutableStateOf(categories.keys.first()) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Category chips
        androidx.compose.foundation.lazy.LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories.keys.toList()) { cat ->
                val isSel = cat == selectedCat
                Box(
                    modifier = Modifier
                        .height(32.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(
                            if (isSel) Brush.horizontalGradient(PinkGrad)
                            else Brush.horizontalGradient(listOf(GlassWhite, GlassWhite))
                        )
                        .border(1.dp, if (isSel) Color.Transparent else GlassStroke, RoundedCornerShape(50.dp))
                        .clickable { selectedCat = cat }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        cat,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSel) Color.White else TextSecondary
                    )
                }
            }
        }

        // Icon grid
        FlowRow(
            modifier                = Modifier.fillMaxWidth(),
            horizontalArrangement   = Arrangement.spacedBy(10.dp),
            verticalArrangement     = Arrangement.spacedBy(10.dp),
            maxItemsInEachRow       = 6
        ) {
            (categories[selectedCat] ?: emptyList()).forEach { ic ->
                NeonIconItem(icon = ic, isSelected = ic == selectedIcon, accentGrad = PinkGrad) {
                    onIconSelect(ic)
                }
            }
        }
    }
}

@Composable
private fun NeonIconItem(
    icon: String,
    isSelected: Boolean,
    accentGrad: List<Color>,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        if (isSelected) 1.18f else 1f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "isc"
    )
    Box(
        modifier = Modifier
            .size(50.dp)
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isSelected) Brush.linearGradient(accentGrad)
                else Brush.linearGradient(listOf(GlassWhite, GlassWhite))
            )
            .border(
                1.dp,
                if (isSelected) accentGrad[0].copy(alpha = 0f) else GlassStroke,
                RoundedCornerShape(14.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(icon, style = MaterialTheme.typography.titleLarge)
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.9f))
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, null, tint = accentGrad[0], modifier = Modifier.size(10.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// NEON COLOR SELECTOR
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun NeonColorSelector(selectedColor: Color, onColorSelect: (Color) -> Unit) {
    val palettes = listOf(
        "Neon"    to listOf(Color(0xFF7C6FFF), Color(0xFF00D4FF), Color(0xFFFF6FD8), Color(0xFF00F5A0), Color(0xFFFFB800), Color(0xFFFF4757)),
        "Sunset"  to listOf(Color(0xFFFF5722), Color(0xFFFF6B6B), Color(0xFFFF9800), Color(0xFFFFA726), Color(0xFFFFCA28), Color(0xFFE91E63)),
        "Ocean"   to listOf(Color(0xFF009688), Color(0xFF26A69A), Color(0xFF4ECDC4), Color(0xFF42A5F5), Color(0xFF29B6F6), Color(0xFF00BCD4)),
        "Galaxy"  to listOf(Color(0xFF6200EE), Color(0xFF7E57C2), Color(0xFFAB47BC), Color(0xFF5C6BC0), Color(0xFF3F51B5), Color(0xFF2196F3))
    )
    var selectedPalette by remember { mutableStateOf("Neon") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Palette chips with gradient preview
        androidx.compose.foundation.lazy.LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(palettes) { (name, colors) ->
                val isSel = name == selectedPalette
                Box(
                    modifier = Modifier
                        .height(32.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(
                            if (isSel) Brush.horizontalGradient(listOf(colors[0], colors[2]))
                            else Brush.horizontalGradient(listOf(GlassWhite, GlassWhite))
                        )
                        .border(1.dp, if (isSel) Color.Transparent else GlassStroke, RoundedCornerShape(50.dp))
                        .clickable { selectedPalette = name }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        name,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSel) Color.White else TextSecondary
                    )
                }
            }
        }

        // Color circles
        val colors = palettes.find { it.first == selectedPalette }?.second ?: emptyList()
        FlowRow(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement   = Arrangement.spacedBy(12.dp),
            maxItemsInEachRow     = 6
        ) {
            colors.forEach { c ->
                NeonColorItem(color = c, isSelected = c == selectedColor) { onColorSelect(c) }
            }
        }
    }
}

@Composable
private fun NeonColorItem(color: Color, isSelected: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(
        if (isSelected) 1.22f else 1f,
        spring(Spring.DampingRatioMediumBouncy), label = "csc"
    )
    Box(
        modifier = Modifier
            .size(52.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(color)
            .then(
                if (isSelected) Modifier.border(3.dp, Color.White, CircleShape)
                else Modifier.border(1.dp, color.copy(alpha = 0.3f), CircleShape)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(visible = isSelected, enter = scaleIn() + fadeIn(), exit = scaleOut() + fadeOut()) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FREQUENCY / SCHEDULE SELECTOR  (new premium feature)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun FrequencySelector() {
    val frequencies = listOf("Daily", "Weekdays", "Weekends", "Custom")
    var selected by remember { mutableStateOf("Daily") }

    val days = listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")
    val defaultOn = setOf("Mo", "Tu", "We", "Th", "Fr")
    var activeDays by remember { mutableStateOf(defaultOn) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Frequency chips
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            frequencies.forEach { freq ->
                val isSel = freq == selected
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSel) Brush.linearGradient(AmberGrad)
                            else Brush.horizontalGradient(listOf(GlassWhite, GlassWhite))
                        )
                        .border(1.dp, if (isSel) Color.Transparent else GlassStroke, RoundedCornerShape(10.dp))
                        .clickable {
                            selected = freq
                            activeDays = when (freq) {
                                "Daily"    -> days.toSet()
                                "Weekdays" -> setOf("Mo","Tu","We","Th","Fr")
                                "Weekends" -> setOf("Sa","Su")
                                else       -> activeDays
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        freq,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSel) Color.White else TextSecondary
                    )
                }
            }
        }

        // Day toggles
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            days.forEach { day ->
                val isOn = day in activeDays
                val bg by animateColorAsState(
                    if (isOn) NeonAmber.copy(alpha = 0.18f) else GlassWhite,
                    tween(200), label = "d$day"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(bg)
                        .border(
                            1.dp,
                            if (isOn) NeonAmber.copy(alpha = 0.5f) else GlassStroke,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            activeDays = if (isOn) activeDays - day else activeDays + day
                            selected = "Custom"
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        day,
                        style     = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isOn) FontWeight.Bold else FontWeight.Normal,
                        color     = if (isOn) NeonAmber else TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Reminder time row
        NeonSectionLabel("Reminder Time", NeonCyan)
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            listOf("06:00 AM", "08:00 AM", "12:00 PM", "09:00 PM").forEach { time ->
                var isSel by remember { mutableStateOf(time == "08:00 AM") }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSel) Brush.horizontalGradient(listOf(NeonCyan.copy(0.2f), NeonViolet.copy(0.15f)))
                            else Brush.horizontalGradient(listOf(GlassWhite, GlassWhite))
                        )
                        .border(1.dp, if (isSel) NeonCyan.copy(0.5f) else GlassStroke, RoundedCornerShape(10.dp))
                        .clickable { isSel = !isSel },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        time,
                        style     = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                        color     = if (isSel) NeonCyan else TextSecondary
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// NEON SAVE BUTTON
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun NeonSaveButton(
    isSaving: Boolean,
    isReady: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val inf = rememberInfiniteTransition(label = "saveglow")
    val glow by inf.animateFloat(
        0.5f, 1f,
        animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "sg"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (isReady)
                    Brush.horizontalGradient(listOf(NeonViolet, NeonCyan))
                else
                    Brush.horizontalGradient(listOf(GlassWhite, GlassWhite))
            )
            .then(
                if (isReady)
                    Modifier.drawBehind {
                        drawRoundRect(
                            brush  = Brush.horizontalGradient(listOf(NeonViolet.copy(alpha = 0.3f * glow), NeonCyan.copy(alpha = 0.3f * glow))),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(18.dp.toPx()),
                            style  = Stroke(width = 8.dp.toPx())
                        )
                    }
                else Modifier
            )
            .clickable(enabled = isReady && !isSaving, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp
                )
                Text("Saving…", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = Color.White)
            } else {
                Icon(
                    Icons.Default.RocketLaunch, null,
                    tint = if (isReady) Color.White else TextSecondary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    "Launch Habit",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = if (isReady) Color.White else TextSecondary,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FOCUS MODIFIER HELPER
// ─────────────────────────────────────────────────────────────────────────────
private fun Modifier.onFocusChanged(
    onFocusChange: (androidx.compose.ui.focus.FocusState) -> Unit
): Modifier = this.then(
    androidx.compose.ui.Modifier.composed {
        onFocusChanged(onFocusChange)
    }
)