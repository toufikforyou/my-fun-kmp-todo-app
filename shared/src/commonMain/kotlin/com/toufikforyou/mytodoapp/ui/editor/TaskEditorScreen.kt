package com.toufikforyou.mytodoapp.ui.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.toufikforyou.mytodoapp.domain.DateTimes
import com.toufikforyou.mytodoapp.domain.model.Priority
import com.toufikforyou.mytodoapp.ui.components.ColorDot
import com.toufikforyou.mytodoapp.ui.icons.AppIcons
import com.toufikforyou.mytodoapp.ui.theme.PriorityColors
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.time.Clock

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskEditorScreen(
    state: TaskEditorState,
    onTitleChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onPriorityChange: (Priority) -> Unit,
    onCategoryChange: (Long?) -> Unit,
    onDueDateChange: (LocalDate?) -> Unit,
    onDueTimeChange: (LocalTime) -> Unit,
    onReminderEnabledChange: (Boolean) -> Unit,
    onReminderLeadChange: (ReminderLead) -> Unit,
    onToggleCompleted: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val now = remember { Clock.System.now() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isNew) "New task" else "Edit task") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(AppIcons.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!state.isNew) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(
                                AppIcons.Delete,
                                contentDescription = "Delete task",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                },
            )
        },
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("What needs doing?") },
                textStyle = MaterialTheme.typography.titleMedium,
                shape = MaterialTheme.shapes.medium,
                singleLine = false,
            )

            OutlinedTextField(
                value = state.notes,
                onValueChange = onNotesChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Notes (optional)") },
                shape = MaterialTheme.shapes.medium,
                minLines = 3,
            )

            EditorSection(title = "Priority") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Priority.selectable.forEach { priority ->
                        FilterChip(
                            selected = state.priority == priority,
                            onClick = { onPriorityChange(priority) },
                            label = { Text(priority.label) },
                            shape = CircleShape,
                            leadingIcon = if (priority != Priority.NONE) {
                                { ColorDot(PriorityColors.accent(priority), size = 10.dp) }
                            } else {
                                null
                            },
                        )
                    }
                }
            }

            EditorSection(title = "When") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    EditorRow(
                        icon = AppIcons.Calendar,
                        label = "Due date",
                        value = state.dueDate
                            ?.let { DateTimes.formatDate(DateTimes.startOfDay(it), now) }
                            ?: "Not set",
                        onClick = { showDatePicker = true },
                        onClear = if (state.dueDate != null) {
                            { onDueDateChange(null) }
                        } else {
                            null
                        },
                    )
                    AnimatedVisibility(visible = state.dueDate != null) {
                        EditorRow(
                            icon = AppIcons.Clock,
                            label = "Time",
                            value = state.dueTime?.let(DateTimes::formatTime) ?: "9:00 AM",
                            onClick = { showTimePicker = true },
                        )
                    }
                }
            }

            EditorSection(title = "Reminder") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                AppIcons.Bell,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp),
                            )
                            Column(
                                Modifier
                                    .weight(1f)
                                    .padding(start = 16.dp),
                            ) {
                                Text("Notify me", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    text = if (state.canSetReminder) {
                                        "Sends a notification before this is due"
                                    } else {
                                        "Set a due date first"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Switch(
                                checked = state.reminderEnabled,
                                onCheckedChange = onReminderEnabledChange,
                                enabled = state.canSetReminder,
                            )
                        }
                    }

                    AnimatedVisibility(visible = state.reminderEnabled) {
                        ReminderLeadPicker(
                            selected = state.reminderLead,
                            onSelect = onReminderLeadChange,
                        )
                    }

                    AnimatedVisibility(visible = state.needsNotificationPermission) {
                        Text(
                            text = "Notifications are turned off for this app. Enable them in " +
                                "system settings for reminders to arrive.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 4.dp),
                        )
                    }
                }
            }

            if (state.categories.isNotEmpty()) {
                EditorSection(title = "List") {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = state.categoryId == null,
                            onClick = { onCategoryChange(null) },
                            label = { Text("None") },
                            shape = CircleShape,
                        )
                        state.categories.forEach { category ->
                            FilterChip(
                                selected = state.categoryId == category.id,
                                onClick = { onCategoryChange(category.id) },
                                label = { Text(category.name) },
                                shape = CircleShape,
                                leadingIcon = { ColorDot(Color(category.colorArgb), size = 10.dp) },
                            )
                        }
                    }
                }
            }

            if (!state.isNew) {
                TextButton(onClick = onToggleCompleted) {
                    Icon(
                        imageVector = if (state.isCompleted) {
                            AppIcons.Circle
                        } else {
                            AppIcons.CheckCircle
                        },
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = if (state.isCompleted) "Mark as not done" else "Mark as done",
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }

            Button(
                onClick = onSave,
                enabled = state.canSave,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (state.isNew) "Add task" else "Save changes")
            }
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.dueDate?.let(DateTimes::toPickerMillis),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let {
                            onDueDateChange(DateTimes.fromPickerMillis(it))
                        }
                        showDatePicker = false
                    },
                ) { Text("Set date") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }

    if (showTimePicker) {
        val initial = state.dueTime ?: LocalTime(9, 0)
        val timeState = rememberTimePickerState(
            initialHour = initial.hour,
            initialMinute = initial.minute,
            is24Hour = false,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDueTimeChange(LocalTime(timeState.hour, timeState.minute))
                        showTimePicker = false
                    },
                ) { Text("Set time") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
            title = { Text("Due time") },
            text = {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timeState)
                }
            },
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete this task?") },
            text = { Text("\"${state.title}\" will be removed. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun EditorSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
        )
        content()
    }
}

@Composable
private fun EditorRow(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit,
    onClear: (() -> Unit)? = null,
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (onClear != null) {
                IconButton(onClick = onClear) {
                    Icon(
                        AppIcons.Close,
                        contentDescription = "Clear $label",
                        modifier = Modifier.size(18.dp),
                    )
                }
            } else {
                Box(Modifier.size(8.dp))
            }
        }
    }
}

@Composable
private fun ReminderLeadPicker(
    selected: ReminderLead,
    onSelect: (ReminderLead) -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    Box {
        EditorRow(
            icon = AppIcons.Clock,
            label = "Remind me",
            value = selected.label,
            onClick = { open = true },
        )
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            ReminderLead.entries.forEach { lead ->
                DropdownMenuItem(
                    text = { Text(lead.label) },
                    onClick = {
                        onSelect(lead)
                        open = false
                    },
                    trailingIcon = {
                        if (lead == selected) Icon(AppIcons.Check, contentDescription = null)
                    },
                )
            }
        }
    }
}
