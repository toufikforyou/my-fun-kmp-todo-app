package com.toufikforyou.mytodoapp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.toufikforyou.mytodoapp.domain.DateTimes
import com.toufikforyou.mytodoapp.domain.model.Category
import com.toufikforyou.mytodoapp.domain.model.Priority
import com.toufikforyou.mytodoapp.domain.model.Todo
import com.toufikforyou.mytodoapp.ui.icons.AppIcons
import com.toufikforyou.mytodoapp.ui.theme.Accents
import com.toufikforyou.mytodoapp.ui.theme.AppMotion
import com.toufikforyou.mytodoapp.ui.theme.softTint
import kotlin.time.Instant

/**
 * A single task card. The card takes a wash of its list's color so a glance down the list reads as
 * colour-coded groups, and the metadata underneath is carried by small tinted pills rather than
 * plain grey text.
 */
@OptIn(ExperimentalLayoutApi::class)
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
    val accent = category?.let { Color(it.colorArgb) }
        ?: Accents.of(todo.priority).takeIf { todo.priority != Priority.NONE }
        ?: MaterialTheme.colorScheme.primary

    // Completed cards drain their color as well as fading, so "done" reads instantly.
    val cardColor by animateColorAsState(
        targetValue = if (todo.isCompleted) {
            MaterialTheme.colorScheme.surfaceContainerLow
        } else {
            softTint(accent)
        },
        animationSpec = AppMotion.quick(),
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (todo.isCompleted) 0.5f else 1f,
        animationSpec = AppMotion.quick(),
    )

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = cardColor,
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 16.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            CompletionToggle(
                checked = todo.isCompleted,
                accent = accent,
                onToggle = onToggle,
                modifier = Modifier.padding(end = 14.dp),
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .alpha(contentAlpha),
                verticalArrangement = Arrangement.spacedBy(6.dp),
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

                val hasMetadata = category != null ||
                    todo.dueAt != null ||
                    todo.hasReminder ||
                    todo.priority != Priority.NONE
                if (hasMetadata) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        if (todo.priority != Priority.NONE) {
                            MetaPill(
                                text = todo.priority.label,
                                color = Accents.of(todo.priority),
                                icon = AppIcons.Flag,
                            )
                        }
                        if (category != null) {
                            MetaPill(
                                text = category.name,
                                color = Color(category.colorArgb),
                                icon = AppIcons.Label,
                            )
                        }
                        if (todo.dueAt != null) {
                            MetaPill(
                                text = DateTimes.formatDateTime(todo.dueAt, now),
                                color = if (overdue) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                icon = if (overdue) AppIcons.Warning else AppIcons.Calendar,
                            )
                        }
                        if (todo.hasReminder) {
                            MetaPill(
                                text = null,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                icon = AppIcons.Bell,
                                contentDescription = "Reminder set",
                            )
                        }
                    }
                }
            }
        }
    }
}

/** A small tinted capsule carrying one piece of metadata. */
@Composable
private fun MetaPill(
    text: String?,
    color: Color,
    icon: ImageVector,
    contentDescription: String? = null,
) {
    Row(
        modifier = Modifier
            .background(color.copy(alpha = 0.16f), CircleShape)
            .padding(horizontal = if (text == null) 6.dp else 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = color,
            modifier = Modifier.size(13.dp),
        )
        if (text != null) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                maxLines = 1,
            )
        }
    }
}

/**
 * A hand-drawn checkbox rather than the Material one, so the ring can pick up the task's list
 * color and the check can pop in on a spring.
 */
@Composable
private fun CompletionToggle(
    checked: Boolean,
    accent: Color,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fill by animateColorAsState(
        targetValue = if (checked) accent else Color.Transparent,
        animationSpec = AppMotion.quick(),
    )
    val ring by animateColorAsState(
        targetValue = if (checked) accent else accent.copy(alpha = 0.55f),
        animationSpec = AppMotion.quick(),
    )
    val checkScale by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = AppMotion.bouncy(),
    )

    Box(
        modifier = modifier
            .size(28.dp)
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
                .size(19.dp)
                .scale(checkScale),
        )
    }
}
