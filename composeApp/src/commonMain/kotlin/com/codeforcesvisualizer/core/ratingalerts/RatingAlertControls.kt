package com.codeforcesvisualizer.core.ratingalerts

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.codeforcesvisualizer.core.components.Chip
import com.codeforcesvisualizer.core.components.SelectableChip
import com.codeforcesvisualizer.core.components.HeightSpacer
import com.codeforcesvisualizer.core.reminders.rememberNotificationPermissionRequester
import com.codeforcesvisualizer.core.theme.CFThemeColors
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import com.codeforcesvisualizer.core.theme.CFText
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.sp

/** Turns rating change alerts for the saved [handle] on or off, asking for notification permission first. */
@Composable
fun RatingAlertToggle(
    handle: String,
    modifier: Modifier = Modifier,
) {
    val alerts = koinInject<RatingChangeAlerts>()
    val enabled by alerts.enabled.collectAsState(initial = false)
    val requestPermission = rememberNotificationPermissionRequester()
    val scope = rememberCoroutineScope()
    var notificationsBlocked by remember { mutableStateOf(false) }
    val colors = CFThemeColors.current
    val hasHandle = handle.isNotBlank()

    Column(modifier = modifier) {
        SelectableChip(
            text = "rating change alerts",
            selected = enabled && hasHandle,
            onClick = if (!hasHandle) null else {
                {
                    if (enabled) {
                        scope.launch { alerts.setEnabled(false) }
                    } else {
                        requestPermission { granted ->
                            notificationsBlocked = !granted
                            if (granted) scope.launch { alerts.setEnabled(true) }
                        }
                    }
                }
            },
        )

        val note = when {
            !hasHandle -> "Save your handle to get an alert when your rating changes."
            notificationsBlocked -> "Notifications are off for this app. Allow them in Settings to get alerts."
            enabled -> "Checks $handle's rating every few hours in the background."
            else -> null
        }
        if (note != null) {
            HeightSpacer(height = 6.dp)
            Text(
                text = note,
                style = CFText.caption.copy(color = if (notificationsBlocked) colors.amber else colors.dim),
            )
        }
    }
}
