package com.toufikforyou.mytodoapp.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toufikforyou.mytodoapp.domain.TodoQuery
import com.toufikforyou.mytodoapp.domain.TodoSection
import com.toufikforyou.mytodoapp.domain.model.Category
import com.toufikforyou.mytodoapp.domain.model.SortOrder
import com.toufikforyou.mytodoapp.domain.model.Todo
import com.toufikforyou.mytodoapp.domain.model.TodoFilter
import com.toufikforyou.mytodoapp.domain.repository.CategoryRepository
import com.toufikforyou.mytodoapp.domain.repository.SettingsRepository
import com.toufikforyou.mytodoapp.domain.repository.TodoRepository
import com.toufikforyou.mytodoapp.notification.ReminderScheduler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

data class TaskListState(
    val isLoading: Boolean = true,
    val sections: List<TodoSection> = emptyList(),
    val categories: List<Category> = emptyList(),
    val search: String = "",
    val filter: TodoFilter = TodoFilter.ALL,
    val selectedCategoryId: Long? = null,
    val sortOrder: SortOrder = SortOrder.DUE_DATE,
    val showCompleted: Boolean = true,
    /** Counts cover every task, not just the filtered view, so the header stays stable. */
    val totalCount: Int = 0,
    val completedCount: Int = 0,
    val overdueCount: Int = 0,
    val now: Instant = Clock.System.now(),
) {
    val isEmpty: Boolean get() = !isLoading && sections.isEmpty()
    val hasAnyTasks: Boolean get() = totalCount > 0
    val progress: Float
        get() = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount
}

/** One-shot results the screen turns into a snackbar. */
sealed interface TaskListEvent {
    data class TodoDeleted(val todo: Todo) : TaskListEvent
    data class CompletedCleared(val todos: List<Todo>) : TaskListEvent
}

class TaskListViewModel(
    private val todos: TodoRepository,
    private val categories: CategoryRepository,
    private val settings: SettingsRepository,
    private val reminders: ReminderScheduler,
) : ViewModel() {

    private val search = MutableStateFlow("")
    private val filter = MutableStateFlow(TodoFilter.ALL)
    private val selectedCategoryId = MutableStateFlow<Long?>(null)

    private val events = Channel<TaskListEvent>(Channel.BUFFERED)
    val uiEvents = events.receiveAsFlow()

    /** Re-emits every minute so "Overdue" and relative date labels stay honest. */
    private val ticker = flow {
        while (true) {
            emit(Clock.System.now())
            delay(1.minutes)
        }
    }

    private val query = combine(
        search,
        filter,
        selectedCategoryId,
        ticker,
    ) { search, filter, categoryId, now -> Query(search, filter, categoryId, now) }

    val state: StateFlow<TaskListState> = combine(
        todos.observeTodos(),
        categories.observeCategories(),
        settings.observeSettings(),
        query,
    ) { allTodos, allCategories, appSettings, query ->
        val visible = TodoQuery.run(
            todos = allTodos,
            search = query.search,
            filter = query.filter,
            categoryId = query.categoryId,
            sort = appSettings.sortOrder,
            showCompleted = appSettings.showCompleted,
            now = query.now,
        )
        TaskListState(
            isLoading = false,
            sections = TodoQuery.sections(visible, appSettings.sortOrder, query.now),
            categories = allCategories,
            search = query.search,
            filter = query.filter,
            selectedCategoryId = query.categoryId,
            sortOrder = appSettings.sortOrder,
            showCompleted = appSettings.showCompleted,
            totalCount = allTodos.size,
            completedCount = allTodos.count { it.isCompleted },
            overdueCount = allTodos.count { it.isOverdue(query.now) },
            now = query.now,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TaskListState(),
    )

    fun onSearchChange(value: String) {
        search.value = value
    }

    fun onFilterChange(value: TodoFilter) {
        filter.value = value
    }

    fun onCategoryChange(value: Long?) {
        selectedCategoryId.value = value
    }

    fun onSortChange(value: SortOrder) {
        viewModelScope.launch { settings.update { it.copy(sortOrder = value) } }
    }

    fun onShowCompletedChange(value: Boolean) {
        viewModelScope.launch { settings.update { it.copy(showCompleted = value) } }
    }

    fun onToggleComplete(todo: Todo) {
        viewModelScope.launch {
            val completed = !todo.isCompleted
            todos.setCompleted(todo.id, completed)
            // A finished task should not keep buzzing; reopening one restores its reminder.
            if (completed) {
                reminders.cancel(todo.id)
            } else {
                reminders.schedule(todo.copy(isCompleted = false))
            }
        }
    }

    fun onDelete(todo: Todo) {
        viewModelScope.launch {
            todos.delete(todo.id)
            reminders.cancel(todo.id)
            events.send(TaskListEvent.TodoDeleted(todo))
        }
    }

    fun onUndoDelete(todo: Todo) {
        viewModelScope.launch {
            todos.restore(todo)
            reminders.schedule(todo)
        }
    }

    fun onClearCompleted() {
        viewModelScope.launch {
            val cleared = todos.clearCompleted()
            cleared.forEach { reminders.cancel(it.id) }
            if (cleared.isNotEmpty()) events.send(TaskListEvent.CompletedCleared(cleared))
        }
    }

    fun onUndoClearCompleted(cleared: List<Todo>) {
        viewModelScope.launch {
            cleared.forEach {
                todos.restore(it)
                reminders.schedule(it)
            }
        }
    }

    private data class Query(
        val search: String,
        val filter: TodoFilter,
        val categoryId: Long?,
        val now: Instant,
    )
}
