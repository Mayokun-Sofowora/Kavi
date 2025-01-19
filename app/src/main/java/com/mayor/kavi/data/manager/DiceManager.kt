package com.mayor.kavi.data.manager

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.*
import kotlin.random.Random
import com.mayor.kavi.R
import com.mayor.kavi.data.models.enums.GameBoard

/**
 * Manages dice-related operations, including rolling dice, holding dice, and updating dice images.
 * This class handles game-specific dice rules and updates the state accordingly.
 */
class DiceManager {

    private var _rollingDice = MutableStateFlow<Set<Int>>(emptySet())
    val isRolling = _rollingDice.map { it.isNotEmpty() }.stateIn(
        CoroutineScope(Dispatchers.Main),
        SharingStarted.Eagerly,
        false
    )

    private val _diceImages = MutableStateFlow(List(6) { R.drawable.empty_dice })
    val diceImages: StateFlow<List<Int>> = _diceImages

    private val _heldDice = MutableStateFlow<Set<Int>>(emptySet())
    val heldDice: StateFlow<Set<Int>> = _heldDice.asStateFlow()

    private val _currentRolls = MutableStateFlow(emptyList<Int>())
    val currentRolls: StateFlow<List<Int>> = _currentRolls.asStateFlow()

    private var _customDiceCount = MutableStateFlow(2)

    /**
     * Returns the number of dice needed for the specified board type.
     *
     * @param boardType The name of the board mode (e.g., "PIG", "GREED").
     * @return The number of dice for the specified board type.
     */
    fun getDiceCountForBoard(boardType: String): Int = when (boardType) {
        GameBoard.PIG.modeName -> 1
        GameBoard.GREED.modeName -> 6
        GameBoard.BALUT.modeName -> 6
        GameBoard.CUSTOM.modeName -> _customDiceCount.value
        else -> 1
    }

    /**
     * Sets the number of dice to be used for the custom board.
     * Updates the dice images, clears held dice, and resets current rolls.
     *
     * @param count The number of dice to set, clamped between 1 and 6.
     */
    fun setDiceCount(count: Int) {
        _customDiceCount.value = count.coerceIn(1, 6)
        // Update dice images to match new count
        _diceImages.value = List(_customDiceCount.value) { R.drawable.empty_dice }
        // Clear held dice and current rolls
        _heldDice.value = emptySet()
        _currentRolls.value = emptyList()
    }

    /**
     * Rolls the dice for the specified board type, while preserving the values of held dice.
     *
     * @param boardType The name of the board mode to roll dice for.
     * @return A list of integers representing the results of the dice roll.
     */
    suspend fun rollDiceForBoard(boardType: String): List<Int> {
        val diceCount = getDiceCountForBoard(boardType)
        val effectiveHeldDice = _heldDice.value

        // Set rolling state for non-held dice
        _rollingDice.value = (0 until diceCount).filter { !effectiveHeldDice.contains(it) }.toSet()

        // Generate new results while preserving held dice values
        val newRolls = generateDiceRolls(diceCount)
        val results = List(diceCount) { index ->
            if (effectiveHeldDice.contains(index)) {
                _currentRolls.value.getOrNull(index) ?: newRolls[index]
            } else {
                newRolls[index]
            }
        }
        _currentRolls.value = results
        updateDiceImages(results, effectiveHeldDice)

        // Simulate rolling delay and reset rolling state
        delay(500)
        _rollingDice.value = emptySet()
        return results
    }

    /**
     * Toggles the hold state of a specific die, based on its index.
     * Held dice are preserved across rolls.
     *
     * @param index The index of the die to toggle.
     */
    fun toggleHold(index: Int) {
        if (_currentRolls.value.isEmpty() || index >= _currentRolls.value.size) {
            return
        }
        val currentHeld = _heldDice.value.toMutableSet()
        if (currentHeld.contains(index)) {
            currentHeld.remove(index)
        } else {
            currentHeld.add(index)
        }
        _heldDice.value = currentHeld
    }

    /**
     * Resets the held dice to an empty set.
     */
    fun resetHeldDice() {
        _heldDice.value = emptySet()
    }

    /**
     * Resets the game state, clearing dice images, held dice, rolling state, and current rolls.
     */
    fun resetGame() {
        _diceImages.value = List(6) { R.drawable.empty_dice }
        resetHeldDice()
        _rollingDice.value = emptySet()
        _currentRolls.value = emptyList()
    }

    /**
     * Generates a list of random dice rolls, each between 1 and 6.
     *
     * @param count The number of dice to roll.
     * @return A list of integers representing the dice rolls.
     */
    private fun generateDiceRolls(count: Int): List<Int> {
        val rolls = List(count) { Random.nextInt(6) + 1 }
        return rolls
    }

    /**
     * Updates the dice images based on the results of the rolls and the held dice.
     *
     * @param results A list of integers representing the dice roll results.
     * @param heldDice A set of indices representing the held dice.
     */
    private fun updateDiceImages(results: List<Int>, heldDice: Set<Int>) {
        _diceImages.value = results.mapIndexed { index, result ->
            if (_heldDice.value.contains(index)) {
                _diceImages.value.getOrNull(index) ?: R.drawable.empty_dice
            } else {
                getDiceImage(result)
            }
        }
    }

    /**
     * Returns the resource ID for the image corresponding to a specific dice value.
     *
     * @param value The value of the dice (1 to 6).
     * @return The resource ID of the dice image.
     */
    private fun getDiceImage(value: Int): Int = when (value) {
        1 -> R.drawable.dice_1
        2 -> R.drawable.dice_2
        3 -> R.drawable.dice_3
        4 -> R.drawable.dice_4
        5 -> R.drawable.dice_5
        6 -> R.drawable.dice_6
        else -> R.drawable.empty_dice
    }
}
