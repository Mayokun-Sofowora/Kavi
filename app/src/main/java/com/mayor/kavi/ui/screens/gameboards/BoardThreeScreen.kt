package com.mayor.kavi.ui.screens.gameboards

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.mayor.kavi.data.models.GameScoreState
import com.mayor.kavi.data.models.enums.GameBoard
import com.mayor.kavi.ui.components.*
import com.mayor.kavi.ui.viewmodel.GameViewModel
import com.mayor.kavi.util.*
import kotlinx.coroutines.delay

/**
 * Game screen for the Balut dice game variant (Board Three).
 *
 * Features:
 * - Five dice rolling interface
 * - Category-based scoring system
 * - Dice holding mechanism
 * - Score sheet display
 * - AI opponent with strategic decision making
 * - Turn management
 * - Settings access
 *
 * The screen implements the complete Balut game rules, including:
 * - Category selection
 * - Score calculation
 * - Three rolls per turn
 * - Strategic dice holding
 *
 * @param viewModel Game view model for state management and game logic
 * @param navController Navigation controller for screen transitions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardThreeScreen(
    viewModel: GameViewModel = hiltViewModel(),
    navController: NavController
) {
    val selectedBoard by viewModel.selectedBoard.collectAsState()
    val gameState by viewModel.gameState.collectAsState()
    val isRolling by viewModel.isRolling.collectAsState()
    val diceImages by viewModel.diceImages.collectAsState()
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    val heldDice by viewModel.heldDice.collectAsState()
    val isRollAllowed by viewModel.isRollAllowed.collectAsState()
    val settingsManager = LocalSettingsManager.current
    val boardColor by settingsManager.getBoardColor().collectAsState(initial = "default")
    var showExitGameDialog by remember { mutableStateOf(false) }

    // Safe cast with early return if wrong game type
    val balutState = (gameState as? GameScoreState.BalutScoreState) ?: run {
        LaunchedEffect(Unit) {
            showExitGameDialog = true
            navController.navigateUp()
        }
        return
    }

    BackHandler { showExitGameDialog = true }

    LaunchedEffect(selectedBoard) {
        if (selectedBoard != GameBoard.BALUT.modeName) {
            viewModel.setSelectedBoard(GameBoard.BALUT.modeName)
            viewModel.resetGame()
        }
    }

    // Add launched effect for ai turns
    LaunchedEffect(balutState.currentPlayerIndex) {
        viewModel.resetHeldDice()
        selectedCategory = null // Reset category when player changes

        // Handle AI turn if it's AI's turn
        if (balutState.currentPlayerIndex == GameViewModel.AI_PLAYER_ID.hashCode() &&
            !balutState.isGameOver
            && !isRolling
        ) {
            delay(500)

            // First roll
            viewModel.rollDice()
            delay(1000)

            // Second roll if needed
            if (balutState.rollsLeft > 0) {
                viewModel.rollDice()
                delay(1000)
            }

            // Final roll if needed
            if (balutState.rollsLeft > 0) {
                viewModel.rollDice()
                delay(1000)
            }

            // Let AI choose category and end turn
            val diceResults = viewModel.getCurrentRolls()
            val category = viewModel.chooseAICategory(diceResults)
            delay(1000)
            viewModel.endBalutTurn(category)
        }
    }

    // Reset held dice when player changes
    LaunchedEffect(balutState.currentPlayerIndex) {
        viewModel.resetHeldDice()
        selectedCategory = null // Reset category when player changes
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
                title = { Text("Balut Dice Game") },
                navigationIcon = {
                    IconButton(onClick = { showExitGameDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.primary_container),
                    titleContentColor = colorResource(id = R.color.on_primary_container)
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .then(
                    when (val color = BoardColors.getColor(boardColor)) {
                        is Color -> Modifier.background(color = color)
                        is Brush -> Modifier.background(brush = color)
                        else -> Modifier.background(color = BoardColors.getColor("default") as Color)
                    }
                ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Game Info
            item { GameInfoCard(balutState) }
            // Score Display
            item {
                val currentScore =
                    balutState.playerScores[balutState.currentPlayerIndex]?.values?.sum() ?: 0
                val gameMessage = GameMessages.buildBalutCategoryMessage(
                    category = selectedCategory ?: "",
                    playerIndex = balutState.currentPlayerIndex,
                    isGameOver = balutState.isGameOver,
                    totalScore = currentScore
                )
                ScoreDisplay(
                    scores = balutState.playerScores.mapValues { it.value.values.sum() },
                    currentTurnScore = 0,
                    message = gameMessage,
                    currentPlayerIndex = balutState.currentPlayerIndex
                )
            }
            // Dice Display and Controls
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent,
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        DiceDisplay(
                            diceImages = diceImages,
                            isRolling = isRolling,
                            heldDice = heldDice,
                            isMyTurn = true,
                            onDiceHold = { viewModel.toggleDiceHold(it) }
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
                                onSelectedCategory = if (selectedCategory != null) {
                                    { viewModel.endBalutTurn(selectedCategory!!) }
                                } else null,
                                isRolling = isRolling,
                                canReroll = isRollAllowed &&
                                        !balutState.isGameOver &&
                                        balutState.rollsLeft > 0
                            )
                        }
                    }
                }
            }
            // Score Categories
            item {
                BalutCategoriesCard(
                    currentPlayerIndex = balutState.currentPlayerIndex,
                    playerScores = balutState.playerScores,
                    selectedCategory = selectedCategory,
                    onCategorySelect = { selectedCategory = it }
                )
            }
            // Score Display
            item {
                BalutScoreDisplay(
                    playerScores = balutState.playerScores[0] ?: emptyMap(),
                    aiScores = balutState.playerScores[GameViewModel.AI_PLAYER_ID.hashCode()]
                        ?: emptyMap()
                )
            }
        }
    }

    // Game End Dialog
    if (balutState.isGameOver) {
        EndGameDialog(
            balutState = balutState,
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
private fun GameInfoCard(state: GameScoreState.BalutScoreState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.surface_variant),
            contentColor = colorResource(id = R.color.on_surface_variant)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Round ${state.currentRound}/${state.maxRounds}",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Rolls Left: ${state.rollsLeft}",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun BalutCategoriesCard(
    currentPlayerIndex: Int,
    playerScores: Map<Int, Map<String, Int>>,
    selectedCategory: String?,
    onCategorySelect: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.surface_variant)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Categories", style = MaterialTheme.typography.titleMedium)

            // Numbers (Ones through Sixes)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    listOf(
                        "Ones", "Twos", "Threes",
                        "Fours", "Fives", "Sixes"
                    )
                ) { category ->
                    playerScores[currentPlayerIndex]?.containsKey(category)?.let {
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { onCategorySelect(category) },
                            enabled = !it && currentPlayerIndex == 0,
                            label = { Text(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = colorResource(id = R.color.surface_variant),
                                selectedLabelColor = colorResource(id = R.color.on_primary)
                            )
                        )
                    }
                }
            }

            // Special Categories
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    listOf(
                        "Small Straight", "Large Straight", "Full House",
                        "Four of a Kind", "Five of a Kind", "Choice"
                    )
                ) { category ->
                    playerScores[currentPlayerIndex]?.containsKey(category)?.let {
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { onCategorySelect(category) },
                            enabled = !it && currentPlayerIndex == 0,
                            label = { Text(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = colorResource(id = R.color.surface_variant),
                                selectedLabelColor = colorResource(id = R.color.on_primary)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EndGameDialog(
    balutState: GameScoreState.BalutScoreState,
    onPlayAgain: () -> Unit,
    onExit: () -> Unit
) {
    val playerScore = balutState.playerScores[0]?.values?.sum() ?: 0
    val aiScore = balutState.playerScores[GameViewModel.AI_PLAYER_ID.hashCode()]?.values?.sum() ?: 0
    val isPlayerWinner = playerScore > aiScore
    GameEndDialog(
        message = "Your Score: $playerScore\n" +
                "AI Score: $aiScore\n\n" +
                if (isPlayerWinner) "You win with $playerScore points!"
                else "AI wins with $aiScore points. Better luck next time!",
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
