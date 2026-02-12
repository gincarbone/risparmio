package com.youandmedia.risparmio.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.youandmedia.risparmio.ui.calendar.CalendarScreen
import com.youandmedia.risparmio.ui.calendar.CalendarViewModel
import com.youandmedia.risparmio.ui.dashboard.DashboardScreen
import com.youandmedia.risparmio.ui.dashboard.DashboardViewModel
import com.youandmedia.risparmio.ui.daydetail.DayDetailScreen
import com.youandmedia.risparmio.ui.daydetail.DayDetailViewModel
import com.youandmedia.risparmio.ui.settings.SettingsScreen
import com.youandmedia.risparmio.ui.splash.SplashScreen
import com.youandmedia.risparmio.ui.stats.StatsScreen
import com.youandmedia.risparmio.ui.stats.StatsViewModel
import java.time.LocalDate

object Routes {
    const val SPLASH = "splash"
    const val CALENDAR = "calendar"
    const val DASHBOARD = "dashboard"
    const val STATS = "stats"
    const val SETTINGS = "settings"
    const val DAY_DETAIL = "day_detail/{year}/{month}/{day}"

    fun dayDetail(date: LocalDate): String =
        "day_detail/${date.year}/${date.monthValue}/${date.dayOfMonth}"
}

// Bottom nav tabs (excluding splash and day_detail which are not tabs)
val bottomNavRoutes = listOf(Routes.CALENDAR, Routes.DASHBOARD, Routes.STATS, Routes.SETTINGS)

@Composable
fun NavGraph(
    navController: NavHostController,
    calendarViewModel: CalendarViewModel,
    dashboardViewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(navController = navController, startDestination = Routes.SPLASH, modifier = modifier) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onTimeout = {
                    navController.navigate(Routes.CALENDAR) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.CALENDAR) {
            CalendarScreen(
                viewModel = calendarViewModel,
                onDaySelected = { date ->
                    navController.navigate(Routes.dayDetail(date))
                }
            )
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen(viewModel = dashboardViewModel)
        }

        composable(Routes.STATS) {
            val statsViewModel: StatsViewModel = viewModel()
            StatsScreen(viewModel = statsViewModel)
        }

        composable(Routes.SETTINGS) {
            SettingsScreen()
        }

        composable(
            route = Routes.DAY_DETAIL,
            arguments = listOf(
                navArgument("year") { type = NavType.IntType },
                navArgument("month") { type = NavType.IntType },
                navArgument("day") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val year = backStackEntry.arguments?.getInt("year") ?: LocalDate.now().year
            val month = backStackEntry.arguments?.getInt("month") ?: LocalDate.now().monthValue
            val day = backStackEntry.arguments?.getInt("day") ?: LocalDate.now().dayOfMonth
            val date = LocalDate.of(year, month, day)
            val dayDetailViewModel: DayDetailViewModel = viewModel()

            DayDetailScreen(
                date = date,
                viewModel = dayDetailViewModel,
                onBack = {
                    calendarViewModel.loadData()
                    dashboardViewModel.loadData()
                    navController.popBackStack()
                }
            )
        }
    }
}
