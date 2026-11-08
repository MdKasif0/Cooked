package com.cooked.app.domain.model

enum class Personality {
    MILD,
    SPICY,
    UNHINGED;

    val displayName: String
        get() = when (this) {
            MILD -> "Mild (Polite)"
            SPICY -> "Spicy (Blunt)"
            UNHINGED -> "Unhinged (Chaotic)"
        }
}

