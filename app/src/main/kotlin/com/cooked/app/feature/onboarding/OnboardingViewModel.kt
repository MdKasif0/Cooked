package com.cooked.app.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cooked.app.data.dao.ItemEventDao
import com.cooked.app.data.datastore.UserPreferencesRepository
import com.cooked.app.data.entity.ItemEventEntity
import com.cooked.app.domain.model.ItemEventType
import com.cooked.app.domain.model.Personality
import com.cooked.app.domain.usecase.CreateItemFromExternal
import com.cooked.app.scheduling.battery.ReliabilityChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val createItemFromExternal: CreateItemFromExternal,
    private val reliabilityChecker: ReliabilityChecker,
    private val itemEventDao: ItemEventDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        refreshPermissions()
    }

    fun onNext() {
        val current = _uiState.value.currentStep
        logStepCompleted(current)

        if (current >= _uiState.value.totalSteps - 1) {
            completeOnboarding()
        } else {
            _uiState.update { it.copy(currentStep = current + 1) }
            refreshPermissions()
        }
    }

    fun onSkip() {
        logStepCompleted(-1) // skipped
        completeOnboarding()
    }

    fun onPersonalitySelected(personality: Personality) {
        viewModelScope.launch {
            userPreferencesRepository.setPersonality(personality)
            _uiState.update { it.copy(selectedPersonality = personality) }
        }
    }

    fun onFirstWinInputChanged(text: String) {
        _uiState.update { it.copy(firstWinInput = text) }
    }

    fun saveFirstWin(text: String) {
        viewModelScope.launch {
            val result = createItemFromExternal(text)
            if (result.isSuccess) {
                val item = result.getOrNull()
                _uiState.update {
                    it.copy(
                        firstWinCreated = true,
                        createdItem = item,
                        firstWinInput = text
                    )
                }
            }
        }
    }

    fun refreshPermissions() {
        val report = reliabilityChecker.check()
        _uiState.update {
            it.copy(
                notificationsGranted = report.notificationPermissionGranted,
                exactAlarmsGranted = report.canScheduleExactAlarms,
                batteryUnrestricted = report.isIgnoringBatteryOptimizations
            )
        }
    }

    private fun completeOnboarding() {
        viewModelScope.launch {
            userPreferencesRepository.setOnboardingDone(true)
            _uiState.update { it.copy(isComplete = true) }
        }
    }

    private fun logStepCompleted(step: Int) {
        viewModelScope.launch {
            try {
                itemEventDao.insertEvent(
                    ItemEventEntity(
                        id = UUID.randomUUID().toString(),
                        itemId = "onboarding_flow",
                        type = ItemEventType.CREATED,
                        at = Instant.now(),
                        meta = mapOf("onboarding_step" to step.toString())
                    )
                )
            } catch (_: Exception) {}
        }
    }
}
