package com.polyscores.kenya.presentation.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.polyscores.kenya.presentation.ui.components.PolyScoresTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: com.polyscores.kenya.presentation.viewModel.SettingsViewModel,
    isAdminSession: Boolean,
    onBackClick: () -> Unit,
    onAdminClick: () -> Unit,
    onAdminLogoutClick: () -> Unit
) {
    val preferences by settingsViewModel.userPreferences.collectAsState()

    val notificationsEnabled = preferences.notificationsEnabled
    val soundEnabled = preferences.soundEnabled
    val vibrationEnabled = preferences.vibrationEnabled
    val darkMode = preferences.darkMode

    Scaffold(
        topBar = {
            PolyScoresTopBar(
                title = "Settings",
                showBackButton = true,
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Notifications Section
            SettingsSection(title = "Notifications") {
                SettingsSwitchItem(
                    icon = Icons.Default.Notifications,
                    title = "Enable Notifications",
                    subtitle = "Receive live score updates",
                    checked = notificationsEnabled,
                    onCheckedChange = { settingsViewModel.updateNotificationsEnabled(it) }
                )
                SettingsSwitchItem(
                    icon = Icons.Default.VolumeUp,
                    title = "Sound",
                    subtitle = "Play sound for notifications",
                    checked = soundEnabled,
                    enabled = notificationsEnabled,
                    onCheckedChange = { settingsViewModel.updateSoundEnabled(it) }
                )
                SettingsSwitchItem(
                    icon = Icons.Default.Vibration,
                    title = "Vibration",
                    subtitle = "Vibrate for notifications",
                    checked = vibrationEnabled,
                    enabled = notificationsEnabled,
                    onCheckedChange = { settingsViewModel.updateVibrationEnabled(it) }
                )
            }

            // Appearance Section
            SettingsSection(title = "Appearance") {
                SettingsSwitchItem(
                    icon = Icons.Default.DarkMode,
                    title = "Dark Mode",
                    subtitle = "Use dark theme",
                    checked = darkMode,
                    onCheckedChange = { settingsViewModel.updateDarkMode(it) }
                )
            }

            // Admin Section
            SettingsSection(title = "Admin") {
                if (isAdminSession) {
                    SettingsNavigationItem(
                        icon = Icons.Default.AdminPanelSettings,
                        title = "Admin Dashboard",
                        subtitle = "Manage matches and scores",
                        onClick = onAdminClick
                    )
                    SettingsNavigationItem(
                        icon = Icons.Default.Logout,
                        title = "Logout Admin",
                        subtitle = "End your admin session",
                        onClick = onAdminLogoutClick
                    )
                } else {
                    SettingsNavigationItem(
                        icon = Icons.Default.AdminPanelSettings,
                        title = "Admin Login",
                        subtitle = "Enter PIN to manage matches",
                        onClick = onAdminClick
                    )
                }
            }

            if (isAdminSession) {
                var showWipeDialog by remember { mutableStateOf(false) }
                val isWiping by settingsViewModel.isWipingDatabase.collectAsState()
                val wipeResult by settingsViewModel.wipeDatabaseResult.collectAsState()

                val context = LocalContext.current

                LaunchedEffect(wipeResult) {
                    if (wipeResult == true) {
                        settingsViewModel.resetWipeState()
                        showWipeDialog = false
                        android.widget.Toast.makeText(context, "Database wiped successfully!", android.widget.Toast.LENGTH_LONG).show()
                    } else if (wipeResult == false) {
                        settingsViewModel.resetWipeState()
                        showWipeDialog = false
                        android.widget.Toast.makeText(context, "Failed to wipe database", android.widget.Toast.LENGTH_LONG).show()
                    }
                }

                if (showWipeDialog) {
                    AlertDialog(
                        onDismissRequest = { if (!isWiping) showWipeDialog = false },
                        title = { Text("Reset Database") },
                        text = { Text("Are you sure you want to delete ALL leagues, teams, players, matches, and standings? This action cannot be undone.") },
                        confirmButton = {
                            Button(
                                onClick = { settingsViewModel.wipeDatabase() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                if (isWiping) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onError)
                                } else {
                                    Text("Yes, Wipe Data")
                                }
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showWipeDialog = false },
                                enabled = !isWiping
                            ) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                SettingsSection(title = "Danger Zone") {
                    SettingsNavigationItem(
                        icon = Icons.Default.Warning,
                        title = "Reset Database",
                        subtitle = "Wipe all dummy data",
                        onClick = { showWipeDialog = true }
                    )
                }
            }

            // About Section
            SettingsSection(title = "About") {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "App Version",
                    subtitle = "1.0.0"
                )
                SettingsItem(
                    icon = Icons.Default.Policy,
                    title = "Privacy Policy",
                    subtitle = "Read our privacy policy"
                )
                SettingsItem(
                    icon = Icons.Default.Description,
                    title = "Terms of Service",
                    subtitle = "Read our terms of service"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // App info footer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "KNP Live Scores",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Live football scores for Kitale National Polytechnic",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Developed by Pipis",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (enabled) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            }
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                }
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                }
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

@Composable
private fun SettingsNavigationItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Navigate",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
