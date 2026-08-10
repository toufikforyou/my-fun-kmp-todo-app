package com.toufikforyou.mytodoapp

import android.content.Context
import com.toufikforyou.mytodoapp.data.local.createDatabaseBuilder
import com.toufikforyou.mytodoapp.notification.AndroidReminderScheduler

/**
 * Builds the Android dependency graph. Call this once — from `Application.onCreate` — so the Room
 * instance survives configuration changes.
 *
 * @param permissionPrompt shows the POST_NOTIFICATIONS dialog and returns the user's answer. Only
 *   an Activity can do this, so it is injected rather than reached for.
 */
fun createAppContainer(
    context: Context,
    permissionPrompt: suspend () -> Boolean = { false },
): AppContainer = AppContainer(
    databaseBuilder = createDatabaseBuilder(context),
    reminderScheduler = AndroidReminderScheduler(context, permissionPrompt),
)
