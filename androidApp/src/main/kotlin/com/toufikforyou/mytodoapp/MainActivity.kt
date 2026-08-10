package com.toufikforyou.mytodoapp

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.CompletableDeferred

class MainActivity : ComponentActivity() {

    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    /** Completed by the permission callback; lets the shared code `await` the user's answer. */
    private var pendingPermission: CompletableDeferred<Boolean>? = null

    private val app: TodoApplication get() = application as TodoApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { granted ->
            pendingPermission?.complete(granted)
            pendingPermission = null
        }
        app.notificationPermissionPrompt = ::requestNotificationPermission

        setContent {
            App(app.container)
        }
    }

    override fun onDestroy() {
        // Do not leave the Application holding a reference to a dead Activity.
        if (app.notificationPermissionPrompt == ::requestNotificationPermission) {
            app.notificationPermissionPrompt = null
        }
        pendingPermission?.complete(false)
        pendingPermission = null
        super.onDestroy()
    }

    private suspend fun requestNotificationPermission(): Boolean {
        // POST_NOTIFICATIONS did not exist before Android 13; nothing to ask for.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        pendingPermission?.complete(false)
        val answer = CompletableDeferred<Boolean>()
        pendingPermission = answer
        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        return answer.await()
    }
}
