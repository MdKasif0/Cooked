package com.cooked.app.scheduling.review

import android.app.Activity
import com.cooked.app.data.datastore.UserPreferencesRepository
import com.cooked.app.domain.model.HeatLevel
import com.cooked.app.domain.model.Item
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewPromptManager @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository
) {

    companion object {
        const val REQUIRED_TOASTY_WINS = 3
        const val MINIMUM_DAYS_BETWEEN_PROMPTS = 90L
    }

    /**
     * Evaluates whether to request a Google Play In-App Review:
     * - Triggered on the 3rd on-time Done of a Toasty/Cooked item
     * - Cooldown: max once per 90 days
     */
    suspend fun onItemCompleted(
        item: Item,
        onTime: Boolean,
        heatLevel: HeatLevel,
        activity: Activity?
    ): Boolean {
        if (!onTime) return false
        if (heatLevel != HeatLevel.TOASTY && heatLevel != HeatLevel.COOKED) return false

        val newCount = preferencesRepository.incrementToastyCompletions()
        if (newCount < REQUIRED_TOASTY_WINS) return false

        val prefs = preferencesRepository.userPrefs.first()
        val todayEpochDay = LocalDate.now().toEpochDay()
        val daysSinceLastPrompt = todayEpochDay - prefs.lastReviewPromptEpochDay

        if (prefs.lastReviewPromptEpochDay != 0L && daysSinceLastPrompt < MINIMUM_DAYS_BETWEEN_PROMPTS) {
            return false
        }

        if (activity == null) return false

        try {
            val reviewManager = ReviewManagerFactory.create(activity)
            val requestFlow = reviewManager.requestReviewFlow()
            requestFlow.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val reviewInfo = task.result
                    reviewManager.launchReviewFlow(activity, reviewInfo)
                }
            }
            preferencesRepository.setLastReviewPromptEpochDay(todayEpochDay)
            return true
        } catch (_: Exception) {
            return false
        }
    }
}
