package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "cric_taught_settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode") // "SYSTEM", "LIGHT", "DARK"
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val NOTIFICATION_FREQUENCY = stringPreferencesKey("notification_frequency") // "ALL", "MAJOR", "NONE"
        val SIMULATION_ACTIVE = booleanPreferencesKey("simulation_active") // Live updates simulation
        val FAVORITE_TEAMS = stringSetPreferencesKey("favorite_teams")
    }

    val themeModeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME_MODE] ?: "SYSTEM"
    }

    val notificationsEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATIONS_ENABLED] ?: true
    }

    val notificationFrequencyFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATION_FREQUENCY] ?: "ALL"
    }

    val simulationActiveFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SIMULATION_ACTIVE] ?: true
    }

    val favoriteTeamsFlow: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[FAVORITE_TEAMS] ?: emptySet()
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setNotificationFrequency(frequency: String) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_FREQUENCY] = frequency
        }
    }

    suspend fun setSimulationActive(active: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SIMULATION_ACTIVE] = active
        }
    }

    suspend fun toggleFavoriteTeam(teamName: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[FAVORITE_TEAMS] ?: emptySet()
            val updated = if (current.contains(teamName)) {
                current - teamName
            } else {
                current + teamName
            }
            preferences[FAVORITE_TEAMS] = updated
        }
    }
}
