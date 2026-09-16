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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codeforcesvisualizer.core.components.CFLoadingIndicator
import com.codeforcesvisualizer.core.components.Center
import com.codeforcesvisualizer.core.components.Chip
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
        BackRow(onNavigateBack)
        ScreenHeader(prompt = "upsolve", title = "Upsolve")

        when (val current = state) {
            UpsolveUiState.Checking -> Unit
            UpsolveUiState.NoHandle -> Center(modifier = Modifier.weight(1f)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 24.dp)) {
                    MonoText(
                        text = "Save your Codeforces handle to build an upsolve queue from your rated rounds.",
                        color = colors.dim,
                        size = 13,
                        center = true,
                    )
                    HeightSpacer(height = 12.dp)
                    MonoText(
                        text = "$ set handle",
                        color = colors.violet,
                        size = 12,
                        bold = true,
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
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip("all ${state.triedCount + state.notOpenedCount}", state.filter == UpsolveFilter.ALL) {
                onSelectFilter(UpsolveFilter.ALL)
            }
            FilterChip("tried ${state.triedCount}", state.filter == UpsolveFilter.TRIED) {
                onSelectFilter(UpsolveFilter.TRIED)
            }
            FilterChip("not opened ${state.notOpenedCount}", state.filter == UpsolveFilter.NOT_OPENED) {
                onSelectFilter(UpsolveFilter.NOT_OPENED)
            }
        }
        if (!state.problemsetLoaded) {
            MonoText(
                text = "Loading the problem list; problems you haven't opened will appear when it's saved.",
                color = colors.dim,
                size = 10,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp),
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
                            size = 12,
                            center = true,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 32.dp),
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
    val shape = RoundedCornerShape(12.dp)
    val problem = item.problem

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(shape)
            .background(colors.surface)
            .border(width = 1.dp, color = colors.border, shape = shape)
            .clickable(role = Role.Button, onClickLabel = "Open problem") {
                onOpenProblem("$BASE_URL/contest/${problem.contestId}/problem/${problem.index}")
            }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = colors.violet)) { append(problem.label) }
                    withStyle(SpanStyle(color = colors.fg)) { append("  ${problem.name}") }
                },
                style = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp),
                maxLines = 2,
            )
            HeightSpacer(height = 3.dp)
            MonoText(text = item.contestName, color = colors.dim, size = 10)
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
                MonoText(text = rating.toString(), color = rankColorFor(rating), size = 12, bold = true)
            }
            MonoText(
                text = when {
                    item.reason == UpsolveReason.NOT_OPENED -> "not opened"
                    item.wrongAttempts == 1 -> "tried · 1 wrong"
                    item.wrongAttempts > 1 -> "tried · ${item.wrongAttempts} wrong"
                    else -> "tried"
                },
                color = if (item.reason == UpsolveReason.TRIED) colors.amber else colors.dim,
                size = 10,
            )
        }
    }
}

@Composable
private fun FilterChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val colors = CFThemeColors.current
    Chip(
        text = if (selected) "✓ $text" else text,
        color = if (selected) colors.violet else colors.dim,
        subtle = !selected,
        onClick = onClick,
    )
}

@Composable
private fun BackRow(onNavigateBack: () -> Unit) {
    val colors = CFThemeColors.current
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = colors.violet)) { append("← ") }
            withStyle(SpanStyle(color = colors.fg)) { append("cd ..") }
        },
        style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp, fontWeight = FontWeight.Bold),
        modifier = Modifier
            .padding(start = 18.dp, top = 12.dp)
            .clickable(role = Role.Button, onClickLabel = "Back", onClick = onNavigateBack)
            .padding(vertical = 4.dp),
    )
}

@Composable
private fun MonoText(
    text: String,
    color: androidx.compose.ui.graphics.Color,
    size: Int,
    modifier: Modifier = Modifier,
    bold: Boolean = false,
    center: Boolean = false,
) {
    Text(
        text = text,
        modifier = modifier,
        textAlign = if (center) TextAlign.Center else null,
        style = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = size.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = color,
        ),
    )
}
