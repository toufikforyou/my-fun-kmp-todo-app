package com.toufikforyou.mytodoapp.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.toufikforyou.mytodoapp.buildDatabase
import com.toufikforyou.mytodoapp.core.ioDispatcher
import com.toufikforyou.mytodoapp.data.local.TodoDatabase
import com.toufikforyou.mytodoapp.data.local.createDatabaseBuilder
import com.toufikforyou.mytodoapp.data.repository.TodoRepositoryImpl
import com.toufikforyou.mytodoapp.shared.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Posts the notification when an alarm set by [AndroidReminderScheduler] fires. */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val todoId = intent.getLongExtra(EXTRA_TODO_ID, -1L)
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        if (todoId < 0L || title.isBlank()) return
        val notes = intent.getStringExtra(EXTRA_NOTES).orEmpty()
        val body = notes.ifBlank { "Your task is due." }

        NotificationChannels.ensureCreated(context)

        val openApp = context.packageManager
            .getLaunchIntentForPackage(context.packageName)
            ?.let { launchIntent ->
                PendingIntent.getActivity(
                    context,
                    todoId.toRequestCode(),
                    launchIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            }

        val markDone = PendingIntent.getBroadcast(
            context,
            todoId.toRequestCode(),
            Intent(context, MarkDoneReceiver::class.java).putExtra(EXTRA_TODO_ID, todoId),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, NotificationChannels.REMINDERS)
            .setSmallIcon(R.drawable.ic_reminder)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .apply { openApp?.let(::setContentIntent) }
            .addAction(R.drawable.ic_reminder, "Mark done", markDone)
            .build()

        // Posting without POST_NOTIFICATIONS is a silent no-op on Android 13+, not a crash.
        context.getSystemService<NotificationManager>()
            ?.notify(todoId.toRequestCode(), notification)
    }

    companion object {
        const val EXTRA_TODO_ID = "extra_todo_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_NOTES = "extra_notes"
    }
}

/** Handles the notification's "Mark done" action without opening the app. */
class MarkDoneReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val todoId = intent.getLongExtra(ReminderReceiver.EXTRA_TODO_ID, -1L)
        if (todoId < 0L) return
        context.applicationContext.getSystemService<NotificationManager>()
            ?.cancel(todoId.toRequestCode())

        withDatabase(context, this) { database ->
            TodoRepositoryImpl(database.todoDao()).setCompleted(todoId, completed = true)
        }
    }
}

/** Alarms do not survive a reboot, so pending reminders are re-registered when the device starts. */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            return
        }
        val appContext = context.applicationContext
        withDatabase(context, this) { database ->
            val scheduler = AndroidReminderScheduler(appContext)
            TodoRepositoryImpl(database.todoDao()).observeTodos().first()
                .filter { it.isReminderPending() }
                .forEach { scheduler.schedule(it) }
        }
    }
}

/**
 * Runs [block] against a short-lived database connection while keeping the broadcast alive.
 * Receivers have no scope of their own, so [BroadcastReceiver.goAsync] is what stops the process
 * from being torn down mid-write.
 */
private fun withDatabase(
    context: Context,
    receiver: BroadcastReceiver,
    block: suspend (TodoDatabase) -> Unit,
) {
    val appContext = context.applicationContext
    val pending = receiver.goAsync()
    CoroutineScope(SupervisorJob() + ioDispatcher).launch {
        var database: TodoDatabase? = null
        try {
            database = buildDatabase(createDatabaseBuilder(appContext))
            block(database)
        } finally {
            database?.close()
            pending.finish()
        }
    }
}
