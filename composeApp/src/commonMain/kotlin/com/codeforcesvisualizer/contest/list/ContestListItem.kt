package com.codeforcesvisualizer.contest.list

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codeforcesvisualizer.core.components.Chip
import com.codeforcesvisualizer.core.components.HeightSpacer
import com.codeforcesvisualizer.core.theme.CFThemeColors
import com.codeforcesvisualizer.core.utils.convertTimeStampToDateString
import com.codeforcesvisualizer.core.utils.convertToHMS
import com.codeforcesvisualizer.core.utils.formatTimeUntil
import com.codeforcesvisualizer.shared.domain.entity.Contest
import kotlinx.datetime.Clock

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ContestListItem(
    modifier: Modifier = Modifier,
    contest: Contest,
    isUpcoming: Boolean,
    onOpenContest: (Int) -> Unit,
    /** Set for running rounds, for example "ends in 1h 20m". */
    liveLabel: String? = null,
) {
    val colors = CFThemeColors.current
    val shape = RoundedCornerShape(12.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surface)
            .border(width = 1.dp, color = colors.border, shape = shape)
            .clickable { onOpenContest(contest.id) }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        // Left content
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contest.name,
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = colors.fg,
                ),
                maxLines = 2,
            )

            HeightSpacer(height = 4.dp)

            // Date + duration line
            val dateStr = contest.startTimeSeconds.convertTimeStampToDateString()
            val durationStr = contest.durationSeconds.convertToHMS()
            Text(
                text = "$dateStr  ·  $durationStr",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.5.sp,
                    color = colors.dim,
                ),
                maxLines = 1,
            )

            HeightSpacer(height = 8.dp)

            // Chips row
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                // Division chip (extract from name)
                val division = extractDivision(contest.name)
                if (division != null) {
                    Chip(text = division, color = colors.blue)
                }

                // Kind chip
                val kind = contest.kind ?: "Codeforces"
                Chip(text = kind, color = colors.dim, subtle = true)

                if (liveLabel != null) {
                    Chip(text = "● live", color = colors.green)
                } else if (isUpcoming) {
                    Chip(text = "scheduled", color = colors.green, subtle = true)
                }
            }
        }

        // Right side info
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.padding(start = 8.dp),
        ) {
            if (liveLabel != null) {
                Text(
                    text = liveLabel,
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.green,
                    ),
                )
            } else if (isUpcoming) {
                val timeUntil = formatTimeUntil(contest.startTimeSeconds)
                Text(
                    text = "in $timeUntil",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = colors.violet,
                    ),
                )
            } else {
                // For past contests, show phase
                Text(
                    text = contest.phase.lowercase(),
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = colors.dim,
                    ),
                )
            }
        }
    }
}

/**
 * Extract division from contest name, e.g. "Div. 2", "Div. 1 + 2"
 */
internal fun extractDivision(name: String): String? {
    val regex = Regex("""(Div\.\s*\d(?:\s*\+\s*\d)?)""", RegexOption.IGNORE_CASE)
    val match = regex.find(name)
    return match?.value
}

/**
 * Format time until a contest starts as a human-readable short string.
 */
internal fun formatTimeUntil(startTimeSeconds: Int): String =
    formatTimeUntil(startTimeSeconds.toLong(), Clock.System.now().epochSeconds)
