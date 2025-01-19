package com.mayor.kavi.ui.viewmodel

import androidx.lifecycle.*
import com.mayor.kavi.data.manager.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.*
import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import com.mayor.kavi.data.manager.StatisticsManager

/**
 * ViewModel that manages the application's state, user profile, sign-in status, and settings.
 * It communicates with the user repository, authentication repository, statistics manager,
 * and Firebase Firestore.
 */
@HiltViewModel
class AppViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val statisticsManager: StatisticsManager
) : ViewModel() {

    private val diceDataStore = DataStoreManager.getInstance(context)

    /**
     * State flow that holds the retraining status of the model.
     */
    private val _modelRetrainingStatus = MutableStateFlow<String?>(null)
    val modelRetrainingStatus: StateFlow<String?> = _modelRetrainingStatus.asStateFlow()

    init {
        viewModelScope.launch {
            // Listen for model retraining updates
            statisticsManager.modelTrainingStatus.collect { status ->
                _modelRetrainingStatus.value = status
            }
        }
    }

    /**
     * Sets the interface mode for the application.
     *
     * @param mode The mode to set for the interface (e.g., "dark" or "light").
     */
    fun setInterfaceMode(mode: String) = viewModelScope.launch(Dispatchers.IO) {
        diceDataStore.setInterfaceMode(mode)
    }
}
