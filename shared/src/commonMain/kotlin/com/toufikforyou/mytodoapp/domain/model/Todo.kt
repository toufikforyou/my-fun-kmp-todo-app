package com.toufikforyou.mytodoapp.domain.model

import kotlin.time.Clock
import kotlin.time.Instant

/**
 * A single task. [id] is 0 for a task that has not been persisted yet; Room assigns the
 * real id on insert.
 */
data class Todo(
    val id: Long = 0L,
    val title: String,
    val notes: String = "",
    val isCompleted: Boolean = false,
    val priority: Priority = Priority.NONE,
    val dueAt: Instant? = null,
    val reminderAt: Instant? = null,
    val categoryId: Long? = null,
    val createdAt: Instant = Clock.System.now(),
    val updatedAt: Instant = Clock.System.now(),
    val completedAt: Instant? = null,
) {
    val hasReminder: Boolean get() = reminderAt != null

    /** A reminder only matters while the task is open and the fire time is still ahead of us. */
    fun isReminderPending(now: Instant = Clock.System.now()): Boolean =
        !isCompleted && reminderAt != null && reminderAt > now

    fun isOverdue(now: Instant = Clock.System.now()): Boolean =
        !isCompleted && dueAt != null && dueAt < now
}
