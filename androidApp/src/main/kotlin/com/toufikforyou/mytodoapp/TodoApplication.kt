package com.toufikforyou.mytodoapp

import android.app.Application

/**
 * Owns the dependency graph. Living on the Application rather than the Activity means the Room
 * connection is opened once per process instead of once per rotation.
 */
class TodoApplication : Application() {

    lateinit var container: AppContainer
        private set

    /**
     * Installed by the foreground Activity, since only an Activity can show the notification
     * permission dialog. Null whenever no Activity is resumed.
     */
    var notificationPermissionPrompt: (suspend () -> Boolean)? = null

    override fun onCreate() {
        super.onCreate()
        container = createAppContainer(
            context = this,
            permissionPrompt = { notificationPermissionPrompt?.invoke() ?: false },
        )
    }
}
