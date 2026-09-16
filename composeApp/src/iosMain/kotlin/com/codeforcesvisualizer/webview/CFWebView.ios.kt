@file:OptIn(ExperimentalForeignApi::class)

package com.codeforcesvisualizer.webview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import com.codeforcesvisualizer.core.components.ScreenHeader
import com.codeforcesvisualizer.core.theme.CFThemeColors
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.cinterop.readValue
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.darwin.NSObject

@Composable
actual fun CFWebViewScreen(
    modifier: Modifier,
    link: String,
    onNavigateBack: () -> Unit,
) {
    val safeLink = remember(link) { link.ifBlank { "https://codeforces.com" } }
    var isLoading by remember(safeLink) { mutableStateOf(true) }
    val request = remember(safeLink) {
        NSURL(string = safeLink)?.let { NSURLRequest.requestWithURL(it) }
            ?: NSURLRequest.requestWithURL(NSURL(string = "https://codeforces.com")!!)
    }
    var navigationDelegate = remember(safeLink) {
        CFWebViewNavigationDelegate { loading -> isLoading = loading }
    }
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
            UIKitView(
                modifier = Modifier.fillMaxSize(),
                factory = {
                    WKWebView(
                        frame = CGRectZero.readValue(),
                        configuration = WKWebViewConfiguration()
                    ).apply {
                        loadRequest(request)
                    }
                },
                update = { view ->
                    val webView = view as WKWebView
                    if (webView.URL?.absoluteString != safeLink) {
                        isLoading = true
                        webView.loadRequest(request)
                    }
                    if (webView.navigationDelegate !== navigationDelegate) {
                        webView.navigationDelegate = navigationDelegate
                    }
                }
            )
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

private class CFWebViewNavigationDelegate(
    private val onLoadingStateChange: (Boolean) -> Unit,
) : NSObject(), WKNavigationDelegateProtocol {
    @ObjCSignatureOverride
    override fun webView(webView: WKWebView, didStartProvisionalNavigation: WKNavigation?) {
        onLoadingStateChange(true)
    }

    @ObjCSignatureOverride
    override fun webView(webView: WKWebView, didFinishNavigation: WKNavigation?) {
        onLoadingStateChange(false)
    }

    @ObjCSignatureOverride
    override fun webView(webView: WKWebView, didFailNavigation: WKNavigation?, withError: NSError) {
        onLoadingStateChange(false)
    }

    @ObjCSignatureOverride
    override fun webView(
        webView: WKWebView,
        didFailProvisionalNavigation: WKNavigation?,
        withError: NSError,
    ) {
        onLoadingStateChange(false)
    }
}
