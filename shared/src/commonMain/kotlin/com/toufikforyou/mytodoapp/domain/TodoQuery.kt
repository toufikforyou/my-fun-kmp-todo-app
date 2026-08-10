package com.toufikforyou.mytodoapp.domain

import com.toufikforyou.mytodoapp.domain.model.Priority
import com.toufikforyou.mytodoapp.domain.model.SortOrder
import com.toufikforyou.mytodoapp.domain.model.Todo
import com.toufikforyou.mytodoapp.domain.model.TodoFilter
import kotlinx.datetime.TimeZone
import kotlin.time.Instant

/** A titled run of tasks in the list, e.g. "Overdue" or "High priority". */
data class TodoSection(
    val title: String,
    val todos: List<Todo>,
)

/**
 * Pure search / filter / sort / group logic. Kept out of SQL so it can be unit tested without a
 * database, and so the whole pipeline re-runs cheaply as the user types.
 */
object TodoQuery {

    fun run(
        todos: List<Todo>,
        search: String = "",
        filter: TodoFilter = TodoFilter.ALL,
        categoryId: Long? = null,
        sort: SortOrder = SortOrder.DUE_DATE,
        showCompleted: Boolean = true,
        now: Instant,
        tz: TimeZone = TimeZone.currentSystemDefault(),
    ): List<Todo> = todos
        .asSequence()
        .filter { matchesSearch(it, search) }
        .filter { categoryId == null || it.categoryId == categoryId }
        .filter { matchesFilter(it, filter, now, tz) }
        .filter { showCompleted || !it.isCompleted || filter == TodoFilter.COMPLETED }
        .sortedWith(comparatorFor(sort))
        .toList()

    fun matchesSearch(todo: Todo, search: String): Boolean {
        val term = search.trim()
        if (term.isEmpty()) return true
        return todo.title.contains(term, ignoreCase = true) ||
            todo.notes.contains(term, ignoreCase = true)
    }

    fun matchesFilter(
        todo: Todo,
        filter: TodoFilter,
        now: Instant,
        tz: TimeZone = TimeZone.currentSystemDefault(),
    ): Boolean {
        val today = DateTimes.today(tz, now)
        return when (filter) {
            TodoFilter.ALL -> true
            TodoFilter.ACTIVE -> !todo.isCompleted
            TodoFilter.COMPLETED -> todo.isCompleted
            TodoFilter.TODAY -> !todo.isCompleted && todo.dueAt != null &&
                todo.dueAt < DateTimes.endOfDay(today, tz)
            TodoFilter.UPCOMING -> !todo.isCompleted && todo.dueAt != null &&
                todo.dueAt >= DateTimes.endOfDay(today, tz)
            TodoFilter.OVERDUE -> todo.isOverdue(now)
        }
    }

    /**
     * Open tasks always sort ahead of completed ones; within each group the chosen [sort] applies,
     * with title as a stable tie-breaker.
     */
    fun comparatorFor(sort: SortOrder): Comparator<Todo> {
        val within = when (sort) {
            // Tasks without a due date sort last rather than first.
            SortOrder.DUE_DATE -> compareBy<Todo> { it.dueAt == null }
                .thenBy { it.dueAt }
                .thenByDescending { it.priority }
            SortOrder.PRIORITY -> compareByDescending<Todo> { it.priority }
                .thenBy { it.dueAt == null }
                .thenBy { it.dueAt }
            SortOrder.CREATED -> compareByDescending { it.createdAt }
            SortOrder.ALPHABETICAL -> compareBy { it.title.lowercase() }
        }
        return compareBy<Todo> { it.isCompleted }
            .then(within)
            .thenBy { it.title.lowercase() }
            .thenBy { it.id }
    }

    /**
     * Splits an already-sorted list into the headers shown in the UI. Due-date sorting groups by
     * when work is due, priority sorting groups by level, and the remaining orders stay flat.
     */
    fun sections(
        todos: List<Todo>,
        sort: SortOrder,
        now: Instant,
        tz: TimeZone = TimeZone.currentSystemDefault(),
    ): List<TodoSection> {
        if (todos.isEmpty()) return emptyList()
        val (open, done) = todos.partition { !it.isCompleted }

        val openSections = when (sort) {
            SortOrder.DUE_DATE -> groupByDueBucket(open, now, tz)
            SortOrder.PRIORITY -> open.groupBy { it.priority }
                .map { (priority, items) -> TodoSection(priority.sectionTitle(), items) }
            SortOrder.CREATED, SortOrder.ALPHABETICAL ->
                listOf(TodoSection("Tasks", open))
        }.filter { it.todos.isNotEmpty() }

        return if (done.isEmpty()) {
            openSections
        } else {
            openSections + TodoSection("Completed", done)
        }
    }

    private fun groupByDueBucket(
        todos: List<Todo>,
        now: Instant,
        tz: TimeZone,
    ): List<TodoSection> {
        val today = DateTimes.today(tz, now)
        val endOfToday = DateTimes.endOfDay(today, tz)
        val buckets = LinkedHashMap<String, MutableList<Todo>>()
        // Fixed order so headers never jump around as tasks move between buckets.
        listOf("Overdue", "Today", "Tomorrow", "This week", "Later", "No date").forEach {
            buckets[it] = mutableListOf()
        }
        todos.forEach { todo ->
            val due = todo.dueAt
            val key = when {
                due == null -> "No date"
                due < now -> "Overdue"
                due < endOfToday -> "Today"
                else -> when (DateTimes.daysBetween(today, DateTimes.dateOf(due, tz))) {
                    1 -> "Tomorrow"
                    in 2..7 -> "This week"
                    else -> "Later"
                }
            }
            buckets.getValue(key).add(todo)
        }
        return buckets.map { (title, items) -> TodoSection(title, items) }
    }
}

private fun Priority.sectionTitle(): String =
    if (this == Priority.NONE) "No priority" else "$label priority"
