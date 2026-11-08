package com.cooked.app.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.BatteryAlert
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cooked.app.R
import kotlinx.coroutines.launch
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.core.designsystem.components.AvatarBubble
import com.cooked.app.feature.add.QuickAddBottomSheet
import com.cooked.app.feature.add.QuickAddViewModel
import com.cooked.app.feature.deadlines.DeadlinesRoute
import com.cooked.app.feature.detail.ReminderDetailRoute
import com.cooked.app.feature.home.TodayRoute
import com.cooked.app.feature.insights.InsightsRoute
import com.cooked.app.feature.onboarding.OnboardingRoute
import com.cooked.app.feature.paywall.PaywallRoute
import com.cooked.app.feature.settings.DesignGalleryScreen
import com.cooked.app.feature.settings.SettingsRoute
import com.cooked.app.feature.settings.SettingsViewModel
import com.cooked.app.feature.settings.reliability.ReliabilityCheckRoute
import com.cooked.app.ui.addedit.AddEditReminderScreen
import com.cooked.app.ui.addedit.AddEditReminderViewModel
import com.cooked.app.ui.oem.OemGuideScreen

data class BottomNavItem<T : Any>(
    val route: T,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookedAppShell(
    modifier: Modifier = Modifier,
    startDestination: Any = TodayTabRoute,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomNavItems = listOf(
        BottomNavItem(TodayTabRoute, "Today", Icons.Rounded.CalendarToday),
        BottomNavItem(DeadlinesTabRoute, "Deadlines", Icons.Rounded.LocalFireDepartment),
        BottomNavItem(InsightsTabRoute, "Insights", Icons.Rounded.BarChart)
    )

    val isTopLevelDestination = bottomNavItems.any { item ->
        currentDestination?.hierarchy?.any { it.hasRoute(item.route::class) } == true
    }

    var showQuickAddSheet by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val quickAddViewModel: QuickAddViewModel = hiltViewModel()
    val snackbarHostState = androidx.compose.runtime.remember { androidx.compose.material3.SnackbarHostState() }
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CookedTheme.colors.background,
        snackbarHost = {
            androidx.compose.material3.SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            if (isTopLevelDestination) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Cooked",
                            style = CookedTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = CookedTheme.colors.onBackground
                        )
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate(OemGuideNavRoute) }) {
                            Icon(
                                imageVector = Icons.Rounded.BatteryAlert,
                                contentDescription = stringResource(R.string.nav_oem_guide),
                                tint = CookedTheme.colors.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { navController.navigate(SettingsNavRoute) }) {
                            AvatarBubble(
                                size = 32.dp,
                                onClick = { navController.navigate(SettingsNavRoute) }
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = CookedTheme.colors.background
                    )
                )
            }
        },
        bottomBar = {
            if (isTopLevelDestination) {
                // Tonal navigation bar without heavy drop shadows
                NavigationBar(
                    containerColor = CookedTheme.colors.surfaceContainer,
                    tonalElevation = 0.dp
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
                                    style = CookedTheme.typography.labelSmall.copy(
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    )
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CookedTheme.colors.primary,
                                selectedTextColor = CookedTheme.colors.primary,
                                indicatorColor = CookedTheme.colors.primaryContainer,
                                unselectedIconColor = CookedTheme.colors.onSurfaceVariant,
                                unselectedTextColor = CookedTheme.colors.onSurfaceVariant
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
                    containerColor = CookedTheme.colors.primary,
                    contentColor = CookedTheme.colors.onPrimary,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
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
                startDestination = startDestination,
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
                        },
                        onNavigateToReliability = {
                            navController.navigate(ReliabilityNavRoute)
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
                        },
                        onNavigateToPaywall = {
                            navController.navigate(PaywallNavRoute())
                        }
                    )
                }

                composable<SettingsNavRoute> {
                    SettingsRoute(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToReliability = { navController.navigate(ReliabilityNavRoute) },
                        onNavigateToPaywall = { navController.navigate(PaywallNavRoute()) },
                        onNavigateToDesignGallery = { navController.navigate(DesignGalleryNavRoute) }
                    )
                }

                composable<PaywallNavRoute> {
                    PaywallRoute(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable<ReliabilityNavRoute> {
                    ReliabilityCheckRoute(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable<OnboardingNavRoute> {
                    OnboardingRoute(
                        onComplete = {
                            navController.navigate(TodayTabRoute) {
                                popUpTo(OnboardingNavRoute) { inclusive = true }
                            }
                        }
                    )
                }

                composable<DesignGalleryNavRoute> {
                    val settingsViewModel: SettingsViewModel = hiltViewModel()
                    val settingsUiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
                    DesignGalleryScreen(
                        currentThemeMode = settingsUiState.themeMode,
                        onThemeModeChange = settingsViewModel::onThemeModeSelected,
                        onNavigateBack = { navController.popBackStack() },
                        onSeedData = settingsViewModel::seedSampleData
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
            viewModel = quickAddViewModel,
            onShowSnackbar = { message, onUndo ->
                coroutineScope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = message,
                        actionLabel = "Undo",
                        duration = androidx.compose.material3.SnackbarDuration.Short
                    )
                    if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                        onUndo()
                    }
                }
            }
        )
    }
}
