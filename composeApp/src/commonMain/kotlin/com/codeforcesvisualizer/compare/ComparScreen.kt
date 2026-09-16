package com.codeforcesvisualizer.compare

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import codeforces_visualizer.composeapp.generated.resources.Res
import codeforces_visualizer.composeapp.generated.resources.compare_users
import codeforces_visualizer.composeapp.generated.resources.hanldles_can_not_be_empty
import com.codeforcesvisualizer.core.EventLogger
import com.codeforcesvisualizer.core.components.RecentSearchesSection
import com.codeforcesvisualizer.core.components.HeightSpacer
import com.codeforcesvisualizer.core.components.ScreenHeader
import com.codeforcesvisualizer.core.components.WidthSpacer
import com.codeforcesvisualizer.core.theme.CFThemeColors
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import com.codeforcesvisualizer.core.theme.CFText
import com.codeforcesvisualizer.core.theme.CFAlpha
import com.codeforcesvisualizer.core.theme.CFShapes
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.sp
import com.codeforcesvisualizer.core.theme.CFSpace
import com.codeforcesvisualizer.core.components.CFTextField
import com.codeforcesvisualizer.core.components.CFButton
import com.codeforcesvisualizer.core.components.CFButtonSize

@Composable
fun CompareScreenHandleInput(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    openCompare: () -> Unit,
    viewModel: CompareHandlesViewModel = koinViewModel()
) {
    val colors = CFThemeColors.current
    val snackbarHostState = SnackbarHostState()
    val coroutineScope = rememberCoroutineScope()

    val handleOne by viewModel.handle1State.collectAsState()
    val handleTwo by viewModel.handle2State.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()

    val errorMessage = stringResource(Res.string.hanldles_can_not_be_empty)

    fun onCompare() {
        if (handleOne.isBlank() || handleTwo.isBlank()) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(errorMessage)
            }
            return
        }
        viewModel.compare(handleOne, handleTwo)
        openCompare()
        EventLogger.logScreenView(screen = "Compare")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg)
            .padding(horizontal = CFSpace.gutter),
    ) {
        ScreenHeader(
            prompt = "compare",
            title = "diff",
            trailing = {
                Text(
                    text = "2 users",
                    style = CFText.label.copy(color = colors.dim),
                )
            }
        )

        HeightSpacer(height = 24.dp)

        // User A handle picker box
        HandlePickerBox(
            label = "USER.A",
            handle = handleOne,
            accentColor = colors.violet,
            onTextChange = { viewModel.onHandle1Change(it) },
            imeAction = ImeAction.Next,
        )

        HeightSpacer(height = 16.dp)

        // User B handle picker box
        HandlePickerBox(
            label = "USER.B",
            handle = handleTwo,
            accentColor = colors.green,
            onTextChange = { viewModel.onHandle2Change(it) },
            imeAction = ImeAction.Go,
            onGo = { onCompare() },
        )

        HeightSpacer(height = 24.dp)

        // Compare button
        CFButton(
            text = stringResource(Res.string.compare_users).lowercase(),
            onClick = { onCompare() },
            modifier = Modifier.fillMaxWidth(),
            size = CFButtonSize.Large,
        )

        if (handleOne.isBlank() && handleTwo.isBlank()) {
            HeightSpacer(height = 24.dp)
            RecentSearchesSection(
                searches = recentSearches,
                onSearchClick = { handle ->
                    if (handleOne.isBlank()) {
                        viewModel.onHandle1Change(handle)
                    } else {
                        viewModel.onHandle2Change(handle)
                    }
                },
                onClearAll = { viewModel.clearRecentSearches() },
            )
        }
    }
}

@Composable
private fun HandlePickerBox(
    label: String,
    handle: String,
    accentColor: androidx.compose.ui.graphics.Color,
    onTextChange: (String) -> Unit,
    imeAction: ImeAction,
    onGo: (() -> Unit)? = null,
) {
    val colors = CFThemeColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CFShapes.card)
            .background(colors.surface)
            .border(
                width = 1.dp,
                color = accentColor.copy(alpha = 0.4f),
                shape = CFShapes.card
            )
            .padding(14.dp),
    ) {
        // Label row with colored dot
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(accentColor)
            )
            WidthSpacer(width = 8.dp)
            Text(
                text = label,
                style = CFText.caption.copy(color = colors.dim),
            )
            if (handle.isNotBlank()) {
                WidthSpacer(width = 8.dp)
                Text(
                    text = "@$handle",
                    style = CFText.caption.copy(color = accentColor),
                )
            }
        }

        HeightSpacer(height = 10.dp)

        // Input field
        CFTextField(
            value = handle,
            onValueChange = onTextChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = "enter handle...",
            imeAction = imeAction,
            onImeAction = { onGo?.invoke() },
            accent = accentColor,
        )
    }
}

@Preview
@Composable
private fun Preview() {
    CompareScreenHandleInput(onNavigateBack = {}, openCompare = { })
}
