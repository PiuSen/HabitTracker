package com.peu.habittracker.screen


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.peu.habittracker.viewModel.SettingsViewModel

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val notificationTime by viewModel.notificationTime.collectAsState()
    val darkMode by viewModel.darkMode.collectAsState()

    var showTimePicker by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {
            if (notificationsEnabled) {
                SettingsItem(
                    icon = Icons.Default.Schedule,
                    title = "Notification Time",
                    // UPDATED: Now shows AM/PM
                    subtitle = formatTime(notificationTime.first, notificationTime.second),
                    onClick = { showTimePicker = true }
                )
            }

// Update the TimePickerDialog call
            if (showTimePicker) {
                TimePickerDialog(
                    initialHour = notificationTime.first,
                    initialMinute = notificationTime.second,
                    onDismiss = { showTimePicker = false },
                    onConfirm = { hour, minute ->
                        viewModel.updateNotificationTime(hour, minute)
                        showTimePicker = false
                    }
                )
            }
            // Notifications Section
            SettingsSection(title = "Notifications") {
                SettingsItem(
                    icon = Icons.Default.Notifications,
                    title = "Enable Notifications",
                    subtitle = "Get daily reminders for your habits",
                    trailing = {
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { viewModel.toggleNotifications(it) }
                        )
                    }
                )

                if (notificationsEnabled) {
                    SettingsItem(
                        icon = Icons.Default.Schedule,
                        title = "Notification Time",
                        subtitle = String.format("%02d:%02d", notificationTime.first, notificationTime.second),
                        onClick = { showTimePicker = true }
                    )
                }
            }

            HorizontalDivider()

            // Appearance Section
            SettingsSection(title = "Appearance") {
                SettingsItem(
                    icon = Icons.Default.DarkMode,
                    title = "Dark Mode",
                    subtitle = "Use dark theme",
                    trailing = {
                        Switch(
                            checked = darkMode,
                            onCheckedChange = { viewModel.toggleDarkMode(it) }
                        )
                    }
                )
            }

            HorizontalDivider()

            // About Section
            SettingsSection(title = "About") {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "Version",
                    subtitle = "1.0.0"
                )

                SettingsItem(
                    icon = Icons.Default.Code,
                    title = "Developer",
                    subtitle = "Built with Jetpack Compose"
                )
            }
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        TimePickerDialog(
            initialHour = notificationTime.first,
            initialMinute = notificationTime.second,
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute ->
                viewModel.updateNotificationTime(hour, minute)
                showTimePicker = false
            }
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        content()
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
                else Modifier
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        trailing?.invoke()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Notification Time") },
        text = {
            TimePicker(state = timePickerState)
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(timePickerState.hour, timePickerState.minute)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

data class SettingsUiState(
    val notificationsEnabled: Boolean = true,
    val notificationHour: Int = 9,
    val notificationMinute: Int = 0,
    val darkMode: Boolean = false
)
// Helper function to format time to AM/PM
fun formatTime(hour: Int, minute: Int): String {
    val calendar = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.HOUR_OF_DAY, hour)
        set(java.util.Calendar.MINUTE, minute)
    }
    val formatter = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
    return formatter.format(calendar.time)
}

// Inside SettingsScreen Column:
