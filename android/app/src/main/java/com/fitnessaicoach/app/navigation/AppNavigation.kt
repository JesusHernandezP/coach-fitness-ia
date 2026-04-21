package com.fitnessaicoach.app.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.fitnessaicoach.app.ui.theme.Background
import com.fitnessaicoach.app.ui.theme.Border
import com.fitnessaicoach.app.ui.theme.Gold
import com.fitnessaicoach.app.ui.theme.Surface
import com.fitnessaicoach.app.ui.theme.TextMuted
import com.fitnessaicoach.app.ui.theme.TextPrimary
import kotlinx.coroutines.delay

sealed class Screen(val route: String) {
    object Splash            : Screen("splash")
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
fun SplashScreen(isLoggedIn: Boolean, onNavigate: (Boolean) -> Unit) {
    val currentIsLoggedIn by rememberUpdatedState(isLoggedIn)
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
        delay(1400)
        onNavigate(currentIsLoggedIn)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentAlignment = Alignment.Center,
    ) {
        // Gold glow behind logo
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Gold.copy(alpha = 0.12f), Color.Transparent),
                        radius = 600f,
                    )
                )
        )

        Column(
            modifier = Modifier.alpha(alpha.value),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "FITNESS",
                color = Gold,
                fontSize = 52.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 8.sp,
                lineHeight = 52.sp,
            )
            Text(
                text = "AI COACH",
                color = TextPrimary,
                fontSize = 52.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 8.sp,
                lineHeight = 56.sp,
            )
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .width(56.dp)
                    .height(3.dp)
                    .background(Gold, RoundedCornerShape(2.dp))
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = "TU COACH DE BOLSILLO",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp,
            )
        }
    }
}

@Composable
fun AppNavigation(isLoggedIn: Boolean) {
    val navController = rememberNavController()
    val startDest = Screen.Splash.route

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
            composable(Screen.Splash.route) {
                SplashScreen(isLoggedIn = isLoggedIn) { loggedIn ->
                    val dest = if (loggedIn) Screen.Dashboard.route else Screen.Login.route
                    navController.navigate(dest) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            }
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
