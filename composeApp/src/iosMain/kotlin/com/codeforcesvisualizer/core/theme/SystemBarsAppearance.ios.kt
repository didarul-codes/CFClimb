package com.codeforcesvisualizer.core.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.codeforcesvisualizer.shared.domain.entity.UiThemeMode
import platform.UIKit.UIApplication
import platform.UIKit.UIUserInterfaceStyle
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene

@Composable
actual fun SystemBarsAppearance(themeMode: UiThemeMode, isDarkTheme: Boolean) {
    DisposableEffect(themeMode) {
        // The status bar follows the window's interface style, as do system sheets and alerts.
        // System mode must clear the override: the window style is also what Compose reads as the
        // system theme, so forcing it would keep the app in the last explicit theme.
        val style = when (themeMode) {
            UiThemeMode.Dark -> UIUserInterfaceStyle.UIUserInterfaceStyleDark
            UiThemeMode.Light -> UIUserInterfaceStyle.UIUserInterfaceStyleLight
            UiThemeMode.System -> UIUserInterfaceStyle.UIUserInterfaceStyleUnspecified
        }
        UIApplication.sharedApplication.connectedScenes
            .filterIsInstance<UIWindowScene>()
            .flatMap { it.windows.filterIsInstance<UIWindow>() }
            .forEach { it.overrideUserInterfaceStyle = style }
        onDispose { }
    }
}
