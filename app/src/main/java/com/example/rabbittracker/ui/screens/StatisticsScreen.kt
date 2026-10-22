package com.example.rabbittracker.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rabbittracker.ui.HabitViewModel
import com.example.rabbittracker.ui.components.TopBar
import com.example.rabbittracker.ui.components.habitIcon
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: HabitViewModel,
    onNavigateToSettings: () -> Unit = {}
) {
    var selectedSegment by remember { mutableStateOf("Month") }
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val recentLogs by viewModel.recentLogs.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.loadRecentLogs() }

    val isDone: (Int, String) -> Boolean = { id, key ->
        val log = recentLogs[key]?.get(id)
        log?.completed == true
    }
    val orderedDates = remember(recentLogs) { recentLogs.keys.sortedDescending() }
    val doneToday = habits.count { isDone(it.id, viewModel.today) }
    val completionToday = if (habits.isEmpty()) 0 else (doneToday * 100) / habits.size
    val totalFinished = habits.sumOf { habit -> recentLogs.values.count { dayLogs -> dayLogs[habit.id]?.completed == true } }

    val statsCards = listOf(
        Triple("Completion Today", "$completionToday %", "$doneToday of ${habits.size} done"),
        Triple("Active Habits", "${habits.size}", "Tracked now"),
        Triple("Days Logged", "${recentLogs.count { it.value.isNotEmpty() }}", "With activity"),
        Triple("Total Completed", "$totalFinished", "All check-ins"),
    )

    data class BreakItem(val name: String, val pct: Int, val detail: String)
    val breakdown = habits.map { habit ->
        val doneDays = orderedDates.count { isDone(habit.id, it) }
        val pct = if (orderedDates.isEmpty()) 0 else (doneDays * 100) / orderedDates.size
        val detail = "${habit.category} • ${habit.frequency}"
        BreakItem(habit.name, pct, detail)
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHigh

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        TopBar(title = "Statistics", onProfileClick = onNavigateToSettings)
        Spacer(modifier = Modifier.height(8.dp))

        // Segmented control using SingleChoiceSegmentedButtonRow
        val segmentOptions = listOf("Week", "Month")
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            segmentOptions.forEachIndexed { index, label ->
                SegmentedButton(
                    selected = selectedSegment == label,
                    onClick = { selectedSegment = label },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = segmentOptions.size)
                ) {
                    Text(label, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // 2x2 stat cards
        val firstRow = statsCards.take(2)
        val secondRow = statsCards.drop(2)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            firstRow.forEach { (label, value, sub) ->
                StatCard(label, value, sub, modifier = Modifier.weight(1f))
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            secondRow.forEach { (label, value, sub) ->
                StatCard(label, value, sub, modifier = Modifier.weight(1f))
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Weekly Flow card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Weekly Flow", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text("Performance by calendar day", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.primaryContainer) {
                        Text("Week", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceEvenly) {
                    val dayFmt = remember { SimpleDateFormat("EEE", Locale.US) }
                    val dateFmt = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
                    orderedDates.reversed().forEach { key ->
                        val pct = if (habits.isEmpty()) 0f else {
                            val done = habits.count { isDone(it.id, key) }
                            done * 100f / habits.size
                        }
                        val isToday = key == viewModel.today
                        val dayLabel = remember(key) {
                            runCatching { dayFmt.format(dateFmt.parse(key)!!) }.getOrDefault("")
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            if (pct > 0) {
                                Text("${pct.toInt()}%", style = MaterialTheme.typography.labelSmall, color = if (isToday) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            val barColor = if (isToday) primaryColor else (if (pct > 0) MaterialTheme.colorScheme.onSurfaceVariant else trackColor)
                            Box(modifier = Modifier.width(12.dp).height(if (pct > 0) (24.dp.value * pct / 100 + 8.dp.value).dp else 4.dp).background(barColor, MaterialTheme.shapes.extraSmall))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(dayLabel, style = MaterialTheme.typography.labelSmall, color = if (isToday) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Consistency Grid
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Consistency Grid", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text("${recentLogs.size} Days Monitored", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Less", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(modifier = Modifier.padding(horizontal = 4.dp)) {
                            repeat(3) { i ->
                                Box(modifier = Modifier.size(8.dp).padding(horizontal = 1.dp).clip(MaterialTheme.shapes.extraSmall).background(if (i == 2) primaryColor else trackColor))
                            }
                        }
                        Text("More", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                orderedDates.reversed().forEach { key ->
                    val doneCount = habits.count { isDone(it.id, key) }
                    val intensity = if (habits.isEmpty()) 0f else doneCount.toFloat() / habits.size
                    val isToday = key == viewModel.today
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = key,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(80.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(10.dp)
                                .clip(MaterialTheme.shapes.extraSmall)
                                .background(trackColor)
                        ) {
                            if (intensity > 0f) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(intensity)
                                        .fillMaxSize()
                                        .clip(MaterialTheme.shapes.extraSmall)
                                        .background(primaryColor)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$doneCount/${habits.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isToday) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Habit Breakdown section
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Habit Breakdown", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            Text("${habits.size} Active Habits", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.height(8.dp))
        breakdown.forEach { item ->
            val name = item.name
            val pct = item.pct
            val detail = item.detail
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    val icon = habits.find { it.name == name }?.let { habitIcon(it.iconName) } ?: Icons.Rounded.CheckCircle
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = name, tint = primaryColor, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("$pct%", style = MaterialTheme.typography.labelLarge, color = primaryColor)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(detail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { (pct / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(MaterialTheme.shapes.extraSmall),
                            color = primaryColor,
                            trackColor = trackColor,
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Bottom summary card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            shape = MaterialTheme.shapes.medium
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(primaryColor)
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Rounded.Insights, contentDescription = "Discipline", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Solid Habit Discipline", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    val summary = if (habits.isEmpty()) {
                        "No habits yet. Create your first habit to start building a rhythm."
                    } else {
                        "You've completed $doneToday of ${habits.size} habits today. Keep building the rhythm one day at a time."
                    }
                    Text(summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun StatCard(label: String, value: String, sub: String, modifier: Modifier = Modifier) {
    val icon = when (label) {
        "Completion Today" -> Icons.Rounded.CheckCircle
        "Active Habits" -> Icons.Rounded.FormatListBulleted
        "Days Logged" -> Icons.Rounded.CalendarToday
        else -> Icons.Rounded.CheckCircle
    }
    val primaryColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Icon(icon, contentDescription = label, tint = primaryColor, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(sub, style = MaterialTheme.typography.labelSmall, color = primaryColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
