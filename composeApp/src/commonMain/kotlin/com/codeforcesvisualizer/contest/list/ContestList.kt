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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import com.codeforcesvisualizer.core.components.CalendarToast
import kotlinx.coroutines.delay
import com.codeforcesvisualizer.core.components.Chip
import com.codeforcesvisualizer.core.components.CountdownTimer
import com.codeforcesvisualizer.core.components.HeightSpacer
import com.codeforcesvisualizer.core.platform.CalendarResult
import com.codeforcesvisualizer.core.platform.rememberCalendarLauncher
import com.codeforcesvisualizer.core.platform.toCalendarEvent
import com.codeforcesvisualizer.core.theme.CFThemeColors
import com.codeforcesvisualizer.core.utils.convertTimeStampToDateString
import com.codeforcesvisualizer.core.utils.convertToHMS
import com.codeforcesvisualizer.shared.domain.entity.Contest

private enum class ContestTab { UPCOMING, PAST }

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ContestList(
    modifier: Modifier = Modifier,
    contestList: List<Contest>,
    username: String,
    openContestDetails: (Int) -> Unit,
) {
    val colors = CFThemeColors.current
    var selectedTab by remember { mutableStateOf(ContestTab.UPCOMING) }
    var calendarResult by remember { mutableStateOf<CalendarResult?>(null) }
    val launchCalendar = rememberCalendarLauncher { result -> calendarResult = result }

    val upcoming = remember(contestList) {
        contestList.filter { it.scheduled }.sortedBy { it.startTimeSeconds }
    }
    val past = remember(contestList) {
        contestList.filter { !it.scheduled }
    }

    val state = rememberLazyListState()

    Box(modifier = modifier) {
        LazyColumn(modifier = Modifier.fillMaxSize(), state = state) {
            // Tab bar
            item {
                TabBar(
                    selectedTab = selectedTab,
                    upcomingCount = upcoming.size,
                    pastCount = past.size,
                    onTabSelected = { selectedTab = it },
                )
            }

            // Streak banner (shown when username is set)
            if (username.isNotBlank()) {
                item {
                    StreakBanner(username = username)
                }
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
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        contest = contest,
                        isUpcoming = true,
                        onOpenContest = openContestDetails,
                    )
                }
            }

            if (selectedTab == ContestTab.PAST) {
                items(past, key = { it.id }) { contest ->
                    ContestListItem(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
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

@Composable
private fun TabBar(
    selectedTab: ContestTab,
    upcomingCount: Int,
    pastCount: Int,
    onTabSelected: (ContestTab) -> Unit,
) {
    val colors = CFThemeColors.current
    val containerShape = RoundedCornerShape(10.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(containerShape)
            .background(colors.surface)
            .border(1.dp, colors.border, containerShape)
            .padding(3.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        TabButton(
            text = "$ upcoming ($upcomingCount)",
            isSelected = selectedTab == ContestTab.UPCOMING,
            onClick = { onTabSelected(ContestTab.UPCOMING) },
            modifier = Modifier.weight(1f),
        )
        TabButton(
            text = "$ past ($pastCount)",
            isSelected = selectedTab == ContestTab.PAST,
            onClick = { onTabSelected(ContestTab.PAST) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CFThemeColors.current
    val shape = RoundedCornerShape(8.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .then(
                if (isSelected) Modifier.background(colors.surface2)
                else Modifier
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) colors.fg else colors.dim,
            ),
        )
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
    val shape = RoundedCornerShape(12.dp)
    val gradient = Brush.linearGradient(
        colors = listOf(colors.violet.copy(alpha = 0.12f), colors.surface),
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
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
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.5.sp,
                    color = colors.dim,
                ),
            )
        }

        HeightSpacer(height = 12.dp)

        // Contest name
        Text(
            text = contest.name,
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = colors.fg,
            ),
        )

        HeightSpacer(height = 4.dp)

        // Duration info
        Text(
            text = "duration: ${contest.durationSeconds.convertToHMS()}",
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = colors.dim,
            ),
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
            ActionButton(
                text = "open",
                color = colors.violet,
                modifier = Modifier.weight(1f),
                onClick = { onOpenContest(contest.id) },
            )
            ActionButton(
                text = "calendar",
                color = colors.amber,
                modifier = Modifier.weight(1f),
                onClick = onAddToCalendar,
            )
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.3f), shape)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "$ $text",
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color,
            ),
        )
    }
}

@Composable
private fun StreakBanner(username: String) {
    val colors = CFThemeColors.current
    val shape = RoundedCornerShape(10.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(shape)
            .background(colors.amber.copy(alpha = 0.08f))
            .border(1.dp, colors.amber.copy(alpha = 0.2f), shape)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "🔥",
            fontSize = 16.sp,
        )
        Text(
            text = "  @$username — competitive mode",
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = colors.amber,
            ),
        )
    }
}
