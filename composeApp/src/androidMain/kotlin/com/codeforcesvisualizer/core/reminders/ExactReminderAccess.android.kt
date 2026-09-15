package com.codeforcesvisualizer.core.reminders

import android.app.AlarmManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect

@Composable
actual fun rememberExactReminderAccess(): ExactReminderAccess? {
    // Before Android 12 exact alarms need no permission.
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null

    val context = LocalContext.current
    val alarmManager = remember(context) { context.getSystemService(AlarmManager::class.java) }
    var allowed by remember { mutableStateOf(alarmManager.canScheduleExactAlarms()) }

    // The user decides on a system settings screen, so check again on return.
    LifecycleResumeEffect(alarmManager) {
        allowed = alarmManager.canScheduleExactAlarms()
        onPauseOrDispose { }
    }

    return ExactReminderAccess(allowed) {
        context.startActivity(
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}"))
        )
    }
}
