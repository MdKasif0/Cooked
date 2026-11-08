package com.cooked.app.domain.snitch

import com.cooked.app.domain.model.Personality
import org.junit.Assert.assertTrue
import org.junit.Test

class SnitchManagerTest {

    @Test
    fun buildSnitchMessage_mildPersonality() {
        val message = SnitchManager.buildSnitchMessage(
            personality = Personality.MILD,
            contactName = "Aman",
            itemTitle = "Complete DSA Sheet",
            snoozeCount = 3
        )

        assertTrue(message.contains("Aman has snoozed 'Complete DSA Sheet' 3 times"))
        assertTrue(message.contains("accountability check"))
        assertTrue(message.contains(SnitchManager.DEFAULT_PLAY_STORE_LINK))
    }

    @Test
    fun buildSnitchMessage_spicyPersonality() {
        val message = SnitchManager.buildSnitchMessage(
            personality = Personality.SPICY,
            contactName = "Priya",
            itemTitle = "Pay Electricity Bill",
            snoozeCount = 4
        )

        assertTrue(message.contains("Priya has snoozed 'Pay Electricity Bill' 4 times"))
        assertTrue(message.contains("bully them into finishing it"))
        assertTrue(message.contains(SnitchManager.DEFAULT_PLAY_STORE_LINK))
    }

    @Test
    fun buildSnitchMessage_unhingedPersonality() {
        val message = SnitchManager.buildSnitchMessage(
            personality = Personality.UNHINGED,
            contactName = "Rahul",
            itemTitle = "File Taxes",
            snoozeCount = 5
        )

        assertTrue(message.contains("EMERGENCY ACCOUNTABILITY ALERT"))
        assertTrue(message.contains("Rahul has snoozed 'File Taxes' 5 TIMES"))
        assertTrue(message.contains("Roast them into submission"))
        assertTrue(message.contains(SnitchManager.DEFAULT_PLAY_STORE_LINK))
    }

    @Test
    fun buildSnitchMessage_fallbackContactNameWhenBlank() {
        val message = SnitchManager.buildSnitchMessage(
            personality = Personality.SPICY,
            contactName = "",
            itemTitle = "Submit Assignment",
            snoozeCount = 3
        )

        assertTrue(message.contains("Your friend has snoozed 'Submit Assignment' 3 times"))
    }
}
