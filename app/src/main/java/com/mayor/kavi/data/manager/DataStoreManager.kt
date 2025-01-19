package com.mayor.kavi.data.manager

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.mayor.kavi.data.models.GameScoreState
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import java.io.IOException

/**
 * Manages user preferences using DataStore for storing and retrieving settings.
 * This includes settings such as interface mode, shake-to-roll, vibration, board color, and custom games.
 *
 * @param context The application context used for initializing DataStore.
 * @param dataStore The DataStore instance for storing preferences (defaults to context's DataStore).
 */
class DataStoreManager(
    context: Context,
    private val dataStore: DataStore<Preferences> = context.dataStore
) {
    companion object {
        // Preference keys and default values

        private val INTERFACE_MODE_KEY = stringPreferencesKey("interface_mode")
        private const val DEFAULT_INTERFACE_MODE = "classic"

        private val BOARD_COLOR_KEY = stringPreferencesKey("board_color_key")
        private val SHAKE_ENABLED_KEY = booleanPreferencesKey("shake_enabled")
        private val VIBRATION_ENABLED_KEY = booleanPreferencesKey("vibration_enabled")
        private val CUSTOM_GAMES_KEY = stringPreferencesKey("custom_games")

        private const val DEFAULT_BOARD_COLOR_KEY = "default"
        private const val DEFAULT_SHAKE_ENABLED = false

        @Volatile
        private var INSTANCE: DataStoreManager? = null

        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

        /**
         * Returns a singleton instance of the DataStoreManager.
         *
         * @param context The application context used for initializing the DataStore.
         * @return An instance of DataStoreManager.
         */
        fun getInstance(context: Context): DataStoreManager =
            INSTANCE ?: synchronized(this) {
                DataStoreManager(context).also { INSTANCE = it }
            }
    }

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    /**
     * Sets the interface mode (e.g., Classic or AR mode).
     *
     * @param mode The interface mode to set.
     */
    suspend fun setInterfaceMode(mode: String) = dataStore.edit { preferences ->
        preferences[INTERFACE_MODE_KEY] = mode
    }

    /**
     * Retrieves the current interface mode as a Flow.
     *
     * @return A Flow emitting the current interface mode.
     */
    fun getInterfaceMode(): Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[INTERFACE_MODE_KEY] ?: DEFAULT_INTERFACE_MODE
        }

    /**
     * Sets whether shake-to-roll is enabled or not.
     *
     * @param enabled True to enable shake-to-roll, false to disable it.
     */
    suspend fun setShakeEnabled(enabled: Boolean) = dataStore.edit { preferences ->
        preferences[SHAKE_ENABLED_KEY] = enabled
    }

    /**
     * Retrieves the current shake-to-roll setting as a Flow.
     *
     * @return A Flow emitting the current shake-to-roll setting.
     */
    fun getShakeEnabled(): Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[SHAKE_ENABLED_KEY] ?: DEFAULT_SHAKE_ENABLED
        }

    /**
     * Sets whether vibration is enabled or not.
     *
     * @param enabled True to enable vibration, false to disable it.
     */
    suspend fun setVibrationEnabled(enabled: Boolean) = dataStore.edit { preferences ->
        preferences[VIBRATION_ENABLED_KEY] = enabled
    }

    /**
     * Retrieves the current vibration setting as a Flow.
     *
     * @return A Flow emitting the current vibration setting.
     */
    fun getVibrationEnabled(): Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[VIBRATION_ENABLED_KEY] != false  // Default to true
        }

    /**
     * Sets the board color preference.
     *
     * @param color The board color to set.
     */
    suspend fun setBoardColor(color: String) = dataStore.edit { preferences ->
        preferences[BOARD_COLOR_KEY] = color
    }

    /**
     * Retrieves the current board color setting as a Flow.
     *
     * @return A Flow emitting the current board color setting.
     */
    fun getBoardColor(): Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[BOARD_COLOR_KEY] ?: DEFAULT_BOARD_COLOR_KEY
        }

    /**
     * Loads the list of custom games stored in preferences.
     * Returns a Flow of the list of custom game states.
     *
     * @return A Flow emitting a list of custom game states.
     */
    fun loadCustomGames(): Flow<List<GameScoreState.CustomScoreState>> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            try {
                val gamesJson = preferences[CUSTOM_GAMES_KEY] ?: "[]"
                json.decodeFromString<List<GameScoreState.CustomScoreState>>(gamesJson)
            } catch (_: Exception) {
                emptyList()
            }
        }
}
