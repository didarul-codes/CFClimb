package com.codeforcesvisualizer.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.codeforcesvisualizer.core.EventLogger
import com.codeforcesvisualizer.core.links.CodeforcesLink
import com.codeforcesvisualizer.core.links.DeepLinks
import com.codeforcesvisualizer.core.ratingalerts.RatingChangeAlerts
import com.codeforcesvisualizer.navigation.LeafScreen
import com.codeforcesvisualizer.core.reminders.ContestReminders
import com.codeforcesvisualizer.core.theme.CFTheme
import com.codeforcesvisualizer.core.theme.CFThemeColors
import com.codeforcesvisualizer.core.theme.SystemBarsAppearance
import com.codeforcesvisualizer.core.widget.HomeWidgets
import com.codeforcesvisualizer.navigation.AppNavigator
import com.codeforcesvisualizer.navigation.Screen
import com.codeforcesvisualizer.preference.ThemeManager
import com.codeforcesvisualizer.shared.domain.entity.UiThemeMode
import org.koin.compose.KoinContext
import org.koin.compose.koinInject

/** Koin must already be started with `initKoin()` by the platform entry point. */
@Composable
fun App() {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components {
                add(KtorNetworkFetcherFactory())
            }
            .build()
    }
    KoinContext {
        Home()
    }
}

@Composable
private fun Home(
    modifier: Modifier = Modifier
) {
    val themeManager = koinInject<ThemeManager>()
    val themeModeUiState by themeManager.themeModeFlow.collectAsState()
    val isDarkTheme = when (themeModeUiState.themeMode) {
        UiThemeMode.System -> isSystemInDarkTheme()
        UiThemeMode.Dark -> true
        UiThemeMode.Light -> false
    }
    val navController = rememberNavController()

    // Keeps contest reminders in step with refreshed start times and reminder settings.
    val contestReminders = koinInject<ContestReminders>()
    LaunchedEffect(contestReminders) {
        contestReminders.keepScheduled()
    }
    // Keeps the background rating check registered while alerts are on.
    val ratingChangeAlerts = koinInject<RatingChangeAlerts>()
    LaunchedEffect(ratingChangeAlerts) {
        ratingChangeAlerts.keepScheduled()
    }
    // Redraws home screen widgets when the contest list, handle or rating changes.
    val homeWidgets = koinInject<HomeWidgets>()
    LaunchedEffect(homeWidgets) {
        homeWidgets.keepUpdated()
    }

    SystemBarsAppearance(themeMode = themeModeUiState.themeMode, isDarkTheme = isDarkTheme)

    CFTheme(
        isDarkTheme = isDarkTheme
    ) {
        val colors = CFThemeColors.current

        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = colors.bg,
            bottomBar = {
                FloatingBottomNav(
                    navController = navController,
                    isDarkTheme = isDarkTheme
                )
            }
        ) { innerPadding ->
            // Screens get the space between the status bar and the bottom bar; consuming the
            // insets stops nested Scaffolds and app bars from adding the status bar again.
            AppNavigator(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
                navController = navController,
                themeManager = themeManager
            )

            // After AppNavigator, so the navigation graph exists when a link opened the app.
            OpenPendingLinks(navController)
        }
    }
}

/** Shows Codeforces links opened from outside the app: shared URLs, browser links, cfclimb:// links. */
@Composable
private fun OpenPendingLinks(navController: NavController) {
    val deepLinks = koinInject<DeepLinks>()
    val pendingLink by deepLinks.pending.collectAsState()
    LaunchedEffect(pendingLink) {
        val link = pendingLink ?: return@LaunchedEffect
        val route = when (link) {
            is CodeforcesLink.Contest -> LeafScreen.ContestDetails.createRoute(Screen.Home, link.contestId)
            is CodeforcesLink.Profile -> LeafScreen.Profile.createRoute(Screen.Profile, link.handle)
        }
        navController.navigate(route)
        deepLinks.consume(link)
    }
}

@Composable
fun FloatingBottomNav(
    navController: NavController,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = CFThemeColors.current
    val bottomNavItems = listOf(
        Screen.Home,
        Screen.Profile,
        Screen.Compare,
        Screen.More,
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val navBarBackground = if (isDarkTheme) {
        Color(0xFF14171D).copy(alpha = 0.86f)
    } else {
        Color(0xFFFFFFFF).copy(alpha = 0.82f)
    }

    val navBarShape = RoundedCornerShape(22.dp)
    val shadowElevation = if (isDarkTheme) 12.dp else 6.dp

    Box(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
            .fillMaxWidth()
            .shadow(elevation = shadowElevation, shape = navBarShape)
            .clip(navBarShape)
            .background(navBarBackground)
            .border(width = 1.dp, color = colors.border, shape = navBarShape)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavItems.forEach { screen ->
                val isSelected =
                    currentDestination?.hierarchy?.any { it.route == screen.route } == true
                val itemColor = if (isSelected) colors.violet else colors.dim

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            EventLogger.logScreenView(screen.route)
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.contentDescription,
                        tint = itemColor
                    )
                    Text(
                        text = screen.label,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = itemColor,
                        ),
                    )
                }
            }
        }
    }
}
