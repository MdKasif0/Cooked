package com.cooked.app.feature.add

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.cooked.app.core.designsystem.CookedTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QuickAddActivity : ComponentActivity() {

    private val viewModel: QuickAddViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Handle shared text (from WhatsApp, Chrome, Notes, etc.) or shortcuts
        val sharedText = when (intent.action) {
            Intent.ACTION_SEND -> {
                if (intent.type == "text/plain") {
                    intent.getStringExtra(Intent.EXTRA_TEXT)
                } else null
            }
            else -> intent.getStringExtra(EXTRA_PREFILL_TEXT)
        }

        val forceKind = intent.getStringExtra(EXTRA_FORCE_KIND)

        setContent {
            CookedTheme {
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

                LaunchedEffect(Unit) {
                    if (!sharedText.isNullOrBlank()) {
                        viewModel.onTextChanged(sharedText)
                    }
                    if (forceKind == "DEADLINE") {
                        viewModel.onSetKind(com.cooked.app.domain.model.ItemKind.DEADLINE)
                    } else if (forceKind == "REMINDER") {
                        viewModel.onSetKind(com.cooked.app.domain.model.ItemKind.REMINDER)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent)
                ) {
                    QuickAddBottomSheet(
                        sheetState = sheetState,
                        onDismissRequest = {
                            finish()
                            @Suppress("DEPRECATION")
                            overridePendingTransition(0, 0)
                        },
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    companion object {
        const val EXTRA_PREFILL_TEXT = "extra_prefill_text"
        const val EXTRA_FORCE_KIND = "extra_force_kind"
    }
}
