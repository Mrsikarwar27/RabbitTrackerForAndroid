package com.example.rabbittracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.NoteAdd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rabbittracker.ui.HabitViewModel
import com.example.rabbittracker.ui.components.TopBar
import com.example.rabbittracker.ui.components.habitAccent
import com.example.rabbittracker.ui.components.habitIcon
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CalendarScreen(
    viewModel: HabitViewModel,
    onNavigateToCreate: () -> Unit = {},
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToSettings: () -> Unit = {}
) {
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val dayNote by viewModel.dayNote.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val userPhotoPath by viewModel.userPhotoPath.collectAsStateWithLifecycle()

    var selectedDate by remember { mutableStateOf(viewModel.today) }
    var noteText by remember(dayNote) { mutableStateOf(dayNote) }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val monthFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.US) }
    val headerFormat = remember { SimpleDateFormat("EEEE, d MMMM yyyy", Locale.US) }
    val selectedDateObj = remember(selectedDate) { dateFormat.parse(selectedDate) ?: Date() }
    var monthOffset by remember { mutableStateOf(0) }
    val monthDate = remember(monthOffset) {
        Calendar.getInstance().apply {
            time = Date()
            add(Calendar.MONTH, monthOffset)
        }.time
    }
    val monthGrid = remember(monthDate) {
        val cal = Calendar.getInstance().apply {
            time = monthDate
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val firstDayOffset = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Monday = 0
        cal.add(Calendar.DAY_OF_MONTH, -firstDayOffset)
        (0 until 6).map {
            (0 until 7).map {
                cal.time.also { cal.add(Calendar.DAY_OF_MONTH, 1) }
            }
        }
    }
    val dateLogs by viewModel.dateLogs.collectAsStateWithLifecycle()
    LaunchedEffect(selectedDate) { viewModel.loadLogsFor(selectedDate) }
    val selectedLogs = if (dateLogs.first == selectedDate) dateLogs.second else emptyMap()
    val doneOnSelected = habits.count { habit ->
        val log = selectedLogs[habit.id]
        log?.completed == true
    }

    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        TopBar(title = "Calendar", userName = userName, userPhotoPath = userPhotoPath, onProfileClick = onNavigateToSettings)
        Spacer(modifier = Modifier.height(12.dp))

        // Month bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            shape = MaterialTheme.shapes.medium
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { monthOffset-- }) {
                    Icon(imageVector = Icons.Rounded.ChevronLeft, contentDescription = "Prev", tint = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(modifier = Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Rounded.CalendarToday, contentDescription = "Calendar", tint = primaryColor, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(monthFormat.format(monthDate), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { monthOffset++ }) {
                    Icon(imageVector = Icons.Rounded.ChevronRight, contentDescription = "Next", tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Calendar grid
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { d ->
                        Text(d, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                monthGrid.forEach { week ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        week.forEach { cellDate ->
                            val dateKey = dateFormat.format(cellDate)
                            val isCurrentMonth = cellDate.month == monthDate.month
                            val isToday = dateKey == viewModel.today
                            val isSelected = dateKey == selectedDate
                            val bg = when {
                                isSelected -> primaryColor
                                isToday -> MaterialTheme.colorScheme.primaryContainer
                                else -> Color.Transparent
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(bg)
                                    .clickable { selectedDate = dateKey },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cellDate.date.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.onPrimary
                                        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                                        !isCurrentMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Selected-day banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            shape = MaterialTheme.shapes.medium
        ) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(primaryColor))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(headerFormat.format(selectedDateObj), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = if (habits.isEmpty()) "No habits" else "$doneOnSelected of ${habits.size} Done",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Habit list or 56dp FAB
        if (habits.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                FloatingActionButton(
                    onClick = onNavigateToCreate,
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Add habit"
                    )
                }
            }
        } else {
            habits.forEach { habit ->
                val log = selectedLogs[habit.id]
                val completed = log?.completed == true
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigateToDetail(habit.id) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(MaterialTheme.shapes.small).background(habitAccent(habit.colorAccent)).padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(habitIcon(habit.iconName), contentDescription = habit.name, tint = primaryColor, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(habit.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            val meta = if (completed) "Completed" else "Scheduled for ${habit.frequency}"
                            Text(meta, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                            if (completed) {
                                Box(modifier = Modifier.size(30.dp).clip(CircleShape).background(primaryColor), contentAlignment = Alignment.Center) {
                                    Icon(imageVector = Icons.Rounded.Check, contentDescription = "Done", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                                }
                            } else {
                                Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceContainerHigh), contentAlignment = Alignment.Center) {
                                    Icon(imageVector = Icons.Rounded.AccessTimeFilled, contentDescription = "Time", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Saved Day Note card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Rounded.NoteAdd, contentDescription = "Note", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SAVED DAY NOTE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = noteText,
                    onValueChange = {
                        noteText = it
                        viewModel.saveDayNote(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    shape = MaterialTheme.shapes.medium,
                    maxLines = 3
                )
            }
        }
        Spacer(modifier = Modifier.height(88.dp))
    }
}
