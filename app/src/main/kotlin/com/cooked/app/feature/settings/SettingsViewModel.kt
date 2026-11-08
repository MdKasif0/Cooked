package com.cooked.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cooked.app.core.designsystem.ThemeMode
import com.cooked.app.core.telemetry.Telemetry
import com.cooked.app.core.sound.SoundManager
import com.cooked.app.core.util.AppIconStyle
import com.cooked.app.core.util.IconManager
import com.cooked.app.core.util.SampleDataSeeder
import com.cooked.app.data.datastore.UserPreferencesRepository
import com.cooked.app.domain.backup.BackupManager
import com.cooked.app.domain.backup.ImportPreview
import com.cooked.app.domain.billing.BillingIssueState
import com.cooked.app.domain.billing.EntitlementRepository
import com.cooked.app.domain.model.Personality
import com.cooked.app.scheduling.alarm.AlarmScheduler
import com.cooked.app.scheduling.battery.ReliabilityChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val backupManager: BackupManager,
    private val alarmScheduler: AlarmScheduler,
    private val reliabilityChecker: ReliabilityChecker,
    private val sampleDataSeeder: SampleDataSeeder,
    private val entitlementRepository: EntitlementRepository,
    private val telemetry: Telemetry
) : ViewModel() {

    private val _alarmsRefreshTrigger = MutableStateFlow(0)
    private val _importPreview = MutableStateFlow<ImportPreview?>(null)
    private val _showDeleteConfirm = MutableStateFlow(false)
    private val _showDeleteDoubleConfirm = MutableStateFlow(false)
    private val _showPrivacyDialog = MutableStateFlow(false)
    private val _snackbarMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesRepository.userPrefs,
        entitlementRepository.billingIssue,
        _importPreview,
        _showDeleteConfirm,
        _showDeleteDoubleConfirm,
        _showPrivacyDialog,
        _snackbarMessage,
        _alarmsRefreshTrigger
    ) { args ->
        val prefs = args[0] as com.cooked.app.data.datastore.UserPrefs
        val billingIssue = args[1] as BillingIssueState?
        val preview = args[2] as ImportPreview?
        val showDelete = args[3] as Boolean
        val showDeleteDouble = args[4] as Boolean
        val showPrivacy = args[5] as Boolean
        val snackbar = args[6] as String?

        val alarms = alarmScheduler.getScheduledAlarms()
        val report = reliabilityChecker.check()

        SettingsUiState(
            themeMode = prefs.themeMode,
            dynamicColor = prefs.dynamicColor,
            defaultNagIntervalMinutes = prefs.defaultNagInterval,
            quietHoursStart = prefs.quietHoursStart,
            quietHoursEnd = prefs.quietHoursEnd,
            personality = prefs.personality,
            dailyCapacityMinutes = prefs.dailyCapacityMinutes,
            preferredNudgeTime = prefs.preferredNudgeTime,
            weekStartsOn = prefs.weekStartsOn,
            isPro = prefs.isPro,
            billingIssue = billingIssue,
            telemetryEnabled = prefs.telemetryEnabled,
            appVersion = "1.0.0",
            scheduledAlarms = alarms,
            reliabilityReport = report,
            importPreview = preview,
            showDeleteConfirmDialog = showDelete,
            showDeleteDoubleConfirmDialog = showDeleteDouble,
            showPrivacyDialog = showPrivacy,
            snackbarMessage = snackbar
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun onThemeModeSelected(mode: ThemeMode) {
        viewModelScope.launch { preferencesRepository.setThemeMode(mode) }
    }

    fun onDynamicColorToggled(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setDynamicColor(enabled) }
    }

    fun onNagIntervalSelected(minutes: Int) {
        viewModelScope.launch { preferencesRepository.setDefaultNagIntervalMinutes(minutes) }
    }

    fun onPersonalitySelected(personality: Personality) {
        viewModelScope.launch { preferencesRepository.setPersonality(personality) }
    }

    fun onQuietHoursChanged(start: LocalTime, end: LocalTime) {
        viewModelScope.launch { preferencesRepository.setQuietHours(start, end) }
    }

    fun onDailyCapacityChanged(minutes: Int) {
        viewModelScope.launch { preferencesRepository.setDailyCapacityMinutes(minutes) }
    }

    fun onPreferredNudgeTimeChanged(time: LocalTime) {
        viewModelScope.launch { preferencesRepository.setPreferredNudgeTime(time) }
    }

    fun onWeekStartsOnChanged(dayOfWeek: DayOfWeek) {
        viewModelScope.launch { preferencesRepository.setWeekStartsOn(dayOfWeek) }
    }

    fun exportBackup(onReady: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val json = backupManager.createBackupJson()
                onReady(json)
                _snackbarMessage.value = "Backup created successfully."
            } catch (e: Exception) {
                _snackbarMessage.value = "Failed to export: ${e.localizedMessage}"
            }
        }
    }

    fun onImportContentReceived(jsonString: String) {
        val result = backupManager.previewImport(jsonString)
        if (result.isSuccess) {
            _importPreview.value = result.getOrNull()
        } else {
            _snackbarMessage.value = "Invalid backup file: ${result.exceptionOrNull()?.localizedMessage}"
        }
    }

    fun confirmImport() {
        val preview = _importPreview.value ?: return
        viewModelScope.launch {
            try {
                val count = backupManager.executeImport(preview.backupData)
                _importPreview.value = null
                _snackbarMessage.value = "Imported $count items successfully."
                refreshAlarms()
            } catch (e: Exception) {
                _snackbarMessage.value = "Import failed: ${e.localizedMessage}"
            }
        }
    }

    fun dismissImportPreview() {
        _importPreview.value = null
    }

    fun requestDeleteAllData() {
        _showDeleteConfirm.value = true
    }

    fun confirmFirstDelete() {
        _showDeleteConfirm.value = false
        _showDeleteDoubleConfirm.value = true
    }

    fun confirmFinalDelete() {
        viewModelScope.launch {
            backupManager.deleteAllData()
            _showDeleteDoubleConfirm.value = false
            _snackbarMessage.value = "All data permanently deleted."
            refreshAlarms()
        }
    }

    fun dismissDeleteDialogs() {
        _showDeleteConfirm.value = false
        _showDeleteDoubleConfirm.value = false
    }

    fun setPrivacyDialogVisible(visible: Boolean) {
        _showPrivacyDialog.value = visible
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun onTelemetryToggled(enabled: Boolean) {
        viewModelScope.launch {
            telemetry.setOptIn(enabled)
        }
    }

    fun onToggleProForTesting(enabled: Boolean) {
        viewModelScope.launch {
            entitlementRepository.setLocalProForTesting(enabled)
        }
    }

    fun seedSampleData() {
        viewModelScope.launch {
            sampleDataSeeder.seedSampleData()
            refreshAlarms()
        }
    }

    fun refreshAlarms() {
        _alarmsRefreshTrigger.update { it + 1 }
    }
}
