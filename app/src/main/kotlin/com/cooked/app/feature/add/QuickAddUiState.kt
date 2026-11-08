package com.cooked.app.feature.add

import com.cooked.app.domain.model.Item
import com.cooked.app.domain.model.ItemKind
import com.cooked.app.domain.model.Recurrence
import com.cooked.app.domain.parse.ParseResult
import java.time.Instant

data class QuickAddUiState(
    val input: String = "",
    val parsedResult: ParseResult? = null,
    val overrideDueAt: Instant? = null,
    val overrideKind: ItemKind? = null,
    val overrideEffortMinutes: Int? = null,
    val overrideRecurrence: Recurrence? = null,
    val overrideCritical: Boolean? = null,
    val overrideAllDay: Boolean? = null,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val savedItem: Item? = null,
    val showDateTimePicker: Boolean = false
) {
    val effectiveTitle: String
        get() = parsedResult?.title?.ifBlank { input.trim() } ?: input.trim()

    val effectiveDueAt: Instant?
        get() = overrideDueAt ?: parsedResult?.dueAt

    val effectiveKind: ItemKind
        get() = overrideKind ?: (parsedResult?.kindGuess ?: ItemKind.REMINDER)

    val effectiveEffortMinutes: Int?
        get() = overrideEffortMinutes ?: parsedResult?.effortMinutes

    val effectiveRecurrence: Recurrence?
        get() = overrideRecurrence ?: parsedResult?.recurrence

    val effectiveCritical: Boolean
        get() = overrideCritical ?: (parsedResult?.critical ?: false)

    val effectiveAllDay: Boolean
        get() = overrideAllDay ?: (parsedResult?.allDay ?: false)

    val canSave: Boolean
        get() = input.isNotBlank()
}
