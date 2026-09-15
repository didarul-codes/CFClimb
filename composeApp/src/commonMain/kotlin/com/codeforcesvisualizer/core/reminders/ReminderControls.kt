package com.codeforcesvisualizer.core.reminders

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codeforcesvisualizer.core.components.CFCard
import com.codeforcesvisualizer.core.components.Chip
import com.codeforcesvisualizer.core.components.HeightSpacer
import com.codeforcesvisualizer.core.theme.CFThemeColors
import com.codeforcesvisualizer.shared.domain.entity.Contest
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/** Turns a reminder for an upcoming [contest] on or off, asking for notification permission first. */
@Composable
fun ContestReminderButton(
    contest: Contest,
    modifier: Modifier = Modifier,
) {
    val reminders = koinInject<ContestReminders>()
    val remindedIds by reminders.remindedContestIds.collectAsState(initial = emptySet())
    val leadTimes by reminders.leadTimes.collectAsState(initial = ReminderLeadTime.Default)
    val requestPermission = rememberNotificationPermissionRequester()
    val scope = rememberCoroutineScope()
    var notificationsBlocked by remember { mutableStateOf(false) }

    val colors = CFThemeColors.current
    val isOn = contest.id in remindedIds
    val accent = if (isOn) colors.green else colors.blue
    val shape = RoundedCornerShape(10.dp)

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(accent.copy(alpha = 0.15f))
                .border(1.dp, accent.copy(alpha = 0.3f), shape)
                .clickable(role = Role.Switch) {
                    if (isOn) {
                        scope.launch { reminders.setReminder(contest.id, enabled = false) }
                    } else {
                        requestPermission { granted ->
                            notificationsBlocked = !granted
                            if (granted) scope.launch { reminders.setReminder(contest.id, enabled = true) }
                        }
                    }
                }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (isOn) "$ reminder on" else "$ remind me",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                ),
            )
        }

        val note = when {
            notificationsBlocked -> "Notifications are off for this app. Allow them in Settings to get reminders."
            isOn && leadTimes.isEmpty() -> "No reminder times selected. Choose them in Settings."
            isOn -> "You'll be notified " + ReminderLeadTime.entries
                .filter { it in leadTimes }
                .joinToString(" and ") { it.label } + " before it starts."
            else -> null
        }
        if (note != null) {
            HeightSpacer(height = 6.dp)
            Text(
                text = note,
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = if (notificationsBlocked || leadTimes.isEmpty()) colors.amber else colors.dim,
                ),
            )
        }
    }
}

/** Lets the user choose how long before a contest reminders arrive. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReminderSettingsCard(modifier: Modifier = Modifier) {
    val reminders = koinInject<ContestReminders>()
    val leadTimes by reminders.leadTimes.collectAsState(initial = ReminderLeadTime.Default)
    val remindedIds by reminders.remindedContestIds.collectAsState(initial = emptySet())
    val scope = rememberCoroutineScope()
    val colors = CFThemeColors.current

    CFCard(
        modifier = modifier,
        title = "reminders",
        titleRight = {
            Text(
                text = if (remindedIds.size == 1) "1 contest" else "${remindedIds.size} contests",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = colors.dim,
                ),
            )
        },
    ) {
        Text(
            text = "notify me before a contest",
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = colors.dim,
            ),
        )
        HeightSpacer(height = 8.dp)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ReminderLeadTime.entries.forEach { leadTime ->
                val selected = leadTime in leadTimes
                Chip(
                    text = if (selected) "✓ ${leadTime.label}" else leadTime.label,
                    color = if (selected) colors.violet else colors.dim,
                    subtle = !selected,
                    onClick = { scope.launch { reminders.setLeadTime(leadTime, enabled = !selected) } },
                )
            }
        }
        if (leadTimes.isEmpty()) {
            HeightSpacer(height = 8.dp)
            Text(
                text = "Pick at least one time to get reminders.",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = colors.amber,
                ),
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        val exactAccess = rememberExactReminderAccess()
        if (exactAccess != null) {
            HeightSpacer(height = 12.dp)
            if (exactAccess.allowed) {
                Text(
                    text = "Exact timing is on.",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = colors.dim,
                    ),
                )
            } else {
                Chip(
                    text = "allow exact timing",
                    color = colors.blue,
                    onClick = exactAccess.request,
                )
                HeightSpacer(height = 6.dp)
                Text(
                    text = "Without it, reminders can arrive up to 10 min early, or late while the phone is idle.",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = colors.dim,
                    ),
                )
            }
        }
    }
}
