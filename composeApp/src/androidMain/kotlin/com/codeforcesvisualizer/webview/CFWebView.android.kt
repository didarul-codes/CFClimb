package com.codeforcesvisualizer.webview

import android.annotation.SuppressLint
import android.webkit.WebSettings
import android.webkit.WebSettings.LOAD_DEFAULT
import android.webkit.WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.codeforcesvisualizer.core.components.ScreenHeader
import com.codeforcesvisualizer.core.theme.CFThemeColors
import com.kevinnzou.web.WebView
import com.kevinnzou.web.rememberWebViewState

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun CFWebViewScreen(
    modifier: Modifier,
    link: String,
    onNavigateBack: () -> Unit,
) {
    val state = rememberWebViewState(url = link)
    val colors = CFThemeColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg),
    ) {
        ScreenHeader(
            prompt = "codeforces.com",
            title = "Codeforces",
            onNavigateBack = onNavigateBack,
        )
        Box(modifier = Modifier.weight(1f)) {
            WebView(
                state = state,
                onCreated = { webview ->
                    setWebViewSettings(webview.settings)
                }
            )
            if (state.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

private fun setWebViewSettings(settings: WebSettings) {
    settings.apply {
        setSupportZoom(true)
        builtInZoomControls = true
        displayZoomControls = false
        useWideViewPort = true
        allowFileAccess = true
        allowContentAccess = true
        setSupportMultipleWindows(false)
        databaseEnabled = true
        domStorageEnabled = true
        javaScriptCanOpenWindowsAutomatically = false
        cacheMode = LOAD_DEFAULT
        mixedContentMode = MIXED_CONTENT_COMPATIBILITY_MODE
        javaScriptEnabled = true
    }
}
