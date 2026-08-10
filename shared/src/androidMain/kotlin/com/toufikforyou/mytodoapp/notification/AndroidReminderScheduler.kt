package com.toufikforyou.mytodoapp.notification

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.toufikforyou.mytodoapp.domain.model.Todo

/**
 * Backs reminders with [AlarmManager]. The alarm fires a broadcast to [ReminderReceiver], which is
 * what actually posts the notification — that way a reminder still arrives if the app has been
 * swiped away.
 *
 * @param permissionPrompt supplied by the Activity, the only component that can show the runtime
 *   permission dialog. Defaults to "declined" for non-UI callers such as the boot receiver.
 */
class AndroidReminderScheduler(
    context: Context,
    private val permissionPrompt: suspend () -> Boolean = { false },
) : ReminderScheduler {

    private val appContext = context.applicationContext

    private val alarmManager: AlarmManager?
        get() = appContext.getSystemService()

    override suspend fun hasPermission(): Boolean {
        // POST_NOTIFICATIONS only exists on Android 13+; before that it is granted implicitly.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    override suspend fun requestPermission(): Boolean =
        if (hasPermission()) true else permissionPrompt()

    override suspend fun schedule(todo: Todo) {
        cancel(todo.id)
        if (!todo.isReminderPending()) return
        val fireAt = todo.reminderAt?.toEpochMilliseconds() ?: return
        val manager = alarmManager ?: return
        val pendingIntent = alarmIntent(todo)

        NotificationChannels.ensureCreated(appContext)
        // Exact alarms need a separate user grant on Android 12+; fall back to an inexact alarm
        // rather than crashing or sending the user to a settings screen.
        val canBeExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            manager.canScheduleExactAlarms()
        if (canBeExact) {
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fireAt, pendingIntent)
        } else {
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fireAt, pendingIntent)
        }
    }

    override suspend fun cancel(todoId: Long) {
        val existing = PendingIntent.getBroadcast(
            appContext,
            todoId.toRequestCode(),
            Intent(appContext, ReminderReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        )
        if (existing != null) {
            alarmManager?.cancel(existing)
            existing.cancel()
        }
        // Also clear the notification if it has already been shown.
        appContext.getSystemService<NotificationManager>()?.cancel(todoId.toRequestCode())
    }

    private fun alarmIntent(todo: Todo): PendingIntent {
        val intent = Intent(appContext, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_TODO_ID, todo.id)
            putExtra(ReminderReceiver.EXTRA_TITLE, todo.title)
            putExtra(ReminderReceiver.EXTRA_NOTES, todo.notes)
        }
        return PendingIntent.getBroadcast(
            appContext,
            todo.id.toRequestCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}

/** Alarm request codes are Ints, so ids are folded into the positive Int range. */
internal fun Long.toRequestCode(): Int = (this % Int.MAX_VALUE).toInt()

internal object NotificationChannels {
    const val REMINDERS = "task_reminders"

    fun ensureCreated(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService<NotificationManager>() ?: return
        if (manager.getNotificationChannel(REMINDERS) != null) return
        manager.createNotificationChannel(
            NotificationChannel(
                REMINDERS,
                "Task reminders",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Notifies you when a task you set a reminder for is due."
            },
        )
    }
}
