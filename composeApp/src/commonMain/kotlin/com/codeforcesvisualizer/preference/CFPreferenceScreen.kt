package com.codeforcesvisualizer.preference

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.codeforcesvisualizer.core.EventLogger
import com.codeforcesvisualizer.core.components.CFCard
import com.codeforcesvisualizer.core.components.CFTextField
import com.codeforcesvisualizer.core.components.HeightSpacer
import com.codeforcesvisualizer.core.components.ScreenHeader
import com.codeforcesvisualizer.core.data.UserSettingsRepository
import com.codeforcesvisualizer.core.ratingalerts.RatingAlertToggle
import com.codeforcesvisualizer.core.reminders.ReminderSettingsCard
import com.codeforcesvisualizer.core.theme.CFThemeColors
import com.codeforcesvisualizer.core.links.rememberLinkHandlingSetting
import com.codeforcesvisualizer.core.widget.rememberAddWidgetAction
import com.codeforcesvisualizer.shared.domain.entity.UiThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import com.codeforcesvisualizer.core.theme.CFText
import com.codeforcesvisualizer.core.theme.bold
import com.codeforcesvisualizer.core.theme.CFAlpha
import com.codeforcesvisualizer.core.theme.CFShapes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.sp
import com.codeforcesvisualizer.core.theme.CFSpace

@Composable
fun PreferenceScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    themeManager: ThemeManager = koinViewModel<ThemeManagerViewModel>()
) {
    val colors = CFThemeColors.current
    val themeModeUiState by themeManager.themeModeFlow.collectAsState()
    val rateAppHandler = rememberRateAppHandler()
    val addWidget = rememberAddWidgetAction()
    val linkSetting = rememberLinkHandlingSetting()
    var showStoreError by remember { mutableStateOf(false) }
    val userSettingsRepository = koinInject<UserSettingsRepository>()
    val savedUsername by userSettingsRepository.username.collectAsState(initial = "")
    var usernameInput by remember(savedUsername) { mutableStateOf(savedUsername) }
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg)
            .padding(horizontal = CFSpace.gutter),
    ) {
        item {
            ScreenHeader(
                prompt = "settings",
                title = "Settings",
            )
        }

        // Username setting
        item {
            CFCard(title = "account") {
                Column {
                    Text(
                        text = "codeforces handle",
                        style = CFText.label.copy(color = colors.dim),
                    )
                    HeightSpacer(height = 8.dp)
                    CFTextField(
                        value = usernameInput,
                        onValueChange = { usernameInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = "enter handle...",
                        onImeAction = {
                            keyboardController?.hide()
                            scope.launch { userSettingsRepository.setUsername(usernameInput) }
                        },
                    )
                    if (usernameInput != savedUsername && usernameInput.isNotBlank()) {
                        HeightSpacer(height = 8.dp)
                        Text(
                            text = "$ save",
                            style = CFText.label.bold().copy(color = colors.violet),
                            modifier = Modifier
                                .clickable {
                                    scope.launch { userSettingsRepository.setUsername(usernameInput) }
                                }
                                .padding(vertical = 4.dp),
                        )
                    }
                    HeightSpacer(height = 12.dp)
                    RatingAlertToggle(handle = savedUsername)
                }
            }
        }

        item { HeightSpacer(height = 16.dp) }

        item {
            ReminderSettingsCard()
        }

        item { HeightSpacer(height = 16.dp) }

        // Theme picker
        item {
            CFCard(title = "appearance") {
                AppearanceSection(
                    themeMode = themeModeUiState.themeMode,
                    onThemeModeChanged = { selectedThemeMode ->
                        themeManager.setUiThemeMode(selectedThemeMode)
                        EventLogger.logEvent(
                            event = "Theme Changed",
                            param = mapOf(
                                "ThemeMode" to selectedThemeMode.toString()
                            )
                        )
                    }
                )
            }
        }

        item { HeightSpacer(height = 16.dp) }

        // Preferences list
        item {
            CFCard(title = "other", contentPadding = 0.dp) {
                Column {
                    PreferenceRow(
                        label = "rate app",
                        value = "open store",
                        isAction = true,
                        onClick = {
                            showStoreError = !rateAppHandler.openStore()
                        },
                    )
                    if (showStoreError) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = CFSpace.card, vertical = 8.dp),
                        ) {
                            Text(
                                text = "store not found",
                                style = CFText.caption.copy(color = colors.red),
                            )
                        }
                    }
                    if (linkSetting != null) {
                        HorizontalDivider(color = colors.border, thickness = 1.dp)
                        PreferenceRow(
                            label = "open codeforces links",
                            value = if (linkSetting.enabled) "in app" else "set up",
                            isAction = true,
                            onClick = linkSetting.open,
                        )
                    }
                    if (addWidget != null) {
                        HorizontalDivider(color = colors.border, thickness = 1.dp)
                        PreferenceRow(
                            label = "home screen widget",
                            value = "add",
                            isAction = true,
                            onClick = addWidget,
                        )
                    }
                    HorizontalDivider(color = colors.border, thickness = 1.dp)
                    PreferenceRow(
                        label = "theme mode",
                        value = themeModeLabel(themeModeUiState.themeMode)
                    )

                }
            }
        }

        item { HeightSpacer(height = 24.dp) }

        // Build info and the disclaimer store listings require.
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "cfclimb v${rateAppHandler.versionName}",
                    modifier = Modifier.fillMaxWidth(),
                    style = CFText.caption.copy(color = colors.dim),
                    textAlign = TextAlign.Center,
                )
                HeightSpacer(height = 4.dp)
                Text(
                    text = "Unofficial. Not affiliated with Codeforces.",
                    modifier = Modifier.fillMaxWidth(),
                    style = CFText.caption.copy(color = colors.dim),
                    textAlign = TextAlign.Center,
                )
            }
        }

        item { HeightSpacer(height = 24.dp) }
    }
}

@Composable
fun PreferenceRow(
    label: String,
    value: String,
    isAction: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val colors = CFThemeColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable { onClick() }
                else Modifier
            )
            .padding(horizontal = CFSpace.card, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = CFText.body.copy(color = colors.fg),
            modifier = Modifier.weight(1f, fill = false),
        )
        Text(
            text = value,
            style = CFText.body.copy(color = if (isAction) colors.violet else colors.dim),
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}

fun themeModeLabel(mode: UiThemeMode): String {
    return when (mode) {
        UiThemeMode.Dark -> "dark"
        UiThemeMode.Light -> "light"
        UiThemeMode.System -> "system"
    }
}

@Preview
@Composable
private fun Preview() {
    PreferenceScreen(
        onNavigateBack = {},
        themeManager = PreviewThemeManager()
    )
}

private class PreviewThemeManager : ThemeManager {
    private val state = MutableStateFlow(ThemeModeUiState())
    override val themeModeFlow: StateFlow<ThemeModeUiState> = state

    override fun setUiThemeMode(themeMode: UiThemeMode) {
        state.value = state.value.copy(themeMode = themeMode)
    }
}
