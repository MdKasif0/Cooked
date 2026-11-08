package com.cooked.app.core.telemetry

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.cooked.app.data.datastore.UserPreferencesRepository
import com.cooked.app.data.db.CookedDatabase
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TelemetryTest {

    private lateinit var context: Context
    private lateinit var database: CookedDatabase
    private lateinit var userPrefsRepo: UserPreferencesRepository
    private lateinit var telemetry: TelemetryImpl

    @Before
    fun setup() = runTest {
        context = ApplicationProvider.getApplicationContext()
        java.io.File(context.filesDir, "datastore").deleteRecursively()
        database = Room.inMemoryDatabaseBuilder(context, CookedDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        userPrefsRepo = UserPreferencesRepository(context)
        userPrefsRepo.setTelemetryEnabled(true)
        telemetry = TelemetryImpl(userPrefsRepo, database.itemEventDao())
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun defaultOptInState_isTrue() = runTest {
        val optedIn = telemetry.isOptedIn.first()
        assertThat(optedIn).isTrue()
    }

    @Test
    fun setOptIn_togglesPreference() = runTest {
        telemetry.setOptIn(false)
        val state1 = userPrefsRepo.userPrefs.first().telemetryEnabled
        assertThat(state1).isFalse()

        telemetry.setOptIn(true)
        val state2 = userPrefsRepo.userPrefs.first().telemetryEnabled
        assertThat(state2).isTrue()
    }

    @Test
    fun logEvent_recordsToDatabaseWhenOptedIn() = runTest {
        telemetry.logItemCreated(kind = "DEADLINE", via = "app")

        // Wait brief moment for async IO scope
        val start = System.currentTimeMillis()
        var events = emptyList<com.cooked.app.data.entity.ItemEventEntity>()
        while (events.isEmpty() && System.currentTimeMillis() - start < 1000) {
            Thread.sleep(20)
            events = database.itemEventDao().getEventsInRange(
                from = Instant.now().minus(java.time.Duration.ofHours(1)),
                to = Instant.now().plus(java.time.Duration.ofHours(1))
            )
        }

        assertThat(events).isNotEmpty()
        assertThat(events.first().meta["kind"]).isEqualTo("DEADLINE")
        assertThat(events.first().meta["via"]).isEqualTo("app")
    }

    @Test
    fun logEvent_dropsEventsWhenOptedOut() = runTest {
        telemetry.setOptIn(false)
        Thread.sleep(50)

        telemetry.logSnooze(minutes = 15)
        Thread.sleep(100)

        val events = database.itemEventDao().getEventsInRange(
            from = Instant.now().minus(java.time.Duration.ofHours(1)),
            to = Instant.now().plus(java.time.Duration.ofHours(1))
        )

        assertThat(events).isEmpty()
    }
}
