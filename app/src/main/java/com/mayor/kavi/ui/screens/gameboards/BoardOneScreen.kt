package com.mayor.kavi.ui.screens.gameboards

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mayor.kavi.R
import com.mayor.kavi.data.manager.SettingsManager.Companion.LocalSettingsManager
import com.mayor.kavi.data.models.*
import com.mayor.kavi.data.models.enums.DiceArrangement
import com.mayor.kavi.data.models.enums.GameBoard
import com.mayor.kavi.ui.viewmodel.*
import com.mayor.kavi.ui.viewmodel.GameViewModel.Companion.AI_PLAYER_ID
import com.mayor.kavi.ui.components.*
import com.mayor.kavi.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Game screen for the Pig dice game variant (Board One).
 *
 * Features:
 * - Single die rolling interface
 * - Score display for player and AI
 * - Turn management and banking controls
 * - Dynamic message display
 * - Game state visualization
 * - Settings access
 * - Exit game confirmation
 *
 * The screen maintains game state and handles all user interactions
 * for the Pig dice game variant, including roll animations and
 * turn transitions.
 *
 * @param viewModel Game view model for state management and game logic
 * @param navController Navigation controller for screen transitions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardOneScreen(
    viewModel: GameViewModel = hiltViewModel(),
    navController: NavController
) {
    val selectedBoard by viewModel.selectedBoard.collectAsState()
    val gameState by viewModel.gameState.collectAsState()
    val isRolling by viewModel.isRolling.collectAsState()
    val diceImages by viewModel.diceImages.collectAsState()
    val heldDice by viewModel.heldDice.collectAsState()
    val isRollAllowed by viewModel.isRollAllowed.collectAsState()
    val settingsManager = LocalSettingsManager.current
    val boardColor by settingsManager.getBoardColor().collectAsState(initial = "default")
    var showExitGameDialog by remember { mutableStateOf(false) }
    var currentMessage by remember { mutableStateOf("") }
    var hasRolledThisTurn by remember { mutableStateOf(false) }
    var justBanked by remember { mutableStateOf(false) }

    val pigGameMutex = Mutex()

    // Safe cast with early return if wrong game type
    val pigState = (gameState as? GameScoreState.PigScoreState) ?: run {
        LaunchedEffect(Unit) {
            showExitGameDialog = true
            navController.navigateUp()
        }
        return
    }

    BackHandler {
        showExitGameDialog = true
    }

    LaunchedEffect(selectedBoard) {
        if (selectedBoard != GameBoard.PIG.modeName) {
            viewModel.setSelectedBoard(GameBoard.PIG.modeName)
            viewModel.resetGame()
            try {
                pigGameMutex.withLock {
                    hasRolledThisTurn = false
                    justBanked = false
                }
            } catch (e: Exception) {
                println("Error resetting flags: ${e.message}")
            }
        }
    }

    // Reset flags when player changes
    LaunchedEffect(pigState.currentPlayerIndex) {
        hasRolledThisTurn = false
        justBanked = false
    }

    // Update message based on game state changes
    LaunchedEffect(
        pigState.currentPlayerIndex,
        isRolling,
        pigState.currentTurnScore,
        pigState.isGameOver
    ) {
        if (!isRolling) {
            val die = when {
                !hasRolledThisTurn -> 0
                else -> pigState.currentTurnScore
            }

            currentMessage = GameMessages.buildPigScoreMessage(
                die = die,
                score = pigState.playerScores[pigState.currentPlayerIndex] ?: 0,
                playerIndex = pigState.currentPlayerIndex,
                isGameOver = pigState.isGameOver,
                hasRolled = hasRolledThisTurn)

            if (!hasRolledThisTurn && pigState.currentTurnScore > 0) {
                hasRolledThisTurn = true
            }
        }
    }

    // Handle AI turns
    LaunchedEffect(pigState.currentPlayerIndex, isRolling) {
        if (pigState.currentPlayerIndex == AI_PLAYER_ID.hashCode() && !pigState.isGameOver && !isRolling) {
            delay(1000) // Initial delay before AI turn

            // First roll
            viewModel.rollDice()
            delay(2000) // Wait for roll animation

            // After the roll completes, check if AI should bank
            if (!pigState.isGameOver && pigState.currentTurnScore > 0 &&
                viewModel.shouldAIBank(
                    currentTurnScore = pigState.currentTurnScore,
                    aiTotalScore = pigState.playerScores[AI_PLAYER_ID.hashCode()] ?: 0,
                    playerTotalScore = pigState.playerScores[0] ?: 0
                )
            ) {
                delay(1000)
                viewModel.endPigTurn()
            } else if (!pigState.isGameOver && pigState.currentTurnScore == 0) {
                // AI rolled a 1
                delay(1000)
                viewModel.endPigTurn()
            } else if (!pigState.isGameOver && pigState.currentPlayerIndex == AI_PLAYER_ID.hashCode()) {
                // If AI decides to roll again
                delay(1000)
                viewModel.rollDice()
            }
        }
    }

    DisposableEffect(Unit) {
        viewModel.resumeShakeDetection()
        onDispose {
            viewModel.pauseShakeDetection()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pig Dice Game") },
                navigationIcon = {
                    IconButton(onClick = { showExitGameDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.primary_container),
                    titleContentColor = colorResource(id = R.color.on_primary_container)
                ),
                actions = {
                    IconButton(
                        onClick = { navController.navigateToSettings() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            modifier = Modifier.size(24.dp),
                            tint = colorResource(id = R.color.on_primary_container)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .then(
                    when (val color = BoardColors.getColor(boardColor)) {
                        is Color -> Modifier.background(color = color)
                        is Brush -> Modifier.background(brush = color)
                        else -> Modifier.background(color = BoardColors.getColor("default") as Color)
                    }
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Score Display
            ScoreDisplay(
                scores = pigState.playerScores,
                currentTurnScore = pigState.currentTurnScore,
                message = currentMessage,
                currentPlayerIndex = pigState.currentPlayerIndex
            )

            // Dice Display (Pig only uses one die)
            DiceDisplay(
                diceImages = diceImages.take(1),
                isRolling = isRolling,
                heldDice = heldDice,
                isMyTurn = pigState.currentPlayerIndex != AI_PLAYER_ID.hashCode(),
                onDiceHold = null,
                diceSize = 200.dp,
                arrangement = DiceArrangement.ROW,
                modifier = Modifier.padding(top = 10.dp)
            )

            // Turn Score Display
            if (pigState.currentTurnScore > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = colorResource(id = R.color.surface_variant)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Turn Score",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${pigState.currentTurnScore}",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Game Controls
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                GameControls(
                    onRoll = {
                        viewModel.rollDice()
                    },
                    onEndTurn = if (pigState.currentPlayerIndex == AI_PLAYER_ID.hashCode()) null
                    else { ->
                        justBanked = true
                        viewModel.endPigTurn()
                    },
                    isRolling = isRolling,
                    canReroll = isRollAllowed && !pigState.isGameOver &&
                            pigState.currentPlayerIndex != AI_PLAYER_ID.hashCode()
                )
            }
        }
    }

    // Game End Dialog
    if (pigState.isGameOver) {
        EndGameDialog(
            pigState = pigState,
            onPlayAgain = { viewModel.resetGame() },
            onExit = { navController.exitGame() }
        )
    }

    // Exit Game Dialog
    if (showExitGameDialog) {
        ExitDialog(
            onDismiss = { showExitGameDialog = false },
            onConfirm = { navController.exitGame() }
        )
    }
}

@Composable
private fun EndGameDialog(
    pigState: GameScoreState.PigScoreState,
    onPlayAgain: () -> Unit,
    onExit: () -> Unit
) {
    GameEndDialog(
        message = GameMessages.buildPigScoreMessage(
            die = 0,
            score = pigState.playerScores[pigState.currentPlayerIndex] ?: 0,
            playerIndex = pigState.currentPlayerIndex,
            isGameOver = pigState.isGameOver
        ),
        onPlayAgain = onPlayAgain,
        onExit = onExit
    )
}

@Composable
private fun ExitDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Exit Game") },
        text = { Text("Are you sure you want to exit the game? Your progress will be lost.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Exit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
