package com.cooked.app.feature.cook

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cooked.app.core.telemetry.Telemetry
import com.cooked.app.domain.model.Item
import com.cooked.app.domain.model.ItemEventType
import com.cooked.app.domain.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class CookSessionViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val telemetry: Telemetry,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val itemId: String = checkNotNull(savedStateHandle["itemId"])

    val item: StateFlow<Item?> = itemRepository.observeItem(itemId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun finishSession(sessionMinutes: Int, newProgress: Int, onComplete: () -> Unit) {
        viewModelScope.launch {
            val currentItem = itemRepository.getItem(itemId)
            if (currentItem != null) {
                val updated = currentItem.copy(
                    progress = newProgress,
                    updatedAt = Instant.now()
                )
                itemRepository.update(updated)
                itemRepository.logEvent(
                    itemId = itemId,
                    type = ItemEventType.PROGRESS,
                    meta = mapOf(
                        "sessionMinutes" to sessionMinutes.toString(),
                        "progress" to newProgress.toString()
                    )
                )
                telemetry.cookSessionCompleted(sessionMinutes)
            }
            onComplete()
        }
    }
}

