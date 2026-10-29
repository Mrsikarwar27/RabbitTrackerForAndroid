package com.example.rabbittracker.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rabbittracker.ui.HabitViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class CompanionStage(val title: String, val minCompletions: Int) {
    BABY("Baby", 0),
    YOUNG("Young", 25),
    ADULT("Adult", 75),
    CHAMPION("Champion", 150)
}

fun getCompanionStage(allTimeCompletions: Int): CompanionStage {
    return when {
        allTimeCompletions >= 150 -> CompanionStage.CHAMPION
        allTimeCompletions >= 75 -> CompanionStage.ADULT
        allTimeCompletions >= 25 -> CompanionStage.YOUNG
        else -> CompanionStage.BABY
    }
}

@Composable
fun RabbitCompanionCard(
    viewModel: HabitViewModel,
    modifier: Modifier = Modifier
) {
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val logs by viewModel.logs.collectAsStateWithLifecycle()
    val totalCompletions by viewModel.totalCompletions.collectAsStateWithLifecycle()

    val totalHabits = habits.size
    val doneCount = habits.count { logs[it.id]?.completed == true }
    val stage = getCompanionStage(totalCompletions)

    var messageIndex by remember { mutableIntStateOf(0) }
    val offsetY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val isSleeping = habits.isNotEmpty() && doneCount == 0
    val isAllDone = habits.isNotEmpty() && doneCount == totalHabits

    // Trigger hop on completion change
    LaunchedEffect(doneCount) {
        if (doneCount > 0) {
            offsetY.animateTo(-16f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            offsetY.animateTo(0f, spring())
        }
    }

    fun triggerHop() {
        scope.launch {
            offsetY.animateTo(-16f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            offsetY.animateTo(0f, spring())
        }
    }

    val messages = when {
        isSleeping -> listOf(
            "Your rabbit is resting. One small step when you're ready!",
            "Shh... rabbit is sleeping. A habit will wake them up!",
            "Resting up for a productive day ahead. You've got this!"
        )
        isAllDone -> listOf(
            "All habits done! Feast day for your rabbit 🎉",
            "Superstar status! Every habit completed today! ✨",
            "Feast time! Your rabbit is super proud of you! 🥕🎉"
        )
        else -> listOf(
            "$doneCount of $totalHabits done — tasty carrots!",
            "Munching away! $doneCount habits completed, keep going!",
            "Great rhythm today! $doneCount down so far!"
        )
    }

    val currentMessage = messages[messageIndex % messages.size]

    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val onPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceContainer = MaterialTheme.colorScheme.surfaceContainerHigh

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                messageIndex++
                triggerHop()
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rabbit Canvas
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .offset { IntOffset(0, offsetY.value.roundToInt()) },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(72.dp)) {
                    val width = size.width
                    val height = size.height

                    val rabbitColor = primaryColor
                    val innerEarColor = primaryContainer
                    val accessoryColor = Color(0xFFFFB74D) // warm gold/orange

                    val scaleFactor = when (stage) {
                        CompanionStage.BABY -> 0.82f
                        CompanionStage.YOUNG -> 0.90f
                        CompanionStage.ADULT -> 0.96f
                        CompanionStage.CHAMPION -> 1.0f
                    }

                    val centerX = width / 2f
                    val headCenterY = height * 0.52f
                    val headRadius = (width * 0.28f) * scaleFactor

                    // Body
                    drawCircle(
                        color = rabbitColor,
                        radius = (width * 0.32f) * scaleFactor,
                        center = Offset(centerX, height * 0.72f)
                    )

                    // Ears
                    val earWidth = (width * 0.10f) * scaleFactor
                    val earHeight = (height * 0.38f) * scaleFactor

                    // Left Ear
                    drawOval(
                        color = rabbitColor,
                        topLeft = Offset(centerX - earWidth * 2.2f, headCenterY - earHeight - headRadius * 0.4f),
                        size = Size(earWidth * 1.6f, earHeight)
                    )
                    drawOval(
                        color = innerEarColor,
                        topLeft = Offset(centerX - earWidth * 1.8f, headCenterY - earHeight * 0.85f - headRadius * 0.4f),
                        size = Size(earWidth * 0.9f, earHeight * 0.7f)
                    )

                    // Right Ear
                    drawOval(
                        color = rabbitColor,
                        topLeft = Offset(centerX + earWidth * 0.6f, headCenterY - earHeight - headRadius * 0.4f),
                        size = Size(earWidth * 1.6f, earHeight)
                    )
                    drawOval(
                        color = innerEarColor,
                        topLeft = Offset(centerX + earWidth * 0.9f, headCenterY - earHeight * 0.85f - headRadius * 0.4f),
                        size = Size(earWidth * 0.9f, earHeight * 0.7f)
                    )

                    // Head
                    drawCircle(
                        color = rabbitColor,
                        radius = headRadius,
                        center = Offset(centerX, headCenterY)
                    )

                    // Eyes
                    if (isSleeping) {
                        // Sleeping eyes (- -)
                        drawLine(
                            color = onPrimaryContainer,
                            start = Offset(centerX - headRadius * 0.45f, headCenterY - headRadius * 0.05f),
                            end = Offset(centerX - headRadius * 0.15f, headCenterY - headRadius * 0.05f),
                            strokeWidth = 3f
                        )
                        drawLine(
                            color = onPrimaryContainer,
                            start = Offset(centerX + headRadius * 0.15f, headCenterY - headRadius * 0.05f),
                            end = Offset(centerX + headRadius * 0.45f, headCenterY - headRadius * 0.05f),
                            strokeWidth = 3f
                        )
                    } else if (isAllDone) {
                        // Celebration happy eyes (^ ^)
                        val leftEyePath = Path().apply {
                            moveTo(centerX - headRadius * 0.45f, headCenterY)
                            lineTo(centerX - headRadius * 0.30f, headCenterY - headRadius * 0.20f)
                            lineTo(centerX - headRadius * 0.15f, headCenterY)
                        }
                        drawPath(leftEyePath, onPrimaryContainer, style = Stroke(width = 3.5f))

                        val rightEyePath = Path().apply {
                            moveTo(centerX + headRadius * 0.15f, headCenterY)
                            lineTo(centerX + headRadius * 0.30f, headCenterY - headRadius * 0.20f)
                            lineTo(centerX + headRadius * 0.45f, headCenterY)
                        }
                        drawPath(rightEyePath, onPrimaryContainer, style = Stroke(width = 3.5f))
                    } else {
                        // Normal happy eyes (dots • •)
                        drawCircle(
                            color = onPrimaryContainer,
                            radius = headRadius * 0.11f,
                            center = Offset(centerX - headRadius * 0.30f, headCenterY - headRadius * 0.08f)
                        )
                        drawCircle(
                            color = onPrimaryContainer,
                            radius = headRadius * 0.11f,
                            center = Offset(centerX + headRadius * 0.30f, headCenterY - headRadius * 0.08f)
                        )
                    }

                    // Nose / Mouth
                    drawCircle(
                        color = Color(0xFFFF8A80), // soft pink
                        radius = headRadius * 0.09f,
                        center = Offset(centerX, headCenterY + headRadius * 0.18f)
                    )

                    // Cheeks
                    drawCircle(
                        color = Color(0xFFFF8A80).copy(alpha = 0.4f),
                        radius = headRadius * 0.14f,
                        center = Offset(centerX - headRadius * 0.50f, headCenterY + headRadius * 0.22f)
                    )
                    drawCircle(
                        color = Color(0xFFFF8A80).copy(alpha = 0.4f),
                        radius = headRadius * 0.14f,
                        center = Offset(centerX + headRadius * 0.50f, headCenterY + headRadius * 0.22f)
                    )

                    // Stage Accessories
                    when (stage) {
                        CompanionStage.BABY -> {
                            // No accessory
                        }
                        CompanionStage.YOUNG -> {
                            // Green leaf on head
                            val leafPath = Path().apply {
                                moveTo(centerX, headCenterY - headRadius * 0.9f)
                                quadraticTo(
                                    centerX + 12f, headCenterY - headRadius * 1.3f,
                                    centerX + 4f, headCenterY - headRadius * 1.5f
                                )
                                quadraticTo(
                                    centerX - 8f, headCenterY - headRadius * 1.2f,
                                    centerX, headCenterY - headRadius * 0.9f
                                )
                            }
                            drawPath(leafPath, Color(0xFF66BB6A))
                        }
                        CompanionStage.ADULT -> {
                            // Scarf around neck
                            drawOval(
                                color = Color(0xFFEF5350),
                                topLeft = Offset(centerX - headRadius * 0.65f, headCenterY + headRadius * 0.65f),
                                size = Size(headRadius * 1.30f, headRadius * 0.40f)
                            )
                        }
                        CompanionStage.CHAMPION -> {
                            // Golden Star Medal on chest
                            val starCenter = Offset(centerX, headCenterY + headRadius * 0.85f)
                            drawCircle(color = accessoryColor, radius = headRadius * 0.22f, center = starCenter)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Right Info Column
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = surfaceContainer
                    ) {
                        Text(
                            text = "${stage.title} Rabbit",
                            style = MaterialTheme.typography.labelSmall,
                            color = onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = "🥕 x $doneCount",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = currentMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
