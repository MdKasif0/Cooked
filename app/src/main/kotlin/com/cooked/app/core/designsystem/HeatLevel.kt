package com.cooked.app.core.designsystem

/**
 * Semantic urgency scale for Cooked.
 * "Heat" = urgency: Chill -> Warm -> Toasty -> Cooked, plus Done.
 */
enum class HeatLevel {
    CHILL,
    WARM,
    TOASTY,
    COOKED,
    DONE;

    val displayName: String
        get() = when (this) {
            CHILL -> "Chill"
            WARM -> "Warm"
            TOASTY -> "Toasty"
            COOKED -> "Cooked"
            DONE -> "Done"
        }
}
