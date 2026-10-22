package com.example.rabbittracker.ui.screens

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTimeFilled
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rabbittracker.ui.HabitViewModel
import com.example.rabbittracker.ui.components.habitAccentNames
import com.example.rabbittracker.ui.components.habitIcon
import com.example.rabbittracker.ui.components.habitIcons
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateHabitScreen(
    viewModel: HabitViewModel,
    editHabitId: Int? = null,
    onBack: () -> Unit
) {
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
        } else {
            viewModel.addHabit(habit)
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
        Spacer(modifier = Modifier.height(12.dp))

        // Preview card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            shape = MaterialTheme.shapes.medium
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(primaryColor)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = habitIcon(habitIcons[selectedIconIndex]),
                        contentDescription = "Icon",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        name.ifBlank { "New Habit" },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        selectedFrequency,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Form
        Text("Habit name", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            shape = MaterialTheme.shapes.medium,
            singleLine = true,
            trailingIcon = {
                if (name.isNotEmpty()) {
                    IconButton(onClick = { name = "" }) {
                        Icon(imageVector = Icons.Rounded.Cancel, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        )
        Spacer(modifier = Modifier.height(12.dp))

        Text("Category", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Health", "Fitness", "Mind", "Productivity").forEach { cat ->
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
        ReminderTimePickerDialog(
            initialTime = reminderTime,
            onDismiss = { showTimePicker = false },
            onConfirm = { t ->
                reminderTime = t
                showTimePicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderTimePickerDialog(
    initialTime: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val cal = Calendar.getInstance()
    var initialHour = 8
    var initialMinute = 0
    try {
        val sdf = SimpleDateFormat("h:mm a", Locale.US)
        val date = sdf.parse(initialTime)
        if (date != null) {
            cal.time = date
            initialHour = cal.get(Calendar.HOUR_OF_DAY)
            initialMinute = cal.get(Calendar.MINUTE)
        }
    } catch (_: Exception) {}

    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = false
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        title = { Text("Select Reminder Time", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface) },
        text = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TimePicker(state = timePickerState)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val hour = timePickerState.hour
                val minute = timePickerState.minute
                val c = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                }
                val formatted = SimpleDateFormat("h:mm a", Locale.US).format(c.time)
                onConfirm(formatted)
            }) {
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
