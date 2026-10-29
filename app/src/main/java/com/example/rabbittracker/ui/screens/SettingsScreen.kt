package com.example.rabbittracker.ui.screens

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTimeFilled
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rabbittracker.R
import com.example.rabbittracker.ui.HabitViewModel
import com.example.rabbittracker.ui.components.ProfileAvatar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: HabitViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val userPhotoPath by viewModel.userPhotoPath.collectAsStateWithLifecycle()
    val remindersEnabled by viewModel.remindersEnabled.collectAsStateWithLifecycle()
    val reminderTime by viewModel.reminderTime.collectAsStateWithLifecycle()

    var showTimePicker by remember { mutableStateOf(false) }
    var isEditingName by remember { mutableStateOf(false) }
    var nameInput by remember(userName) { mutableStateOf(userName) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { viewModel.saveProfilePhoto(context, it) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.setRemindersEnabled(true)
        } else {
            viewModel.setRemindersEnabled(false)
            scope.launch {
                snackbarHostState.showSnackbar("Notification permission denied.")
            }
        }
    }

    val primaryColor = MaterialTheme.colorScheme.primary

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onBack() }
                ) {
                    Icon(imageVector = Icons.Rounded.KeyboardArrowLeft, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            // Profile Section
            Text("PROFILE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ProfileAvatar(
                            userName = userName,
                            userPhotoPath = userPhotoPath,
                            size = 54.dp,
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            if (isEditingName) {
                                OutlinedTextField(
                                    value = nameInput,
                                    onValueChange = { nameInput = it },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                    ),
                                    trailingIcon = {
                                        IconButton(onClick = {
                                            if (nameInput.isNotBlank()) {
                                                viewModel.setUserName(nameInput.trim())
                                                isEditingName = false
                                            }
                                        }) {
                                            Icon(imageVector = Icons.Rounded.Check, contentDescription = "Save", tint = primaryColor)
                                        }
                                    }
                                )
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        userName,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    IconButton(onClick = { isEditingName = true }) {
                                        Icon(imageVector = Icons.Rounded.Edit, contentDescription = "Edit name", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                            Text("Local Account", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text("Change Photo")
                        }
                        if (!userPhotoPath.isNullOrEmpty()) {
                            OutlinedButton(
                                onClick = { viewModel.removeProfilePhoto(context) },
                                modifier = Modifier.weight(1f),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text("Remove Photo", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Preferences Section
            Text("PREFERENCES", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(imageVector = Icons.Rounded.Notifications, contentDescription = "Notifications", tint = primaryColor)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Daily Reminders", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                                Text("Push notifications", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = remindersEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val granted = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (granted) {
                                        viewModel.setRemindersEnabled(true)
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                } else {
                                    viewModel.setRemindersEnabled(enabled)
                                }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.onPrimary, checkedTrackColor = primaryColor)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTimePicker = true }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Rounded.AccessTimeFilled, contentDescription = "Clock", tint = primaryColor)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Reminder Time", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Text(
                                reminderTime,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Permissions Section
            PermissionsSection()

            Spacer(modifier = Modifier.height(20.dp))

            // About Section
            Text("ABOUT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Rounded.Info, contentDescription = "Version", tint = primaryColor)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("App Version", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Text("1.0.0", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            DeveloperSection()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showTimePicker) {
        val initialMins = com.example.rabbittracker.utils.NotificationHelper.parseReminderTime(reminderTime)
        val timePickerState = androidx.compose.material3.rememberTimePickerState(
            initialHour = initialMins.first,
            initialMinute = initialMins.second,
            is24Hour = false
        )
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { androidx.compose.material3.Text("Select Reminder Time") },
            text = { androidx.compose.material3.TimePicker(state = timePickerState) },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    val cal = java.util.Calendar.getInstance().apply {
                        set(java.util.Calendar.HOUR_OF_DAY, timePickerState.hour)
                        set(java.util.Calendar.MINUTE, timePickerState.minute)
                    }
                    val formatted = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US).format(cal.time)
                    viewModel.setReminderTime(formatted)
                    showTimePicker = false
                }) {
                    androidx.compose.material3.Text("OK")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showTimePicker = false }) {
                    androidx.compose.material3.Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PermissionsSection() {
    val context = LocalContext.current
    val alarmManager = remember { context.getSystemService(Context.ALARM_SERVICE) as AlarmManager }
    val powerManager = remember { context.getSystemService(Context.POWER_SERVICE) as PowerManager }

    val notifGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    } else {
        NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    val exactAlarmAllowed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        alarmManager.canScheduleExactAlarms()
    } else {
        true
    }

    val batteryIgnored = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        powerManager.isIgnoringBatteryOptimizations(context.packageName)
    } else {
        true
    }

    Text("PERMISSIONS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(modifier = Modifier.height(8.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Row 1: Notifications
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Notifications", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text(if (notifGranted) "Granted" else "Denied", style = MaterialTheme.typography.bodySmall, color = if (notifGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                }
                if (!notifGranted) {
                    Button(onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                            context.startActivity(intent)
                        }
                    }, shape = MaterialTheme.shapes.small) {
                        Text("Enable")
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Row 2: Exact Alarms
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Exact Alarms", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text(if (exactAlarmAllowed) "Allowed" else "Not allowed", style = MaterialTheme.typography.bodySmall, color = if (exactAlarmAllowed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                }
                if (!exactAlarmAllowed && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Button(onClick = {
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val fallback = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
                            context.startActivity(fallback)
                        }
                    }, shape = MaterialTheme.shapes.small) {
                        Text("Enable")
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Row 3: Battery Optimization
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Battery Background", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text(if (batteryIgnored) "Unrestricted" else "Restricted", style = MaterialTheme.typography.bodySmall, color = if (batteryIgnored) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                }
                if (!batteryIgnored) {
                    Button(onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        context.startActivity(intent)
                    }, shape = MaterialTheme.shapes.small) {
                        Text("Enable")
                    }
                }
            }
        }
    }
}

@Composable
fun DeveloperSection() {
    val context = LocalContext.current
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val onPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer
    val secondaryContainer = MaterialTheme.colorScheme.secondaryContainer
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val onSurface = MaterialTheme.colorScheme.onSurface
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    val surfaceContainer = MaterialTheme.colorScheme.surfaceContainer

    Spacer(modifier = Modifier.height(20.dp))
    Text("DEVELOPER", style = MaterialTheme.typography.labelSmall, color = onSurfaceVariant)
    Spacer(modifier = Modifier.height(8.dp))

    // Card 1: Developer Info & Links
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = surfaceContainer),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "VS",
                        style = MaterialTheme.typography.titleMedium,
                        color = onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Vijay Singh Sikarwar", style = MaterialTheme.typography.titleMedium, color = onSurface, fontWeight = FontWeight.SemiBold)
                    Text("Developer of Rabbit Habit Tracker", style = MaterialTheme.typography.bodySmall, color = onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = outlineVariant)
            Spacer(modifier = Modifier.height(14.dp))

            // LinkedIn row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com/in/vijay-singh-sikarwar-9b59362a4/"))
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_linkedin),
                        contentDescription = "LinkedIn",
                        tint = onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("LinkedIn", style = MaterialTheme.typography.titleSmall, color = onSurface)
                    Text("Connect with me", style = MaterialTheme.typography.bodySmall, color = onSurfaceVariant)
                }
                Icon(
                    imageVector = Icons.Rounded.OpenInNew,
                    contentDescription = "Open",
                    tint = onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // GitHub row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Mrsikarwar27"))
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_github),
                        contentDescription = "GitHub",
                        tint = onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("GitHub", style = MaterialTheme.typography.titleSmall, color = onSurface)
                    Text("See my projects", style = MaterialTheme.typography.bodySmall, color = onSurfaceVariant)
                }
                Icon(
                    imageVector = Icons.Rounded.OpenInNew,
                    contentDescription = "Open",
                    tint = onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Card 2: Disclaimer
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = surfaceContainer),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Privacy & Disclaimer", style = MaterialTheme.typography.titleSmall, color = onSurface)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Rabbit Habit Tracker works completely offline. All your data  your habits, streaks, reminders, day notes and profile  is stored only on this device. The app has no accounts, no servers, no ads and no tracking, and the developer cannot see, collect or access your data in any way. If you uninstall the app, your data is deleted along with it. Found a bug or have a suggestion? Reach out to the developer on LinkedIn or GitHub above  your feedback helps improve the app.",
                style = MaterialTheme.typography.bodySmall,
                color = onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
