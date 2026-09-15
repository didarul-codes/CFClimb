package com.codeforcesvisualizer

import android.os.Bundle
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

        setContent {
            App()
        }
    }

    private fun logEvent(event: String, param: Bundle) {
        firebaseAnalytics.logEvent(event, param)
    }
}