package fr.eda.monclavierconfigurable

import kotlinx.serialization.Serializable

@Serializable
data class KeyboardLayout(
    val rows: List<List<KeyModel>>
)

@Serializable
data class KeyModel(
    val display: DisplayType,
    val value: String,
    val action: KeyAction,
    val weight: Float
)

@Serializable
enum class DisplayType {
    TEXT,
    SVG
}

@Serializable
enum class KeyAction {
    CHARACTER,
    BACKSPACE,
    SPACE,
    ENTER,
    NEXT_KEYBOARD
}