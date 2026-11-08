package com.cooked.app.ui.navigation

import android.content.Intent
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DeepLinkTest {

    @Test
    fun notificationTap_withItemId_opensItemDetail() {
        val link = DeepLink.parse(action = null, mimeType = null, navigateTo = null, itemId = "abc", sharedStream = null)
        assertThat(link).isEqualTo(DeepLink.ItemDetail("abc"))
    }

    @Test
    fun deadlineShortcut_opensItemDetail() {
        val link = DeepLink.parse(
            action = Intent.ACTION_VIEW,
            mimeType = null,
            navigateTo = DeepLink.TARGET_DEADLINE_DETAIL,
            itemId = "d1",
            sharedStream = null
        )
        assertThat(link).isEqualTo(DeepLink.ItemDetail("d1"))
    }

    @Test
    fun weeklyReport_opensInsights() {
        val link = DeepLink.parse(null, null, DeepLink.TARGET_INSIGHTS_REPORT, null, null)
        assertThat(link).isEqualTo(DeepLink.Insights)
    }

    @Test
    fun todayShortcut_opensToday() {
        val link = DeepLink.parse(Intent.ACTION_VIEW, null, DeepLink.TARGET_TODAY, null, null)
        assertThat(link).isEqualTo(DeepLink.Today)
    }

    @Test
    fun sharedImage_opensDatesheetReview() {
        val link = DeepLink.parse(Intent.ACTION_SEND, "image/jpeg", null, null, "content://x/1")
        assertThat(link).isEqualTo(DeepLink.SharedImage("content://x/1"))
    }

    @Test
    fun plainLauncherIntent_hasNoDeepLink() {
        assertThat(DeepLink.parse(Intent.ACTION_MAIN, null, null, null, null)).isNull()
        assertThat(DeepLink.parse(null, null, null, "  ", null)).isNull()
    }
}
