package com.codeforcesvisualizer.core.reminders

import androidx.compose.runtime.Composable

/**
 * Returns a function that asks for permission to show notifications, if needed, and reports
 * whether they are allowed. Call it when the user turns on a reminder, not at launch.
 */
@Composable
expect fun rememberNotificationPermissionRequester(): (onResult: (granted: Boolean) -> Unit) -> Unit
