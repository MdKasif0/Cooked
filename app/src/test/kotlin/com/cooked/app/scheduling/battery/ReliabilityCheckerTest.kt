package com.cooked.app.scheduling.battery

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ReliabilityCheckerTest {

    @Test
    fun `checker generates report with all reliability fields populated`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val checker = ReliabilityChecker(context)

        val report = checker.check()
        assertNotNull(report.manufacturer)
        assertNotNull(report.channelsStatus)
        // Check that all 5 channels are tested
        assertTrue(report.channelsStatus.size >= 5)
    }

    @Test
    fun `auto start intents list contains intents for major OEMs`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val checker = ReliabilityChecker(context)

        val intents = checker.getOemAutoStartIntents()
        assertTrue(intents.isNotEmpty())
        assertTrue(intents.any { it.component?.packageName?.contains("miui") == true })
        assertTrue(intents.any { it.component?.packageName?.contains("coloros") == true || it.component?.packageName?.contains("oplus") == true })
        assertTrue(intents.any { it.component?.packageName?.contains("vivo") == true })
    }
}
