package com.toufikforyou.mytodoapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.toufikforyou.mytodoapp.ui.theme.AppMotion

/**
 * The greeting block at the top of the list: a plain-language summary of what is left, with a ring
 * showing how much of the day's work is done.
 */
@Composable
fun ProgressHeader(
    total: Int,
    completed: Int,
    overdue: Int,
    modifier: Modifier = Modifier,
) {
    val remaining = total - completed
    val progress by animateFloatAsState(
        targetValue = if (total == 0) 0f else completed.toFloat() / total,
        animationSpec = AppMotion.quick(),
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = when {
                    total == 0 -> "Nothing planned"
                    remaining == 0 -> "All done"
                    else -> "$remaining to go"
                },
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = when {
                    total == 0 -> "Add your first task to get started."
                    overdue > 0 -> "$overdue overdue · $completed of $total done"
                    remaining == 0 -> "Every task is complete. Enjoy the quiet."
                    else -> "$completed of $total done"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (overdue > 0) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }

        ProgressRing(progress = progress, label = "$completed/$total")
    }
}

@Composable
private fun ProgressRing(progress: Float, label: String) {
    val track = MaterialTheme.colorScheme.surfaceContainerHighest
    val indicator = MaterialTheme.colorScheme.primary

    Box(contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(64.dp)) {
            val stroke = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
            val inset = stroke.width / 2
            val arcSize = Size(
                width = size.width - stroke.width,
                height = size.height - stroke.width,
            )
            val topLeft = Offset(inset, inset)
            drawArc(
                color = track,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke,
            )
            if (progress > 0f) {
                drawArc(
                    color = indicator,
                    // Start at 12 o'clock and fill clockwise.
                    startAngle = -90f,
                    sweepAngle = 360f * progress.coerceIn(0f, 1f),
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = stroke,
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
