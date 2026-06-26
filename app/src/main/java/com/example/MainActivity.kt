package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.ui.components.PushNotificationAlertOverlay
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.CricketViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val repository = (application as CricTaughtApplication).container.cricketRepository
            val viewModel: CricketViewModel = viewModel(
                factory = CricketViewModel.provideFactory(repository)
            )

            val themeMode by viewModel.themeMode.collectAsState()
            val pushNotifications by viewModel.pushNotifications.collectAsState()

            val isDarkTheme = when (themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Define routes where bottom navigation should be visible
                val topLevelRoutes = listOf("home", "live", "schedule", "teams", "profile")
                val isTopLevel = currentRoute in topLevelRoutes

                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            if (isTopLevel) {
                                NavigationBar {
                                    // Home Tab
                                    NavigationBarItem(
                                        icon = {
                                            Icon(
                                                imageVector = if (currentRoute == "home") Icons.Default.Home else Icons.Outlined.Home,
                                                contentDescription = "Home"
                                            )
                                        },
                                        label = { Text("Home") },
                                        selected = currentRoute == "home",
                                        onClick = {
                                            navController.navigate("home") {
                                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )

                                    // Live Tab
                                    NavigationBarItem(
                                        icon = {
                                            Icon(
                                                imageVector = if (currentRoute == "live") Icons.Default.LiveTv else Icons.Outlined.LiveTv,
                                                contentDescription = "Live"
                                            )
                                        },
                                        label = { Text("Live") },
                                        selected = currentRoute == "live",
                                        onClick = {
                                            navController.navigate("live") {
                                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )

                                    // Schedule Tab
                                    NavigationBarItem(
                                        icon = {
                                            Icon(
                                                imageVector = if (currentRoute == "schedule") Icons.Default.CalendarMonth else Icons.Outlined.CalendarMonth,
                                                contentDescription = "Schedule"
                                            )
                                        },
                                        label = { Text("Schedule") },
                                        selected = currentRoute == "schedule",
                                        onClick = {
                                            navController.navigate("schedule") {
                                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )

                                    // Teams Tab
                                    NavigationBarItem(
                                        icon = {
                                            Icon(
                                                imageVector = if (currentRoute == "teams") Icons.Default.Groups else Icons.Outlined.Groups,
                                                contentDescription = "Teams"
                                            )
                                        },
                                        label = { Text("Teams") },
                                        selected = currentRoute == "teams",
                                        onClick = {
                                            navController.navigate("teams") {
                                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )

                                    // Settings Tab (Profile)
                                    NavigationBarItem(
                                        icon = {
                                            Icon(
                                                imageVector = if (currentRoute == "profile") Icons.Default.Settings else Icons.Outlined.Settings,
                                                contentDescription = "Profile"
                                            )
                                        },
                                        label = { Text("Settings") },
                                        selected = currentRoute == "profile",
                                        onClick = {
                                            navController.navigate("profile") {
                                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = "home",
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable("home") {
                                HomeScreen(
                                    viewModel = viewModel,
                                    onNavigateToMatch = { matchId ->
                                        navController.navigate("match_details/$matchId")
                                    },
                                    onNavigateToSearch = {
                                        navController.navigate("search")
                                    }
                                )
                            }
                            composable("live") {
                                LiveScreen(
                                    viewModel = viewModel,
                                    onNavigateToMatch = { matchId ->
                                        navController.navigate("match_details/$matchId")
                                    }
                                )
                            }
                            composable("schedule") {
                                ScheduleScreen(
                                    viewModel = viewModel,
                                    onNavigateToMatch = { matchId ->
                                        navController.navigate("match_details/$matchId")
                                    }
                                )
                            }
                            composable("teams") {
                                TeamsScreen(
                                    viewModel = viewModel,
                                    onNavigateToTeam = { teamId ->
                                        navController.navigate("team_profile/$teamId")
                                    }
                                )
                            }
                            composable("profile") {
                                ProfileScreen(
                                    viewModel = viewModel,
                                    onNavigateToMatch = { matchId ->
                                        navController.navigate("match_details/$matchId")
                                    }
                                )
                            }
                            composable(
                                route = "match_details/{matchId}",
                                arguments = listOf(navArgument("matchId") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val matchId = backStackEntry.arguments?.getString("matchId") ?: ""
                                MatchDetailScreen(
                                    matchId = matchId,
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable(
                                route = "player_profile/{playerId}",
                                arguments = listOf(navArgument("playerId") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val playerId = backStackEntry.arguments?.getString("playerId") ?: ""
                                PlayerProfileScreen(
                                    playerId = playerId,
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable(
                                route = "team_profile/{teamId}",
                                arguments = listOf(navArgument("teamId") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val teamId = backStackEntry.arguments?.getString("teamId") ?: ""
                                TeamProfileScreen(
                                    teamId = teamId,
                                    viewModel = viewModel,
                                    onNavigateToPlayer = { playerId ->
                                        navController.navigate("player_profile/$playerId")
                                    },
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable("search") {
                                SearchScreen(
                                    viewModel = viewModel,
                                    onNavigateToTeam = { teamId ->
                                        navController.navigate("team_profile/$teamId")
                                    },
                                    onNavigateToPlayer = { playerId ->
                                        navController.navigate("player_profile/$playerId")
                                    },
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }

                    // Floating real-time Alert Center overlays on top of all scaffolds
                    PushNotificationAlertOverlay(
                        notifications = pushNotifications,
                        onClear = { viewModel.clearNotifications() }
                    )
                }
            }
        }
    }
}
