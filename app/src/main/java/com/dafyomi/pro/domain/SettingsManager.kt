package com.dafyomi.pro.domain

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dafyomi.pro.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Manages persistent user settings using DataStore.
 */
class SettingsManager(private val context: Context) {

    companion object {
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val FONT_SIZE_KEY = floatPreferencesKey("font_size")
    }

    /**
     * Saves the theme mode preference.
     */
    suspend fun saveThemeMode(themeMode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = themeMode.name
        }
    }

    /**
     * Loads the theme mode preference as a Flow.
     */
    val themeModeFlow: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        val name = preferences[THEME_MODE_KEY] ?: ThemeMode.AUTO.name
        try {
            ThemeMode.valueOf(name)
        } catch (e: Exception) {
            ThemeMode.AUTO
        }
    }

    /**
     * Saves the font size multiplier preference.
     */
    suspend fun saveFontSize(fontSizeMultiplier: Float) {
        context.dataStore.edit { preferences ->
            preferences[FONT_SIZE_KEY] = fontSizeMultiplier
        }
    }

    /**
     * Loads the font size multiplier as a Flow.
     */
    val fontSizeFlow: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[FONT_SIZE_KEY] ?: 1.0f
    }
}
