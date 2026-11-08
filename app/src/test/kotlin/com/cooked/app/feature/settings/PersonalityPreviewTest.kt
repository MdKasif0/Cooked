package com.cooked.app.feature.settings

import com.cooked.app.domain.model.Personality
import com.cooked.app.scheduling.notification.NagStage
import com.cooked.app.scheduling.notification.NotificationCopy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonalityPreviewTest {

    @Test
    fun allPersonalitiesAndStagesGenerateValidCopy() {
        val personalities = listOf(Personality.MILD, Personality.SPICY, Personality.UNHINGED)
        val stages = listOf(NagStage.FIRST, NagStage.NAG_EARLY, NagStage.NAG_PERSISTENT, NagStage.FINAL_CALL)

        for (personality in personalities) {
            for (stage in stages) {
                val copy = NotificationCopy.getCopy(
                    personality = personality,
                    stage = stage,
                    title = "Review PR",
                    nagCount = 3
                )
                assertFalse("Copy should not be blank for $personality at $stage", copy.isBlank())
                assertTrue("Copy should contain task title for $personality at $stage", copy.contains("Review PR"))
            }
        }
    }
}
