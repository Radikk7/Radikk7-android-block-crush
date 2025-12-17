package com.example.blockcrush.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.blockcrush.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "block_crush_settings"

val Context.dataStore by preferencesDataStore(DATASTORE_NAME)

class SettingsRepository(private val context: Context) {

    private val bestScoreKey = intPreferencesKey("best_score")
    private val soundKey = booleanPreferencesKey("sound_enabled")
    private val hapticsKey = booleanPreferencesKey("haptics_enabled")
    private val themeKey = stringPreferencesKey("theme_mode")

    val settings: Flow<SettingsState> = context.dataStore.data.map { prefs ->
        SettingsState(
            bestScore = prefs[bestScoreKey] ?: 0,
            soundEnabled = prefs[soundKey] ?: true,
            hapticsEnabled = prefs[hapticsKey] ?: true,
            theme = prefs[themeKey]?.let { ThemeMode.valueOf(it) } ?: ThemeMode.MONO
        )
    }

    suspend fun saveBestScore(score: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[bestScoreKey] ?: 0
            if (score > current) {
                prefs[bestScoreKey] = score
            }
        }
    }

    suspend fun setTheme(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[themeKey] = mode.name
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[soundKey] = enabled }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[hapticsKey] = enabled }
    }
}

data class SettingsState(
    val bestScore: Int = 0,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val theme: ThemeMode = ThemeMode.MONO
)
