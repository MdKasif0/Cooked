package com.cooked.app.data.db

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File

/**
 * Guards against the Auto Backup rules drifting from the real Room database file name,
 * which would silently exclude every task from Google backup and device transfer.
 */
class BackupRulesTest {

    private fun xml(name: String): String {
        val candidates = listOf(File("src/main/res/xml/$name"), File("app/src/main/res/xml/$name"))
        return candidates.first { it.exists() }.readText()
    }

    @Test
    fun backupRules_includeTheRealDatabaseFile() {
        for (file in listOf("full_backup_content.xml", "data_extraction_rules.xml")) {
            val content = xml(file)
            val dbPaths = Regex("""domain="database" path="([^"]+)"""").findAll(content).map { it.groupValues[1] }.toList()
            assertThat(dbPaths).isNotEmpty()
            assertThat(dbPaths).contains(CookedDatabase.DATABASE_NAME)
            dbPaths.forEach { assertThat(it).startsWith(CookedDatabase.DATABASE_NAME) }
        }
    }
}
