package com.codeforcesvisualizer

import androidx.compose.ui.window.ComposeUIViewController
import com.codeforcesvisualizer.home.App
import com.codeforcesvisualizer.inject.initKoin
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    initKoin()
    return ComposeUIViewController { App() }
}
