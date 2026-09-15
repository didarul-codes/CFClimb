package com.codeforcesvisualizer.compare

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codeforcesvisualizer.core.components.CFCard
import com.codeforcesvisualizer.core.components.Center
import com.codeforcesvisualizer.core.components.HeightSpacer
import com.codeforcesvisualizer.core.components.RatingLineChart
import com.codeforcesvisualizer.core.components.RatingPoint
import com.codeforcesvisualizer.core.components.RatingSeriesData
import com.codeforcesvisualizer.core.components.RadarSeries
import com.codeforcesvisualizer.core.components.ScreenHeader
import com.codeforcesvisualizer.core.components.TagRadarChart
import com.codeforcesvisualizer.core.components.WidthSpacer
import com.codeforcesvisualizer.core.theme.CFThemeColors
import com.codeforcesvisualizer.shared.domain.entity.UserRating
import com.codeforcesvisualizer.shared.domain.entity.UserStatus
import com.codeforcesvisualizer.shared.domain.stats.profileSummary
import com.codeforcesvisualizer.shared.domain.stats.solvedTagCounts
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CompareHandlesScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    viewModel: CompareHandlesViewModel = koinViewModel()
) {
    val handleOne by viewModel.handle1State.collectAsState()
    val handleTwo by viewModel.handle2State.collectAsState()

    val userRatingsUiState by viewModel.userRatingState.collectAsState()
    val userStatusUiState by viewModel.userStatusState.collectAsState()

    val colors = CFThemeColors.current

    // Rating and submission requests can fail independently, and the API words the same failure
    // differently per endpoint, so show one error per handle.
    val errorMessage = (userStatusUiState.errors + userRatingsUiState.errors)
        .entries
        .joinToString("\n") { (handle, message) -> "@$handle: $message" }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg)
            .padding(horizontal = 18.dp),
    ) {
        item {
            ScreenHeader(
                prompt = "compare",
                title = "$handleOne vs $handleTwo",
            )
        }

        // Loading state
        if (userRatingsUiState.loading || userStatusUiState.loading) {
            item {
                Center(modifier = Modifier.padding(vertical = 32.dp)) {
                    CircularProgressIndicator(color = colors.violet)
                }
            }
        }

        // Error state
        if (errorMessage.isNotBlank()) {
            item {
                Center(modifier = Modifier.padding(vertical = 16.dp)) {
                    Text(
                        text = errorMessage,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = colors.red,
                        ),
                    )
                }
            }
        }

        // Winner hype section
        val ratings1 = userRatingsUiState.userRatings1
        val ratings2 = userRatingsUiState.userRatings2
        if (ratings1 != null && ratings2 != null) {
            if (ratings1.isNotEmpty() || ratings2.isNotEmpty()) {
                item {
                    WinnerHypeSection(
                        handle1 = handleOne,
                        handle2 = handleTwo,
                        ratings1 = ratings1,
                        ratings2 = ratings2,
                    )
                }

                item {
                    HeightSpacer(height = 16.dp)
                }
            }

            // Rating chart
            item {
                RatingChartCard(
                    handle1 = handleOne,
                    handle2 = handleTwo,
                    ratings1 = ratings1,
                    ratings2 = ratings2,
                )
            }

            item {
                HeightSpacer(height = 16.dp)
            }

            // Head-to-head diff table
            item {
                HeadToHeadCard(
                    handle1 = handleOne,
                    handle2 = handleTwo,
                    ratings1 = ratings1,
                    ratings2 = ratings2,
                    status1 = userStatusUiState.userStatus1,
                    status2 = userStatusUiState.userStatus2,
                )
            }

            // Tag radar chart
            val status1 = userStatusUiState.userStatus1
            val status2 = userStatusUiState.userStatus2
            if (status1 != null && status2 != null) {
                item {
                    HeightSpacer(height = 16.dp)
                }
                item {
                    TagRadarCard(
                        handle1 = handleOne,
                        handle2 = handleTwo,
                        status1 = status1,
                        status2 = status2,
                    )
                }
            }

            item {
                HeightSpacer(height = 24.dp)
            }
        }
    }
}

@Composable
private fun WinnerHypeSection(
    handle1: String,
    handle2: String,
    ratings1: List<UserRating>,
    ratings2: List<UserRating>,
) {
    val colors = CFThemeColors.current
    val rating1 = ratings1.lastOrNull()?.newRating ?: 0
    val rating2 = ratings2.lastOrNull()?.newRating ?: 0
    val margin = kotlin.math.abs(rating1 - rating2)
    val winner = if (rating1 >= rating2) handle1 else handle2
    val winnerColor = if (rating1 >= rating2) colors.violet else colors.green

    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(
            winnerColor.copy(alpha = 0.15f),
            winnerColor.copy(alpha = 0.05f),
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(gradientBrush)
            .border(
                width = 1.dp,
                color = winnerColor.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 18.dp, vertical = 16.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Column {
            Text(
                text = "@$winner",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = winnerColor,
                ),
            )
            HeightSpacer(height = 4.dp)
            Text(
                text = "+$margin rating gap",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = colors.dim,
                ),
            )
        }
    }
}

@Composable
private fun RatingChartCard(
    handle1: String,
    handle2: String,
    ratings1: List<UserRating>,
    ratings2: List<UserRating>,
) {
    val colors = CFThemeColors.current

    val series1 = remember(ratings1) {
        RatingSeriesData(
            data = ratings1.mapIndexed { i, r -> RatingPoint(index = i, rating = r.newRating) },
            color = colors.violet,
            label = handle1,
        )
    }
    val series2 = remember(ratings2) {
        RatingSeriesData(
            data = ratings2.mapIndexed { i, r -> RatingPoint(index = i, rating = r.newRating) },
            color = colors.green,
            label = handle2,
        )
    }

    CFCard(title = "rating.history × 2") {
        if (ratings1.size >= 2 || ratings2.size >= 2) {
            RatingLineChart(
                series = listOf(series1, series2).filter { it.data.isNotEmpty() },
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            Center(modifier = Modifier.padding(vertical = 32.dp)) {
                Text(
                    text = "not enough rating history to plot",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = colors.dim,
                    ),
                )
            }
        }

        HeightSpacer(height = 10.dp)

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            LegendDot(color = colors.violet, label = handle1)
            WidthSpacer(width = 20.dp)
            LegendDot(color = colors.green, label = handle2)
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    val colors = CFThemeColors.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        WidthSpacer(width = 6.dp)
        Text(
            text = "@$label",
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = colors.dim,
            ),
        )
    }
}

@Composable
private fun HeadToHeadCard(
    handle1: String,
    handle2: String,
    ratings1: List<UserRating>,
    ratings2: List<UserRating>,
    status1: List<UserStatus>?,
    status2: List<UserStatus>?,
) {
    val colors = CFThemeColors.current

    val summary1 = remember(ratings1, status1) { profileSummary(ratings1, status1.orEmpty()) }
    val summary2 = remember(ratings2, status2) { profileSummary(ratings2, status2.orEmpty()) }
    val hasSubmissions = status1 != null && status2 != null

    data class DiffRow(
        val label: String,
        val value1: Int?,
        val value2: Int?,
        val suffix: String = "",
        val higherIsWinner: Boolean = true,
    ) {
        val val1: String get() = value1?.let { "$it$suffix" } ?: "–"
        val val2: String get() = value2?.let { "$it$suffix" } ?: "–"
    }

    val rows = buildList {
        add(DiffRow("rating", summary1.rating, summary2.rating))
        add(DiffRow("max", summary1.maxRating, summary2.maxRating))
        if (hasSubmissions) add(DiffRow("solved", summary1.solvedProblems, summary2.solvedProblems))
        add(DiffRow("contests", summary1.ratedContests, summary2.ratedContests))
        if (hasSubmissions) {
            add(DiffRow("ac rate", summary1.acceptanceRatePercent, summary2.acceptanceRatePercent, suffix = "%"))
        }
        add(DiffRow("best rank", summary1.bestRank, summary2.bestRank, higherIsWinner = false))
        add(DiffRow("max streak", summary1.longestGainStreak, summary2.longestGainStreak))
    }

    CFCard(title = "head.to.head", contentPadding = 0.dp) {
        // Column headers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "@$handle1",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.violet,
                ),
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "stat",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = colors.dim,
                ),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
            )
            Text(
                text = "@$handle2",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.green,
                ),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End,
            )
        }

        HorizontalDivider(color = colors.border, thickness = 1.dp)

        rows.forEachIndexed { index, row ->
            val v1 = row.value1
            val v2 = row.value2
            val winner1 = v1 != null && v2 != null && (if (row.higherIsWinner) v1 > v2 else v1 < v2)
            val winner2 = v1 != null && v2 != null && (if (row.higherIsWinner) v2 > v1 else v2 < v1)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    if (winner1) {
                        Text(
                            text = "▲ ",
                            style = TextStyle(
                                fontSize = 8.sp,
                                color = colors.violet,
                            ),
                        )
                    }
                    Text(
                        text = row.val1,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            fontWeight = if (winner1) FontWeight.Bold else FontWeight.Normal,
                            color = if (winner1) colors.violet else colors.fg,
                        ),
                    )
                }

                Text(
                    text = row.label,
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = colors.dim,
                    ),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )

                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = row.val2,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            fontWeight = if (winner2) FontWeight.Bold else FontWeight.Normal,
                            color = if (winner2) colors.green else colors.fg,
                        ),
                    )
                    if (winner2) {
                        Text(
                            text = " ▲",
                            style = TextStyle(
                                fontSize = 8.sp,
                                color = colors.green,
                            ),
                        )
                    }
                }
            }

            if (index < rows.size - 1) {
                HorizontalDivider(
                    color = colors.border,
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 14.dp),
                )
            }
        }
    }
}

@Composable
private fun TagRadarCard(
    handle1: String,
    handle2: String,
    status1: List<UserStatus>,
    status2: List<UserStatus>,
) {
    val colors = CFThemeColors.current

    // Distinct solved problems per tag for each user
    val tagCounts1 = remember(status1) { status1.solvedTagCounts().associate { it.tag to it.count } }
    val tagCounts2 = remember(status2) { status2.solvedTagCounts().associate { it.tag to it.count } }

    // Find top 8 shared tags by combined count
    val allTags = (tagCounts1.keys + tagCounts2.keys)
    val topTags = remember(tagCounts1, tagCounts2) {
        allTags
            .distinct()
            .sortedByDescending { (tagCounts1[it] ?: 0) + (tagCounts2[it] ?: 0) }
            .take(8)
    }

    if (topTags.size < 3) return

    val series1Values = topTags.map { tagCounts1[it] ?: 0 }
    val series2Values = topTags.map { tagCounts2[it] ?: 0 }

    CFCard(title = "tag.coverage") {
        TagRadarChart(
            series = listOf(
                RadarSeries(values = series1Values, color = colors.violet),
                RadarSeries(values = series2Values, color = colors.green),
            ),
            axes = topTags,
            modifier = Modifier.fillMaxWidth(),
        )

        HeightSpacer(height = 10.dp)

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            LegendDot(color = colors.violet, label = handle1)
            WidthSpacer(width = 20.dp)
            LegendDot(color = colors.green, label = handle2)
        }
    }
}
