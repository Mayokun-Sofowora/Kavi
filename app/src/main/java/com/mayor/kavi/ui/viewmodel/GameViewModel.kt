package com.mayor.kavi.ui.viewmodel

import android.content.Context
import android.os.*
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import com.mayor.kavi.data.models.*
import com.mayor.kavi.data.manager.*
import com.mayor.kavi.data.manager.games.*
import com.mayor.kavi.data.models.enums.GameBoard
import com.mayor.kavi.data.service.GameTracker
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

/**
 * Primary ViewModel for managing game state and user interactions across all dice game variants.
 *
 * This ViewModel handles:
 * - Game state management for all variants (Pig, Greed, Balut, Custom)
 * - Dice rolling and management
 * - Score tracking and statistics
 * - AI opponent interactions
 * - User settings and preferences
 * - Device interactions (vibration, shake detection)
 *
 * @property context Application context for system services
 * @property pigGameManager Manager for Pig game variant
 * @property greedGameManager Manager for Greed game variant
 * @property balutGameManager Manager for Balut game variant
 * @property myGameManager Manager for custom game variants
 * @property statisticsManager Tracks game statistics and player behavior
 * @property shakeDetectionManager Handles shake-to-roll functionality
 * @property diceManager Manages dice state and animations
 * @property gameTracker Tracks user interactions and game decisions
 */
@HiltViewModel
class GameViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pigGameManager: PigGameManager,
    private val greedGameManager: GreedGameManager,
    private val balutGameManager: BalutGameManager,
    private val myGameManager: MyGameManager,
    private val statisticsManager: StatisticsManager,
    private val shakeDetectionManager: ShakeDetectionManager,
    private val diceManager: DiceManager,
    private val dataStoreManager: DataStoreManager,
    private val gameTracker: GameTracker
) : ViewModel() {

    companion object {
        /** Identifier for the AI opponent */
        const val AI_PLAYER_ID = "AI_OPPONENT"
    }

    // Core States
    private val _gameState = MutableStateFlow<GameScoreState>(GameScoreState.PigScoreState())

    /** Current game state, varies by game variant */
    val gameState: StateFlow<GameScoreState> = _gameState.asStateFlow()
    private val _selectedBoard = MutableStateFlow("")

    /** Currently selected game board/variant */
    val selectedBoard: StateFlow<String> = _selectedBoard
    private val _heldDice = MutableStateFlow<Set<Int>>(emptySet())

    // UI States
    private val _showWinDialog = MutableStateFlow(false)

    // Settings & Board Selection
    private val _vibrationEnabled = MutableStateFlow(false)
    private val _boardColor = MutableStateFlow("")

    // Game States
    private val _isLoading = MutableStateFlow(false)
    private val shakeFlow = MutableSharedFlow<Unit>()

    // Delegate to DiceManager
    val diceImages = diceManager.diceImages
    val isRolling = diceManager.isRolling
    val heldDice = diceManager.heldDice

    init {
        viewModelScope.launch {
            // Load vibration setting
            dataStoreManager.getVibrationEnabled()
                .collect { enabled -> _vibrationEnabled.value = enabled }

            if (_selectedBoard.value.isEmpty()) setSelectedBoard(GameBoard.PIG.modeName)
            getBoardColor()

            // Store the current state in a local variable to avoid smart cast issues
            val currentState = _gameState.value
            if (currentState is GameScoreState.PigScoreState &&
                currentState.playerScores.isEmpty()
            ) resetGame()
        }
    }

    /**
     * Sets the number of dice for custom game variants.
     * Updates both the game state and dice manager configuration.
     *
     * @param count Number of dice to use (will be constrained to valid range)
     */
    fun setDiceCount(count: Int) {
        val customState = (_gameState.value as? GameScoreState.CustomScoreState) ?: return
        _gameState.value = myGameManager.setDiceCount(customState, count)
        diceManager.setDiceCount(count)
    }

    fun addPlayer() {
        val customState = (_gameState.value as? GameScoreState.CustomScoreState) ?: return
        _gameState.value = myGameManager.addPlayer(customState)
    }

    fun updatePlayerName(playerIndex: Int, name: String) {
        val customState = (_gameState.value as? GameScoreState.CustomScoreState) ?: return
        _gameState.value = myGameManager.updatePlayerName(customState, playerIndex, name)
    }

    fun addScore(playerIndex: Int, score: Int) {
        val customState = (_gameState.value as? GameScoreState.CustomScoreState) ?: return
        _gameState.value = myGameManager.addScore(customState, playerIndex, score)
    }

    fun addNote(playerIndex: Int, note: String) {
        val customState = (_gameState.value as? GameScoreState.CustomScoreState) ?: return
        _gameState.value = myGameManager.addNote(customState, playerIndex, note)
    }

    /**
     * Processes a dice roll action.
     *
     * This method:
     * 1. Validates if rolling is allowed
     * 2. Triggers dice animation
     * 3. Provides haptic feedback if enabled
     * 4. Updates game state based on results
     */
    fun rollDice() {
        if (isRolling.value || !isRollAllowed.value) return
        viewModelScope.launch {
            trackDecision()
            trackRoll()
            _isLoading.value = true
            val results = diceManager.rollDiceForBoard(_selectedBoard.value)
            if (_vibrationEnabled.value) provideHapticFeedback()
            // Process game state after rolling
            val newState = processGameState(results)
            _gameState.value = newState
        }
    }

    /**
     * Toggles whether a specific die is held.
     *
     * Used in games where players can choose which dice to keep
     * between rolls (Greed, Balut).
     *
     * @param index Index of the die to toggle
     */
    fun toggleDiceHold(index: Int) = diceManager.toggleHold(index)

    /**
     * Determines if the AI should bank based on the current game variant
     *
     * @param currentTurnScore Points accumulated in current turn
     * @param aiTotalScore AI's total score
     * @param playerTotalScore Player's total score
     * @return true if AI should bank, false to continue rolling
     */
    fun shouldAIBank(currentTurnScore: Int, aiTotalScore: Int, playerTotalScore: Int): Boolean =
        when (_selectedBoard.value) {
            GameBoard.PIG.modeName -> pigGameManager.shouldAIBank(
                currentTurnScore = currentTurnScore,
                aiTotalScore = aiTotalScore,
                playerTotalScore = playerTotalScore
            )

            GameBoard.GREED.modeName -> greedGameManager.shouldAIBank(
                currentTurnScore = currentTurnScore,
                aiTotalScore = aiTotalScore
            )

            else -> false
        }

    fun setGameName(name: String) {
        val customState = (_gameState.value as? GameScoreState.CustomScoreState) ?: return
        _gameState.value = myGameManager.setGameName(customState, name)
    }

    fun endPigTurn() {
        val pigState = (_gameState.value as? GameScoreState.PigScoreState) ?: return
        if (pigState.isGameOver) {
            val playerScore = pigState.playerScores[0] ?: 0
            val aiScore = pigState.playerScores[AI_PLAYER_ID.hashCode()] ?: 0
            handleGameEnd(playerScore, playerScore > aiScore)
        }
        viewModelScope.launch {
            val currentState = _gameState.value as? GameScoreState.PigScoreState ?: return@launch
            val newState = pigGameManager.bankScore(currentState)
            _gameState.value = newState
            if (newState.isGameOver) {
                _showWinDialog.value = true
                val playerScore = newState.playerScores[0] ?: 0
                val aiScore = newState.playerScores[AI_PLAYER_ID.hashCode()] ?: 0
                handleGameEnd(playerScore, playerScore > aiScore)
            }
        }
    }

    fun endGreedTurn() {
        val greedState = (_gameState.value as? GameScoreState.GreedScoreState) ?: return
        if (greedState.isGameOver) {
            val playerScore = greedState.playerScores[0] ?: 0
            val aiScore = greedState.playerScores[AI_PLAYER_ID.hashCode()] ?: 0
            handleGameEnd(playerScore, playerScore > aiScore)
        }
        val currentState = (_gameState.value as? GameScoreState.GreedScoreState) ?: return
        val newState = greedGameManager.bankScore(currentState)
        _gameState.value = newState
        if (newState.isGameOver) {
            _showWinDialog.value = true
            val playerScore = newState.playerScores[0] ?: 0
            val aiScore = newState.playerScores[AI_PLAYER_ID.hashCode()] ?: 0
            handleGameEnd(playerScore, playerScore > aiScore)
        }
    }

    fun endBalutTurn(category: String) {
        val currentState = (_gameState.value as? GameScoreState.BalutScoreState) ?: return
        val dice = getCurrentRolls()
        val newState = balutGameManager.scoreCategory(currentState, dice, category)

        _gameState.value = newState
        if (newState.isGameOver) {
            _showWinDialog.value = true
            val playerScore = newState.playerScores[0]?.values?.sum() ?: 0
            val aiScore = newState.playerScores[AI_PLAYER_ID.hashCode()]?.values?.sum() ?: 0
            // Player wins if they have more points
            handleGameEnd(playerScore, playerScore > aiScore)
        }
    }

    fun resetHeldDice() = diceManager.resetHeldDice()

    fun resetScores() {
        val customState = (_gameState.value as? GameScoreState.CustomScoreState) ?: return
        _gameState.value = myGameManager.resetScores(customState)
    }

    fun resetGame() = viewModelScope.launch {
        // Reset all game state first
        _showWinDialog.value = false
        _heldDice.value = emptySet()
        diceManager.resetGame()

        // Start timing for the new game
        statisticsManager.startGameTiming()

        // Then initialize new game
        _gameState.value = when (GameBoard.valueOf(_selectedBoard.value.uppercase())) {
            GameBoard.PIG -> pigGameManager.initializeGame()
            GameBoard.GREED -> greedGameManager.initializeGame()
            GameBoard.BALUT -> balutGameManager.initializeGame()
            GameBoard.CUSTOM -> myGameManager.initializeGame()
        }
    }

    /**
     * Manages shake detection for dice rolling.
     *
     * Enables shake-to-roll functionality when:
     * - Rolling is currently allowed
     * - No roll is in progress
     */
    @OptIn(FlowPreview::class)
    fun resumeShakeDetection() {
        shakeDetectionManager.setOnShakeListener {
            viewModelScope.launch {
                shakeFlow.emit(Unit)
            }
        }
        shakeDetectionManager.startListening()
        viewModelScope.launch {
            shakeFlow
                .debounce(300) // Debounce for 300ms
                .collect {
                    if (!isRolling.value && isRollAllowed.value) rollDice()
                }
        }
    }

    fun pauseShakeDetection() {
        shakeDetectionManager.clearOnShakeListener()
        shakeDetectionManager.stopListening()
    }

    fun setVibrationEnabled(enabled: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        dataStoreManager.setVibrationEnabled(enabled)
    }

    fun setSelectedBoard(board: String) {
        _selectedBoard.value = board
        resetGame()
    }

    fun chooseAICategory(diceResults: List<Int>): String {
        val currentState = (_gameState.value as? GameScoreState.BalutScoreState) ?: run {
            Timber.e("Invalid game state for AI category selection")
            return balutGameManager.initializeGame().let {
                balutGameManager.chooseAICategory(diceResults, it)
            }
        }
        return balutGameManager.chooseAICategory(diceResults, currentState)
    }

    fun getCurrentRolls(): List<Int> = diceManager.currentRolls.value

    private fun getBoardColor() = viewModelScope.launch {
        dataStoreManager.getBoardColor().collect {
            _boardColor.value = it
        }
    }

    private fun provideHapticFeedback() {
        if (!_vibrationEnabled.value) return

        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    val isRollAllowed: StateFlow<Boolean> = combine(
        _gameState,
        _selectedBoard,
        diceManager.isRolling
    ) { gameState, selectedBoard, isRolling ->
        when {
            isRolling -> false
            else -> true
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        true
    )

    private fun trackDecision() {
        gameTracker.trackDecision()
    }

    private fun trackRoll() {
        gameTracker.trackRoll()
    }

    private fun handleGameEnd(finalScore: Int, isWin: Boolean) = viewModelScope.launch {
        try {
            // Set game over state immediately to prevent further moves
            _gameState.value = when (val currentState = _gameState.value) {
                is GameScoreState.PigScoreState -> currentState.copy(isGameOver = true)
                is GameScoreState.GreedScoreState -> currentState.copy(isGameOver = true)
                is GameScoreState.BalutScoreState -> currentState.copy(isGameOver = true)
                is GameScoreState.CustomScoreState -> currentState.copy(isGameOver = true)
                else -> return@launch
            }

            // Show win dialog after state is updated
            _showWinDialog.value = true

            // Update statistics based on game mode
            when (_gameState.value) {
                is GameScoreState.PigScoreState -> {
                    statisticsManager.updateGameStatistics(
                        gameMode = GameBoard.PIG.modeName,
                        score = finalScore,
                        isWin = isWin
                    )
                }

                is GameScoreState.GreedScoreState -> {
                    statisticsManager.updateGameStatistics(
                        gameMode = GameBoard.GREED.modeName,
                        score = finalScore,
                        isWin = isWin
                    )
                }

                is GameScoreState.BalutScoreState -> {
                    statisticsManager.updateGameStatistics(
                        gameMode = GameBoard.BALUT.modeName,
                        score = finalScore,
                        isWin = isWin
                    )
                }

                is GameScoreState.CustomScoreState -> {/* Custom games don't contribute to statistics */
                }

                else -> {}
            }
        } catch (e: Exception) {
            Timber.e(e, "Error handling game end: ${e.message}")
        }
    }

    private suspend fun processGameState(results: List<Int>): GameScoreState {
        // Prevent processing if game is over
        if (_gameState.value.isGameOver == true) _gameState.value

        return when (val currentState = _gameState.value) {
            is GameScoreState.PigScoreState -> withContext(Dispatchers.Default) {
                pigGameManager.handleTurn(currentState, results.firstOrNull())
            }

            is GameScoreState.GreedScoreState -> withContext(Dispatchers.Default) {
                greedGameManager.handleTurn(results, currentState, _heldDice.value)
            }

            is GameScoreState.BalutScoreState -> withContext(Dispatchers.Default) {
                balutGameManager.handleTurn(results, currentState, _heldDice.value)
            }

            is GameScoreState.CustomScoreState -> withContext(Dispatchers.Default) {
                myGameManager.handleTurn(currentState, results)
            }

            else -> throw IllegalArgumentException("Unsupported Game State")
        }
    }

    /**
     * Updates the game state with dice values detected from the camera.
     *
     * @param detectedValues List of detected dice values
     */
    fun updateDiceFromDetection(detectedValues: List<Int>) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Process game state with detected values
                val newState = processGameState(detectedValues)
                _gameState.value = newState
            } catch (e: Exception) {
                Timber.e(e, "Error updating dice from detection: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
