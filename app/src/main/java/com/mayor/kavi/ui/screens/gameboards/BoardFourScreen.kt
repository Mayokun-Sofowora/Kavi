package com.mayor.kavi.ui.screens.gameboards

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mayor.kavi.data.manager.games.MyGameManager
import com.mayor.kavi.data.models.GameScoreState
import com.mayor.kavi.ui.components.DiceDisplay
import com.mayor.kavi.ui.viewmodel.GameViewModel
import com.mayor.kavi.util.*
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.navigation.NavController
import com.mayor.kavi.R
import com.mayor.kavi.data.manager.SettingsManager.Companion.LocalSettingsManager
import com.mayor.kavi.data.models.enums.GameBoard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardFourScreen(
    viewModel: GameViewModel = hiltViewModel(),
    navController: NavController
) {
    val selectedBoard by viewModel.selectedBoard.collectAsState()
    val gameState = viewModel.gameState.collectAsState()
    val isRolling by viewModel.isRolling.collectAsState()
    val diceImages by viewModel.diceImages.collectAsState()
    var gameName by remember { mutableStateOf("") }
    var diceCount by remember { mutableStateOf(MyGameManager.DEFAULT_DICE.toString()) }
    var newPlayerName by remember { mutableStateOf("") }
    var selectedPlayerIndex by remember { mutableIntStateOf(0) }
    var scoreInput by remember { mutableStateOf("") }
    var noteInput by remember { mutableStateOf("") }

    val settingsManager = LocalSettingsManager.current
    var showExitGameDialog by remember { mutableStateOf(false) }
    val boardColor by settingsManager.getBoardColor().collectAsState(initial = "default")

    BackHandler {
        showExitGameDialog = true
    }

    // Initialize game state when the screen is first loaded
    LaunchedEffect(selectedBoard) {
        if (selectedBoard != GameBoard.CUSTOM.modeName) {
            viewModel.setSelectedBoard(GameBoard.CUSTOM.modeName)
            viewModel.resetGame()
        }
    }

    DisposableEffect(Unit) {
        viewModel.resumeShakeDetection()
        onDispose {
            viewModel.pauseShakeDetection()
        }
    }

    // Safe cast with early return if wrong game type
    val customState = (gameState.value as? GameScoreState.CustomScoreState) ?: run {
        LaunchedEffect(Unit) { navController.navigateUp() }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(customState.gameName.ifEmpty { "Custom Dice Board" }) },
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
            // Game Settings Section
            item {
                GameSettingsCard(gameName,
                    onGameNameChange = {
                        gameName = it
                        viewModel.setGameName(it)
                    },
                    diceCount,
                    onDiceCountChange = { dice ->
                        if (true) {
                            diceCount = dice.toString()
                            dice.let { count -> viewModel.setDiceCount(count) }
                        }
                    })
            }

            // Player Management
            item {
                PlayerManagementCard(customState,
                    newPlayerName,
                    onNewPlayerNameChange = { newPlayerName = it },
                    selectedPlayerIndex,
                    onSelectedPlayerChange = { selectedPlayerIndex = it },
                    onAddPlayer = { viewModel.addPlayer() },
                    onUpdatePlayer = {
                        viewModel.updatePlayerName(
                            selectedPlayerIndex,
                            newPlayerName
                        ); newPlayerName = ""
                    }
                )
            }

            // Dice Display
            item {
                DiceCard(isRolling = isRolling, diceImages = diceImages, viewModel = viewModel)
            }

            // Score Management
            item {
                ScoreManagementCard(
                    scoreInput = scoreInput,
                    onScoreInputChange = { newScore ->
                        if (newScore.isEmpty() || newScore.toIntOrNull() != null) {
                            scoreInput = newScore
                        }
                    },
                    noteInput = noteInput,
                    onNoteInputChange = { noteInput = it },
                    onAddNote = {
                        if (noteInput.isNotEmpty()) {
                            viewModel.addNote(selectedPlayerIndex, noteInput)
                            noteInput = ""
                        }
                    },
                    selectedPlayerIndex = selectedPlayerIndex,
                )
            }

            // Score History
            item {
                ScoreHistoryCard(customState, selectedPlayerIndex)
            }

            // Action Buttons
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    ActionButtonsCard(
                        onResetScores = { viewModel.resetScores() })
                }
            }
        }
    }

    // Exit Game Dialog
    if (showExitGameDialog) {
        ExitDialog(
            onDismiss = { showExitGameDialog = false },
            onConfirm = navController::navigateUp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GameSettingsCard(
    gameName: String,
    onGameNameChange: (String) -> Unit,
    diceCount: String,
    onDiceCountChange: (Int) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var tempGameName by remember { mutableStateOf(gameName) }
    val focusManager = LocalFocusManager.current

    Card(
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.surface_variant),
            contentColor = colorResource(id = R.color.on_surface_variant)
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            OutlinedTextField(
                value = if (isEditing) tempGameName else gameName,
                onValueChange = { tempGameName = it },
                label = { Text("Game Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        isEditing = focusState.isFocused
                        if (!focusState.isFocused && tempGameName != gameName) {
                            onGameNameChange(tempGameName)
                        }
                    },
                trailingIcon = {
                    if (isEditing) {
                        IconButton(onClick = {
                            onGameNameChange(tempGameName)
                            isEditing = false
                            focusManager.clearFocus()
                        }) {
                            Icon(Icons.Default.Check, contentDescription = "Confirm")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    onGameNameChange(tempGameName)
                    isEditing = false
                    focusManager.clearFocus()
                })
            )
            DiceCountSelector(
                label = { Text("Dice Count") },
                diceCount = diceCount.toIntOrNull() ?: 0,
                onDiceCountChange = onDiceCountChange,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun DiceCountSelector(
    label: @Composable (() -> Unit)? = null,
    diceCount: Int,
    onDiceCountChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    minDice: Int = 1,
    maxDice: Int = 6
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .sizeIn(minWidth = 200.dp)
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        // Label
        label?.let {
            it()
            Spacer(modifier = Modifier.width(16.dp))
        }

        // Decrement Button
        Button(
            onClick = { if (diceCount > minDice) onDiceCountChange(diceCount - 1) },
            enabled = diceCount > minDice,
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.white),
                contentColor = colorResource(id = R.color.black)
            )
        ) {
            Text("-", style = MaterialTheme.typography.titleLarge)
        }

        // Dice Count Display
        Text(
            text = diceCount.toString(),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Increment Button
        Button(
            onClick = { if (diceCount < maxDice) onDiceCountChange(diceCount + 1) },
            enabled = diceCount < maxDice,
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.white),
                contentColor = colorResource(id = R.color.black)
            )
        ) {
            Text("+", style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun DiceCard(isRolling: Boolean, diceImages: List<Int>, viewModel: GameViewModel) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DiceDisplay(
                diceImages = diceImages,
                isRolling = isRolling,
                heldDice = emptySet(),
                isMyTurn = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.rollDice() },
                enabled = !isRolling,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    contentColor = colorResource(id = R.color.on_primary)
                )
            ) {
                Text("Roll Dice")
            }
        }
    }
}

@Composable
private fun PlayerManagementCard(
    customState: GameScoreState.CustomScoreState,
    newPlayerName: String,
    onNewPlayerNameChange: (String) -> Unit,
    selectedPlayerIndex: Int,
    onSelectedPlayerChange: (Int) -> Unit,
    onAddPlayer: () -> Unit,
    onUpdatePlayer: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var tempPlayerName by remember { mutableStateOf(newPlayerName) }
    val focusManager = LocalFocusManager.current

    Card(
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.surface_variant),
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Players", style = MaterialTheme.typography.titleMedium)
                Button(
                    onClick = onAddPlayer,
                    enabled = customState.playerScores.size < MyGameManager.MAX_PLAYERS
                ) {
                    Text("Add Player")
                }
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(customState.playerNames.toList()) { (index, name) ->
                    FilterChip(
                        selected = selectedPlayerIndex == index,
                        onClick = { onSelectedPlayerChange(index) },
                        label = { Text(name) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = colorResource(id = R.color.surface_variant),
                            selectedLabelColor = colorResource(id = R.color.on_primary)
                        )
                    )
                }
            }

            // Selected Player Controls
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = if (isEditing) tempPlayerName else newPlayerName,
                    onValueChange = { tempPlayerName = it },
                    label = { Text("Player Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            isEditing = focusState.isFocused
                            if (!focusState.isFocused && tempPlayerName != newPlayerName) {
                                onNewPlayerNameChange(tempPlayerName)
                            }
                        },
                    trailingIcon = {
                        if (isEditing) {
                            IconButton(onClick = {
                                onNewPlayerNameChange(tempPlayerName)
                                onUpdatePlayer()
                                isEditing = false
                                focusManager.clearFocus()
                            }) {
                                Icon(Icons.Default.Check, contentDescription = "Confirm")
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        onNewPlayerNameChange(tempPlayerName)
                        onUpdatePlayer()
                        isEditing = false
                        focusManager.clearFocus()
                    })
                )
            }
        }
    }
}

@Composable
private fun ScoreManagementCard(
    scoreInput: String,
    selectedPlayerIndex: Int,
    onScoreInputChange: (String) -> Unit,
    noteInput: String,
    onNoteInputChange: (String) -> Unit,
    onAddNote: () -> Unit
) {
    val viewModel: GameViewModel = hiltViewModel()
    Card(
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.surface_variant)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Score Modifier", style = MaterialTheme.typography.titleMedium)

            // Score Modifiers
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val modifiers = listOf(
                    1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                    11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
                    21, 22, 23, 24, 25, 26, 27, 28, 29, 30,
                    31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
                    41, 42, 43, 44, 45, 46, 47, 48, 49, 50,
                    51, 52, 53, 54, 55, 56, 57, 58, 59, 60,
                    61, 62, 63, 64, 65, 66, 67, 68, 69, 70,
                    71, 72, 73, 74, 75, 76, 77, 78, 79, 80,
                    81, 82, 83, 84, 85, 86, 87, 88, 89, 90,
                    91, 92, 93, 94, 95, 96, 97, 98, 99, 100
                )
                items(modifiers) { modifier ->
                    Button(
                        onClick = {
                            val currentScore = scoreInput.toIntOrNull() ?: 0
                            val newScore = currentScore + modifier
                            onScoreInputChange(newScore.toString())
                            viewModel.addScore(selectedPlayerIndex, newScore)
                        },
                        colors = ButtonDefaults.buttonColors(
                            contentColor = colorResource(id = R.color.on_primary)
                        )
                    ) {
                        Text("+$modifier")
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Score: ${scoreInput.toIntOrNull() ?: 0}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = { onScoreInputChange("0") }
                ) {
                    Text("Reset score")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = noteInput,
                    onValueChange = onNoteInputChange,
                    label = { Text("Note") },
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = onAddNote,
                    enabled = noteInput.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        contentColor = colorResource(id = R.color.on_primary)
                    ),
                ) {
                    Text("Add")
                }
            }
        }
    }
}

@Composable
private fun ScoreHistoryCard(
    customState: GameScoreState.CustomScoreState,
    selectedPlayerIndex: Int
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.surface_variant),
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Notes", style = MaterialTheme.typography.titleMedium)
            customState.scoreHistory[selectedPlayerIndex]?.forEach { note ->
                Text(note, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun ActionButtonsCard(onResetScores: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onResetScores,
            modifier = Modifier.weight(1f),
        ) {
            Text("Reset All")
        }
    }
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
