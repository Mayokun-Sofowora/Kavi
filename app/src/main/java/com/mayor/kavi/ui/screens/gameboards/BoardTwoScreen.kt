package com.mayor.kavi.ui.screens.gameboards

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mayor.kavi.R
import com.mayor.kavi.data.manager.SettingsManager.Companion.LocalSettingsManager
import com.mayor.kavi.data.models.GameScoreState
import com.mayor.kavi.data.models.enums.*
import com.mayor.kavi.ui.components.*
import com.mayor.kavi.ui.viewmodel.GameViewModel
import com.mayor.kavi.ui.viewmodel.GameViewModel.Companion.AI_PLAYER_ID
import com.mayor.kavi.util.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardTwoScreen(
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

    // Safe cast with early return if wrong game type
    val greedState = (gameState as? GameScoreState.GreedScoreState) ?: run {
        LaunchedEffect(Unit) {
            showExitGameDialog = true
            navController.navigateUp()
        }
        return
    }

    BackHandler { showExitGameDialog = true }

    LaunchedEffect(selectedBoard) {
        if (selectedBoard != GameBoard.GREED.modeName) {
            viewModel.setSelectedBoard(GameBoard.GREED.modeName)
            viewModel.resetGame()
        }
    }

    // Update message when game state changes
    var lastRollingPlayer by remember { mutableIntStateOf(greedState.currentPlayerIndex) }
    LaunchedEffect(greedState, isRolling) {
        if (isRolling) {
            lastRollingPlayer = greedState.currentPlayerIndex
        }
        currentMessage = GameMessages.buildGreedScoreMessage(
            dice = greedState.lastRoll,
            score = greedState.playerScores[greedState.currentPlayerIndex] ?: 0,
            turnScore = greedState.currentTurnScore,
            playerIndex = if (greedState.lastRoll.isEmpty()) greedState.currentPlayerIndex else lastRollingPlayer,
            isGameOver = greedState.isGameOver
        )
    }

    // Handle AI turns
    LaunchedEffect(greedState.currentPlayerIndex, isRolling) {
        if (greedState.currentPlayerIndex == AI_PLAYER_ID.hashCode() && !greedState.isGameOver &&
            !isRolling
        ) {
            delay(500) // Short initial delay

            // Keep rolling until AI decides to bank or busts
            while (greedState.currentPlayerIndex == AI_PLAYER_ID.hashCode() &&
                !greedState.isGameOver
            ) {
                // Roll the dice
                viewModel.rollDice()
                delay(1500) // Wait for roll animation and to show result

                // If the turn score is 0, AI busted
                if (greedState.currentTurnScore == 0) {
                    delay(1000) // Wait a moment to show the bust
                    viewModel.endGreedTurn()
                    break
                }

                // Check if AI should bank
                if (viewModel.shouldAIBank(
                        currentTurnScore = greedState.currentTurnScore,
                        aiTotalScore = greedState.playerScores[AI_PLAYER_ID.hashCode()] ?: 0,
                        playerTotalScore = greedState.playerScores[0] ?: 0
                    )
                ) {
                    delay(1000) // Wait a moment before banking
                    viewModel.endGreedTurn()
                    break
                }

                delay(1000) // Wait before potentially rolling again
            }
        }
    }

    // Reset held dice when player changes
    LaunchedEffect(greedState.currentPlayerIndex) {
        viewModel.resetHeldDice()
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
                title = { Text("Greed Dice Game") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Score Display
            ScoreDisplay(
                scores = greedState.playerScores,
                currentTurnScore = greedState.currentTurnScore,
                message = currentMessage,
                currentPlayerIndex = greedState.currentPlayerIndex
            )

            // Dice Display with holdable dice
            DiceDisplay(
                diceImages = diceImages,
                isRolling = isRolling,
                heldDice = heldDice,
                isMyTurn = greedState.currentPlayerIndex != AI_PLAYER_ID.hashCode(),
                onDiceHold = if (greedState.currentPlayerIndex != AI_PLAYER_ID.hashCode()
                    && !isRolling
                ) { index ->
                    viewModel.toggleDiceHold(index)
                } else null,
                arrangement = DiceArrangement.GRID
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Game Controls
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                GameControls(
                    onRoll = { viewModel.rollDice() },
                    onEndTurn = if (greedState.currentPlayerIndex == AI_PLAYER_ID.hashCode()) null
                    else { -> viewModel.endGreedTurn() },
                    isRolling = isRolling,
                    canReroll = isRollAllowed &&
                            !greedState.isGameOver &&
                            greedState.currentPlayerIndex != AI_PLAYER_ID.hashCode() &&
                            greedState.canReroll
                )
            }
            // Scoring Combinations Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = colorResource(id = R.color.surface_variant)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Scoring Combinations",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val scoringCombinations = listOf(
                        "Straight (1-2-3-4-5-6): 1000",
                        "Six of a Kind: 3000 points",
                        "Five of a Kind: 2000 points",
                        "Three Pairs: 1000",
                        "Three of a Kind (of 1): 1000",
                        "Three of a Kind: Number × 100",
                        "Single 1: 100",
                        "Single 5: 50"
                    )

                    scoringCombinations.forEach { combination ->
                        Text(
                            text = combination,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    // Game End Dialog
    if (greedState.isGameOver) {
        EndGameDialog(
            greedState = greedState,
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
    greedState: GameScoreState.GreedScoreState,
    onPlayAgain: () -> Unit,
    onExit: () -> Unit
) {
    val playerScore = greedState.playerScores[0] ?: 0
    val aiScore = greedState.playerScores[AI_PLAYER_ID.hashCode()] ?: 0
    val winningPlayerIndex = if (playerScore > aiScore) 0 else AI_PLAYER_ID.hashCode()
    val winningScore = if (playerScore > aiScore) playerScore else aiScore
    
    GameEndDialog(
        message = GameMessages.buildGreedScoreMessage(
            dice = emptyList(),
            score = winningScore,
            turnScore = 0,
            playerIndex = winningPlayerIndex,
            isGameOver = true
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