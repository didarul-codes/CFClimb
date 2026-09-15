package com.codeforcesvisualizer.navigation

import androidx.navigation.*
import androidx.navigation.compose.composable
import androidx.savedstate.read
import com.codeforcesvisualizer.contest.details.ContestDetailsScreen
import com.codeforcesvisualizer.contest.list.ContestListScreen
import com.codeforcesvisualizer.contest.search.ContestSearchScreen
import com.codeforcesvisualizer.shared.data.config.BASE_URL
import com.codeforcesvisualizer.webview.CFWebViewScreen

internal fun NavGraphBuilder.addHomeTopLevel(
    navController: NavController
) {
    navigation(
        route = Screen.Home.route,
        startDestination = LeafScreen.ContestList.createRoute(Screen.Home)
    ) {
        addContestList(navController, Screen.Home)
        addContestSearch(navController, Screen.Home)
        addContestDetails(navController, Screen.Home)
        addWebView(navController, Screen.Home)
    }
}

private fun NavGraphBuilder.addContestList(
    navController: NavController,
    root: Screen
) {
    composable(route = LeafScreen.ContestList.createRoute(root = root)) {
        ContestListScreen(
            openSearch = {
                navController.navigate(LeafScreen.ContestSearch.createRoute(root = root))
            },
            openContestDetails = { contestId ->
                navController.navigate(
                    LeafScreen.ContestDetails.createRoute(
                        root = root,
                        contestId = contestId
                    )
                )
            },
            onOpenWebSite = { contestId ->
                val url = "$BASE_URL/contests/$contestId"
                navController.navigate(LeafScreen.WebView.createRoute(root = root, link = url))
            },
        )
    }
}

private fun NavGraphBuilder.addContestDetails(
    navController: NavController,
    root: Screen,
) {
    composable(
        route = LeafScreen.ContestDetails.createRoute(root = root),
        arguments = listOf(
            navArgument("contestId") {
                type = NavType.IntType
            }
        )
    ) { backStackEntry ->
        val contestId = backStackEntry.arguments?.read {
            getInt("contestId")
        } ?: -1
        ContestDetailsScreen(
            contestId = contestId,
            onNavigateBack = { navController.navigateUp() },
            onOpenWebSite = {
                val url = "$BASE_URL/contests/$contestId"
                navController.navigate(LeafScreen.WebView.createRoute(root = root, link = url))
            },
        )
    }
}

private fun NavGraphBuilder.addContestSearch(
    navController: NavController,
    root: Screen
) {
    composable(route = LeafScreen.ContestSearch.createRoute(root = root)) {
        ContestSearchScreen(
            onNavigateBack = { navController.navigateUp() },
            openContestDetails = { contestId ->
                navController.navigate(
                    LeafScreen.ContestDetails.createRoute(
                        root = root,
                        contestId = contestId
                    )
                )
            },
            onOpenWebSite = { contestId ->
                val url = "$BASE_URL/contests/$contestId"
                navController.navigate(LeafScreen.WebView.createRoute(root = root, link = url))
            },
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
            } ?: ""
        )
    }
}
