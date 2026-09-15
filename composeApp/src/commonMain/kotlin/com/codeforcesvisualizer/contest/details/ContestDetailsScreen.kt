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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codeforcesvisualizer.contest.list.extractDivision
import com.codeforcesvisualizer.core.EventLogger
import com.codeforcesvisualizer.core.components.CFCard
import com.codeforcesvisualizer.core.components.CalendarToast
import com.codeforcesvisualizer.core.components.CFLoadingIndicator
import com.codeforcesvisualizer.core.components.Center
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
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

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
        // Back button / breadcrumb
        BackNavigation(
            contestId = contestId,
            onNavigateBack = onNavigateBack,
        )

        when {
            uiState.loading -> {
                CFLoadingIndicator(modifier = Modifier.weight(1f))
            }

            uiState.userMessage.isNotBlank() -> {
                Center(modifier = Modifier.weight(1f)) {
                    Text(
                        text = uiState.userMessage,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = colors.dim,
                        ),
                    )
                }
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

@Composable
private fun BackNavigation(
    contestId: Int,
    onNavigateBack: () -> Unit,
) {
    val colors = CFThemeColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 18.dp)
            .padding(top = 12.dp, bottom = 6.dp),
    ) {
        // Back button
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = colors.violet)) {
                    append("← ")
                }
                withStyle(SpanStyle(color = colors.fg)) {
                    append("cd ..")
                }
            },
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            ),
            modifier = Modifier
                .clickable { onNavigateBack() }
                .padding(vertical = 4.dp),
        )

        HeightSpacer(height = 4.dp)

        // Breadcrumb
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = colors.violet)) {
                    append("cf://")
                }
                withStyle(SpanStyle(color = colors.dim)) {
                    append("contests/$contestId")
                }
            },
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
            ),
        )
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
                .padding(horizontal = 18.dp)
                .padding(top = 8.dp),
        ) {
            // Contest name as h1
            Text(
                text = contest.name,
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = colors.fg,
                ),
            )

            HeightSpacer(height = 6.dp)

            // Date
            Text(
                text = contest.startTimeSeconds.convertTimeStampToDateString(),
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = colors.dim,
                ),
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
                CalendarButton(
                    onClick = {
                        launchCalendar(contest.toCalendarEvent())
                        EventLogger.logEvent(
                            event = "Add to Calender",
                            param = mapOf("from" to "Contest Details")
                        )
                    },
                )
                HeightSpacer(height = 14.dp)
            }

            // Open on website button
            OpenWebsiteButton(
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
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = colors.dim,
            ),
        )
        Text(
            text = value,
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = colors.fg,
            ),
        )
    }
}

@Composable
private fun CalendarButton(onClick: () -> Unit) {
    val colors = CFThemeColors.current
    val shape = RoundedCornerShape(10.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.violet.copy(alpha = 0.15f))
            .border(1.dp, colors.violet.copy(alpha = 0.3f), shape)
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "$ add to system calendar",
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = colors.violet,
            ),
        )
    }
}

@Composable
private fun OpenWebsiteButton(onClick: () -> Unit) {
    val colors = CFThemeColors.current
    val shape = RoundedCornerShape(10.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surface)
            .border(1.dp, colors.border, shape)
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "$ open on codeforces.com",
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = colors.fg,
            ),
        )
    }
}


