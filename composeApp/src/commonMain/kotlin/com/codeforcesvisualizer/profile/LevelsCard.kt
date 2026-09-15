package com.codeforcesvisualizer.profile

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.codeforcesvisualizer.core.components.CFCard
import com.codeforcesvisualizer.core.components.DifficultyBucket
import com.codeforcesvisualizer.core.components.DifficultyHistogram
import com.codeforcesvisualizer.shared.domain.entity.UserStatus
import com.codeforcesvisualizer.shared.domain.stats.solvedByDifficulty

@Composable
fun LevelsCard(
    modifier: Modifier = Modifier,
    userStatusList: List<UserStatus>
) {
    val buckets = remember(userStatusList) {
        userStatusList.solvedByDifficulty().map { (rating, count) ->
            DifficultyBucket(range = rating.toString(), count = count)
        }
    }

    if (buckets.isEmpty()) return

    CFCard(
        modifier = modifier.fillMaxWidth(),
        title = "solved by difficulty",
    ) {
        DifficultyHistogram(buckets = buckets)
    }
}
