package com.cooked.app.scheduling.notification

import com.cooked.app.domain.model.Personality
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationCopyTest {

    @Test
    fun `all personalities and stages have at least 8 distinct variants`() {
        for (personality in Personality.entries) {
            for (stage in NagStage.entries) {
                val copies = (0..50).map { nagCount ->
                    NotificationCopy.getCopy(
                        personality = personality,
                        stage = stage,
                        title = "Task $nagCount",
                        nagCount = nagCount
                    )
                }.toSet()

                assertTrue(
                    "Expected at least 8 variants for $personality and $stage, found ${copies.size}",
                    copies.size >= 8
                )
            }
        }
    }

    @Test
    fun `title placeholder is cleanly substituted without leaking braces`() {
        val testTitle = "Master Algorithms"
        for (personality in Personality.entries) {
            for (stage in NagStage.entries) {
                val copy = NotificationCopy.getCopy(
                    personality = personality,
                    stage = stage,
                    title = testTitle,
                    nagCount = 3
                )
                assertTrue("Copy should contain title: '$copy'", copy.contains(testTitle))
                assertFalse("Copy should not leak placeholder: '$copy'", copy.contains("{title}"))
            }
        }
    }
}

