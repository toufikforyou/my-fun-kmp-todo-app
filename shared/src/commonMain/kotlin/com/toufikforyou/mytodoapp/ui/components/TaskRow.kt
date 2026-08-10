package com.toufikforyou.mytodoapp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.toufikforyou.mytodoapp.domain.DateTimes
import com.toufikforyou.mytodoapp.domain.model.Category
import com.toufikforyou.mytodoapp.domain.model.Priority
import com.toufikforyou.mytodoapp.domain.model.Todo
import com.toufikforyou.mytodoapp.ui.icons.AppIcons
import com.toufikforyou.mytodoapp.ui.theme.AppMotion
import com.toufikforyou.mytodoapp.ui.theme.PriorityColors
import kotlin.time.Instant

/**
 * A single task card: completion toggle, title, and a metadata line describing the list it belongs
 * to, when it is due, and whether a reminder is armed.
 */
@Composable
fun TaskRow(
    todo: Todo,
    category: Category?,
    now: Instant,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val overdue = todo.isOverdue(now)
    val contentAlpha by animateFloatAsState(
        targetValue = if (todo.isCompleted) 0.55f else 1f,
        animationSpec = AppMotion.quick(),
    )

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            modifier = Modifier.padding(end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PriorityStripe(todo.priority, completed = todo.isCompleted)

            CompletionToggle(
                checked = todo.isCompleted,
                priority = todo.priority,
                onToggle = onToggle,
                modifier = Modifier.padding(start = 8.dp, end = 12.dp),
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 14.dp)
                    .alpha(contentAlpha),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = todo.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else null,
                )
                if (todo.notes.isNotBlank()) {
                    Text(
                        text = todo.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                TaskMetadata(todo = todo, category = category, now = now, overdue = overdue)
            }
        }
    }
}

@Composable
private fun TaskMetadata(
    todo: Todo,
    category: Category?,
    now: Instant,
    overdue: Boolean,
) {
    val hasDue = todo.dueAt != null
    if (!hasDue && category == null && !todo.hasReminder) return

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (category != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Box(
                    Modifier
                        .size(8.dp)
                        .background(Color(category.colorArgb), CircleShape),
                )
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (todo.dueAt != null) {
            val dueColor = when {
                overdue -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = if (overdue) AppIcons.Warning else AppIcons.Calendar,
                    contentDescription = null,
                    tint = dueColor,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = DateTimes.formatDateTime(todo.dueAt, now),
                    style = MaterialTheme.typography.labelMedium,
                    color = dueColor,
                )
            }
        }

        if (todo.hasReminder) {
            Icon(
                imageVector = AppIcons.Bell,
                contentDescription = "Reminder set",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

/** A colour bar on the leading edge; only urgent work earns one. */
@Composable
private fun PriorityStripe(priority: Priority, completed: Boolean) {
    val visible = !completed && priority != Priority.NONE && priority != Priority.LOW
    val color by animateColorAsState(
        targetValue = if (visible) PriorityColors.accent(priority) else Color.Transparent,
        animationSpec = AppMotion.quick(),
    )
    Box(
        Modifier
            .padding(vertical = 10.dp)
            .width(4.dp)
            .height(36.dp)
            .background(color, RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)),
    )
}

/**
 * A hand-drawn checkbox rather than the Material one so the ring can pick up the task's priority
 * colour and the check can pop in on a spring.
 */
@Composable
private fun CompletionToggle(
    checked: Boolean,
    priority: Priority,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = PriorityColors.accent(priority)
    val fill by animateColorAsState(
        targetValue = if (checked) accent else Color.Transparent,
        animationSpec = AppMotion.quick(),
    )
    val ring by animateColorAsState(
        targetValue = if (checked) accent else MaterialTheme.colorScheme.outline,
        animationSpec = AppMotion.quick(),
    )
    val checkScale by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = AppMotion.bouncy(),
    )

    Box(
        modifier = modifier
            .size(26.dp)
            .background(fill, CircleShape)
            .border(2.dp, ring, CircleShape)
            .clickable(
                role = Role.Checkbox,
                onClickLabel = if (checked) "Mark as not done" else "Mark as done",
                onClick = onToggle,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = AppIcons.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .size(18.dp)
                .scale(checkScale),
        )
    }
}
