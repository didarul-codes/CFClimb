package com.codeforcesvisualizer.core.reminders

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

@Composable
actual fun rememberNotificationPermissionRequester(): (onResult: (granted: Boolean) -> Unit) -> Unit {
    val context = LocalContext.current
    var pendingResult by remember { mutableStateOf<((Boolean) -> Unit)?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        pendingResult?.invoke(granted)
        pendingResult = null
    }

    return remember(context, launcher) {
        { onResult ->
            val needsRuntimePermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            if (needsRuntimePermission) {
                pendingResult = onResult
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // Before Android 13 there is no prompt, but the user can still block notifications.
                onResult(NotificationManagerCompat.from(context).areNotificationsEnabled())
            }
        }
    }
}
