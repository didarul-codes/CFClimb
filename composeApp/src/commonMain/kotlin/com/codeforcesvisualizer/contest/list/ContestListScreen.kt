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
import androidx.compose.ui.Modifier
import com.codeforcesvisualizer.core.EventLogger
import com.codeforcesvisualizer.core.components.CFLoadingIndicator
import com.codeforcesvisualizer.core.components.ErrorState
import com.codeforcesvisualizer.core.components.OfflineBanner
import com.codeforcesvisualizer.core.components.ScreenHeader
import com.codeforcesvisualizer.core.theme.CFThemeColors
import org.koin.compose.viewmodel.koinViewModel
import com.codeforcesvisualizer.core.theme.CFText
import com.codeforcesvisualizer.core.theme.bold
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.codeforcesvisualizer.core.components.WidthSpacer

@Composable
fun ContestListScreen(
    modifier: Modifier = Modifier,
    viewModel: ContestViewModel = koinViewModel(),
    openSearch: () -> Unit = {},
    openContestDetails: (Int) -> Unit = {},
    onOpenWebSite: (Int) -> Unit = {},
    openProfile: (handle: String) -> Unit = {},
    openSettings: () -> Unit = {},
    openUpsolve: () -> Unit = {},
) {
    val uiState = viewModel.uiState.collectAsState()
    ContestListScreenContent(
        modifier = modifier,
        state = uiState,
        onRefresh = viewModel::refreshContestList,
        openSearch = openSearch,
        openContestDetails = { contestId ->
            openContestDetails(contestId)
            EventLogger.logScreenView(
                screen = "Contest Details",
                param = mapOf("ContestId" to contestId)
            )
        },
        openProfile = openProfile,
        openSettings = openSettings,
        openUpsolve = openUpsolve,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContestListScreenContent(
    modifier: Modifier = Modifier,
    state: State<ContestListUiState>,
    onRefresh: () -> Unit,
    openSearch: () -> Unit,
    openContestDetails: (Int) -> Unit,
    openProfile: (String) -> Unit,
    openSettings: () -> Unit,
    openUpsolve: () -> Unit,
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
            trailing = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    when {
                        liveCount > 0 -> HeaderStatus(text = "● $liveCount live", color = colors.green)
                        uiState.refreshError.isNotBlank() ->
                            HeaderStatus(text = "● offline", color = colors.amber)
                        else -> Unit
                    }
                    WidthSpacer(width = 10.dp)
                    Text(
                        text = "$ search",
                        style = CFText.label.bold().copy(color = colors.violet),
                        modifier = Modifier
                            .clickable(role = Role.Button, onClick = openSearch)
                            .padding(vertical = 4.dp),
                    )
                }
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
                        openContestDetails = openContestDetails,
                        openProfile = openProfile,
                        openSettings = openSettings,
                        openUpsolve = openUpsolve,
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
        style = CFText.label.bold().copy(color = color),
    )
}
