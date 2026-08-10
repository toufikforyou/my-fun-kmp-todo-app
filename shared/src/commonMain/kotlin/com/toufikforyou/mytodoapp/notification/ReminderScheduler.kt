package com.toufikforyou.mytodoapp.notification

import com.toufikforyou.mytodoapp.domain.model.Todo

/**
 * Schedules the local notification that fires at a task's reminder time.
 *
 * Implementations are platform specific (AlarmManager on Android, UNUserNotificationCenter on
 * iOS) and are handed to [com.toufikforyou.mytodoapp.AppContainer] by each entry point.
 */
interface ReminderScheduler {

    /** True once the OS has granted permission to post notifications. */
    suspend fun hasPermission(): Boolean

    /** Prompts the user if needed. Returns whether notifications may now be posted. */
    suspend fun requestPermission(): Boolean

    /**
     * Registers (or re-registers) the reminder for [todo]. A task with no reminder, a reminder in
     * the past, or one that is already done has its pending notification cancelled instead.
     */
    suspend fun schedule(todo: Todo)

    suspend fun cancel(todoId: Long)
}

/** Used in previews and tests, where no OS scheduling should happen. */
object NoOpReminderScheduler : ReminderScheduler {
    override suspend fun hasPermission(): Boolean = false
    override suspend fun requestPermission(): Boolean = false
    override suspend fun schedule(todo: Todo) = Unit
    override suspend fun cancel(todoId: Long) = Unit
}
