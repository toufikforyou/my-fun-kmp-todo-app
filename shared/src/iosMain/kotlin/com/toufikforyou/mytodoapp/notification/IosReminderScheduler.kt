package com.toufikforyou.mytodoapp.notification

import com.toufikforyou.mytodoapp.domain.model.Todo
import platform.Foundation.NSError
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Clock

class IosReminderScheduler : ReminderScheduler {

    private val center: UNUserNotificationCenter
        get() = UNUserNotificationCenter.currentNotificationCenter()

    override suspend fun hasPermission(): Boolean = suspendCoroutine { continuation ->
        center.getNotificationSettingsWithCompletionHandler { settings ->
            val status = settings?.authorizationStatus
            continuation.resume(
                status == UNAuthorizationStatusAuthorized ||
                    status == UNAuthorizationStatusProvisional,
            )
        }
    }

    override suspend fun requestPermission(): Boolean = suspendCoroutine { continuation ->
        val options = UNAuthorizationOptionAlert or
            UNAuthorizationOptionSound or
            UNAuthorizationOptionBadge
        center.requestAuthorizationWithOptions(options) { granted: Boolean, _: NSError? ->
            continuation.resume(granted)
        }
    }

    override suspend fun schedule(todo: Todo) {
        cancel(todo.id)
        val fireAt = todo.reminderAt ?: return
        if (todo.isCompleted) return

        val secondsFromNow = (fireAt - Clock.System.now()).inWholeSeconds.toDouble()
        // UNTimeIntervalNotificationTrigger rejects intervals of zero or less.
        if (secondsFromNow < 1.0) return

        val content = UNMutableNotificationContent().apply {
            setTitle(todo.title)
            setBody(todo.notes.ifBlank { "Your task is due." })
            setSound(UNNotificationSound.defaultSound())
        }
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = identifierFor(todo.id),
            content = content,
            trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
                timeInterval = secondsFromNow,
                repeats = false,
            ),
        )
        center.addNotificationRequest(request, withCompletionHandler = null)
    }

    override suspend fun cancel(todoId: Long) {
        val identifiers = listOf(identifierFor(todoId))
        center.removePendingNotificationRequestsWithIdentifiers(identifiers)
        center.removeDeliveredNotificationsWithIdentifiers(identifiers)
    }

    private fun identifierFor(todoId: Long) = "todo-reminder-$todoId"
}
