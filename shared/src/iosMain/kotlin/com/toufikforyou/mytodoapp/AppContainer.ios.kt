package com.toufikforyou.mytodoapp

import com.toufikforyou.mytodoapp.data.local.createDatabaseBuilder
import com.toufikforyou.mytodoapp.notification.IosReminderScheduler

/** Builds the iOS dependency graph. Held by [MainViewController] for the life of the process. */
fun createAppContainer(): AppContainer = AppContainer(
    databaseBuilder = createDatabaseBuilder(),
    reminderScheduler = IosReminderScheduler(),
)
