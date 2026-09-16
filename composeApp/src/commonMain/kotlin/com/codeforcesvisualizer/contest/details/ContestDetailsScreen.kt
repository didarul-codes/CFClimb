package com.codeforcesvisualizer.contest.details

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.codeforcesvisualizer.contest.list.extractDivision
import com.codeforcesvisualizer.core.EventLogger
import com.codeforcesvisualizer.core.components.CFCard
import com.codeforcesvisualizer.core.components.CalendarToast
import com.codeforcesvisualizer.core.components.CFLoadingIndicator
import com.codeforcesvisualizer.core.components.ErrorState
import com.codeforcesvisualizer.core.components.ScreenHeader
import com.codeforcesvisualizer.core.components.Chip
import com.codeforcesvisualizer.core.components.CountdownTimer
import com.codeforcesvisualizer.core.components.HeightSpacer
import com.codeforcesvisualizer.core.platform.CalendarResult
import com.codeforcesvisualizer.core.reminders.ContestReminderButton
import com.codeforcesvisualizer.core.platform.rememberCalendarLauncher
import com.codeforcesvisualizer.core.platform.toCalendarEvent
import com.codeforcesvisualizer.core.theme.CFThemeColors
import com.codeforcesvisualizer.core.utils.convertTimeStampToDateString
import com.codeforcesvisualizer.core.utils.convertToHMS
import com.codeforcesvisualizer.shared.domain.entity.Contest
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel
import com.codeforcesvisualizer.core.theme.CFText
import com.codeforcesvisualizer.core.theme.bold
import com.codeforcesvisualizer.core.theme.CFAlpha
import com.codeforcesvisualizer.core.theme.CFShapes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.sp
import com.codeforcesvisualizer.core.theme.CFSpace
import com.codeforcesvisualizer.core.components.CFButton
import com.codeforcesvisualizer.core.components.CFButtonSize
import com.codeforcesvisualizer.core.components.CFButtonStyle

@Composable
fun ContestDetailsScreen(
    modifier: Modifier = Modifier,
    contestId: Int,
    onNavigateBack: () -> Unit,
    onOpenWebSite: (Int) -> Unit,
    viewModel: ContestDetailsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val remainingTimeState by viewModel.remainingTimeFlow.collectAsState()
    val colors = CFThemeColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg),
    ) {
        ScreenHeader(
            prompt = "contests/$contestId",
            title = uiState.contest?.name ?: "contest",
            onNavigateBack = onNavigateBack,
        )

        when {
            uiState.loading -> {
                CFLoadingIndicator(modifier = Modifier.weight(1f))
            }

            uiState.userMessage.isNotBlank() -> {
                ErrorState(
                    message = uiState.userMessage,
                    onRetry = { viewModel.getContestById(contestId) },
                    modifier = Modifier.weight(1f),
                )
            }

            uiState.contest != null -> {
                ContestDetailsContent(
                    modifier = Modifier.weight(1f),
                    contest = uiState.contest!!,
                    remainingTime = remainingTimeState,
                    onOpenWebSite = onOpenWebSite,
                )
            }
        }
    }

    LaunchedEffect(contestId) {
        viewModel.getContestById(contestId)
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ContestDetailsContent(
    modifier: Modifier = Modifier,
    contest: Contest,
    remainingTime: Long,
    onOpenWebSite: (Int) -> Unit,
) {
    val colors = CFThemeColors.current
    val isUpcoming = contest.scheduled
    var calendarResult by remember { mutableStateOf<CalendarResult?>(null) }
    val launchCalendar = rememberCalendarLauncher { result -> calendarResult = result }

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = CFSpace.gutter)
                .padding(top = 8.dp),
        ) {
            // Date
            Text(
                text = contest.startTimeSeconds.convertTimeStampToDateString(),
                style = CFText.label.copy(color = colors.dim),
            )

            HeightSpacer(height = 12.dp)

            // Chips row
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                val division = extractDivision(contest.name)
                if (division != null) {
                    Chip(text = division, color = colors.blue)
                }
                Chip(text = contest.kind ?: "Codeforces", color = colors.dim, subtle = true)
                val phaseColor = when {
                    contest.scheduled -> colors.green
                    contest.phase.equals("Running", ignoreCase = true) -> colors.amber
                    else -> colors.dim
                }
                Chip(text = contest.phase.lowercase(), color = phaseColor)
            }

            HeightSpacer(height = 20.dp)

            // Countdown card for upcoming
            if (isUpcoming) {
                CFCard(title = "countdown") {
                    CountdownTimer(targetEpochSeconds = contest.startTimeSeconds.toLong())
                }
                HeightSpacer(height = 14.dp)
            }

            // Spec card
            CFCard(title = "spec") {
                SpecRow(label = "duration", value = contest.durationSeconds.convertToHMS())
                HeightSpacer(height = 8.dp)
                SpecRow(label = "type", value = contest.type)
                HeightSpacer(height = 8.dp)
                val division = extractDivision(contest.name)
                SpecRow(label = "division", value = division ?: "—")
                HeightSpacer(height = 8.dp)
                SpecRow(label = "kind", value = contest.kind ?: "Codeforces")
                HeightSpacer(height = 8.dp)
                SpecRow(label = "phase", value = contest.phase.lowercase())
                contest.preparedBy?.let { preparedBy ->
                    HeightSpacer(height = 8.dp)
                    SpecRow(label = "prepared by", value = preparedBy)
                }
                contest.country?.let { country ->
                    HeightSpacer(height = 8.dp)
                    SpecRow(label = "country", value = country)
                }
                contest.season?.let { season ->
                    HeightSpacer(height = 8.dp)
                    SpecRow(label = "season", value = season)
                }
            }

            HeightSpacer(height = 14.dp)

            // Calendar button for upcoming
            if (isUpcoming) {
                CFButton(
                    text = "add to system calendar",
                    size = CFButtonSize.Large,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        launchCalendar(contest.toCalendarEvent())
                        EventLogger.logEvent(
                            event = "Add to Calender",
                            param = mapOf("from" to "Contest Details")
                        )
                    },
                )
                HeightSpacer(height = 14.dp)
                ContestReminderButton(contest = contest)
                HeightSpacer(height = 14.dp)
            }

            // Open on website button
            CFButton(
                text = "open on codeforces.com",
                style = CFButtonStyle.Plain,
                size = CFButtonSize.Large,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onOpenWebSite(contest.id)
                    EventLogger.logEvent(
                        event = "Open Contest Website",
                        param = mapOf("ContestId" to contest.id)
                    )
                },
            )

            HeightSpacer(height = 80.dp)
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
private fun SpecRow(label: String, value: String) {
    val colors = CFThemeColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = CFText.label.copy(color = colors.dim),
        )
        Text(
            text = value,
            style = CFText.label.bold().copy(color = colors.fg),
        )
    }
}




