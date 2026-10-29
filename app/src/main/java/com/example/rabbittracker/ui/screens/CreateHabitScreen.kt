package com.example.rabbittracker.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.rounded.AccessTimeFilled
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rabbittracker.ui.HabitViewModel
import com.example.rabbittracker.ui.components.habitAccentNames
import com.example.rabbittracker.ui.components.habitIcon
import com.example.rabbittracker.ui.components.habitIcons
import com.example.rabbittracker.utils.NotificationHelper

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateHabitScreen(
    viewModel: HabitViewModel,
    editHabitId: Int? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val existing = remember(habits, editHabitId) {
        editHabitId?.let { id -> habits.find { it.id == id } }
    }

    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Health") }
    var selectedIconIndex by remember { mutableStateOf(0) }
    var selectedAccentIndex by remember { mutableStateOf(0) }
    var selectedFrequency by remember { mutableStateOf("Daily") }
    var reminderTime by remember { mutableStateOf("08:00 AM") }
    var reminderEnabled by remember { mutableStateOf(true) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var prefilled by remember { mutableStateOf(false) }

    var whyText by remember { mutableStateOf("") }
    var whyPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var existingPhotoPath by remember { mutableStateOf("") }

    val whyTextFromStore by remember(existing?.id) {
        if (existing != null) viewModel.getWhyTextFlow(existing.id) else kotlinx.coroutines.flow.flowOf("")
    }.collectAsStateWithLifecycle(initialValue = "")

    val whyPhotoPathFromStore by remember(existing?.id) {
        if (existing != null) viewModel.getWhyPhotoPathFlow(existing.id) else kotlinx.coroutines.flow.flowOf("")
    }.collectAsStateWithLifecycle(initialValue = "")

    LaunchedEffect(whyTextFromStore, whyPhotoPathFromStore) {
        if (existing != null) {
            if (whyText.isBlank() && whyTextFromStore.isNotBlank()) whyText = whyTextFromStore
            if (existingPhotoPath.isBlank() && whyPhotoPathFromStore.isNotBlank()) existingPhotoPath = whyPhotoPathFromStore
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            whyPhotoUri = uri
            existingPhotoPath = ""
        }
    }

    LaunchedEffect(existing) {
        val h = existing ?: return@LaunchedEffect
        if (prefilled) return@LaunchedEffect
        prefilled = true
        name = h.name
        selectedCategory = h.category
        selectedFrequency = h.frequency
        reminderTime = h.reminderTime
        reminderEnabled = h.reminderEnabled
        selectedIconIndex = habitIcons.indexOf(h.iconName).takeIf { it >= 0 } ?: 0
        selectedAccentIndex = habitAccentNames.indexOf(h.colorAccent).takeIf { it >= 0 } ?: 0
    }

    val canSave = name.isNotBlank()
    val primaryColor = MaterialTheme.colorScheme.primary

    fun save() {
        if (!canSave) return
        val habit = com.example.rabbittracker.data.Habit(
            id = existing?.id ?: 0,
            name = name.trim(),
            category = selectedCategory,
            frequency = selectedFrequency,
            iconName = habitIcons[selectedIconIndex],
            colorAccent = habitAccentNames[selectedAccentIndex],
            description = existing?.description ?: "",
            reminderTime = reminderTime,
            reminderEnabled = reminderEnabled
        )
        if (existing != null) {
            viewModel.updateHabit(habit)
            viewModel.saveHabitWhy(existing.id, whyText, whyPhotoUri)
        } else {
            viewModel.addHabit(habit, whyText, whyPhotoUri)
        }
        onBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onBack() }
            ) {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowLeft,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    if (existing != null) "Edit Habit" else "New Habit",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Name
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Habit Name", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            shape = MaterialTheme.shapes.medium
        )
        Spacer(modifier = Modifier.height(12.dp))

        Text("Category", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        val categories = listOf("Health", "Fitness", "Mindset", "Work", "Personal")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat) }
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        Text("Select Icon", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))

        val iconNames = habitIcons
        val icons = habitIcons.map { habitIcon(it) }
        Column {
            for (row in 0 until 4) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    for (col in 0 until 6) {
                        val index = row * 6 + col
                        if (index < icons.size) {
                            val isSelected = index == selectedIconIndex
                            Box(
                                modifier = Modifier
                                    .padding(2.dp)
                                    .size(42.dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(if (isSelected) primaryColor else MaterialTheme.colorScheme.surfaceContainerHigh)
                                    .clickable { selectedIconIndex = index },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icons[index],
                                    contentDescription = iconNames[index],
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else primaryColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        Text("Frequency", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        val frequencyOptions = listOf("Daily", "Weekly")
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            frequencyOptions.forEachIndexed { index, freq ->
                SegmentedButton(
                    selected = selectedFrequency == freq,
                    onClick = { selectedFrequency = freq },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = frequencyOptions.size)
                ) {
                    Text(freq, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Notifications card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Rounded.Notifications, contentDescription = "Bell", tint = primaryColor, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Daily Reminder", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text("Push notification prompt", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = { reminderEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.onPrimary, checkedTrackColor = primaryColor)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTimePicker = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Reminder Time", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.weight(1f))
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                            Icon(imageVector = Icons.Rounded.AccessTimeFilled, contentDescription = "Clock", tint = primaryColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(reminderTime, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Your Why (optional) section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Rounded.Favorite, contentDescription = "Why", tint = primaryColor, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Your Why (optional)", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text("Personal reason or photo to stay motivated", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = whyText,
                    onValueChange = { if (it.length <= 120) whyText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. BP control karna hai", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    maxLines = 3,
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    supportingText = {
                        Text(
                            "${whyText.length}/120",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.End
                        )
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))

                val photoBitmap = remember(whyPhotoUri, existingPhotoPath) {
                    when {
                        whyPhotoUri != null -> {
                            try {
                                val stream = context.contentResolver.openInputStream(whyPhotoUri!!)
                                val b = android.graphics.BitmapFactory.decodeStream(stream)
                                stream?.close()
                                b
                            } catch (_: Exception) { null }
                        }
                        existingPhotoPath.isNotBlank() -> {
                            try {
                                val file = java.io.File(existingPhotoPath)
                                if (file.exists()) android.graphics.BitmapFactory.decodeFile(file.absolutePath) else null
                            } catch (_: Exception) { null }
                        }
                        else -> null
                    }
                }

                if (photoBitmap != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(MaterialTheme.shapes.small)
                    ) {
                        Image(
                            bitmap = photoBitmap.asImageBitmap(),
                            contentDescription = "Why photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        IconButton(
                            onClick = {
                                whyPhotoUri = null
                                if (existing != null && existingPhotoPath.isNotBlank()) {
                                    viewModel.removeHabitWhyPhoto(existing.id)
                                }
                                existingPhotoPath = ""
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), CircleShape)
                        ) {
                            Icon(imageVector = Icons.Rounded.Close, contentDescription = "Remove photo", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Rounded.AddAPhoto, contentDescription = "Add photo", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Photo")
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Save button
        Button(
            onClick = { save() },
            enabled = canSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Icon(imageVector = Icons.Rounded.CheckCircle, contentDescription = "Save", modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Habit")
            }
        }
        if (existing != null) {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Delete Habit", color = MaterialTheme.colorScheme.error)
            }
        }
        Spacer(modifier = Modifier.height(88.dp))
    }

    if (showDeleteDialog && existing != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete habit?") },
            text = {
                Text("Delete '${existing.name}'? Its history and streak data will be permanently deleted and its reminder cancelled. This cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteHabit(existing)
                        onBack()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showTimePicker) {
        val initialMins = NotificationHelper.parseReminderTime(reminderTime)
        val timePickerState = rememberTimePickerState(
            initialHour = initialMins.first,
            initialMinute = initialMins.second,
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Reminder Time") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    val cal = java.util.Calendar.getInstance().apply {
                        set(java.util.Calendar.HOUR_OF_DAY, timePickerState.hour)
                        set(java.util.Calendar.MINUTE, timePickerState.minute)
                    }
                    reminderTime = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US).format(cal.time)
                    showTimePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
