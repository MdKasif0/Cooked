package com.cooked.app.feature.scan

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cooked.app.core.telemetry.Telemetry
import com.cooked.app.domain.model.Item
import com.cooked.app.domain.model.ItemKind
import com.cooked.app.domain.repository.ItemRepository
import com.cooked.app.domain.scan.DatesheetScanner
import com.cooked.app.domain.scan.ScannedDeadlineCandidate
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

sealed interface DatesheetUiState {
    object Scanning : DatesheetUiState
    data class Ready(val candidates: List<ScannedDeadlineCandidate>) : DatesheetUiState
    data class Error(val message: String) : DatesheetUiState
}

@HiltViewModel
class DatesheetViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val scanner: DatesheetScanner,
    private val itemRepository: ItemRepository,
    private val telemetry: Telemetry,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val imageUriString: String? = savedStateHandle["imageUri"]
    private val _uiState = MutableStateFlow<DatesheetUiState>(DatesheetUiState.Scanning)
    val uiState: StateFlow<DatesheetUiState> = _uiState.asStateFlow()

    init {
        telemetry.logScanStarted()
        if (!imageUriString.isNullOrBlank()) {
            scanUri(Uri.parse(imageUriString))
        } else {
            _uiState.value = DatesheetUiState.Error("No image provided for scan")
        }
    }

    private fun scanUri(uri: Uri) {
        viewModelScope.launch {
            try {
                val candidates = scanner.scanImageUri(context, uri)
                if (candidates.isEmpty()) {
                    _uiState.value = DatesheetUiState.Error("No deadlines detected in image. Please try a clearer picture.")
                } else {
                    _uiState.value = DatesheetUiState.Ready(candidates)
                }
            } catch (e: Exception) {
                _uiState.value = DatesheetUiState.Error("Scanning failed: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }

    fun confirmAdd(selected: List<ScannedDeadlineCandidate>, onComplete: () -> Unit) {
        viewModelScope.launch {
            selected.forEach { candidate ->
                val item = Item(
                    id = UUID.randomUUID().toString(),
                    title = candidate.title,
                    dueAt = candidate.dueAt,
                    allDay = false,
                    kind = ItemKind.DEADLINE,
                    effortMinutes = candidate.effortMinutes,
                    createdAt = Instant.now(),
                    updatedAt = Instant.now()
                )
                itemRepository.create(item)
            }
            telemetry.logScanItemsAdded(selected.size)
            onComplete()
        }
    }
}
