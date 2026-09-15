package com.codeforcesvisualizer.core.reminders

import androidx.compose.runtime.Composable

// Local notifications on iOS are delivered at the scheduled time.
@Composable
actual fun rememberExactReminderAccess(): ExactReminderAccess? = null
