package com.codeforcesvisualizer.climb

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codeforcesvisualizer.core.components.CFCard
import com.codeforcesvisualizer.core.components.HeightSpacer
import com.codeforcesvisualizer.core.components.RetryButton
import com.codeforcesvisualizer.core.components.WidthSpacer
import com.codeforcesvisualizer.core.theme.CFThemeColors
import org.koin.compose.viewmodel.koinViewModel

private val CardShape = RoundedCornerShape(12.dp)

/** The saved handle's rating, tier progress and week, or a prompt to save a handle. */
@Composable
fun MyClimbCard(
    onOpenProfile: (handle: String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenUpsolve: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MyClimbViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    when (val current = state) {
        MyClimbUiState.Checking -> Unit
        MyClimbUiState.NoHandle -> HandlePrompt(onOpenSettings, modifier)
        is MyClimbUiState.Loading -> CFCard(modifier = modifier, title = "my climb") {
            ClimbText(text = "loading @${current.handle}…", color = CFThemeColors.current.dim)
        }
        is MyClimbUiState.Failed -> CFCard(modifier = modifier, title = "my climb") {
            ClimbText(text = "@${current.handle}: ${current.message}", color = CFThemeColors.current.dim)
            HeightSpacer(height = 10.dp)
            RetryButton(onClick = viewModel::retry)
        }
        is MyClimbUiState.Ready -> ClimbDetails(current.summary, onOpenProfile, onOpenUpsolve, modifier)
    }
}

@Composable
private fun HandlePrompt(onOpenSettings: () -> Unit, modifier: Modifier) {
    val colors = CFThemeColors.current
    CFCard(
        modifier = modifier
            .clip(CardShape)
            .clickable(role = Role.Button, onClickLabel = "Open settings", onClick = onOpenSettings),
        title = "my climb",
    ) {
        ClimbText(
            text = "Save your Codeforces handle to see your rating, tier progress and streak here.",
            color = colors.dim,
        )
        HeightSpacer(height = 8.dp)
        ClimbText(text = "$ set handle", color = colors.violet, bold = true)
    }
}

@Composable
private fun ClimbDetails(
    summary: ClimbSummary,
    onOpenProfile: (String) -> Unit,
    onOpenUpsolve: () -> Unit,
    modifier: Modifier,
) {
    val colors = CFThemeColors.current
    val progress = summary.tierProgress

    CFCard(
        modifier = modifier
            .clip(CardShape)
            .clickable(role = Role.Button, onClickLabel = "Open your profile") { onOpenProfile(summary.handle) },
        title = "my climb",
        titleRight = { ClimbText(text = "@${summary.handle} →", color = colors.violet) },
    ) {
        if (progress == null || summary.rating == null) {
            Text(
                text = "unrated",
                style = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = colors.fg),
            )
            ClimbText(text = "Play a rated round to get your first rating.", color = colors.dim)
        } else {
            val tierColor = progress.tier.color
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = summary.rating.toString(),
                    style = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 26.sp, color = tierColor),
                )
                WidthSpacer(width = 8.dp)
                Text(
                    text = progress.tier.name,
                    style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = tierColor),
                    modifier = Modifier.padding(bottom = 4.dp).weight(1f),
                )
                summary.lastChange?.let { change ->
                    Text(
                        text = if (change >= 0) "+$change" else "$change",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (change >= 0) colors.green else colors.red,
                        ),
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
            }
            HeightSpacer(height = 8.dp)
            TierBar(fraction = progress.fraction, color = tierColor, track = colors.surface2)
            HeightSpacer(height = 4.dp)
            val next = progress.nextTier
            ClimbText(
                text = if (next == null) "top tier" else "+${progress.pointsToNext} to ${next.name}",
                color = colors.dim,
            )
        }

        HeightSpacer(height = 12.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ClimbStat(label = "streak", value = days(summary.streakDays), color = colors.amber, modifier = Modifier.weight(1f))
            ClimbStat(label = "last 7 days", value = "${summary.solvedThisWeek} solved", color = colors.green, modifier = Modifier.weight(1f))
            ClimbStat(label = "rated", value = rounds(summary.ratedContests), color = colors.blue, modifier = Modifier.weight(1f))
        }
        if (summary.ratedContests > 0) {
            HeightSpacer(height = 8.dp)
            Text(
                text = "$ upsolve queue →",
                style = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = colors.violet),
                modifier = Modifier
                    .clickable(role = Role.Button, onClick = onOpenUpsolve)
                    .padding(vertical = 6.dp),
            )
        }
    }
}

@Composable
private fun TierBar(fraction: Float, color: Color, track: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(track),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction)
                .fillMaxHeight()
                .background(color),
        )
    }
}

@Composable
private fun ClimbStat(label: String, value: String, color: Color, modifier: Modifier) {
    Column(modifier = modifier) {
        Text(
            text = label.uppercase(),
            style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 9.sp, letterSpacing = 0.08.sp, color = CFThemeColors.current.dim),
        )
        Text(
            text = value,
            style = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = color),
        )
    }
}

@Composable
private fun ClimbText(text: String, color: Color, bold: Boolean = false) {
    Text(
        text = text,
        style = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = color,
        ),
    )
}

private fun days(count: Int) = if (count == 1) "1 day" else "$count days"

private fun rounds(count: Int) = if (count == 1) "1 round" else "$count rounds"
