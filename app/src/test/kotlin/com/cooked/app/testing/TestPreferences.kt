package com.cooked.app.testing

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.cooked.app.data.datastore.UserPreferencesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import java.io.File
import java.nio.file.Files

/**
 * A [UserPreferencesRepository] backed by its own DataStore file whose reads and writes run on
 * [dispatcher]. With a test dispatcher, `advanceUntilIdle()` fully settles every preference
 * update, so tests don't race real IO threads (which made them flaky on slower CI runners),
 * and no state leaks between tests through the app-wide DataStore singleton.
 */
fun testUserPreferencesRepository(dispatcher: CoroutineDispatcher): UserPreferencesRepository {
    val dir: File = Files.createTempDirectory("cooked-prefs-test").toFile().apply { deleteOnExit() }
    val dataStore = PreferenceDataStoreFactory.create(
        scope = CoroutineScope(dispatcher + SupervisorJob()),
        produceFile = { File(dir, "test_prefs.preferences_pb") }
    )
    return UserPreferencesRepository(dataStore)
}
