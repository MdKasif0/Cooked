package com.cooked.app.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.cooked.app.R
import com.cooked.app.core.designsystem.FlameOrange
import com.cooked.app.feature.add.QuickAddBottomSheet
import com.cooked.app.feature.add.QuickAddViewModel
import com.cooked.app.feature.deadlines.DeadlinesRoute
import com.cooked.app.feature.detail.ReminderDetailRoute
import com.cooked.app.feature.home.TodayRoute
import com.cooked.app.feature.insights.InsightsRoute
import com.cooked.app.feature.settings.SettingsRoute
import com.cooked.app.ui.addedit.AddEditReminderScreen
import com.cooked.app.ui.addedit.AddEditReminderViewModel
import com.cooked.app.ui.oem.OemGuideScreen
import kotlinx.coroutines.launch

data class BottomNavItem<T : Any>(
    val route: T,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookedAppShell(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomNavItems = listOf(
        BottomNavItem(TodayTabRoute, "Today", Icons.Default.CalendarToday),
        BottomNavItem(DeadlinesTabRoute, "Deadlines", Icons.Default.LocalFireDepartment),
        BottomNavItem(InsightsTabRoute, "Insights", Icons.Default.BarChart)
    )

    val isTopLevelDestination = bottomNavItems.any { item ->
        currentDestination?.hierarchy?.any { it.hasRoute(item.route::class) } == true
    }

    var showQuickAddSheet by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val quickAddViewModel: QuickAddViewModel = hiltViewModel()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (isTopLevelDestination) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Cooked 🔥",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate(OemGuideNavRoute) }) {
                            Icon(
                                imageVector = Icons.Default.BatteryAlert,
                                contentDescription = stringResource(R.string.nav_oem_guide),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { navController.navigate(SettingsNavRoute) }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = stringResource(R.string.cd_settings),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        bottomBar = {
            if (isTopLevelDestination) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.hasRoute(item.route::class) } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                ),
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = FlameOrange,
                                selectedTextColor = FlameOrange,
                                indicatorColor = FlameOrange.copy(alpha = 0.15f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (isTopLevelDestination) {
                FloatingActionButton(
                    onClick = { showQuickAddSheet = true },
                    containerColor = FlameOrange,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.home_fab_add),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = TodayTabRoute,
                modifier = Modifier.fillMaxSize(),
                enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it / 4 }) + fadeOut() },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it / 4 }) + fadeIn() },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
            ) {
                composable<TodayTabRoute> {
                    TodayRoute(
                        onReminderClick = { id ->
                            navController.navigate(ReminderDetailNavRoute(reminderId = id))
                        },
                        onCookedScoreClick = {
                            navController.navigate(DeadlinesTabRoute) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }

                composable<DeadlinesTabRoute> {
                    DeadlinesRoute(
                        onDeadlineClick = { id ->
                            navController.navigate(ReminderDetailNavRoute(reminderId = id))
                        }
                    )
                }

                composable<InsightsTabRoute> {
                    InsightsRoute(
                        onCookedScoreClick = {
                            navController.navigate(DeadlinesTabRoute) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }

                composable<SettingsNavRoute> {
                    SettingsRoute(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToOemGuide = { navController.navigate(OemGuideNavRoute) }
                    )
                }

                composable<AddEditReminderRoute> {
                    val viewModel: AddEditReminderViewModel = hiltViewModel()
                    AddEditReminderScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable<ReminderDetailNavRoute> {
                    ReminderDetailRoute(
                        onNavigateBack = { navController.popBackStack() },
                        onEditClick = { id ->
                            navController.navigate(AddEditReminderRoute(reminderId = id))
                        }
                    )
                }

                composable<OemGuideNavRoute> {
                    OemGuideScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }

    if (showQuickAddSheet) {
        QuickAddBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { showQuickAddSheet = false },
            viewModel = quickAddViewModel
        )
    }
}
