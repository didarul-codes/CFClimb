package com.codeforcesvisualizer.profile

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.codeforcesvisualizer.core.components.CFCard
import com.codeforcesvisualizer.core.components.RatingLineChart
import com.codeforcesvisualizer.core.components.RatingPoint
import com.codeforcesvisualizer.core.components.RatingSeriesData
import com.codeforcesvisualizer.core.theme.CFThemeColors
import com.codeforcesvisualizer.shared.domain.entity.UserRating
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp

@Composable
fun RatingsCard(
    modifier: Modifier = Modifier,
    userRatingList: List<UserRating>
) {
    val colors = CFThemeColors.current

    val chartData = remember(userRatingList) {
        userRatingList.mapIndexed { index, rating ->
            RatingPoint(index = index, rating = rating.newRating)
        }
    }

    val bestRank = remember(userRatingList) { userRatingList.minOfOrNull { it.rank } ?: 0 }
    val maxUp = remember(userRatingList) {
        userRatingList.maxOfOrNull { it.newRating - it.oldRating } ?: 0
    }

    CFCard(
        modifier = modifier.fillMaxWidth(),
        title = "rating.history",
        titleRight = {
            if (userRatingList.isNotEmpty()) {
                val totalGain = userRatingList.last().newRating - userRatingList.first().oldRating
                val prefix = if (totalGain >= 0) "+" else ""
                androidx.compose.material3.Text(
                    text = "$prefix$totalGain",
                    style = com.codeforcesvisualizer.core.theme.CFText.caption
                        .copy(color = if (totalGain >= 0) colors.green else colors.red),
                )
            }
        },
    ) {
        if (chartData.size >= 2) {
            RatingLineChart(
                series = listOf(
                    RatingSeriesData(
                        data = chartData,
                        color = colors.violet,
                        label = "rating",
                    )
                ),
            )
        }
    }
}
