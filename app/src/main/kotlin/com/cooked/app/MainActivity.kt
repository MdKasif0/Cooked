package com.cooked.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.core.designsystem.ThemeMode
import com.cooked.app.data.datastore.UserPreferencesRepository
import com.cooked.app.ui.navigation.CookedAppShell
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        checkNotificationPermission()

        setContent {
            val userPrefs by userPreferencesRepository.userPrefs
                .collectAsStateWithLifecycle(initialValue = null)

            val themeMode = userPrefs?.themeMode ?: ThemeMode.SYSTEM
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            CookedTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CookedTheme.colors.background
                ) {
                    val isOnboarding = userPrefs != null && !userPrefs!!.onboardingDone
                    val sharedImageUri = getSharedImageUri(intent)
                    val startDestination: Any = if (isOnboarding) {
                        com.cooked.app.ui.navigation.OnboardingNavRoute
                    } else if (sharedImageUri != null) {
                        com.cooked.app.ui.navigation.DatesheetReviewNavRoute(imageUri = sharedImageUri.toString())
                    } else {
                        com.cooked.app.ui.navigation.TodayTabRoute
                    }

                    CookedAppShell(
                        startDestination = startDestination
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun getSharedImageUri(intent: Intent?): Uri? {
        if (intent?.action == Intent.ACTION_SEND && intent.type?.startsWith("image/") == true) {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(Intent.EXTRA_STREAM)
            }
        }
        return null
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
