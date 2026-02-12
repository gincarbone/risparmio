package com.youandmedia.risparmio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.youandmedia.risparmio.navigation.NavGraph
import com.youandmedia.risparmio.navigation.Routes
import com.youandmedia.risparmio.navigation.bottomNavRoutes
import com.youandmedia.risparmio.ui.calendar.CalendarViewModel
import com.youandmedia.risparmio.ui.dashboard.DashboardViewModel
import com.youandmedia.risparmio.ui.theme.RisparmioTheme

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RisparmioTheme {
                val navController = rememberNavController()
                val calendarViewModel: CalendarViewModel = viewModel()
                val dashboardViewModel: DashboardViewModel = viewModel()

                val navItems = listOf(
                    BottomNavItem(Routes.CALENDAR, "Calendario", Icons.Default.CalendarMonth),
                    BottomNavItem(Routes.DASHBOARD, "Dashboard", Icons.Default.Dashboard),
                    BottomNavItem(Routes.STATS, "Statistiche", Icons.Default.BarChart),
                    BottomNavItem(Routes.SETTINGS, "Impostazioni", Icons.Default.Settings)
                )

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Show bottom bar only when not on splash or day_detail
                val showBottomBar = currentRoute in bottomNavRoutes

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar {
                                navItems.forEach { item ->
                                    NavigationBarItem(
                                        icon = { Icon(item.icon, contentDescription = item.label) },
                                        label = { Text(item.label, maxLines = 1) },
                                        selected = currentRoute == item.route,
                                        onClick = {
                                            if (currentRoute != item.route) {
                                                navController.navigate(item.route) {
                                                    popUpTo(navController.graph.findStartDestination().id) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                ) { paddingValues ->
                    NavGraph(
                        navController = navController,
                        calendarViewModel = calendarViewModel,
                        dashboardViewModel = dashboardViewModel,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
}
