package com.codeforcesvisualizer.navigation

import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.codeforcesvisualizer.core.data.UserSettingsRepository
import org.koin.compose.koinInject
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import androidx.savedstate.read
import com.codeforcesvisualizer.profile.ProfileSearchScreen
import com.codeforcesvisualizer.shared.data.config.BASE_URL
import com.codeforcesvisualizer.webview.CFWebViewScreen
import androidx.compose.runtime.getValue

internal fun NavGraphBuilder.addProfileTopLevel(
    navController: NavController
) {
    navigation(
        route = Screen.Profile.route,
        startDestination = LeafScreen.Profile.createRoute(Screen.Profile)
    ) {
        addProfileSearchScreen(navController, Screen.Profile)
        addWebView(navController, Screen.Profile)
    }
}

private fun NavGraphBuilder.addProfileSearchScreen(
    navController: NavController,
    root: Screen
) {
    composable(
        route = LeafScreen.Profile.createRoute(root),
        arguments = listOf(navArgument("handle") {
            defaultValue = ""
            type = NavType.StringType
        })
    ) { backStackEntry ->
        // Without a handle in the route, the profile tab opens the saved handle.
        val savedHandle by koinInject<UserSettingsRepository>().username.collectAsState(initial = null)
        val routeHandle = backStackEntry.arguments?.read { getString("handle") } ?: ""
        val initialHandle = routeHandle.ifBlank { savedHandle ?: return@composable }
        ProfileSearchScreen(
            modifier = Modifier,
            initialHandle = initialHandle,
            onNavigateBack = { navController.navigateUp() },
            onOpenWebSite = { problem ->
                val (contestId, problemIndex) = problem.split("-")
                val url = "$BASE_URL/contest/$contestId/problem/$problemIndex"
                navController.navigate(LeafScreen.WebView.createRoute(root = root, link = url))
            }
        )
    }
}

private fun NavGraphBuilder.addWebView(
    navController: NavController,
    root: Screen
) {
    composable(
        route = LeafScreen.WebView.createRoute(root = root),
        arguments = listOf(navArgument("link") {
            defaultValue = ""
            type = NavType.StringType
        })
    ) { backStackEntry ->
        CFWebViewScreen(
            onNavigateBack = { navController.navigateUp() },
            link = backStackEntry.arguments?.read {
                getString("link")
            }!!
        )
    }
}