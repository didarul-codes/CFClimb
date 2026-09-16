package com.codeforcesvisualizer.contest.list

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import com.codeforcesvisualizer.climb.MyClimbCard
import com.codeforcesvisualizer.core.components.CalendarToast
import kotlinx.coroutines.delay
import com.codeforcesvisualizer.core.components.Chip
import com.codeforcesvisualizer.core.components.SegmentedTabs
import com.codeforcesvisualizer.core.components.CountdownTimer
import com.codeforcesvisualizer.core.components.HeightSpacer
import com.codeforcesvisualizer.core.platform.CalendarResult
import com.codeforcesvisualizer.core.platform.rememberCalendarLauncher
import com.codeforcesvisualizer.core.platform.toCalendarEvent
import com.codeforcesvisualizer.core.theme.CFThemeColors
import com.codeforcesvisualizer.core.utils.convertTimeStampToDateString
import com.codeforcesvisualizer.core.utils.convertToHMS
import com.codeforcesvisualizer.shared.domain.entity.Contest
import com.codeforcesvisualizer.core.theme.CFText
import com.codeforcesvisualizer.core.theme.bold
import com.codeforcesvisualizer.core.theme.CFAlpha
import com.codeforcesvisualizer.core.theme.CFShapes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.sp
import com.codeforcesvisualizer.core.theme.CFSpace
import com.codeforcesvisualizer.core.components.CFButton

private enum class ContestTab { UPCOMING, PAST }

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ContestList(
    modifier: Modifier = Modifier,
    groups: ContestGroups,
    nowEpochSeconds: Long,
    openContestDetails: (Int) -> Unit,
    openProfile: (String) -> Unit,
    openSettings: () -> Unit,
    openUpsolve: () -> Unit,
) {
    val colors = CFThemeColors.current
    var selectedTab by remember { mutableStateOf(ContestTab.UPCOMING) }
    var calendarResult by remember { mutableStateOf<CalendarResult?>(null) }
    val launchCalendar = rememberCalendarLauncher { result -> calendarResult = result }

    val live = groups.live
    val upcoming = groups.upcoming
    val past = groups.past

    val state = rememberLazyListState()

    Box(modifier = modifier) {
        LazyColumn(modifier = Modifier.fillMaxSize(), state = state) {
            // Tab bar
            item {
                SegmentedTabs(
                    labels = listOf(
                        "upcoming (${live.size + upcoming.size})",
                        "past (${past.size})",
                    ),
                    selectedIndex = if (selectedTab == ContestTab.UPCOMING) 0 else 1,
                    onSelect = { index ->
                        selectedTab = if (index == 0) ContestTab.UPCOMING else ContestTab.PAST
                    },
                    modifier = Modifier.padding(horizontal = CFSpace.gutter, vertical = 8.dp),
                )
            }

            // The saved handle's rating, tier progress and week.
            item {
                MyClimbCard(
                    onOpenProfile = openProfile,
                    onOpenSettings = openSettings,
                    onOpenUpsolve = openUpsolve,
                    modifier = Modifier.padding(horizontal = CFSpace.gutter, vertical = 6.dp),
                )
            }

            // Running rounds are pinned above everything else in the upcoming tab.
            if (selectedTab == ContestTab.UPCOMING && live.isNotEmpty()) {
                item { SectionLabel(text = "live now") }
                items(live, key = { it.id }) { contest ->
                    ContestListItem(
                        modifier = Modifier.padding(horizontal = CFSpace.gutter, vertical = 4.dp),
                        contest = contest,
                        isUpcoming = false,
                        onOpenContest = openContestDetails,
                        liveLabel = liveStatusLabel(contest, nowEpochSeconds),
                    )
                }
                if (upcoming.isNotEmpty()) {
                    item { SectionLabel(text = "next up") }
                }
            }

            if (selectedTab == ContestTab.UPCOMING && live.isEmpty() && upcoming.isEmpty()) {
                item { SectionLabel(text = "No upcoming rounds saved. Pull down to refresh.") }
            }

            // Hero card for first upcoming contest
            if (selectedTab == ContestTab.UPCOMING && upcoming.isNotEmpty()) {
                item {
                    HeroContestCard(
                        contest = upcoming.first(),
                        onOpenContest = { openContestDetails(it) },
                        onAddToCalendar = {
                            launchCalendar(upcoming.first().toCalendarEvent())
                        },
                    )
                }

                // Remaining upcoming items (skip first since it's the hero)
                val remaining = upcoming.drop(1)
                items(remaining, key = { it.id }) { contest ->
                    ContestListItem(
                        modifier = Modifier.padding(horizontal = CFSpace.gutter, vertical = 4.dp),
                        contest = contest,
                        isUpcoming = true,
                        onOpenContest = openContestDetails,
                    )
                }
            }

            if (selectedTab == ContestTab.PAST) {
                items(past, key = { it.id }) { contest ->
                    ContestListItem(
                        modifier = Modifier.padding(horizontal = CFSpace.gutter, vertical = 4.dp),
                        contest = contest,
                        isUpcoming = false,
                        onOpenContest = openContestDetails,
                    )
                }
            }

            // Bottom spacing
            item {
                HeightSpacer(height = 80.dp)
            }
        }

        // Calendar result overlay
        calendarResult?.let { result ->
            CalendarToast(
                result = result,
                onDismiss = { calendarResult = null },
            )

            LaunchedEffect(result) {
                delay(2500)
                calendarResult = null
            }
        }
    }
}



@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HeroContestCard(
    contest: Contest,
    onOpenContest: (Int) -> Unit,
    onAddToCalendar: () -> Unit,
) {
    val colors = CFThemeColors.current
    val shape = CFShapes.card
    val gradient = Brush.linearGradient(
        colors = listOf(colors.violet.copy(alpha = CFAlpha.WASH), colors.surface),
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CFSpace.gutter, vertical = 8.dp)
            .clip(shape)
            .background(gradient)
            .border(1.dp, colors.border, shape)
            .padding(16.dp),
    ) {
        // Top row: chip + date
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Chip(text = "next.up", color = colors.green)

            Text(
                text = contest.startTimeSeconds.convertTimeStampToDateString(),
                style = CFText.micro.copy(color = colors.dim),
            )
        }

        HeightSpacer(height = 12.dp)

        // Contest name
        Text(
            text = contest.name,
            style = CFText.heading.copy(color = colors.fg),
        )

        HeightSpacer(height = 4.dp)

        // Duration info
        Text(
            text = "duration: ${contest.durationSeconds.convertToHMS()}",
            style = CFText.caption.copy(color = colors.dim),
        )

        HeightSpacer(height = 16.dp)

        // Countdown timer
        CountdownTimer(targetEpochSeconds = contest.startTimeSeconds.toLong())

        HeightSpacer(height = 16.dp)

        // Chips
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            val division = extractDivision(contest.name)
            if (division != null) {
                Chip(text = division, color = colors.blue)
            }
            Chip(text = contest.kind ?: "Codeforces", color = colors.dim, subtle = true)
        }

        HeightSpacer(height = 14.dp)

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CFButton(
                text = "open",
                color = colors.violet,
                modifier = Modifier.weight(1f),
                onClick = { onOpenContest(contest.id) },
            )
            CFButton(
                text = "calendar",
                color = colors.amber,
                modifier = Modifier.weight(1f),
                onClick = onAddToCalendar,
            )
        }
    }
}


@Composable
private fun SectionLabel(text: String) {
    val colors = CFThemeColors.current
    Text(
        text = "// $text",
        style = CFText.caption.copy(color = colors.dim),
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 2.dp),
    )
}

