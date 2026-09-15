package com.codeforcesvisualizer.core.links

import android.content.Context
import android.content.Intent
import android.content.pm.verify.domain.DomainVerificationManager
import android.content.pm.verify.domain.DomainVerificationUserState
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect

/**
 * The app can't verify codeforces.com, so from Android 12 its links open in the browser until
 * the user allows them for the app in system settings. Earlier versions ask which app to use.
 */
@Composable
actual fun rememberLinkHandlingSetting(): LinkHandlingSetting? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null

    val context = LocalContext.current
    var enabled by remember { mutableStateOf(opensCodeforcesLinks(context)) }

    // The user decides on a system settings screen, so check again on return.
    LifecycleResumeEffect(context) {
        enabled = opensCodeforcesLinks(context)
        onPauseOrDispose { }
    }

    return LinkHandlingSetting(enabled) {
        context.startActivity(
            Intent(Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS, Uri.parse("package:${context.packageName}"))
        )
    }
}

@RequiresApi(Build.VERSION_CODES.S)
private fun opensCodeforcesLinks(context: Context): Boolean {
    val manager = context.getSystemService(DomainVerificationManager::class.java) ?: return false
    val state = manager.getDomainVerificationUserState(context.packageName) ?: return false
    return state.hostToStateMap["codeforces.com"] == DomainVerificationUserState.DOMAIN_STATE_SELECTED
}
