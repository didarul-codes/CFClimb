package com.codeforcesvisualizer.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.codeforcesvisualizer.core.theme.CFThemeColors
import com.codeforcesvisualizer.core.theme.rankColorFor
import com.codeforcesvisualizer.core.theme.CFText
import com.codeforcesvisualizer.core.theme.bold
import com.codeforcesvisualizer.core.theme.CFAlpha
import androidx.compose.ui.unit.sp

enum class RankBadgeSize { Large, Small }

@Composable
fun RankBadge(
    rating: Int,
    rank: String,
    modifier: Modifier = Modifier,
    size: RankBadgeSize = RankBadgeSize.Large,
) {
    val colors = CFThemeColors.current
    val tierColor = rankColorFor(rating)
    val bgColor = tierColor.copy(alpha = CFAlpha.FILL)
    val borderColor = tierColor.copy(alpha = CFAlpha.BORDER)

    val pillRadius = when (size) {
        RankBadgeSize.Large -> 20.dp
        RankBadgeSize.Small -> 14.dp
    }
    val circleSize = when (size) {
        RankBadgeSize.Large -> 26.dp
        RankBadgeSize.Small -> 18.dp
    }
    val ratingStyle = when (size) {
        RankBadgeSize.Large -> CFText.subtitle.bold()
        RankBadgeSize.Small -> CFText.caption.bold()
    }
    val rankStyle = when (size) {
        RankBadgeSize.Large -> CFText.label
        RankBadgeSize.Small -> CFText.micro
    }
    val initialStyle = when (size) {
        RankBadgeSize.Large -> CFText.label.bold()
        RankBadgeSize.Small -> CFText.nano.bold()
    }
    val pillShape = RoundedCornerShape(pillRadius)

    Row(
        modifier = modifier
            .clip(pillShape)
            .background(bgColor)
            .border(width = 1.dp, color = borderColor, shape = pillShape)
            .padding(end = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(circleSize)
                .clip(CircleShape)
                .background(tierColor),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = rating.toString().take(1),
                style = initialStyle.copy(color = colors.bg, textAlign = TextAlign.Center),
            )
        }

        WidthSpacer(width = 6.dp)

        Text(
            text = rating.toString(),
            style = ratingStyle.copy(color = tierColor),
        )

        WidthSpacer(width = 6.dp)

        Text(
            text = rank.lowercase(),
            style = rankStyle.copy(color = colors.dim),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
