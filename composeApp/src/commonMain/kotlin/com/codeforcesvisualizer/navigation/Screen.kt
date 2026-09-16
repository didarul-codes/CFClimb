package com.codeforcesvisualizer.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Compare
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

internal sealed class Screen(
    val route: String,
    val contentDescription: String,
    val label: String,
    val icon: ImageVector
) {
    object Home : Screen("home", "Contests", "contests", Icons.AutoMirrored.Outlined.FormatListBulleted)
    object Profile : Screen("profile", "Profile", "profile", Icons.Outlined.Person)
    object Compare : Screen("compare", "Compare", "compare", Icons.Outlined.Compare)
    object More : Screen("more", "Settings", "settings", Icons.Outlined.Settings)
}

internal sealed class LeafScreen(val route: String) {
    open fun createRoute(root: Screen) = "${root.route}/$route"

    object ContestList : LeafScreen("contest")
    object ContestSearch : LeafScreen("contest/search")
    object Upsolve : LeafScreen("upsolve")
    object ContestDetails : LeafScreen("contest/{contestId}") {
        fun createRoute(root: Screen, contestId: Int): String {
            return "${root.route}/contest/$contestId"
        }
    }

    /** The optional handle opens that profile straight away, for example from a shared link. */
    object Profile : LeafScreen("profile?handle={handle}") {
        fun createRoute(root: Screen, handle: String): String {
            return "${root.route}/profile?handle=$handle"
        }
    }

    object CompareHandleInput : LeafScreen("compare/handle-input")
    object CompareViewResult : LeafScreen("compare/view-result")

    object More : LeafScreen("more")

    object WebView : LeafScreen("webview") {
        override fun createRoute(root: Screen): String {
            return "${root.route}/$route?link={link}"
        }

        fun createRoute(root: Screen, link: String): String {
            return "${root.route}/webview?link=$link"
        }
    }
}
