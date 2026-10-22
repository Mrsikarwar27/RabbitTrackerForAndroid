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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.rabbittracker.ui.HabitViewModel
import com.example.rabbittracker.ui.components.ProfileAvatar

@Composable
fun OnboardingScreen(
    viewModel: HabitViewModel,
    onFinished: () -> Unit
) {
    val context = LocalContext.current

    var step by remember { mutableStateOf(1) } // 1: Profile, 2: How it works, 3: Permissions
    var nameInput by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var photoPath by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            photoUri = uri
            try {
                val file = java.io.File(context.filesDir, "profile.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                photoPath = file.absolutePath
            } catch (_: Exception) {}
        }
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {}

    val primaryColor = MaterialTheme.colorScheme.primary

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top indicator
            Column {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(3) { i ->
                        val active = (i + 1) == step
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(6.dp)
                                .width(if (active) 32.dp else 12.dp)
                                .clip(MaterialTheme.shapes.extraSmall)
                                .background(if (active) primaryColor else MaterialTheme.colorScheme.surfaceContainerHigh)
                        )
                    }
                }
            }

            // Scrollable step content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 16.dp)
            ) {
                when (step) {
                    1 -> {
                        // Step 1: Profile
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Welcome to Rabbit Tracker",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Set up your profile to personalize your habits and streaks.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(32.dp))

                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .clickable {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(
                                                ActivityResultContracts.PickVisualMedia.ImageOnly
                                            )
                                        )
                                    },
                                contentAlignment = Alignment.BottomEnd
                            ) {
                                ProfileAvatar(
                                    userName = nameInput,
                                    userPhotoPath = photoPath,
                                    size = 96.dp
                                )
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(primaryColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CameraAlt,
                                        contentDescription = "Change photo",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(32.dp))

                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Your name (required)") },
                                singleLine = true,
                                shape = MaterialTheme.shapes.medium,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                )
                            )
                        }
                    }
                    2 -> {
                        // Step 2: How it works
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                "How it works",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Build lasting habits in 3 simple steps.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(32.dp))

                            val items = listOf(
                                Triple(Icons.Rounded.AddCircle, "Add a habit", "Create daily or weekly routines tailored to your goals."),
                                Triple(Icons.Rounded.LocalFireDepartment, "Tick it off daily to build a streak", "Mark habits complete and watch your momentum grow."),
                                Triple(Icons.Rounded.Notifications, "Get reminded on time", "Never miss a routine with timely push notifications.")
                            )

                            items.forEach { (icon, title, desc) ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(MaterialTheme.shapes.small)
                                                .background(primaryColor.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(imageVector = icon, contentDescription = title, tint = primaryColor, modifier = Modifier.size(24.dp))
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(desc, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    3 -> {
                        // Step 3: Permissions
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                "Stay on track",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Enable essential permissions for timely reminders and reliable alarms.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            // Permission 1: Notifications
                            val notifGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                            } else {
                                NotificationManagerCompat.from(context).areNotificationsEnabled()
                            }

                            PermissionRowItem(
                                title = "Notifications",
                                reason = "Receive reminders to complete your daily habits.",
                                isGranted = notifGranted,
                                onAllow = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        try {
                                            val intent = android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS.let { action ->
                                                Intent(action).putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                                            }
                                            context.startActivity(intent)
                                        } catch (_: Exception) {}
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Permission 2: Exact Alarms
                            val alarmManager = remember { context.getSystemService(Context.ALARM_SERVICE) as AlarmManager }
                            val exactAlarmAllowed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                alarmManager.canScheduleExactAlarms()
                            } else true

                            PermissionRowItem(
                                title = "Exact Alarms",
                                reason = "Ensure habit reminders ring precisely on schedule.",
                                isGranted = exactAlarmAllowed,
                                onAllow = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        try {
                                            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}"))
                                            context.startActivity(intent)
                                        } catch (_: Exception) {
                                            try {
                                                val fallback = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
                                                context.startActivity(fallback)
                                            } catch (_: Exception) {}
                                        }
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Permission 3: Battery Background
                            val powerManager = remember { context.getSystemService(Context.POWER_SERVICE) as PowerManager }
                            val batteryIgnored = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                powerManager.isIgnoringBatteryOptimizations(context.packageName)
                            } else true

                            PermissionRowItem(
                                title = "Background Reliability",
                                reason = "Prevent battery saver from delaying notifications.",
                                isGranted = batteryIgnored,
                                onAllow = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        try {
                                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
                                            context.startActivity(intent)
                                        } catch (_: Exception) {}
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Bottom action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (step > 1) {
                    OutlinedButton(
                        onClick = { step-- },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Back")
                    }
                }
                Button(
                    onClick = {
                        if (step < 3) {
                            if (step == 1 && nameInput.isNotBlank()) {
                                viewModel.saveProfile(nameInput, photoUri, context)
                            }
                            step++
                        } else {
                            if (nameInput.isNotBlank()) {
                                viewModel.saveProfile(nameInput, photoUri, context)
                            }
                            viewModel.setOnboardingCompleted(true)
                            onFinished()
                        }
                    },
                    enabled = step != 1 || nameInput.isNotBlank(),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(if (step == 3) "Finish" else "Next")
                }
            }
        }
    }
}

@Composable
fun PermissionRowItem(
    title: String,
    reason: String,
    isGranted: Boolean,
    onAllow: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(2.dp))
                Text(reason, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (isGranted) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text("Allowed", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                }
            } else {
                Button(
                    onClick = onAllow,
                    shape = MaterialTheme.shapes.small,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Allow", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
