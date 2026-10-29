package com.example.rabbittracker.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckBoxOutlineBlank
import androidx.compose.material.icons.rounded.NaturePeople
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rabbittracker.data.Habit
import com.example.rabbittracker.ui.HabitViewModel
import com.example.rabbittracker.ui.components.TopBar
import com.example.rabbittracker.ui.components.habitAccent
import com.example.rabbittracker.ui.components.habitIcon
import com.example.rabbittracker.utils.tickVibrate

@Composable
fun HabitsScreen(
    viewModel: HabitViewModel,
    onNavigateToCreate: () -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToSettings: () -> Unit = {}
) {
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val logs by viewModel.logs.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val userPhotoPath by viewModel.userPhotoPath.collectAsStateWithLifecycle()

    var query by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    val categories = remember(habits) {
        listOf("All") + (habits.map { it.category }.distinct().sorted())
    }
    if (selectedFilter !in categories) selectedFilter = "All"

    val filtered = habits.filter { habit ->
        val matchesQuery = query.isBlank() || habit.name.contains(query, ignoreCase = true)
        val matchesFilter = selectedFilter == "All" || habit.category == selectedFilter
        matchesQuery && matchesFilter
    }

    val sortedHabits = remember(filtered, logs) {
        filtered.sortedBy { habit -> logs[habit.id]?.completed == true }
    }

    val doneCount = habits.count { habit ->
        val log = logs[habit.id]
        log?.completed == true
    }
    val momentum = if (habits.isEmpty()) 0f else doneCount.toFloat() / habits.size
    val animatedMomentum by animateFloatAsState(
        targetValue = momentum,
        animationSpec = tween(500)
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHigh

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        TopBar(title = "Habits", userName = userName, userPhotoPath = userPhotoPath, onProfileClick = onNavigateToSettings)
        Spacer(modifier = Modifier.height(4.dp))

        // Search field
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search habits...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            leadingIcon = { Icon(imageVector = Icons.Rounded.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent
            ),
            shape = MaterialTheme.shapes.medium,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = selectedFilter == cat
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = cat },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(cat)
                            if (cat == "All") {
                                Spacer(modifier = Modifier.width(6.dp))
                                Badge(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
                                ) {
                                    Text("${habits.size}")
                                }
                            }
                        }
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Momentum card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            shape = MaterialTheme.shapes.medium
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Rounded.NaturePeople, contentDescription = "Leaf", tint = primaryColor, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Daily Momentum", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text("$doneCount of ${habits.size} routines completed", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = 4.dp.toPx()
                        val ringSize = size.minDimension - stroke * 2
                        drawArc(
                            color = trackColor,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = androidx.compose.ui.geometry.Offset(stroke, stroke),
                            size = androidx.compose.ui.geometry.Size(ringSize, ringSize),
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = primaryColor,
                            startAngle = -90f,
                            sweepAngle = 360f * animatedMomentum,
                            useCenter = false,
                            topLeft = androidx.compose.ui.geometry.Offset(stroke, stroke),
                            size = androidx.compose.ui.geometry.Size(ringSize, ringSize),
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Detailed habit cards or 56dp circular FAB
        if (sortedHabits.isEmpty()) {
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
            sortedHabits.forEach { habit ->
                HabitDetailRow(
                    habit = habit,
                    completed = logs[habit.id]?.completed == true,
                    onToggleDone = { viewModel.toggleHabit(habit.id) },
                    onDetail = { onNavigateToDetail(habit.id) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
        Spacer(modifier = Modifier.height(88.dp))
    }
}

@Composable
fun HabitDetailRow(
    habit: Habit,
    completed: Boolean,
    onToggleDone: () -> Unit,
    onDetail: () -> Unit
) {
    val context = LocalContext.current
    var wasCompleted by remember { mutableStateOf(completed) }
    LaunchedEffect(completed) {
        if (!wasCompleted && completed) {
            context.tickVibrate()
        }
        wasCompleted = completed
    }

    val animatedCardColor by animateColorAsState(
        targetValue = if (completed) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceContainer,
        animationSpec = tween(400)
    )
    val scaleAnim by animateFloatAsState(
        targetValue = if (completed) 1.25f else 1f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMedium)
    )

    val chipText = if (completed) "✓ Done" else "⏱ Pending"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onDetail() },
        colors = CardDefaults.cardColors(containerColor = animatedCardColor),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(habitAccent(habit.colorAccent))
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = habitIcon(habit.iconName),
                            contentDescription = habit.name,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            habit.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "${habit.category} • ${habit.frequency}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = if (completed) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Text(
                        chipText,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (completed) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onToggleDone() },
                    color = if (completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                    shadowElevation = 0.dp
                ) {
                    Box(modifier = Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (completed) Icons.Rounded.Check else Icons.Rounded.CheckBoxOutlineBlank,
                            contentDescription = "Check",
                            tint = if (completed) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp).scale(scaleAnim)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (completed) "Completed today" else "Not done yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
