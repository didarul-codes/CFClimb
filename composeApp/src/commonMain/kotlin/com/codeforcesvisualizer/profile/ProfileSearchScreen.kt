package com.codeforcesvisualizer.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import codeforces_visualizer.composeapp.generated.resources.Res
import codeforces_visualizer.composeapp.generated.resources.enter_handle_hint
import coil3.compose.AsyncImage
import com.codeforcesvisualizer.core.EventLogger
import com.codeforcesvisualizer.core.components.RecentSearchesSection
import com.codeforcesvisualizer.core.components.CFCard
import com.codeforcesvisualizer.climb.tierProgress
import com.codeforcesvisualizer.core.components.CFLoadingIndicator
import com.codeforcesvisualizer.core.components.ErrorState
import com.codeforcesvisualizer.core.components.OfflineBanner
import com.codeforcesvisualizer.core.components.Chip
import com.codeforcesvisualizer.core.components.DifficultyBucket
import com.codeforcesvisualizer.core.components.DifficultyHistogram
import com.codeforcesvisualizer.core.components.HeatmapCell
import com.codeforcesvisualizer.core.components.HeightSpacer
import com.codeforcesvisualizer.core.components.LanguageBarChart
import com.codeforcesvisualizer.core.components.LanguageData
import com.codeforcesvisualizer.core.components.RankBadge
import com.codeforcesvisualizer.core.components.RatingLineChart
import com.codeforcesvisualizer.core.components.RatingPoint
import com.codeforcesvisualizer.core.components.RatingSeriesData
import com.codeforcesvisualizer.core.components.ScreenHeader
import com.codeforcesvisualizer.core.components.SearchBar
import com.codeforcesvisualizer.core.components.StatBox
import com.codeforcesvisualizer.core.components.SubmissionHeatmap
import com.codeforcesvisualizer.core.components.TagBarsChart
import com.codeforcesvisualizer.core.components.TagData
import com.codeforcesvisualizer.core.components.VerdictDonut
import com.codeforcesvisualizer.core.components.WidthSpacer
import com.codeforcesvisualizer.core.theme.CFThemeColors
import com.codeforcesvisualizer.core.theme.VerdictColors
import com.codeforcesvisualizer.core.theme.rankColorFor
import com.codeforcesvisualizer.shared.domain.entity.User
import com.codeforcesvisualizer.shared.domain.entity.UserRating
import com.codeforcesvisualizer.shared.domain.entity.UserStatus
import com.codeforcesvisualizer.shared.domain.stats.acceptanceRatePercent
import com.codeforcesvisualizer.shared.domain.stats.bestRank
import com.codeforcesvisualizer.shared.domain.stats.currentGainStreak
import com.codeforcesvisualizer.shared.domain.stats.heatmapGrid
import com.codeforcesvisualizer.shared.domain.stats.solvedProblems
import com.codeforcesvisualizer.shared.domain.stats.submissionsPerDay
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileSearchScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    onOpenWebSite: (String) -> Unit,
    /** Opens this profile straight away, for example from a shared link. */
    initialHandle: String = "",
    viewModel: ProfileSearchViewModel = koinViewModel()
) {
    val searchTextState by viewModel.searchTextState.collectAsState()
    val userInfoUiState by viewModel.userInfoState.collectAsState()
    val userStatusUiState by viewModel.userStatusState.collectAsState()
    val userRatingsUiState by viewModel.userRatingState.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()
    val showingSavedData by viewModel.showingSavedData.collectAsState()

    // Searches once per link, not again after a configuration change.
    var openedInitialHandle by rememberSaveable(initialHandle) { mutableStateOf(false) }
    LaunchedEffect(initialHandle) {
        if (initialHandle.isNotBlank() && !openedInitialHandle) {
            openedInitialHandle = true
            viewModel.search(initialHandle)
        }
    }

    val colors = CFThemeColors.current
    val showRecent = userInfoUiState.user == null && !userInfoUiState.loading

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg)
    ) {
        SearchBar(
            searchText = searchTextState,
            placeholderText = stringResource(Res.string.enter_handle_hint),
            onSearchTextChanged = { text ->
                viewModel.onSearchTextChanged(text)
            },
            onSearch = {
                viewModel.search(searchTextState)
                EventLogger.logEvent(event = "Search User")
            },
            onClearText = { viewModel.onSearchTextChanged("") },
            onNavigateBack = onNavigateBack,
            requestFocusOnStart = initialHandle.isBlank()
        )

        if (showRecent) {
            RecentSearchesSection(
                searches = recentSearches,
                onSearchClick = { handle ->
                    viewModel.onSearchTextChanged(handle)
                    viewModel.search(handle)
                },
                onClearAll = { viewModel.clearRecentSearches() },
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
            )
        }

        val savedUser = userInfoUiState.user
        if (showingSavedData && savedUser != null) {
            OfflineBanner(
                text = "offline · showing saved data",
                onRetry = { viewModel.search(savedUser.handle) },
            )
        }

        ProfileContent(
            userInfoUiState = userInfoUiState,
            userStatusUiState = userStatusUiState,
            userRatingsUiState = userRatingsUiState,
            onOpenWebSite = onOpenWebSite,
            onRetry = { viewModel.search(searchTextState) }
        )
    }
}

@Composable
private fun ProfileContent(
    modifier: Modifier = Modifier,
    userInfoUiState: UserInfoUiState,
    userStatusUiState: UserStatusUiState,
    userRatingsUiState: UserRatingUiState,
    onOpenWebSite: (String) -> Unit,
    onRetry: () -> Unit
) {
    val colors = CFThemeColors.current
    val isLoading = userInfoUiState.loading || userStatusUiState.loading || userRatingsUiState.loading
    val hasError = userInfoUiState.userMessage.isNotBlank()
    val user = userInfoUiState.user
    val userStatusList = userStatusUiState.userStatus
    val userRatingList = userRatingsUiState.userRatings

    if (isLoading && user == null) {
        CFLoadingIndicator(modifier = Modifier.fillMaxSize())
        return
    }

    if (hasError && user == null) {
        ErrorState(
            message = userInfoUiState.userMessage,
            onRetry = onRetry,
            modifier = Modifier.fillMaxSize(),
        )
        return
    }

    if (user == null) return

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Screen header
        item {
            ScreenHeader(
                prompt = "users/${user.handle}",
                title = "@${user.handle}"
            )
        }

        // Identity section
        item {
            IdentitySection(
                user = user,
                modifier = Modifier.padding(horizontal = 18.dp)
            )
        }

        // Hype banner
        if (userRatingList != null && userRatingList.isNotEmpty()) {
            item {
                HypeBanner(
                    user = user,
                    userRatings = userRatingList,
                    modifier = Modifier.padding(horizontal = 18.dp)
                )
            }
        }

        // Stat grid
        if (userStatusList != null || userRatingList != null) {
            item {
                StatGrid(
                    userStatusList = userStatusList,
                    userRatingList = userRatingList,
                    modifier = Modifier.padding(horizontal = 18.dp)
                )
            }
        }

        // Rating chart
        if (userRatingList != null && userRatingList.size >= 2) {
            item {
                RatingsCard(
                    userRatingList = userRatingList,
                    modifier = Modifier.padding(horizontal = 18.dp)
                )
            }
        }

        // Verdict donut
        if (userStatusList != null && userStatusList.isNotEmpty()) {
            item {
                VerdictCard(
                    userStatusList = userStatusList,
                    modifier = Modifier.padding(horizontal = 18.dp)
                )
            }
        }

        // Submission heatmap
        if (userStatusList != null) {
            item {
                SubmissionHeatmapCard(
                    userStatusList = userStatusList,
                    modifier = Modifier.padding(horizontal = 18.dp)
                )
            }
        }

        // Tag bars
        if (userStatusList != null && userStatusList.isNotEmpty()) {
            item {
                TagsCard(
                    userStatusList = userStatusList,
                    modifier = Modifier.padding(horizontal = 18.dp)
                )
            }
        }

        // Difficulty histogram
        if (userStatusList != null && userStatusList.isNotEmpty()) {
            item {
                LevelsCard(
                    userStatusList = userStatusList,
                    modifier = Modifier.padding(horizontal = 18.dp)
                )
            }
        }

        // Languages
        if (userStatusList != null && userStatusList.isNotEmpty()) {
            item {
                LanguageCard(
                    userStatusList = userStatusList,
                    modifier = Modifier.padding(horizontal = 18.dp)
                )
            }
        }

        // Recent activity
        if (userStatusList != null && userStatusList.isNotEmpty()) {
            item {
                RecentActivityCard(
                    userStatusList = userStatusList,
                    modifier = Modifier.padding(horizontal = 18.dp)
                )
            }
        }

        // Bottom spacing
        item {
            HeightSpacer(height = 32.dp)
        }
    }
}

// --- Identity Section ---

@Composable
private fun IdentitySection(
    user: User,
    modifier: Modifier = Modifier
) {
    val colors = CFThemeColors.current
    val initials = buildString {
        if (user.firstName.isNotBlank()) append(user.firstName.first().uppercase())
        if (user.lastName.isNotBlank()) append(user.lastName.first().uppercase())
        if (isEmpty()) append(user.handle.first().uppercase())
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar with gradient fallback
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(colors.surface2),
            contentAlignment = Alignment.Center
        ) {
            if (user.avatar.isNotBlank()) {
                AsyncImage(
                    model = user.avatar,
                    contentDescription = user.handle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            } else {
                Text(
                    text = initials,
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = colors.violet
                    )
                )
            }
        }

        WidthSpacer(width = 14.dp)

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${user.firstName} ${user.lastName}".trim().ifBlank { user.handle },
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = colors.fg
                ),
                maxLines = 1
            )
            HeightSpacer(height = 4.dp)
            RankBadge(rating = user.rating, rank = user.rank)

            HeightSpacer(height = 4.dp)

            val details = buildList {
                add("max ${user.maxRating}")
                if (user.organization.isNotBlank()) add(user.organization)
                if (user.country.isNotBlank()) add(user.country)
            }.joinToString(" · ")

            Text(
                text = details,
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = colors.dim
                )
            )
        }
    }
}

// --- Hype Banner ---

@Composable
private fun HypeBanner(
    user: User,
    userRatings: List<UserRating>,
    modifier: Modifier = Modifier
) {
    val colors = CFThemeColors.current

    val streak = remember(userRatings) { userRatings.currentGainStreak() }

    // Calculate rating needed for next rank
    val nextRankInfo = remember(user.rating) {
        val progress = tierProgress(user.rating)
        progress.nextTier?.let { "+${progress.pointsToNext} to ${it.name}" }
    }

    if (streak == 0 && nextRankInfo == null) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(colors.amber.copy(alpha = 0.08f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "🔥",
            style = TextStyle(fontSize = 16.sp)
        )
        WidthSpacer(width = 8.dp)
        Column {
            if (streak > 0) {
                Text(
                    text = "$streak contest win streak",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = colors.amber
                    )
                )
            }
            if (nextRankInfo != null) {
                Text(
                    text = nextRankInfo,
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = colors.dim
                    )
                )
            }
        }
    }
}

// --- Stat Grid ---

@Composable
private fun StatGrid(
    userStatusList: List<UserStatus>?,
    userRatingList: List<UserRating>?,
    modifier: Modifier = Modifier
) {
    val colors = CFThemeColors.current

    val solved = remember(userStatusList) { userStatusList?.solvedProblems()?.size ?: 0 }

    val contests = userRatingList?.size ?: 0

    val acRate = remember(userStatusList) { "${userStatusList?.acceptanceRatePercent() ?: 0}%" }

    val bestRank = remember(userRatingList) { userRatingList?.bestRank()?.toString() ?: "-" }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CFCard(
                title = "solved",
                modifier = Modifier.weight(1f)
            ) {
                StatBox(
                    label = "problems",
                    value = solved.toString(),
                    color = colors.green
                )
            }
            CFCard(
                title = "contests",
                modifier = Modifier.weight(1f)
            ) {
                StatBox(
                    label = "participated",
                    value = contests.toString(),
                    color = colors.blue
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CFCard(
                title = "ac rate",
                modifier = Modifier.weight(1f)
            ) {
                StatBox(
                    label = "acceptance",
                    value = acRate,
                    color = colors.amber
                )
            }
            CFCard(
                title = "best rank",
                modifier = Modifier.weight(1f)
            ) {
                StatBox(
                    label = "in contest",
                    value = bestRank,
                    color = colors.violet
                )
            }
        }
    }
}

// --- Submission Heatmap Card ---

@Composable
private fun SubmissionHeatmapCard(
    userStatusList: List<UserStatus>,
    modifier: Modifier = Modifier
) {
    val cells = remember(userStatusList) {
        val timeZone = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(timeZone).date
        heatmapGrid(userStatusList.submissionsPerDay(timeZone), today, weeks = 26)
            .map { HeatmapCell(week = it.week, day = it.dayOfWeek, count = it.count) }
    }

    CFCard(
        title = "submissions / last 26 weeks",
        modifier = modifier
    ) {
        SubmissionHeatmap(cells = cells)
    }
}

// --- Recent Activity Card ---

@Composable
private fun RecentActivityCard(
    userStatusList: List<UserStatus>,
    modifier: Modifier = Modifier
) {
    val colors = CFThemeColors.current
    val recentItems = remember(userStatusList) {
        userStatusList.take(6)
    }

    CFCard(
        title = "recent activity",
        modifier = modifier
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            recentItems.forEach { status ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val verdictShort = minifyVerdict(status.verdict)
                    val verdictColor = VerdictColors[verdictShort]
                        ?: VerdictColors[status.verdict]
                        ?: colors.dim

                    Chip(
                        text = verdictShort,
                        color = verdictColor
                    )

                    WidthSpacer(width = 8.dp)

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = status.problem.name,
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = colors.fg
                            ),
                            maxLines = 1
                        )
                        if (status.problem.tags.isNotEmpty()) {
                            Text(
                                text = status.problem.tags.first(),
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    color = colors.dim
                                )
                            )
                        }
                    }

                    Text(
                        text = status.problem.label,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = colors.dim
                        )
                    )
                }
            }
        }
    }
}

// --- Utility ---

private fun minifyVerdict(verdict: String): String {
    return when (verdict) {
        "OK" -> "AC"
        "COMPILATION_ERROR" -> "CE"
        "RUNTIME_ERROR" -> "RE"
        "WRONG_ANSWER" -> "WA"
        "PRESENTATION_ERROR" -> "PE"
        "TIME_LIMIT_EXCEEDED" -> "TLE"
        "MEMORY_LIMIT_EXCEEDED" -> "MLE"
        "IDLENESS_LIMIT_EXCEEDED" -> "ILE"
        "SECURITY_VIOLATED" -> "SV"
        "INPUT_PREPARATION_CRASHED" -> "IPC"
        else -> verdict
    }
}
