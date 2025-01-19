package com.mayor.kavi.data.manager

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class DataStoreManagerTest {
    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var dataStore: DataStore<Preferences>

    private lateinit var manager: DataStoreManager
    private lateinit var json: Json

    private val interfaceModeKey = stringPreferencesKey("interface_mode")
    private val shakeEnabledKey = booleanPreferencesKey("shake_enabled")
    private val vibrationEnabledKey = booleanPreferencesKey("vibration_enabled")
    private val boardColorKey = stringPreferencesKey("board_color_key")
    private val customGamesKey = stringPreferencesKey("custom_games")

    @Before
    fun setup() {
        // Create test instance of DataStoreManager with mocked DataStore
        manager = DataStoreManager(context, dataStore)
        json = Json { 
            ignoreUnknownKeys = true 
            prettyPrint = true
        }
    }

    @Test
    fun `test getInterfaceMode returns default value when no preference exists`() = runTest {
        // Given
        val preferences = preferencesOf()
        whenever(dataStore.data).thenReturn(flowOf(preferences))

        // When
        val result = manager.getInterfaceMode().first()

        // Then
        assertEquals("classic", result)
    }

    @Test
    fun `test getInterfaceMode returns stored value when preference exists`() = runTest {
        // Given
        val preferences = preferencesOf(interfaceModeKey to "ar_mode")
        whenever(dataStore.data).thenReturn(flowOf(preferences))

        // When
        val result = manager.getInterfaceMode().first()

        // Then
        assertEquals("ar_mode", result)
    }

    @Test
    fun `test setInterfaceMode updates preference value`() = runTest {
        // Given
        val mode = "ar_mode"
        whenever(dataStore.edit(any())).thenReturn(emptyPreferences())

        // When
        manager.setInterfaceMode(mode)

        // Then
        verify(dataStore).edit(any())
    }

    @Test
    fun `test getShakeEnabled returns default value when no preference exists`() = runTest {
        // Given
        val preferences = preferencesOf()
        whenever(dataStore.data).thenReturn(flowOf(preferences))

        // When
        val result = manager.getShakeEnabled().first()

        // Then
        assertFalse(result)
    }

    @Test
    fun `test getShakeEnabled returns stored value when preference exists`() = runTest {
        // Given
        val preferences = preferencesOf(shakeEnabledKey to true)
        whenever(dataStore.data).thenReturn(flowOf(preferences))

        // When
        val result = manager.getShakeEnabled().first()

        // Then
        assertTrue(result)
    }

    @Test
    fun `test setShakeEnabled updates preference value`() = runTest {
        // Given
        whenever(dataStore.edit(any())).thenReturn(emptyPreferences())

        // When
        manager.setShakeEnabled(true)

        // Then
        verify(dataStore).edit(any())
    }

    @Test
    fun `test getVibrationEnabled returns true by default when no preference exists`() = runTest {
        // Given
        val preferences = preferencesOf()
        whenever(dataStore.data).thenReturn(flowOf(preferences))

        // When
        val result = manager.getVibrationEnabled().first()

        // Then
        assertTrue(result)
    }

    @Test
    fun `test setVibrationEnabled updates preference value`() = runTest {
        // Given
        whenever(dataStore.edit(any())).thenReturn(emptyPreferences())

        // When
        manager.setVibrationEnabled(true)

        // Then
        verify(dataStore).edit(any())
    }

    @Test
    fun `test getBoardColor returns default value when no preference exists`() = runTest {
        // Given
        val preferences = preferencesOf()
        whenever(dataStore.data).thenReturn(flowOf(preferences))

        // When
        val result = manager.getBoardColor().first()

        // Then
        assertEquals("default", result)
    }

    @Test
    fun `test setBoardColor updates preference value`() = runTest {
        // Given
        whenever(dataStore.edit(any())).thenReturn(emptyPreferences())

        // When
        manager.setBoardColor("red")

        // Then
        verify(dataStore).edit(any())
    }

    @Test
    fun `test loadCustomGames returns empty list when no games exist`() = runTest {
        // Given
        val preferences = preferencesOf()
        whenever(dataStore.data).thenReturn(flowOf(preferences))

        // When
        val result = manager.loadCustomGames().first()

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `test loadCustomGames returns empty list when json is invalid`() = runTest {
        // Given
        val preferences = preferencesOf(customGamesKey to "invalid json")
        whenever(dataStore.data).thenReturn(flowOf(preferences))

        // When
        val result = manager.loadCustomGames().first()

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `test loadCustomGames returns list of games when json is valid`() = runTest {
        // Given
        val validJson = "[]" // Empty array is valid JSON
        val preferences = preferencesOf(customGamesKey to validJson)
        whenever(dataStore.data).thenReturn(flowOf(preferences))

        // When
        val result = manager.loadCustomGames().first()

        // Then
        assertTrue(result.isEmpty())
    }

    private fun preferencesOf(vararg pairs: Pair<Preferences.Key<*>, Any>): Preferences {
        val preferences = mock<Preferences>()
        pairs.forEach { (key, value) ->
            whenever(preferences[key]).thenReturn(value)
        }
        return preferences
    }
}