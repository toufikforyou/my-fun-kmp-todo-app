package com.toufikforyou.mytodoapp

import com.toufikforyou.mytodoapp.domain.TodoQuery
import com.toufikforyou.mytodoapp.domain.model.Priority
import com.toufikforyou.mytodoapp.domain.model.SortOrder
import com.toufikforyou.mytodoapp.domain.model.Todo
import com.toufikforyou.mytodoapp.domain.model.TodoFilter
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

class TodoQueryTest {

    // Midday UTC, so "start of day" and "end of day" maths stays inside the same date.
    private val now = Instant.parse("2026-08-10T12:00:00Z")
    private val utc = TimeZone.UTC

    private fun todo(
        id: Long,
        title: String = "Task $id",
        notes: String = "",
        completed: Boolean = false,
        priority: Priority = Priority.NONE,
        dueAt: Instant? = null,
        categoryId: Long? = null,
        createdAt: Instant = now,
    ) = Todo(
        id = id,
        title = title,
        notes = notes,
        isCompleted = completed,
        priority = priority,
        dueAt = dueAt,
        categoryId = categoryId,
        createdAt = createdAt,
        updatedAt = createdAt,
    )

    @Test
    fun `search matches title and notes case insensitively`() {
        val target = todo(1, title = "Buy Milk", notes = "semi skimmed")

        assertTrue(TodoQuery.matchesSearch(target, "milk"))
        assertTrue(TodoQuery.matchesSearch(target, "SKIMMED"))
        assertTrue(TodoQuery.matchesSearch(target, "   "), "blank search matches everything")
        assertFalse(TodoQuery.matchesSearch(target, "bread"))
    }

    @Test
    fun `overdue filter only keeps open tasks past their due time`() {
        val overdue = todo(1, dueAt = now - 2.hours)
        val overdueButDone = todo(2, dueAt = now - 2.hours, completed = true)
        val upcoming = todo(3, dueAt = now + 2.hours)
        val undated = todo(4)

        val result = TodoQuery.run(
            todos = listOf(overdue, overdueButDone, upcoming, undated),
            filter = TodoFilter.OVERDUE,
            now = now,
            tz = utc,
        )

        assertContentEquals(listOf(1L), result.map { it.id })
    }

    @Test
    fun `today filter includes anything due before midnight including overdue`() {
        val earlierToday = todo(1, dueAt = now - 3.hours)
        val laterToday = todo(2, dueAt = now + 3.hours)
        val tomorrow = todo(3, dueAt = now + 1.days)

        val result = TodoQuery.run(
            todos = listOf(earlierToday, laterToday, tomorrow),
            filter = TodoFilter.TODAY,
            now = now,
            tz = utc,
        )

        assertContentEquals(listOf(1L, 2L), result.map { it.id }.sorted())
    }

    @Test
    fun `category filter narrows to one list and null keeps everything`() {
        val work = todo(1, categoryId = 10L)
        val personal = todo(2, categoryId = 20L)
        val unfiled = todo(3)
        val all = listOf(work, personal, unfiled)

        assertContentEquals(
            listOf(1L),
            TodoQuery.run(all, categoryId = 10L, now = now, tz = utc).map { it.id },
        )
        assertEquals(3, TodoQuery.run(all, categoryId = null, now = now, tz = utc).size)
    }

    @Test
    fun `completed tasks always sink below open ones`() {
        val done = todo(1, dueAt = now - 5.days, completed = true)
        val open = todo(2, dueAt = now + 5.days)

        val result = TodoQuery.run(listOf(done, open), now = now, tz = utc)

        assertContentEquals(listOf(2L, 1L), result.map { it.id })
    }

    @Test
    fun `due date sort puts undated tasks last`() {
        val undated = todo(1)
        val soon = todo(2, dueAt = now + 1.hours)
        val later = todo(3, dueAt = now + 3.days)

        val result = TodoQuery.run(
            todos = listOf(undated, later, soon),
            sort = SortOrder.DUE_DATE,
            now = now,
            tz = utc,
        )

        assertContentEquals(listOf(2L, 3L, 1L), result.map { it.id })
    }

    @Test
    fun `priority sort ranks high above low`() {
        val low = todo(1, priority = Priority.LOW)
        val high = todo(2, priority = Priority.HIGH)
        val none = todo(3, priority = Priority.NONE)
        val medium = todo(4, priority = Priority.MEDIUM)

        val result = TodoQuery.run(
            todos = listOf(low, high, none, medium),
            sort = SortOrder.PRIORITY,
            now = now,
            tz = utc,
        )

        assertContentEquals(listOf(2L, 4L, 1L, 3L), result.map { it.id })
    }

    @Test
    fun `hiding completed removes them everywhere except the completed filter`() {
        val open = todo(1)
        val done = todo(2, completed = true)
        val all = listOf(open, done)

        val hidden = TodoQuery.run(all, showCompleted = false, now = now, tz = utc)
        assertContentEquals(listOf(1L), hidden.map { it.id })

        val explicitlyAsked = TodoQuery.run(
            todos = all,
            filter = TodoFilter.COMPLETED,
            showCompleted = false,
            now = now,
            tz = utc,
        )
        assertContentEquals(listOf(2L), explicitlyAsked.map { it.id })
    }

    @Test
    fun `due date sections bucket tasks by how soon they are due`() {
        val todos = listOf(
            todo(1, dueAt = now - 1.days),
            todo(2, dueAt = now + 2.hours),
            todo(3, dueAt = now + 1.days),
            todo(4, dueAt = now + 3.days),
            todo(5, dueAt = now + 30.days),
            todo(6),
            todo(7, completed = true),
        )

        val sorted = TodoQuery.run(todos, now = now, tz = utc)
        val sections = TodoQuery.sections(sorted, SortOrder.DUE_DATE, now, utc)

        assertContentEquals(
            listOf("Overdue", "Today", "Tomorrow", "This week", "Later", "No date", "Completed"),
            sections.map { it.title },
        )
        assertContentEquals(listOf(1L), sections.first { it.title == "Overdue" }.todos.map { it.id })
        assertContentEquals(
            listOf(7L),
            sections.first { it.title == "Completed" }.todos.map { it.id },
        )
    }

    @Test
    fun `empty sections are dropped rather than shown as headers with nothing under them`() {
        val sorted = TodoQuery.run(listOf(todo(1, dueAt = now + 2.hours)), now = now, tz = utc)

        val sections = TodoQuery.sections(sorted, SortOrder.DUE_DATE, now, utc)

        assertContentEquals(listOf("Today"), sections.map { it.title })
    }

    @Test
    fun `alphabetical sort ignores case`() {
        val result = TodoQuery.run(
            todos = listOf(todo(1, title = "banana"), todo(2, title = "Apple")),
            sort = SortOrder.ALPHABETICAL,
            now = now,
            tz = utc,
        )

        assertContentEquals(listOf(2L, 1L), result.map { it.id })
    }
}
