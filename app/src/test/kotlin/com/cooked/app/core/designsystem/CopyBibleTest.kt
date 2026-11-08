package com.cooked.app.core.designsystem

import com.cooked.app.domain.model.Personality
import com.cooked.app.scheduling.notification.NagStage
import com.cooked.app.scheduling.notification.NotificationCopy
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File

class CopyBibleTest {

    private val bannedPhrases = listOf(
        "boost your productivity",
        "welcome back",
        "unlock your potential",
        "game-changer",
        "seamless",
        "let's get started",
        "\uD83D\uDE80" // 🚀 rocket emoji
    )

    private val allowedEmojis = setOf(
        "\uD83D\uDC80", // 💀
        "\uD83D\uDD25", // 🔥
        "\uD83E\uDD5A", // 🥚
        "\uD83D\uDC40", // 👀
        "\uD83E\uDD7A", // 🥺
        "\uD83E\uDEE1", // 🫡
        "\uD83C\uDF73"  // 🍳
    )

    @Test
    fun copyXml_containsNoBannedPhrases() {
        val copyXml = File("src/main/res/values/copy.xml")
        val altFile = if (copyXml.exists()) copyXml else File("app/src/main/res/values/copy.xml")
        assertThat(altFile.exists()).isTrue()

        val content = altFile.readText()
            .replace(Regex("<!--.*?-->", RegexOption.DOT_MATCHES_ALL), "")
            .lowercase()

        for (banned in bannedPhrases) {
            assertThat(content).doesNotContain(banned.lowercase())
        }
    }

    @Test
    fun copyXml_stringsOnlyContainAllowedEmojis() {
        val copyXml = File("src/main/res/values/copy.xml")
        val altFile = if (copyXml.exists()) copyXml else File("app/src/main/res/values/copy.xml")
        assertThat(altFile.exists()).isTrue()

        val stringLines = altFile.readLines().filter { it.trim().startsWith("<string ") }

        // Find emojis using regex for standard unicode surrogate pairs
        val emojiRegex = Regex("[\\p{So}\\p{Cn}]")

        for (line in stringLines) {
            val emojisInLine = emojiRegex.findAll(line).map { it.value }.toList()
            for (foundEmoji in emojisInLine) {
                // If it is an emoji, assert it belongs to allowed emojis
                val isKnownAllowed = allowedEmojis.any { line.contains(it) }
                assertThat(isKnownAllowed).isTrue()
            }
        }
    }

    @Test
    fun notificationCopy_containsNoBannedPhrasesAcrossAllPersonalitiesAndStages() {
        val personalities = Personality.entries
        val stages = NagStage.entries

        for (personality in personalities) {
            for (stage in stages) {
                for (nagCount in 0..10) {
                    val copy = NotificationCopy.getCopy(
                        personality = personality,
                        stage = stage,
                        title = "Finish Lab Report",
                        nagCount = nagCount
                    )

                    assertThat(copy).isNotEmpty()
                    assertThat(copy).contains("Finish Lab Report")

                    val lower = copy.lowercase()
                    for (banned in bannedPhrases) {
                        assertThat(lower).doesNotContain(banned.lowercase())
                    }
                }
            }
        }
    }
}

