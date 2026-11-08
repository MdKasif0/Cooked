package com.cooked.app.feature.snitch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.cooked.app.core.designsystem.CookedTheme
import com.cooked.app.core.telemetry.Telemetry
import com.cooked.app.data.datastore.UserPreferencesRepository
import com.cooked.app.domain.model.Item
import com.cooked.app.domain.model.ItemEventType
import com.cooked.app.domain.repository.ItemRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SnitchActivity : ComponentActivity() {

    @Inject lateinit var itemRepository: ItemRepository
    @Inject lateinit var userPreferencesRepository: UserPreferencesRepository
    @Inject lateinit var telemetry: Telemetry

    companion object {
        const val EXTRA_ITEM_ID = "extra_item_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val itemId = intent.getStringExtra(EXTRA_ITEM_ID)
        if (itemId.isNullOrBlank()) {
            finish()
            return
        }

        lifecycleScope.launch {
            val item = itemRepository.getItem(itemId)
            if (item == null) {
                finish()
                return@launch
            }

            val prefs = userPreferencesRepository.userPrefs.first()
            telemetry.logSnitchOffered()
            itemRepository.logEvent(item.id, ItemEventType.SNITCH_OFFERED)

            setContent {
                CookedTheme(darkTheme = androidx.compose.foundation.isSystemInDarkTheme()) {
                    SnitchBottomSheet(
                        item = item,
                        snoozeCount = item.snoozeCount.coerceAtLeast(prefs.snitchSnoozeThreshold),
                        personality = prefs.personality,
                        contactName = prefs.snitchContactName,
                        preferredApp = prefs.snitchPreferredApp,
                        onDismiss = { finish() },
                        onSnitchSent = { app ->
                            lifecycleScope.launch {
                                telemetry.logSnitchShared(app)
                                itemRepository.logEvent(item.id, ItemEventType.SNITCH_SENT, mapOf("app" to app))
                            }
                        }
                    )
                }
            }
        }
    }
}
