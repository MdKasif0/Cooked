package com.cooked.app.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.cooked.app.ui.addedit.AddEditReminderScreen
import com.cooked.app.ui.addedit.AddEditReminderViewModel
import com.cooked.app.ui.deadline.DeadlineDashboardScreen
import com.cooked.app.ui.deadline.DeadlineDashboardViewModel
import com.cooked.app.ui.home.HomeScreen
import com.cooked.app.ui.home.HomeViewModel
import com.cooked.app.ui.oem.OemGuideScreen

@Composable
fun CookedNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: Any = HomeRoute
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it / 4 }) + fadeOut() },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it / 4 }) + fadeIn() },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
    ) {
        composable<HomeRoute> {
            val viewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel = viewModel,
                onNavigateToAddReminder = {
                    navController.navigate(AddEditReminderRoute())
                },
                onNavigateToEditReminder = { reminderId ->
                    navController.navigate(AddEditReminderRoute(reminderId = reminderId))
                },
                onNavigateToDeadlines = {
                    navController.navigate(DeadlineDashboardRoute)
                },
                onNavigateToOemGuide = {
                    navController.navigate(OemGuideRoute)
                }
            )
        }

        composable<AddEditReminderRoute> {
            val viewModel: AddEditReminderViewModel = hiltViewModel()
            AddEditReminderScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<DeadlineDashboardRoute> {
            val viewModel: DeadlineDashboardViewModel = hiltViewModel()
            DeadlineDashboardScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditReminder = { reminderId ->
                    navController.navigate(AddEditReminderRoute(reminderId = reminderId))
                }
            )
        }

        composable<OemGuideRoute> {
            OemGuideScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

