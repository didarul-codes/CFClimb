package com.codeforcesvisualizer.core.reminders

import androidx.compose.runtime.Composable

/** Whether reminders can arrive at the exact minute, and a way to ask the user to allow it. */
class ExactReminderAccess(val allowed: Boolean, val request: () -> Unit)

/** Returns null where the platform delivers reminders on time without extra access. */
@Composable
expect fun rememberExactReminderAccess(): ExactReminderAccess?
