package com.codeforcesvisualizer

import android.content.Intent
import android.os.Bundle
import com.codeforcesvisualizer.core.links.DeepLinks
import org.koin.core.context.GlobalContext
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.codeforcesvisualizer.home.App
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics

class HomeActivity : ComponentActivity() {
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        // Draw behind the system bars from the first frame; SystemBarsAppearance then matches the
        // bar icons to the in-app theme.
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        firebaseAnalytics = Firebase.analytics

        if (!BuildConfig.DEBUG) {
            //EventLogger.initialize(::logEvent)
        }

        // Only on a fresh start: after a configuration change the link was already shown.
        if (savedInstanceState == null) openLink(intent)

        setContent {
            App()
        }
    }

    // singleTask: links opened while the app is running arrive here.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        openLink(intent)
    }

    /** Codeforces links from the browser or another app, or text shared to the app. */
    private fun openLink(intent: Intent?) {
        val text = when (intent?.action) {
            Intent.ACTION_VIEW -> intent.dataString
            Intent.ACTION_SEND -> intent.getStringExtra(Intent.EXTRA_TEXT)
            else -> null
        } ?: return
        GlobalContext.get().get<DeepLinks>().open(text)
    }

    private fun logEvent(event: String, param: Bundle) {
        firebaseAnalytics.logEvent(event, param)
    }
}