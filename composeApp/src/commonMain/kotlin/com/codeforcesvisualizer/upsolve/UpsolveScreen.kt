package com.codeforcesvisualizer.upsolve

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.codeforcesvisualizer.core.components.CFLoadingIndicator
import com.codeforcesvisualizer.core.components.Center
import com.codeforcesvisualizer.core.components.Chip
import com.codeforcesvisualizer.core.components.SelectableChip
import com.codeforcesvisualizer.core.components.ErrorState
import com.codeforcesvisualizer.core.components.HeightSpacer
import com.codeforcesvisualizer.core.components.OfflineBanner
import com.codeforcesvisualizer.core.components.ScreenHeader
import com.codeforcesvisualizer.core.theme.CFThemeColors
import com.codeforcesvisualizer.core.theme.rankColorFor
import com.codeforcesvisualizer.shared.data.config.BASE_URL
import com.codeforcesvisualizer.shared.domain.stats.UpsolveProblem
import com.codeforcesvisualizer.shared.domain.stats.UpsolveReason
import org.koin.compose.viewmodel.koinViewModel
import com.codeforcesvisualizer.core.theme.CFText
import com.codeforcesvisualizer.core.theme.bold
import com.codeforcesvisualizer.core.theme.CFShapes
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.sp
import com.codeforcesvisualizer.core.theme.CFSpace

/** Unsolved problems from the saved handle's rated rounds. */
@Composable
fun UpsolveScreen(
    onNavigateBack: () -> Unit,
    onOpenProblem: (url: String) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UpsolveViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val colors = CFThemeColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg),
    ) {
        ScreenHeader(prompt = "upsolve", title = "Upsolve", onNavigateBack = onNavigateBack)

        when (val current = state) {
            UpsolveUiState.Checking -> Unit
            UpsolveUiState.NoHandle -> Center(modifier = Modifier.weight(1f)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = CFSpace.wideGutter)) {
                    MonoText(
                        text = "Save your Codeforces handle to build an upsolve queue from your rated rounds.",
                        color = colors.dim,
                        style = CFText.subtitle,
                        center = true,
                    )
                    HeightSpacer(height = 12.dp)
                    MonoText(
                        text = "$ set handle",
                        color = colors.violet,
                        style = CFText.body.bold(),
                        modifier = Modifier
                            .clickable(role = Role.Button, onClick = onOpenSettings)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    )
                }
            }
            is UpsolveUiState.Loading -> CFLoadingIndicator(modifier = Modifier.weight(1f))
            is UpsolveUiState.Failed -> ErrorState(
                message = current.message,
                onRetry = viewModel::reload,
                modifier = Modifier.weight(1f),
            )
            is UpsolveUiState.Ready -> ReadyContent(
                state = current,
                onSelectFilter = viewModel::setFilter,
                onRefresh = viewModel::reload,
                onOpenProblem = onOpenProblem,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ReadyContent(
    state: UpsolveUiState.Ready,
    onSelectFilter: (UpsolveFilter) -> Unit,
    onRefresh: () -> Unit,
    onOpenProblem: (String) -> Unit,
    modifier: Modifier,
) {
    val colors = CFThemeColors.current
    Column(modifier = modifier) {
        if (state.refreshError.isNotBlank()) {
            OfflineBanner(text = "offline · showing saved data", onRetry = onRefresh)
        }
        FlowRow(
            modifier = Modifier.padding(horizontal = CFSpace.gutter, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SelectableChip("all ${state.triedCount + state.notOpenedCount}", state.filter == UpsolveFilter.ALL) {
                onSelectFilter(UpsolveFilter.ALL)
            }
            SelectableChip("tried ${state.triedCount}", state.filter == UpsolveFilter.TRIED) {
                onSelectFilter(UpsolveFilter.TRIED)
            }
            SelectableChip("not opened ${state.notOpenedCount}", state.filter == UpsolveFilter.NOT_OPENED) {
                onSelectFilter(UpsolveFilter.NOT_OPENED)
            }
        }
        if (!state.problemsetLoaded) {
            MonoText(
                text = "Loading the problem list; problems you haven't opened will appear when it's saved.",
                color = colors.dim,
                style = CFText.caption,
                modifier = Modifier.padding(horizontal = CFSpace.gutter, vertical = 4.dp),
            )
        }

        PullToRefreshBox(
            isRefreshing = state.refreshing,
            onRefresh = onRefresh,
            modifier = Modifier.weight(1f),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 6.dp, bottom = 16.dp),
            ) {
                val emptyMessage = when {
                    !state.hasRatedRounds -> "Your upsolve queue fills in after your first rated round."
                    state.items.isNotEmpty() -> null
                    state.filter == UpsolveFilter.ALL -> "Nothing to upsolve: every problem from your rated rounds is solved."
                    else -> "No problems match this filter."
                }
                if (emptyMessage != null) {
                    item {
                        MonoText(
                            text = emptyMessage,
                            color = colors.dim,
                            style = CFText.body,
                            center = true,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = CFSpace.wideGutter, vertical = 32.dp),
                        )
                    }
                } else {
                    items(state.items, key = { it.problem.key }) { item ->
                        UpsolveRow(item = item, onOpenProblem = onOpenProblem)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UpsolveRow(item: UpsolveProblem, onOpenProblem: (String) -> Unit) {
    val colors = CFThemeColors.current
    val shape = CFShapes.card
    val problem = item.problem

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CFSpace.gutter, vertical = 4.dp)
            .clip(shape)
            .background(colors.surface)
            .border(width = 1.dp, color = colors.border, shape = shape)
            .clickable(role = Role.Button, onClickLabel = "Open problem") {
                onOpenProblem("$BASE_URL/contest/${problem.contestId}/problem/${problem.index}")
            }
            .padding(horizontal = CFSpace.card, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = colors.violet)) { append(problem.label) }
                    withStyle(SpanStyle(color = colors.fg)) { append("  ${problem.name}") }
                },
                style = CFText.subtitle,
                maxLines = 2,
            )
            HeightSpacer(height = 3.dp)
            MonoText(text = item.contestName, color = colors.dim, style = CFText.caption)
            if (problem.tags.isNotEmpty()) {
                HeightSpacer(height = 6.dp)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    problem.tags.take(3).forEach { tag -> Chip(text = tag, color = colors.dim, subtle = true) }
                }
            }
        }
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(start = 8.dp)) {
            problem.rating?.let { rating ->
                MonoText(text = rating.toString(), color = rankColorFor(rating), style = CFText.body.bold())
            }
            MonoText(
                text = when {
                    item.reason == UpsolveReason.NOT_OPENED -> "not opened"
                    item.wrongAttempts == 1 -> "tried · 1 wrong"
                    item.wrongAttempts > 1 -> "tried · ${item.wrongAttempts} wrong"
                    else -> "tried"
                },
                color = if (item.reason == UpsolveReason.TRIED) colors.amber else colors.dim,
                style = CFText.caption,
            )
        }
    }
}

@Composable
private fun MonoText(
    text: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    style: TextStyle = CFText.body,
    center: Boolean = false,
) {
    Text(
        text = text,
        modifier = modifier,
        textAlign = if (center) TextAlign.Center else null,
        style = style.copy(color = color),
    )
}
