package com.cooked.app.ui.navigation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.cooked.app.core.designsystem.components.CircleIconButton
import com.cooked.app.core.designsystem.components.FloatingNavBar
import com.cooked.app.core.designsystem.components.FloatingNavItem
import com.cooked.app.core.designsystem.components.NavCenterAction
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
import com.cooked.app.feature.cook.CookSessionRoute
import com.cooked.app.feature.deadlines.DeadlinesRoute
import com.cooked.app.feature.detail.ReminderDetailRoute
import com.cooked.app.feature.home.TodayRoute
import com.cooked.app.feature.insights.InsightsRoute
import com.cooked.app.feature.onboarding.OnboardingRoute
import com.cooked.app.feature.paywall.PaywallRoute
import com.cooked.app.feature.rescue.RescuePlanRoute
import com.cooked.app.feature.scan.DatesheetReviewRoute
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
    deepLink: DeepLink? = null,
    onDeepLinkHandled: () -> Unit = {},
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val tabs = listOf(
        ShellTab(TodayTabRoute, FloatingNavItem("today", Icons.Rounded.Home, "Today"), "Your day, handled"),
        ShellTab(DeadlinesTabRoute, FloatingNavItem("deadlines", Icons.Rounded.LocalFireDepartment, "Deadlines"), "Pressure board"),
        ShellTab(InsightsTabRoute, FloatingNavItem("insights", Icons.Rounded.BarChart, "Insights"), "Your rhythm"),
        ShellTab(SettingsNavRoute, FloatingNavItem("you", Icons.Rounded.PersonOutline, "You"), "Preferences")
    )

    val currentTab = tabs.firstOrNull { tab ->
        currentDestination?.hierarchy?.any { it.hasRoute(tab.route::class) } == true
    }
    val isTopLevelDestination = currentTab != null
    // Settings draws its own title bar; the brand header belongs to the three content tabs.
    val showBrandHeader = currentTab != null && currentTab.route != SettingsNavRoute

    var showQuickAddSheet by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val quickAddViewModel: QuickAddViewModel = hiltViewModel()
    val snackbarHostState = androidx.compose.runtime.remember { androidx.compose.material3.SnackbarHostState() }
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    // Route notification taps, shortcuts, the weekly report and shared images.
    // Today is always the root of the back stack (start destination, or what onboarding
    // navigates to), so tab switches pop back to it and Back from a deep link lands there.
    androidx.compose.runtime.LaunchedEffect(deepLink) {
        val link = deepLink ?: return@LaunchedEffect
        when (link) {
            is DeepLink.ItemDetail -> navController.navigate(ReminderDetailNavRoute(reminderId = link.itemId)) {
                launchSingleTop = true
            }
            is DeepLink.SharedImage -> navController.navigate(DatesheetReviewNavRoute(imageUri = link.uri))
            DeepLink.Insights -> navController.navigate(InsightsTabRoute) {
                popUpTo<TodayTabRoute> { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
            DeepLink.Today -> navController.navigate(TodayTabRoute) {
                popUpTo<TodayTabRoute> { inclusive = false }
                launchSingleTop = true
            }
        }
        onDeepLinkHandled()
    }

    val scanLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            showQuickAddSheet = false
            navController.navigate(DatesheetReviewNavRoute(imageUri = it.toString()))
        }
    }

    fun navigateToTab(route: Any) {
        navController.navigate(route) {
            popUpTo<TodayTabRoute> { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CookedTheme.colors.background,
        snackbarHost = {
            androidx.compose.material3.SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = if (isTopLevelDestination) 96.dp else 0.dp)
            )
        },
        topBar = {
            AnimatedVisibility(
                visible = showBrandHeader,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                BrandHeader(
                    subtitle = currentTab?.subtitle ?: "",
                    onReliabilityClick = { navController.navigate(OemGuideNavRoute) },
                    onProfileClick = { navigateToTab(SettingsNavRoute) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.fillMaxSize(),
                enterTransition = {
                    if (isTabSwitch(initialState.destination, targetState.destination, tabs)) fadeIn(tween(220))
                    else slideInHorizontally(initialOffsetX = { it / 3 }) + fadeIn(tween(260))
                },
                exitTransition = {
                    if (isTabSwitch(initialState.destination, targetState.destination, tabs)) fadeOut(tween(180))
                    else slideOutHorizontally(targetOffsetX = { -it / 6 }) + fadeOut(tween(200))
                },
                popEnterTransition = {
                    if (isTabSwitch(initialState.destination, targetState.destination, tabs)) fadeIn(tween(220))
                    else slideInHorizontally(initialOffsetX = { -it / 6 }) + fadeIn(tween(260))
                },
                popExitTransition = {
                    if (isTabSwitch(initialState.destination, targetState.destination, tabs)) fadeOut(tween(180))
                    else slideOutHorizontally(targetOffsetX = { it / 3 }) + fadeOut(tween(200))
                }
            ) {
                composable<TodayTabRoute> {
                    TodayRoute(
                        onReminderClick = { id ->
                            navController.navigate(ReminderDetailNavRoute(reminderId = id))
                        },
                        onCookedScoreClick = {
                            navController.navigate(DeadlinesTabRoute) {
                                popUpTo<TodayTabRoute> {
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
                        },
                        onScanDatesheetClick = {
                            scanLauncher.launch("image/*")
                        },
                        onTriggerRescuePlan = { id ->
                            navController.navigate(RescuePlanNavRoute(targetItemId = id))
                        }
                    )
                }

                composable<InsightsTabRoute> {
                    InsightsRoute(
                        onCookedScoreClick = {
                            navController.navigate(DeadlinesTabRoute) {
                                popUpTo<TodayTabRoute> {
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
                        onNavigateBack = null,
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
                        },
                        onStartCookSession = { id ->
                            navController.navigate(CookSessionNavRoute(itemId = id))
                        },
                        onTriggerRescuePlan = { id ->
                            navController.navigate(RescuePlanNavRoute(targetItemId = id))
                        }
                    )
                }

                composable<OemGuideNavRoute> {
                    OemGuideScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable<DatesheetReviewNavRoute> {
                    DatesheetReviewRoute(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable<CookSessionNavRoute> {
                    CookSessionRoute(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable<RescuePlanNavRoute> {
                    RescuePlanRoute(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }

            // Floating ink pill navigation with neon quick-add in the center
            AnimatedVisibility(
                visible = isTopLevelDestination,
                enter = fadeIn(tween(200)) + androidx.compose.animation.slideInVertically { it / 2 },
                exit = fadeOut(tween(150)) + androidx.compose.animation.slideOutVertically { it / 2 },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, CookedTheme.colors.background.copy(alpha = 0.92f))
                            )
                        )
                        .navigationBarsPadding()
                        .padding(horizontal = 28.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    FloatingNavBar(
                        items = tabs.map { it.item },
                        selectedKey = currentTab?.item?.key,
                        onItemSelected = { item ->
                            tabs.firstOrNull { it.item.key == item.key }?.let { navigateToTab(it.route) }
                        },
                        centerAction = {
                            NavCenterAction(
                                icon = Icons.Rounded.Add,
                                contentDescription = stringResource(R.string.home_fab_add),
                                onClick = { showQuickAddSheet = true }
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
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
            onLaunchScan = {
                showQuickAddSheet = false
                scanLauncher.launch("image/*")
            },
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


private data class ShellTab(
    val route: Any,
    val item: FloatingNavItem,
    val subtitle: String
)

private fun isTabSwitch(
    from: androidx.navigation.NavDestination,
    to: androidx.navigation.NavDestination,
    tabs: List<ShellTab>
): Boolean {
    fun isTab(d: androidx.navigation.NavDestination) = tabs.any { tab -> d.hasRoute(tab.route::class) }
    return isTab(from) && isTab(to)
}

/** Brand row in the spirit of the references: mark + name + context line, round actions on the right. */
@Composable
private fun BrandHeader(
    subtitle: String,
    onReliabilityClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CookedTheme.colors.background)
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        com.cooked.app.core.designsystem.components.CookedAppLogo(size = 44.dp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Cooked",
                style = CookedTheme.typography.titleLarge,
                color = CookedTheme.colors.onBackground
            )
            androidx.compose.animation.AnimatedContent(
                targetState = subtitle,
                transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
                label = "brandSubtitle"
            ) { text ->
                Text(
                    text = text,
                    style = CookedTheme.typography.labelMedium,
                    color = CookedTheme.colors.onSurfaceVariant
                )
            }
        }
        CircleIconButton(
            icon = Icons.Rounded.BatteryAlert,
            contentDescription = stringResource(R.string.nav_oem_guide),
            onClick = onReliabilityClick
        )
        Spacer(Modifier.width(10.dp))
        AvatarBubble(size = 44.dp, onClick = onProfileClick)
    }
}
