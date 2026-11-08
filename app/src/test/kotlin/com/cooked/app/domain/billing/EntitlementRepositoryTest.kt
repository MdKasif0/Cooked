package com.cooked.app.domain.billing

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.cooked.app.data.billing.EntitlementRepositoryImpl
import com.cooked.app.data.datastore.UserPreferencesRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class EntitlementRepositoryTest {

    private lateinit var context: Context
    private lateinit var userPrefsRepo: UserPreferencesRepository
    private lateinit var entitlementRepository: EntitlementRepositoryImpl

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        userPrefsRepo = UserPreferencesRepository(context)
        entitlementRepository = EntitlementRepositoryImpl(userPrefsRepo)
    }

    @Test
    fun defaultState_isNotPro() = runTest {
        val isPro = entitlementRepository.isPro.first()
        assertThat(isPro).isFalse()
    }

    @Test
    fun setLocalProForTesting_updatesProStateAndPersists() = runTest {
        entitlementRepository.setLocalProForTesting(true)
        val isPro = userPrefsRepo.isPro.first()
        assertThat(isPro).isTrue()

        entitlementRepository.setLocalProForTesting(false)
        val isProAfter = userPrefsRepo.isPro.first()
        assertThat(isProAfter).isFalse()
    }

    @Test
    fun billingIssue_defaultsToNull() = runTest {
        val issue = entitlementRepository.billingIssue.first()
        assertThat(issue).isNull()
    }
}
