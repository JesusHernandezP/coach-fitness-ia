package com.fitnessaicoach.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.fitnessaicoach.app.ui.screens.auth.LoginScreen
import com.fitnessaicoach.app.ui.screens.auth.RegisterScreen
import com.fitnessaicoach.app.ui.screens.chat.ChatScreen
import com.fitnessaicoach.app.ui.screens.dashboard.DashboardScreen
import com.fitnessaicoach.app.ui.screens.logs.LogActivityScreen
import com.fitnessaicoach.app.ui.screens.logs.LogWeightScreen
import com.fitnessaicoach.app.ui.screens.profile.ProfileScreen
import com.fitnessaicoach.app.ui.theme.Border
import com.fitnessaicoach.app.ui.theme.Gold
import com.fitnessaicoach.app.ui.theme.Surface
import com.fitnessaicoach.app.ui.theme.TextMuted

sealed class Screen(val route: String) {
    object Login             : Screen("login")
    object Register          : Screen("register")
    object Dashboard         : Screen("dashboard")
    object Chat              : Screen("chat")
    object Profile           : Screen("profile")
    object LogActivity       : Screen("log-activity")
    object LogWeight         : Screen("log-weight")
    object ProfileOnboarding : Screen("profile-onboarding")
}

data class BottomNavItem(
    val screen: Screen,
    val label:  String,
    val icon:   ImageVector,
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard, "Panel",  Icons.Filled.Dashboard),
    BottomNavItem(Screen.Chat,      "Coach",  Icons.Filled.ChatBubble),
    BottomNavItem(Screen.Profile,   "Perfil", Icons.Filled.Person),
)

@Composable
fun AppNavigation(isLoggedIn: Boolean) {
    val navController = rememberNavController()
    val startDest = if (isLoggedIn) Screen.Dashboard.route else Screen.Login.route

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = bottomNavItems.any { it.screen.route == currentDestination?.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = Surface,
                    tonalElevation = 0.dp,
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState     = true
                                }
                            },
                            icon  = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor       = Gold,
                                selectedTextColor       = Gold,
                                indicatorColor          = androidx.compose.ui.graphics.Color(0x22D4B200),
                                unselectedIconColor     = TextMuted,
                                unselectedTextColor     = TextMuted,
                            ),
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController    = navController,
            startDestination = startDest,
            modifier         = androidx.compose.ui.Modifier.padding(padding),
        ) {
            composable(Screen.Login.route)             { LoginScreen(navController) }
            composable(Screen.Register.route)          { RegisterScreen(navController) }
            composable(Screen.Dashboard.route)         { DashboardScreen(navController) }
            composable(Screen.Chat.route)              { ChatScreen(navController) }
            composable(Screen.Profile.route)           { ProfileScreen(navController) }
            composable(Screen.LogActivity.route)       { LogActivityScreen(navController) }
            composable(Screen.LogWeight.route)         { LogWeightScreen(navController) }
            composable(Screen.ProfileOnboarding.route) { ProfileScreen(navController, forceOnboarding = true) }
        }
    }
}
