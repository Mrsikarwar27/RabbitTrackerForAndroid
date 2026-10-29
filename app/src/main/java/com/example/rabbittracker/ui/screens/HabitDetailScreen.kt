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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.rabbittracker.ui.components.TopBar
import com.example.rabbittracker.ui.components.habitAccent
import com.example.rabbittracker.ui.components.habitIcon
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun HabitDetailScreen(
    viewModel: HabitViewModel,
    habitId: Int,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onNavigateToSettings: () -> Unit = {}
) {
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val recentLogs by viewModel.recentLogs.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val userPhotoPath by viewModel.userPhotoPath.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadRecentLogs() }

    val habit = habits.find { habitId == it.id }

    val suggestion by remember(habit) {
        if (habit != null) viewModel.getAdaptiveSuggestionFlow(habit) else kotlinx.coroutines.flow.flowOf(null)
    }.collectAsStateWithLifecycle(initialValue = null)

    var confirmationText by remember { mutableStateOf<String?>(null) }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val dayFormat = remember { SimpleDateFormat("EEE, d MMM", Locale.US) }

    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val history = remember(recentLogs, habit) {
        val targetId = habit?.id
        (0 until 7).map { offset ->
            val date = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -offset) }
            val key = dateFormat.format(date.time)
            val log = targetId?.let { recentLogs[key]?.get(it) }
            val value = when {
                habit == null -> "-"
                log?.completed == true -> "Done"
                else -> "Not done"
            }
            Triple(key, dayFormat.format(date.time), value)
        }
    }

    val habitId0 = habit?.id
    val isDoneOn: (String) -> Boolean = { key ->
        val log = habitId0?.let { recentLogs[key]?.get(it) }
        log?.completed == true
    }
    val orderedDates = remember(recentLogs) { recentLogs.keys.sortedDescending() }
    val currentStreak = remember(recentLogs, habit) {
        var streak = 0
        for (key in orderedDates) {
            if (isDoneOn(key)) streak++ else break
        }
        streak
    }
    val longestStreak = remember(recentLogs, habit) {
        var best = 0
        var run = 0
        for (key in orderedDates) {
            if (isDoneOn(key)) { run++; best = maxOf(best, run) } else run = 0
        }
        best
    }
    val completionRate = remember(recentLogs, habit) {
        if (orderedDates.isEmpty()) 0 else {
            val done = orderedDates.count { isDoneOn(it) }
            (done * 100) / orderedDates.size
        }
    }
    val totalDoneDays = remember(recentLogs, habit) {
        orderedDates.count { isDoneOn(it) }
    }

    val primaryColor = MaterialTheme.colorScheme.primary

    if (habit == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopBar(title = "Habit Detail", userName = userName, userPhotoPath = userPhotoPath, onProfileClick = onNavigateToSettings)
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = "Habit not found",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "This habit may have been deleted.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onBack,
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Go back")
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        TopBar(
            title = "Habit Detail",
            userName = userName,
            userPhotoPath = userPhotoPath,
            onProfileClick = onNavigateToSettings,
            actions = {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "More options",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete habit", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        )
        Spacer(modifier = Modifier.height(4.dp))

        Text("HABIT DOSSIER", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))

        // Header card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            shape = MaterialTheme.shapes.medium
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(habitAccent(habit.colorAccent))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = habitIcon(habit.iconName),
                        contentDescription = habit.name,
                        tint = primaryColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        habit.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            habit.category,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${habit.frequency} • Yes/No",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (suggestion != null) {
            com.example.rabbittracker.ui.components.AdaptiveReminderCard(
                suggestion = suggestion!!,
                onMoveReminder = { suggestedTime ->
                    viewModel.applyAdaptiveSuggestion(habit, suggestedTime)
                    confirmationText = "Reminder moved to $suggestedTime"
                },
                onDismiss = {
                    viewModel.dismissAdaptiveSuggestion(habit.id)
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        } else if (confirmationText != null) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = confirmationText!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(14.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 2x2 stat cards
        val statList = listOf(
            Triple("Current Streak", "$currentStreak days", true),
            Triple("Longest Streak", "$longestStreak days", false),
            Triple("Completion Rate", "$completionRate %", false),
            Triple("Completed Days", "$totalDoneDays", false)
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            statList.take(2).forEach { (label, value, isStreak) ->
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        val subColor = if (isStreak) primaryColor else MaterialTheme.colorScheme.tertiary
                        val subText = if (isStreak) "Active now" else if (label == "Longest Streak") "Personal best" else "Last 7 days"
                        Text(subText, style = MaterialTheme.typography.labelSmall, color = subColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            statList.drop(2).forEach { (label, value, _) ->
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("Across tracked days", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Heatmap card (last 7 days)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Last 7 Days", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Less", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(modifier = Modifier.padding(horizontal = 4.dp)) {
                            repeat(3) { i ->
                                Box(modifier = Modifier.padding(horizontal = 1.dp).size(8.dp).clip(MaterialTheme.shapes.extraSmall).background(if (i == 2) primaryColor else MaterialTheme.colorScheme.surfaceContainerHigh))
                            }
                        }
                        Text("More", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    orderedDates.reversed().forEach { key ->
                        val done = isDoneOn(key)
                        val bg = if (done) primaryColor else MaterialTheme.colorScheme.surfaceContainerHigh
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(bg),
                                contentAlignment = Alignment.Center
                            ) {
                                if (done) {
                                    Icon(imageVector = Icons.Rounded.Check, contentDescription = "Done", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(14.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = key.takeLast(2),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Recent History section
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Recent History", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            Text("Last 7 Days", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.height(8.dp))
        history.forEach { (_, dateLabel, value) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(dateLabel, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text(value, style = MaterialTheme.typography.bodyMedium, color = if (value == "Done") primaryColor else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Edit Habit button
        Button(
            onClick = onEdit,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Icon(imageVector = Icons.Rounded.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Habit")
            }
        }
        Spacer(modifier = Modifier.height(88.dp))
    }

    if (showDeleteDialog && habit != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete habit?") },
            text = {
                Text("Delete '${habit.name}'? Its history and streak data will be permanently deleted and its reminder cancelled. This cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteHabit(habit)
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
}
