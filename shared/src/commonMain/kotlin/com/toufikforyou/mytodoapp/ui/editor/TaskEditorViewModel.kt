package com.toufikforyou.mytodoapp.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toufikforyou.mytodoapp.domain.DateTimes
import com.toufikforyou.mytodoapp.domain.model.Category
import com.toufikforyou.mytodoapp.domain.model.Priority
import com.toufikforyou.mytodoapp.domain.model.Todo
import com.toufikforyou.mytodoapp.domain.repository.CategoryRepository
import com.toufikforyou.mytodoapp.domain.repository.SettingsRepository
import com.toufikforyou.mytodoapp.domain.repository.TodoRepository
import com.toufikforyou.mytodoapp.notification.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/** How far ahead of the due time a reminder fires. */
enum class ReminderLead(val label: String, val minutesBefore: Int) {
    AT_TIME("At the time it's due", 0),
    TEN_MINUTES("10 minutes before", 10),
    THIRTY_MINUTES("30 minutes before", 30),
    ONE_HOUR("1 hour before", 60),
    ONE_DAY("1 day before", 24 * 60),
    ;

    companion object {
        fun closestTo(minutes: Int): ReminderLead =
            entries.minByOrNull { kotlin.math.abs(it.minutesBefore - minutes) } ?: THIRTY_MINUTES
    }
}

data class TaskEditorState(
    val id: Long = 0L,
    val title: String = "",
    val notes: String = "",
    val priority: Priority = Priority.NONE,
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,
    val reminderEnabled: Boolean = false,
    val reminderLead: ReminderLead = ReminderLead.THIRTY_MINUTES,
    val categoryId: Long? = null,
    val isCompleted: Boolean = false,
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = true,
    /** True while the OS notification permission is missing but a reminder is switched on. */
    val needsNotificationPermission: Boolean = false,
) {
    val isNew: Boolean get() = id == 0L
    val canSave: Boolean get() = title.isNotBlank()

    /** A reminder is only offered once the task has a due date to hang it off. */
    val canSetReminder: Boolean get() = dueDate != null

    val dueAt: Instant?
        get() = dueDate?.let { date ->
            DateTimes.instantOf(date, dueTime ?: LocalTime(9, 0))
        }

    val reminderAt: Instant?
        get() = if (!reminderEnabled) null else dueAt?.minus(reminderLead.minutesBefore.minutes)
}

class TaskEditorViewModel(
    private val todoId: Long,
    private val todos: TodoRepository,
    private val categories: CategoryRepository,
    private val settings: SettingsRepository,
    private val reminders: ReminderScheduler,
) : ViewModel() {

    private val _state = MutableStateFlow(TaskEditorState(id = todoId))
    val state: StateFlow<TaskEditorState> = _state.asStateFlow()

    /** Set once the task has been written, so the screen knows to navigate back. */
    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    init {
        viewModelScope.launch {
            val available = categories.observeCategories().first()
            val existing = if (todoId == 0L) null else todos.getTodo(todoId)
            val defaultLead = settings.observeSettings().first().defaultReminderLeadMinutes

            _state.value = if (existing == null) {
                TaskEditorState(
                    categories = available,
                    reminderLead = ReminderLead.closestTo(defaultLead),
                    isLoading = false,
                )
            } else {
                val lead = leadBetween(existing.dueAt, existing.reminderAt)
                TaskEditorState(
                    id = existing.id,
                    title = existing.title,
                    notes = existing.notes,
                    priority = existing.priority,
                    dueDate = existing.dueAt?.let { DateTimes.dateOf(it) },
                    dueTime = existing.dueAt?.let { DateTimes.timeOf(it) },
                    reminderEnabled = existing.reminderAt != null,
                    reminderLead = lead ?: ReminderLead.closestTo(defaultLead),
                    categoryId = existing.categoryId,
                    isCompleted = existing.isCompleted,
                    categories = available,
                    isLoading = false,
                )
            }
        }
    }

    fun onTitleChange(value: String) = _state.update { it.copy(title = value) }

    fun onNotesChange(value: String) = _state.update { it.copy(notes = value) }

    fun onPriorityChange(value: Priority) = _state.update { it.copy(priority = value) }

    fun onCategoryChange(value: Long?) = _state.update { it.copy(categoryId = value) }

    fun onDueDateChange(value: LocalDate?) = _state.update { current ->
        current.copy(
            dueDate = value,
            // Clearing the date takes the reminder with it — there is nothing left to fire against.
            reminderEnabled = if (value == null) false else current.reminderEnabled,
        )
    }

    fun onDueTimeChange(value: LocalTime) = _state.update { it.copy(dueTime = value) }

    fun onReminderLeadChange(value: ReminderLead) = _state.update { it.copy(reminderLead = value) }

    fun onReminderEnabledChange(enabled: Boolean) {
        if (!enabled) {
            _state.update { it.copy(reminderEnabled = false, needsNotificationPermission = false) }
            return
        }
        viewModelScope.launch {
            val granted = reminders.requestPermission()
            _state.update {
                it.copy(reminderEnabled = true, needsNotificationPermission = !granted)
            }
        }
    }

    fun onToggleCompleted() = _state.update { it.copy(isCompleted = !it.isCompleted) }

    fun onSave() {
        val current = _state.value
        if (!current.canSave) return
        viewModelScope.launch {
            val now = Clock.System.now()
            val existing = if (current.isNew) null else todos.getTodo(current.id)
            val todo = Todo(
                id = current.id,
                title = current.title.trim(),
                notes = current.notes.trim(),
                isCompleted = current.isCompleted,
                priority = current.priority,
                dueAt = current.dueAt,
                reminderAt = current.reminderAt,
                categoryId = current.categoryId,
                createdAt = existing?.createdAt ?: now,
                updatedAt = now,
                completedAt = when {
                    !current.isCompleted -> null
                    existing?.completedAt != null -> existing.completedAt
                    else -> now
                },
            )
            val id = todos.save(todo)
            reminders.schedule(todo.copy(id = id))
            _saved.value = true
        }
    }

    fun onDelete() {
        if (_state.value.isNew) return
        viewModelScope.launch {
            todos.delete(_state.value.id)
            reminders.cancel(_state.value.id)
            _saved.value = true
        }
    }

    /** Recovers which preset a stored reminder time corresponds to. */
    private fun leadBetween(dueAt: Instant?, reminderAt: Instant?): ReminderLead? {
        if (dueAt == null || reminderAt == null) return null
        return ReminderLead.closestTo((dueAt - reminderAt).inWholeMinutes.toInt())
    }
}
