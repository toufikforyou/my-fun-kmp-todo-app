package com.toufikforyou.mytodoapp

import com.toufikforyou.mytodoapp.domain.model.Todo
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

class TodoTest {

    private val now = Instant.parse("2026-08-10T12:00:00Z")

    private fun todo(
        dueAt: Instant? = null,
        reminderAt: Instant? = null,
        completed: Boolean = false,
    ) = Todo(
        id = 1L,
        title = "Task",
        isCompleted = completed,
        dueAt = dueAt,
        reminderAt = reminderAt,
        createdAt = now,
        updatedAt = now,
    )

    @Test
    fun `a task is overdue only while it is open and past due`() {
        assertTrue(todo(dueAt = now - 1.hours).isOverdue(now))
        assertFalse(todo(dueAt = now + 1.hours).isOverdue(now))
        assertFalse(todo(dueAt = now - 1.hours, completed = true).isOverdue(now))
        assertFalse(todo(dueAt = null).isOverdue(now))
    }

    @Test
    fun `only a future reminder on an open task is worth scheduling`() {
        assertTrue(todo(reminderAt = now + 1.hours).isReminderPending(now))
        assertFalse(todo(reminderAt = now - 1.hours).isReminderPending(now))
        assertFalse(todo(reminderAt = now + 1.hours, completed = true).isReminderPending(now))
        assertFalse(todo(reminderAt = null).isReminderPending(now))
    }
}
