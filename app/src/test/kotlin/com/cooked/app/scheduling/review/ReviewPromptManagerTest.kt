package com.cooked.app.scheduling.review

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.cooked.app.core.designsystem.HeatLevel
import com.cooked.app.data.datastore.UserPreferencesRepository
import com.cooked.app.domain.model.Item
import com.cooked.app.domain.model.ItemKind
import com.cooked.app.domain.model.ItemStatus
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ReviewPromptManagerTest {

    private lateinit var context: Context
    private lateinit var userPrefsRepo: UserPreferencesRepository
    private lateinit var reviewPromptManager: ReviewPromptManager

    private val sampleItem = Item(
        id = "review-test-item",
        kind = ItemKind.DEADLINE,
        title = "Chemistry Exam",
        dueAt = Instant.now(),
        status = ItemStatus.DONE,
        createdAt = Instant.now(),
        updatedAt = Instant.now()
    )

    @Before
    fun setup() = runTest {
        context = ApplicationProvider.getApplicationContext()
        userPrefsRepo = UserPreferencesRepository(context)
        userPrefsRepo.resetForTesting()
        reviewPromptManager = ReviewPromptManager(userPrefsRepo)
    }

    @Test
    fun lateCompletion_doesNotIncrementOrTriggerReview() = runTest {
        val triggered = reviewPromptManager.onItemCompleted(
            item = sampleItem,
            onTime = false,
            heatLevel = HeatLevel.COOKED,
            activity = null
        )

        assertThat(triggered).isFalse()
        assertThat(userPrefsRepo.userPrefs.first().toastyCompletionsCount).isEqualTo(0)
    }

    @Test
    fun chillItemCompletion_doesNotIncrementOrTriggerReview() = runTest {
        val triggered = reviewPromptManager.onItemCompleted(
            item = sampleItem,
            onTime = true,
            heatLevel = HeatLevel.CHILL,
            activity = null
        )

        assertThat(triggered).isFalse()
        assertThat(userPrefsRepo.userPrefs.first().toastyCompletionsCount).isEqualTo(0)
    }

    @Test
    fun onTimeToastyItem_incrementsCounterUntilThirdWin() = runTest {
        // 1st win
        reviewPromptManager.onItemCompleted(sampleItem, onTime = true, heatLevel = HeatLevel.TOASTY, activity = null)
        assertThat(userPrefsRepo.userPrefs.first().toastyCompletionsCount).isEqualTo(1)

        // 2nd win
        reviewPromptManager.onItemCompleted(sampleItem, onTime = true, heatLevel = HeatLevel.COOKED, activity = null)
        assertThat(userPrefsRepo.userPrefs.first().toastyCompletionsCount).isEqualTo(2)

        // 3rd win
        reviewPromptManager.onItemCompleted(sampleItem, onTime = true, heatLevel = HeatLevel.TOASTY, activity = null)
        assertThat(userPrefsRepo.userPrefs.first().toastyCompletionsCount).isEqualTo(3)
    }

    @Test
    fun recentPrompt_respectsNinetyDayCooldown() = runTest {
        // Set last prompt to 10 days ago
        val tenDaysAgo = LocalDate.now().minusDays(10).toEpochDay()
        userPrefsRepo.setLastReviewPromptEpochDay(tenDaysAgo)

        val triggered = reviewPromptManager.onItemCompleted(
            item = sampleItem,
            onTime = true,
            heatLevel = HeatLevel.COOKED,
            activity = null
        )

        assertThat(triggered).isFalse()
    }
}
