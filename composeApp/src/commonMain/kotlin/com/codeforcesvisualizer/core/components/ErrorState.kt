package com.codeforcesvisualizer.core.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.codeforcesvisualizer.core.theme.CFThemeColors
import com.codeforcesvisualizer.core.theme.CFText
import com.codeforcesvisualizer.core.theme.bold
import com.codeforcesvisualizer.core.theme.CFAlpha
import com.codeforcesvisualizer.core.theme.CFShapes
import androidx.compose.ui.unit.sp
import com.codeforcesvisualizer.core.theme.CFSpace

/** Full-area message for when nothing could be loaded, with a way to try again. */
@Composable
fun ErrorState(
    message: String,
    onRetry: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val colors = CFThemeColors.current
    Center(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = CFSpace.wideGutter),
        ) {
            Text(
                text = message,
                textAlign = TextAlign.Center,
                style = CFText.subtitle.copy(color = colors.dim),
            )
            if (onRetry != null) {
                HeightSpacer(height = 12.dp)
                RetryButton(onClick = onRetry)
            }
        }
    }
}

/** One-line notice that saved data is on screen because a refresh failed. */
@Composable
fun OfflineBanner(
    text: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CFThemeColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = CFSpace.gutter, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = CFText.label.copy(color = colors.amber),
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "$ retry",
            style = CFText.label.bold().copy(color = colors.violet),
            modifier = Modifier
                .clickable(role = Role.Button, onClick = onRetry)
                .padding(horizontal = 8.dp, vertical = 10.dp),
        )
    }
}

@Composable
fun RetryButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    CFButton(
        text = "retry",
        onClick = onClick,
        modifier = modifier,
        style = CFButtonStyle.Outline,
    )
}
