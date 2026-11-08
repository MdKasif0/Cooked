package com.cooked.app.feature.settings.reliability

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cooked.app.data.dao.ItemDao
import com.cooked.app.data.dao.ItemEventDao
import com.cooked.app.domain.model.Item
import com.cooked.app.domain.model.ItemEventType
import com.cooked.app.domain.model.ItemKind
import com.cooked.app.domain.model.ItemStatus
import com.cooked.app.domain.model.NagPolicy
import com.cooked.app.domain.repository.ItemRepository
import com.cooked.app.scheduling.alarm.AlarmScheduler
import com.cooked.app.scheduling.battery.ReliabilityChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class ReliabilityViewModel @Inject constructor(
    private val reliabilityChecker: ReliabilityChecker,
    private val itemRepository: ItemRepository,
    private val itemEventDao: ItemEventDao,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReliabilityUiState())
    val uiState: StateFlow<ReliabilityUiState> = _uiState.asStateFlow()

    companion object {
        const val TEST_ITEM_ID = "test_alarm_reliability_check"
    }

    init {
        refreshChecks()
    }

    fun refreshChecks() {
        val report = reliabilityChecker.check()
        _uiState.update { it.copy(report = report) }
        checkTestAlarmResult()
    }

    fun schedule60SecondTest() {
        viewModelScope.launch {
            val now = Instant.now()
            val targetTime = now.plus(Duration.ofSeconds(60))

            val testItem = Item(
                id = TEST_ITEM_ID,
                kind = ItemKind.REMINDER,
                title = "Reliability Test",
                note = "If you see this, your phone didn't kill Cooked in the background!",
                emoji = "lightning",
                dueAt = targetTime,
                allDay = false,
                critical = true,
                nag = NagPolicy(intervalMinutes = 5, escalate = false, maxNags = 1),
                status = ItemStatus.ACTIVE,
                createdAt = now,
                updatedAt = now
            )

            itemRepository.create(testItem)
            alarmScheduler.schedule(testItem)

            _uiState.update {
                it.copy(
                    isTestingAlarm = true,
                    testScheduledAtEpochMillis = targetTime.toEpochMilli(),
                    testResultMessage = null
                )
            }
        }
    }

    private fun checkTestAlarmResult() {
        viewModelScope.launch {
            val firedEvent = itemEventDao.getLatestEventFor(TEST_ITEM_ID, ItemEventType.FIRED)
            val scheduledAt = _uiState.value.testScheduledAtEpochMillis

            if (firedEvent != null && scheduledAt != null) {
                val delaySeconds = (firedEvent.at.toEpochMilli() - scheduledAt) / 1000
                val message = if (delaySeconds <= 5) {
                    "Test alarm fired on time (delay: ${delaySeconds}s). Cooked is safe on your device!"
                } else {
                    "Alarm fired, but was ${delaySeconds}s late. Background battery optimizations may delay reminders."
                }
                _uiState.update {
                    it.copy(
                        isTestingAlarm = false,
                        testResultMessage = message
                    )
                }
            }
        }
    }
}

