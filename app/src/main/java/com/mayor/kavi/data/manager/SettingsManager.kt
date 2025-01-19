package com.mayor.kavi.data.manager

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.mayor.kavi.util.BoardColors
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages user settings which include shake, vibration, and board color preferences.
 * This class provides methods to store and retrieve user preferences using DataStore.
 */
@Singleton
class SettingsManager @Inject constructor(@ApplicationContext context: Context) {

    companion object {
        private val SHAKE_ENABLED_KEY = booleanPreferencesKey("SHAKE_ENABLED_KEY")
        private val VIBRATION_ENABLED_KEY = booleanPreferencesKey("VIBRATION_ENABLED_KEY")
        private val BOARD_COLOR_KEY = stringPreferencesKey("BOARD_COLOR_KEY")

        val BOARD_COLORS = BoardColors.getAvailableColors()

        /**
         * A static CompositionLocal to access the SettingsManager in composables.
         */
        val LocalSettingsManager = staticCompositionLocalOf<SettingsManager> {
            error("No SettingsManager provided!")
        }
    }

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    private val dataStore = context.dataStore

    /**
     * Sets the shake-to-roll setting.
     *
     * @param enabled A boolean indicating whether shake-to-roll should be enabled.
     */
    suspend fun setShakeEnabled(enabled: Boolean) = dataStore.edit { pref ->
        pref[SHAKE_ENABLED_KEY] = enabled
    }

    /**
     * Retrieves the current shake-to-roll setting.
     *
     * @return A flow that emits a boolean value indicating whether shake-to-roll is enabled.
     */
    fun getShakeEnabled(): Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { pref ->
            pref[SHAKE_ENABLED_KEY] == true
        }

    /**
     * Sets the vibration setting.
     *
     * @param enabled A boolean indicating whether vibration should be enabled.
     */
    suspend fun setVibrationEnabled(enabled: Boolean) = dataStore.edit { pref ->
        pref[VIBRATION_ENABLED_KEY] = enabled
    }

    /**
     * Retrieves the current vibration setting.
     *
     * @return A flow that emits a boolean value indicating whether vibration is enabled.
     */
    fun getVibrationEnabled(): Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { pref ->
            pref[VIBRATION_ENABLED_KEY] == true
        }

    /**
     * Retrieves the current board color preference.
     *
     * @return A flow that emits the current board color, or a default value if not set.
     */
    fun getBoardColor(): Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[BOARD_COLOR_KEY] ?: "default"
        }

    /**
     * Sets the board color preference.
     *
     * @param color The color to set for the board. It must be one of the available board colors.
     */
    suspend fun setBoardColor(color: String) {
        if (color !in BOARD_COLORS) return
        dataStore.edit { preferences ->
            preferences[BOARD_COLOR_KEY] = color
        }
    }
}
