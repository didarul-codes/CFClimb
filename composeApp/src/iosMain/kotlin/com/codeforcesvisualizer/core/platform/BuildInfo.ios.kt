@file:OptIn(ExperimentalNativeApi::class)

package com.codeforcesvisualizer.core.platform

import kotlin.experimental.ExperimentalNativeApi

actual val isDebugBuild: Boolean = Platform.isDebugBinary
