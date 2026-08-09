package fr.eda.monclavierconfigurable

import android.content.Context
import kotlinx.serialization.json.Json

object KeyboardParser {
    fun parse(context: Context, fileName: String): KeyboardLayout? {
        return try {
            val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
            Json.decodeFromString<KeyboardLayout>(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}