package com.codeforcesvisualizer.core.links

import androidx.compose.runtime.Composable
import org.koin.mp.KoinPlatform

// Universal links need a file on codeforces.com, so iOS only opens cfclimb:// links in the app.
@Composable
actual fun rememberLinkHandlingSetting(): LinkHandlingSetting? = null

/** Called from Swift's `onOpenURL`. Returns false when the URL isn't a link the app can show. */
fun openLink(url: String): Boolean = KoinPlatform.getKoin().get<DeepLinks>().open(url)
