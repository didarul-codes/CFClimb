package com.codeforcesvisualizer.profile

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.codeforcesvisualizer.core.components.CFCard
import com.codeforcesvisualizer.core.components.TagBarsChart
import com.codeforcesvisualizer.core.components.TagData
import com.codeforcesvisualizer.core.theme.CFThemeColors
import com.codeforcesvisualizer.shared.domain.entity.UserStatus
import com.codeforcesvisualizer.shared.domain.stats.solvedTagCounts
import com.codeforcesvisualizer.core.theme.CFText
import androidx.compose.ui.unit.sp

@Composable
fun TagsCard(
    modifier: Modifier = Modifier,
    userStatusList: List<UserStatus>
) {
    val colors = CFThemeColors.current

    val tagCounts = remember(userStatusList) {
        userStatusList.solvedTagCounts().map { (tag, count) -> TagData(tag = tag, count = count) }
    }

    if (tagCounts.isEmpty()) return

    CFCard(
        modifier = modifier.fillMaxWidth(),
        title = "solved by tag",
        titleRight = {
            Text(
                text = "${tagCounts.size} tags",
                style = CFText.caption.copy(color = colors.dim),
            )
        },
    ) {
        TagBarsChart(tags = tagCounts, maxItems = 8)
    }
}
