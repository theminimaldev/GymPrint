package com.theminimaldev.gymprint.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val DAY_LABELS = listOf("Mon", "", "Wed", "", "Fri", "", "")

@Composable
fun ContributionGraph(
    visitDates: Set<String>,
    modifier: Modifier = Modifier,
    cellSize: Dp = 12.dp,
    gap: Dp = 2.dp,
    weeks: Int = 52
) {
    val filledColor = MaterialTheme.colorScheme.primary
    val emptyColor = MaterialTheme.colorScheme.surfaceVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    val scrollState = rememberScrollState()
    var selectedCell by remember { mutableStateOf<Pair<LocalDate, Boolean>?>(null) }

    // Compute the start date — Monday of (today - weeks + 1) weeks
    val today = LocalDate.now()
    val startDate = remember(weeks) {
        today.minusWeeks((weeks - 1).toLong())
            .with(DayOfWeek.MONDAY)
    }

    // Month labels: collect month start positions (column index)
    val monthLabels = remember(startDate, weeks) {
        buildList {
            for (col in 0 until weeks) {
                val monday = startDate.plusWeeks(col.toLong())
                if (monday.dayOfMonth <= 7) { // first Monday of a month
                    add(col to monday.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()))
                }
            }
        }
    }

    // Auto-scroll to end (current week) after first composition
    LaunchedEffect(Unit) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Column(modifier = modifier) {
        // Month labels row
        Box(
            modifier = Modifier
                .horizontalScroll(scrollState)
                .padding(start = 28.dp) // offset for day labels
        ) {
            // Month labels as Text composables overlaid via offset
            monthLabels.forEach { (col, label) ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                    modifier = Modifier.offset(x = (cellSize + gap) * col)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row {
            // Day-of-week labels (Y axis)
            Column(modifier = Modifier.width(28.dp)) {
                DAY_LABELS.forEach { label ->
                    Box(modifier = Modifier.size(cellSize + gap)) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = labelColor
                        )
                    }
                }
            }

            // Grid
            var tooltipPosition by remember { mutableStateOf(Offset.Zero) }

            Box {
                Canvas(
                    modifier = Modifier
                        .horizontalScroll(scrollState)
                        .width((cellSize + gap) * weeks - gap)
                        .height((cellSize + gap) * 7 - gap)
                        .pointerInput(visitDates) {
                            detectTapGestures { offset ->
                                val colWidthPx = (cellSize + gap).toPx()
                                val rowHeightPx = (cellSize + gap).toPx()
                                val col = (offset.x / colWidthPx).toInt().coerceIn(0, weeks - 1)
                                val row = (offset.y / rowHeightPx).toInt().coerceIn(0, 6)
                                val tapped = startDate.plusDays((col * 7 + row).toLong())
                                if (!tapped.isAfter(today)) {
                                    selectedCell = tapped to visitDates.contains(tapped.toString())
                                    tooltipPosition = offset
                                }
                            }
                        }
                ) {
                    val colWidthPx = (cellSize + gap).toPx()
                    val rowHeightPx = (cellSize + gap).toPx()
                    val cornerRadiusPx = (cellSize / 4).toPx()
                    val cellPx = cellSize.toPx()

                    for (col in 0 until weeks) {
                        for (row in 0 until 7) {
                            val date = startDate.plusDays((col * 7 + row).toLong())
                            if (date.isAfter(today)) continue
                            val filled = visitDates.contains(date.toString())
                            drawRoundRect(
                                color = if (filled) filledColor else emptyColor,
                                topLeft = Offset(col * colWidthPx, row * rowHeightPx),
                                size = Size(cellPx, cellPx),
                                cornerRadius = CornerRadius(cornerRadiusPx)
                            )
                        }
                    }
                }

                // Tooltip popup
                selectedCell?.let { (date, visited) ->
                    TooltipPopup(
                        date = date,
                        visited = visited,
                        onDismiss = { selectedCell = null }
                    )
                }
            }
        }
    }
}
