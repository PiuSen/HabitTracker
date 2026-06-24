package com.peu.habittracker.screen


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.peu.habittracker.viewModel.ExportViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

// ─────────────────────────────────────────────────────────────────────────────
// DESIGN TOKENS
// ─────────────────────────────────────────────────────────────────────────────
private val NightBase     = Color(0xFF0A0D1A)
private val CardDark      = Color(0xFF1A2035)
private val CardDarker    = Color(0xFF141929)
private val NeonViolet    = Color(0xFF7C6FFF)
private val NeonCyan      = Color(0xFF00D4FF)
private val NeonPink      = Color(0xFFFF6FD8)
private val NeonGreen     = Color(0xFF00F5A0)
private val NeonAmber     = Color(0xFFFFB800)
private val NeonRed       = Color(0xFFFF4757)
private val TextPrimary   = Color(0xFFF1F3FF)
private val TextSecondary = Color(0xFF8B90A8)
private val GlassWhite    = Color(0x14FFFFFF)
private val GlassStroke   = Color(0x26FFFFFF)

private val VioletGrad = listOf(NeonViolet, NeonCyan)
private val GreenGrad  = listOf(NeonGreen, Color(0xFF00B4DB))
private val AmberGrad  = listOf(NeonAmber, Color(0xFFFF6B00))
private val PinkGrad   = listOf(NeonPink, NeonViolet)

// ─────────────────────────────────────────────────────────────────────────────
// DATA FORMAT MODEL
// ─────────────────────────────────────────────────────────────────────────────
enum class ExportFormat(
    val label: String,
    val emoji: String,
    val description: String,
    val extension: String,
    val gradient: List<Color>
) {
    CSV(
        label       = "CSV",
        emoji       = "📊",
        description = "Spreadsheet-compatible.\nOpens in Excel & Google Sheets.",
        extension   = ".csv",
        gradient    = listOf(NeonGreen, Color(0xFF00B4DB))
    ),
    JSON(
        label       = "JSON",
        emoji       = "📦",
        description = "Developer-friendly format.\nPerfect for backups & restore.",
        extension   = ".json",
        gradient    = listOf(NeonViolet, NeonCyan)
    ),
    TXT(
        label       = "TXT",
        emoji       = "📝",
        description = "Plain text summary.\nReadable in any app.",
        extension   = ".txt",
        gradient    = listOf(NeonAmber, Color(0xFFFF6B00))
    )
}

enum class ExportRange(val label: String, val days: Int?) {
    ALL("All Time", null),
    MONTH("Last 30 Days", 30),
    WEEK("Last 7 Days", 7)
}

// ─────────────────────────────────────────────────────────────────────────────
// MAIN SCREEN
// ─────────────────────────────────────────────────────────────────────────────
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ExportDataScreen(
    onNavigateBack: () -> Unit,
    viewModel: ExportViewModel = hiltViewModel()
) {
    val context         = LocalContext.current
    val scope           = rememberCoroutineScope()
    val exportStats     by viewModel.exportStats.collectAsState()
    var selectedFormat  by remember { mutableStateOf(ExportFormat.CSV) }
    var selectedRange   by remember { mutableStateOf(ExportRange.ALL) }
    var includeNotes    by remember { mutableStateOf(true) }
    var includeStreaks  by remember { mutableStateOf(true) }
    var isExporting     by remember { mutableStateOf(false) }
    var exportProgress  by remember { mutableStateOf(0f) }
    var showSuccess     by remember { mutableStateOf(false) }
    var lastExportedUri by remember { mutableStateOf<Uri?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(NightBase)) {
        ExportOrbs()

//
        Scaffold(containerColor = Color.Transparent) { padding ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {


                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ExportTopBar(onBack = onNavigateBack)

                    Spacer(Modifier.height(4.dp))

                    ExportStatsCard(stats = exportStats)

                    ExportFormatSection(
                        selected = selectedFormat,
                        onSelect = { selectedFormat = it }
                    )

                    ExportRangeSection(
                        selected = selectedRange,
                        onSelect = { selectedRange = it }
                    )

                    ExportOptionsSection(
                        includeNotes = includeNotes,
                        includeStreaks = includeStreaks,
                        onNotesToggle = { includeNotes = it },
                        onStreakToggle = { includeStreaks = it }
                    )

                    ExportPreviewCard(
                        format = selectedFormat,
                        range = selectedRange,
                        includeNotes = includeNotes,
                        includeStreaks = includeStreaks,
                        stats = exportStats
                    )

                    AnimatedVisibility(
                        visible = showSuccess,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        ExportSuccessBanner(
                            uri = lastExportedUri,
                            format = selectedFormat,
                            context = context,
                            onDismiss = { showSuccess = false }
                        )
                    }
                }

                // ✅ FIX: FAB INSIDE Scaffold content Box
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                ) {
                    ExportButton(
                        isExporting = isExporting,
                        progress = exportProgress,
                        format = selectedFormat,
                        onClick = {
                            if (!isExporting) {
                                scope.launch {
                                    isExporting = true
                                    showSuccess = false
                                    exportProgress = 0f

                                    val steps = 20
                                    repeat(steps) {
                                        delay(60L)
                                        exportProgress = (it + 1).toFloat() / steps
                                    }

                                    val uri = viewModel.exportData(
                                        context = context,
                                        format = selectedFormat,
                                        range = selectedRange,
                                        includeNotes = includeNotes,
                                        includeStreaks = includeStreaks
                                    )

                                    lastExportedUri = uri
                                    isExporting = false
                                    exportProgress = 0f
                                    showSuccess = (uri != null)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AMBIENT ORBS
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ExportOrbs() {
    val inf = rememberInfiniteTransition(label = "eorbs")
    val t by inf.animateFloat(0f, 1f,
        infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Reverse), label = "et")
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            brush  = Brush.radialGradient(
                listOf(NeonGreen.copy(0.12f + t * 0.05f), Color.Transparent),
                radius = size.width * 0.5f, center = Offset(size.width * 0.85f, size.height * 0.1f)
            ), radius = size.width * 0.5f, center = Offset(size.width * 0.85f, size.height * 0.1f)
        )
        drawCircle(
            brush  = Brush.radialGradient(
                listOf(NeonViolet.copy(0.08f), Color.Transparent),
                radius = size.width * 0.4f, center = Offset(0f, size.height * 0.45f)
            ), radius = size.width * 0.4f, center = Offset(0f, size.height * 0.45f)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TOP BAR
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ExportTopBar(onBack: () -> Unit) {
    Surface(color = NightBase, tonalElevation = 0.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape)
                    .background(GlassWhite).border(1.dp, GlassStroke, CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null,
                    tint = NeonViolet, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Export Data", style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black, color = TextPrimary, letterSpacing = (-0.5).sp)
                Text("Download your habit history", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
            // Info badge
            Surface(shape = RoundedCornerShape(50.dp), color = NeonGreen.copy(0.12f),
                modifier = Modifier.border(1.dp, NeonGreen.copy(0.35f), RoundedCornerShape(50.dp))) {
                Text("📤 Free",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonGreen, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// STATS SUMMARY CARD
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ExportStatsCard(stats: ExportStats) {
    Box(
        modifier = Modifier.fillMaxWidth().height(140.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(
                listOf(Color(0xFF1B1F3B), Color(0xFF0E1225)), Offset.Zero, Offset(1000f, 600f)
            ))
            .border(1.dp, NeonGreen.copy(0.25f), RoundedCornerShape(28.dp))
            .padding(20.dp)
    ) {
        // dot grid
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val s = 22.dp.toPx(); var x = s / 2
            while (x < size.width) {
                var y = s / 2
                while (y < size.height) {
                    drawCircle(Color.White.copy(0.03f), 1.2f, Offset(x, y)); y += s
                }; x += s
            }
        }
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ExportStatPill("📋", "${stats.totalHabits}", "Habits", VioletGrad)
            ExportStatDivider()
            ExportStatPill("✅", "${stats.totalCompletions}", "Completions", GreenGrad)
            ExportStatDivider()
            ExportStatPill("🔥", "${stats.totalStreakDays}", "Streak Days", AmberGrad)
            ExportStatDivider()
            ExportStatPill("📅", stats.earliestDate, "Since", PinkGrad)
        }
    }
}

@Composable
private fun ExportStatPill(emoji: String, value: String, label: String, grad: List<Color>) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(emoji, style = MaterialTheme.typography.titleMedium)
        Text(value, style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black, color = TextPrimary)
        Text(label, style = MaterialTheme.typography.labelSmall, color = grad[0])
    }
}

@Composable
private fun ExportStatDivider() {
    Box(modifier = Modifier.width(1.dp).height(40.dp).background(GlassStroke))
}

// ─────────────────────────────────────────────────────────────────────────────
// FORMAT SECTION
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ExportFormatSection(selected: ExportFormat, onSelect: (ExportFormat) -> Unit) {
    ExportSectionLabel("📂  Export Format", VioletGrad)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ExportFormat.entries.forEach { fmt ->
            val isSel = fmt == selected
            Box(
                modifier = Modifier.weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        if (isSel) Brush.linearGradient(listOf(fmt.gradient[0].copy(0.18f), fmt.gradient[1].copy(0.08f)))
                        else Brush.linearGradient(listOf(CardDark, CardDark))
                    )
                    .border(
                        1.dp,
                        if (isSel) fmt.gradient[0].copy(0.5f) else GlassStroke,
                        RoundedCornerShape(18.dp)
                    )
                    .clickable { onSelect(fmt) }
                    .padding(14.dp)
            ) {
                // Top accent
                if (isSel) {
                    Box(modifier = Modifier.fillMaxWidth().height(2.dp)
                        .background(Brush.horizontalGradient(fmt.gradient))
                        .align(Alignment.TopCenter))
                }
                Column(
                    modifier = Modifier.padding(top = if (isSel) 6.dp else 0.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(fmt.emoji, style = MaterialTheme.typography.headlineSmall)
                    Text(fmt.label, style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = if (isSel) fmt.gradient[0] else TextPrimary)
                    Text(fmt.extension, style = MaterialTheme.typography.labelSmall,
                        color = if (isSel) fmt.gradient[0].copy(0.7f) else TextSecondary)
                    if (isSel) {
                        Surface(shape = RoundedCornerShape(50.dp), color = fmt.gradient[0].copy(0.15f)) {
                            Text("Selected ✓",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = fmt.gradient[0], fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
    // Description of selected
    Box(modifier = Modifier.fillMaxWidth()
        .clip(RoundedCornerShape(12.dp)).background(GlassWhite)
        .border(1.dp, GlassStroke, RoundedCornerShape(12.dp)).padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Default.Info, null, tint = selected.gradient[0], modifier = Modifier.size(16.dp))
            Text(selected.description, style = MaterialTheme.typography.labelSmall,
                color = TextSecondary, lineHeight = 17.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DATE RANGE SECTION
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ExportRangeSection(selected: ExportRange, onSelect: (ExportRange) -> Unit) {
    ExportSectionLabel("📅  Date Range", AmberGrad)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ExportRange.entries.forEach { range ->
            val isSel = range == selected
            Box(
                modifier = Modifier.weight(1f).height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isSel) Brush.linearGradient(AmberGrad.map { it.copy(0.18f) })
                        else Brush.linearGradient(listOf(CardDark, CardDark))
                    )
                    .border(1.dp, if (isSel) NeonAmber.copy(0.5f) else GlassStroke, RoundedCornerShape(14.dp))
                    .clickable { onSelect(range) },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(range.label, style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSel) FontWeight.Black else FontWeight.Medium,
                        color = if (isSel) NeonAmber else TextPrimary)
                    if (range.days != null) {
                        Text("${range.days} days", style = MaterialTheme.typography.labelSmall,
                            color = if (isSel) NeonAmber.copy(0.7f) else TextSecondary)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// OPTIONS SECTION
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ExportOptionsSection(
    includeNotes: Boolean,
    includeStreaks: Boolean,
    onNotesToggle: (Boolean) -> Unit,
    onStreakToggle: (Boolean) -> Unit
) {
    ExportSectionLabel("⚙️  Options", PinkGrad)
    Box(modifier = Modifier.fillMaxWidth()
        .clip(RoundedCornerShape(20.dp)).background(CardDark)
        .border(1.dp, NeonPink.copy(0.2f), RoundedCornerShape(20.dp))
    ) {
        Column {
            ExportOptionRow(
                icon       = Icons.Default.Notes,
                iconGrad   = PinkGrad,
                title      = "Include Notes",
                subtitle   = "Add personal notes to each habit record",
                checked    = includeNotes,
                onToggle   = onNotesToggle
            )
            Box(Modifier.padding(start = 68.dp).fillMaxWidth().height(1.dp).background(GlassStroke))
            ExportOptionRow(
                icon       = Icons.Default.LocalFireDepartment,
                iconGrad   = AmberGrad,
                title      = "Include Streaks",
                subtitle   = "Export streak history & best records",
                checked    = includeStreaks,
                onToggle   = onStreakToggle
            )
        }
    }
}

@Composable
private fun ExportOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconGrad: List<Color>,
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(modifier = Modifier.size(38.dp).clip(RoundedCornerShape(11.dp))
            .background(Brush.linearGradient(listOf(iconGrad[0].copy(0.2f), iconGrad[1].copy(0.1f))))
            .border(1.dp, iconGrad[0].copy(0.3f), RoundedCornerShape(11.dp)),
            contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = iconGrad[0], modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
        ExportNeonSwitch(checked = checked, onToggle = onToggle, gradient = iconGrad)
    }
}

@Composable
private fun ExportNeonSwitch(checked: Boolean, onToggle: (Boolean) -> Unit, gradient: List<Color>) {
    val thumbPos by animateFloatAsState(if (checked) 1f else 0f,
        spring(Spring.DampingRatioMediumBouncy), label = "esw")
    val trackColor by animateColorAsState(
        if (checked) gradient[0].copy(0.3f) else GlassWhite, tween(200), label = "etc")
    val borderColor by animateColorAsState(
        if (checked) gradient[0].copy(0.6f) else GlassStroke, tween(200), label = "ebc")
    Box(
        modifier = Modifier.width(48.dp).height(26.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(trackColor)
            .border(1.dp, borderColor, RoundedCornerShape(13.dp))
            .clickable { onToggle(!checked) }
    ) {
        Box(modifier = Modifier
            .offset(x = 3.dp + (22.dp * thumbPos))
            .align(Alignment.CenterStart)
            .size(20.dp).clip(CircleShape)
            .background(
                if (checked) Brush.linearGradient(gradient)
                else Brush.linearGradient(listOf(TextSecondary.copy(0.5f), TextSecondary.copy(0.3f)))
            ))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PREVIEW CARD
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ExportPreviewCard(
    format: ExportFormat,
    range: ExportRange,
    includeNotes: Boolean,
    includeStreaks: Boolean,
    stats: ExportStats
) {
    val preview = remember(format, range, includeNotes, includeStreaks, stats) {
        buildPreviewText(format, range, includeNotes, includeStreaks, stats)
    }

    ExportSectionLabel("👁  Preview", GreenGrad)
    Box(modifier = Modifier.fillMaxWidth()
        .clip(RoundedCornerShape(20.dp))
        .background(CardDarker)
        .border(1.dp, NeonGreen.copy(0.25f), RoundedCornerShape(20.dp))
        .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // File name preview
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))
                    .background(Brush.linearGradient(format.gradient.map { it.copy(0.2f) }))
                    .border(1.dp, format.gradient[0].copy(0.3f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center) {
                    Text(format.emoji, style = MaterialTheme.typography.bodySmall)
                }
                val dateStamp = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
                Text("habits_export_$dateStamp${format.extension}",
                    style = MaterialTheme.typography.labelMedium,
                    color = format.gradient[0], fontWeight = FontWeight.SemiBold)
            }

            // Code preview box
            Box(modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)).background(NightBase)
                .border(1.dp, GlassStroke, RoundedCornerShape(12.dp)).padding(12.dp)
            ) {
                Text(preview, style = MaterialTheme.typography.labelSmall,
                    color = NeonGreen.copy(0.85f), fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    lineHeight = 18.sp)
            }
        }
    }
}

private fun buildPreviewText(
    format: ExportFormat, range: ExportRange,
    includeNotes: Boolean, includeStreaks: Boolean, stats: ExportStats
): String {
    val rangeLabel = range.days?.let { "Last $it days" } ?: "All time"
    return when (format) {
        ExportFormat.CSV -> buildString {
            append("# HabitTracker Export — $rangeLabel\n")
            append("habit_name,date,completed")
            if (includeStreaks) append(",current_streak,best_streak")
            if (includeNotes)   append(",note")
            append("\n")
            append("Morning Run,2025-05-10,true")
            if (includeStreaks) append(",7,14")
            if (includeNotes)   append(",\"Great run!\"")
            append("\n")
            append("Read 30 min,2025-05-10,true")
            if (includeStreaks) append(",3,21")
            if (includeNotes)   append(",\"Finished chapter 5\"")
            append("\n...")
        }
        ExportFormat.JSON -> buildString {
            append("{\n  \"export\": {\n")
            append("    \"range\": \"$rangeLabel\",\n")
            append("    \"total_habits\": ${stats.totalHabits},\n")
            append("    \"habits\": [\n")
            append("      {\n        \"name\": \"Morning Run\",\n")
            append("        \"completed\": true")
            if (includeStreaks) append(",\n        \"streak\": 7")
            if (includeNotes)   append(",\n        \"note\": \"Great run!\"")
            append("\n      }\n    ]\n  }\n}")
        }
        ExportFormat.TXT -> buildString {
            append("=== HABITTRACKER EXPORT ===\n")
            append("Range  : $rangeLabel\n")
            append("Habits : ${stats.totalHabits}\n")
            append("Done   : ${stats.totalCompletions}\n")
            if (includeStreaks) append("Streaks: ${stats.totalStreakDays} days\n")
            append("\n--- MORNING RUN ---\n")
            append("Date: 2025-05-10  ✓\n")
            if (includeStreaks) append("Streak: 7 days\n")
            if (includeNotes)   append("Note: Great run!\n")
            append("...")
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SUCCESS BANNER
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ExportSuccessBanner(
    uri: Uri?,
    format: ExportFormat,
    context: Context,
    onDismiss: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()
        .clip(RoundedCornerShape(18.dp))
        .background(Brush.linearGradient(listOf(NeonGreen.copy(0.14f), Color(0xFF00B4DB).copy(0.08f))))
        .border(1.dp, NeonGreen.copy(0.4f), RoundedCornerShape(18.dp))
        .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(44.dp).clip(CircleShape)
                .background(NeonGreen.copy(0.15f)).border(1.dp, NeonGreen.copy(0.4f), CircleShape),
                contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Check, null, tint = NeonGreen, modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Export Complete!", style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold, color = NeonGreen)
                Text("Your ${format.label} file is ready to share",
                    style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
            // Share button
            if (uri != null) {
                Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                    .background(Brush.linearGradient(GreenGrad.map { it.copy(0.2f) }))
                    .border(1.dp, NeonGreen.copy(0.4f), RoundedCornerShape(10.dp))
                    .clickable {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = when (format) {
                                ExportFormat.CSV  -> "text/csv"
                                ExportFormat.JSON -> "application/json"
                                ExportFormat.TXT  -> "text/plain"
                            }
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Habits Export"))
                    },
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Share, null, tint = NeonGreen, modifier = Modifier.size(18.dp))
                }
            }
            Box(modifier = Modifier.size(28.dp).clip(CircleShape)
                .background(GlassWhite).clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Close, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// EXPORT BUTTON  — animated neon CTA
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ExportButton(
    isExporting: Boolean,
    progress: Float,
    format: ExportFormat,
    onClick: () -> Unit
) {
    val inf = rememberInfiniteTransition(label = "expglow")
    val glow by inf.animateFloat(0.5f, 1f,
        infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "eg")

    Box(modifier = Modifier.fillMaxWidth().height(58.dp)
        .clip(RoundedCornerShape(18.dp))
        .background(
            if (!isExporting) Brush.horizontalGradient(format.gradient)
            else Brush.horizontalGradient(format.gradient.map { it.copy(0.5f) })
        )
        .border(
            if (!isExporting) BorderStroke(1.dp, Brush.horizontalGradient(
                format.gradient.map { it.copy(0.4f * glow) })) else BorderStroke(0.dp, Color.Transparent),
            RoundedCornerShape(18.dp)
        )
        .clickable(enabled = !isExporting, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isExporting) {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Progress bar inside button
                Box(modifier = Modifier.fillMaxWidth(0.7f).height(4.dp)
                    .clip(RoundedCornerShape(2.dp)).background(Color.White.copy(0.2f))) {
                    Box(modifier = Modifier.fillMaxWidth(progress).fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp)).background(Color.White))
                }
                Text("Exporting… ${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White, fontWeight = FontWeight.Bold)
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.Download, null, tint = Color.White, modifier = Modifier.size(22.dp))
                Text("Export as ${format.label}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 0.3.sp)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SECTION LABEL HELPER
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ExportSectionLabel(title: String, gradient: List<Color>) {
    Row(verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.width(3.dp).height(16.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(Brush.verticalGradient(gradient)))
        Text(title, style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DATA MODEL
// ─────────────────────────────────────────────────────────────────────────────
data class ExportStats(
    val totalHabits: Int      = 0,
    val totalCompletions: Int = 0,
    val totalStreakDays: Int  = 0,
    val earliestDate: String  = "—"
)