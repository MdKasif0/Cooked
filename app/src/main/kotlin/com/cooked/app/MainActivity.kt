package com.cooked.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.core.designsystem.ThemeMode
import com.cooked.app.data.datastore.UserPreferencesRepository
import com.cooked.app.ui.navigation.CookedAppShell
import com.cooked.app.ui.navigation.DeepLink
import com.cooked.app.ui.navigation.OnboardingNavRoute
import com.cooked.app.ui.navigation.TodayTabRoute
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    /** Destination requested by the launching / latest intent, consumed by the nav shell. */
    private val pendingDeepLink = MutableStateFlow<DeepLink?>(null)

    @Volatile
    private var prefsLoaded = false

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Keep the splash up until prefs load, so first launch never flashes Today before Onboarding.
        installSplashScreen().setKeepOnScreenCondition { !prefsLoaded }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        checkNotificationPermission()

        // On recreation (rotation etc.) the launching intent was already handled.
        if (savedInstanceState == null) {
            pendingDeepLink.value = DeepLink.fromIntent(intent)
        }

        setContent {
            val userPrefs by userPreferencesRepository.userPrefs
                .collectAsStateWithLifecycle(initialValue = null)
            val deepLink by pendingDeepLink.collectAsStateWithLifecycle()

            LaunchedEffect(userPrefs != null) {
                if (userPrefs != null) prefsLoaded = true
            }

            val prefs = userPrefs ?: return@setContent

            val darkTheme = when (prefs.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            CookedTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CookedTheme.colors.background
                ) {
                    // Decided once per activity; changing a NavHost's start destination later
                    // would rebuild the graph and wipe the back stack.
                    val startDestination: Any = remember {
                        if (!prefs.onboardingDone) OnboardingNavRoute else TodayTabRoute
                    }

                    CookedAppShell(
                        startDestination = startDestination,
                        // Hold deep links until onboarding is finished.
                        deepLink = if (prefs.onboardingDone) deepLink else null,
                        onDeepLinkHandled = { pendingDeepLink.value = null }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        DeepLink.fromIntent(intent)?.let { pendingDeepLink.value = it }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(permission)
            }
        }
    }
}
