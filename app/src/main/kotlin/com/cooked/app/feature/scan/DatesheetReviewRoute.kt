package com.cooked.app.feature.scan

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.core.designsystem.components.PrimaryButton

@Composable
fun DatesheetReviewRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DatesheetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is DatesheetUiState.Scanning -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = CookedTheme.colors.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Analyzing datesheet & timetable...",
                        style = CookedTheme.typography.bodyLarge,
                        color = CookedTheme.colors.onSurface
                    )
                }
            }
        }
        is DatesheetUiState.Error -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.message,
                        style = CookedTheme.typography.bodyLarge,
                        color = CookedTheme.colors.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    PrimaryButton(
                        text = "Go Back",
                        onClick = onNavigateBack
                    )
                }
            }
        }
        is DatesheetUiState.Ready -> {
            DatesheetReviewScreen(
                candidates = state.candidates,
                onConfirmAdd = { selected ->
                    viewModel.confirmAdd(selected, onNavigateBack)
                },
                onNavigateBack = onNavigateBack,
                modifier = modifier
            )
        }
    }
}
