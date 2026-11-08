package com.cooked.app.feature.cook

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cooked.app.core.designsystem.CookedTheme

@Composable
fun CookSessionRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CookSessionViewModel = hiltViewModel()
) {
    val item by viewModel.item.collectAsStateWithLifecycle()

    if (item == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = CookedTheme.colors.primary)
        }
    } else {
        CookSessionScreen(
            item = item!!,
            onFinishSession = { sessionMinutes, newProgress ->
                viewModel.finishSession(sessionMinutes, newProgress, onNavigateBack)
            },
            onNavigateBack = onNavigateBack,
            modifier = modifier
        )
    }
}

