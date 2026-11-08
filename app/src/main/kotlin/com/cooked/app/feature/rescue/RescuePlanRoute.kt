package com.cooked.app.feature.rescue

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cooked.app.core.designsystem.CookedTheme

@Composable
fun RescuePlanRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RescuePlanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is RescuePlanUiState.Loading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CookedTheme.colors.primary)
            }
        }
        is RescuePlanUiState.Error -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Could not generate rescue plan", color = CookedTheme.colors.onSurface)
            }
        }
        is RescuePlanUiState.Success -> {
            RescuePlanScreen(
                plan = state.plan,
                onLockItIn = {
                    viewModel.lockItIn(onNavigateBack)
                },
                onNavigateBack = onNavigateBack,
                modifier = modifier
            )
        }
    }
}
