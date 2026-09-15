package com.codeforcesvisualizer.core.theme

import androidx.compose.runtime.Composable
import com.codeforcesvisualizer.shared.domain.entity.UiThemeMode

/**
 * Keeps system bar icons readable against the app background. The app has its own light and dark
 * setting, so bar appearance follows [isDarkTheme] (resolved from [themeMode]) rather than the
 * system theme.
 */
@Composable
expect fun SystemBarsAppearance(themeMode: UiThemeMode, isDarkTheme: Boolean)
