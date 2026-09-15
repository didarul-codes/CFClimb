package com.codeforcesvisualizer.contest.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.codeforcesvisualizer.core.EventLogger
import com.codeforcesvisualizer.core.components.CFLoadingIndicator
import com.codeforcesvisualizer.core.components.ErrorState
import com.codeforcesvisualizer.core.components.OfflineBanner
import com.codeforcesvisualizer.core.components.ScreenHeader
import com.codeforcesvisualizer.core.data.UserSettingsRepository
import com.codeforcesvisualizer.core.theme.CFThemeColors
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ContestListScreen(
    modifier: Modifier = Modifier,
    viewModel: ContestViewModel = koinViewModel(),
    openSearch: () -> Unit = {},
    openContestDetails: (Int) -> Unit = {},
    onOpenWebSite: (Int) -> Unit = {},
) {
    val uiState = viewModel.uiState.collectAsState()
    val userSettingsRepository = koinInject<UserSettingsRepository>()
    val username by userSettingsRepository.username.collectAsState(initial = "")
    ContestListScreenContent(
        modifier = modifier,
        state = uiState,
        username = username,
        onRefresh = viewModel::refreshContestList,
        openContestDetails = { contestId ->
            openContestDetails(contestId)
            EventLogger.logScreenView(
                screen = "Contest Details",
                param = mapOf("ContestId" to contestId)
            )
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContestListScreenContent(
    modifier: Modifier = Modifier,
    state: State<ContestListUiState>,
    username: String,
    onRefresh: () -> Unit,
    openContestDetails: (Int) -> Unit,
) {
    val colors = CFThemeColors.current
    val uiState = state.value
    val liveCount = uiState.groups.live.size

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg),
    ) {
        // Terminal-style header; the status only appears when there is something to say.
        ScreenHeader(
            prompt = "contests",
            title = "Contests",
            trailing = when {
                liveCount > 0 -> {
                    { HeaderStatus(text = "● $liveCount live", color = colors.green) }
                }
                uiState.refreshError.isNotBlank() -> {
                    { HeaderStatus(text = "● offline", color = colors.amber) }
                }
                else -> null
            },
        )

        when {
            uiState.loading -> {
                CFLoadingIndicator(modifier = Modifier.weight(1f))
            }

            uiState.userMessage.isNotBlank() -> {
                ErrorState(
                    message = uiState.userMessage,
                    onRetry = onRefresh,
                    modifier = Modifier.weight(1f),
                )
            }

            else -> {
                if (uiState.refreshError.isNotBlank()) {
                    OfflineBanner(text = "offline · showing saved contests", onRetry = onRefresh)
                }
                PullToRefreshBox(
                    isRefreshing = uiState.refreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier.weight(1f),
                ) {
                    ContestList(
                        modifier = Modifier.fillMaxSize(),
                        groups = uiState.groups,
                        nowEpochSeconds = uiState.nowEpochSeconds,
                        username = username,
                        openContestDetails = openContestDetails,
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderStatus(text: String, color: androidx.compose.ui.graphics.Color) {
    Text(
        text = text,
        style = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = color,
        ),
    )
}
