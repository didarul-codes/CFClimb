package com.codeforcesvisualizer.core.components

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.codeforcesvisualizer.core.theme.CFThemeColors

@Composable
fun CFAppBar(
    modifier: Modifier = Modifier,
    title: String,
    onNavigateBack: (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null,
) {
    if (onNavigateBack != null) {
        CFAppBarWithBackNavigation(
            modifier = modifier,
            title = title,
            onNavigateBack = onNavigateBack,
            actions = actions
        )
    } else {
        CFAppBar(
            modifier = modifier,
            title = title,
            actions = { actions?.invoke() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CFAppBar(
    modifier: Modifier = Modifier,
    title: String,
    actions: @Composable (() -> Unit)? = null,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        actions = { actions?.invoke() },
        colors = cfAppBarColors()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CFAppBarWithBackNavigation(
    modifier: Modifier = Modifier,
    title: String,
    onNavigateBack: () -> Unit,
    actions: @Composable (() -> Unit)? = null,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = { actions?.invoke() },
        colors = cfAppBarColors()
    )
}

// Same background as the rest of the app, so the bar blends with the area behind the status bar.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun cfAppBarColors(): TopAppBarColors {
    val colors = CFThemeColors.current
    return TopAppBarDefaults.topAppBarColors(
        containerColor = colors.bg,
        titleContentColor = colors.fg,
        navigationIconContentColor = colors.fg,
        actionIconContentColor = colors.fg,
    )
}
