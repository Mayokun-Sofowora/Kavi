package com.mayor.kavi.util

import androidx.navigation.NavController

/**
 * Extension functions for NavController to navigate to different screens. These are helper
 * functions for navigating between screens in the app.
 */

// Main Menu Navigation
fun NavController.navigateToSettings() = navigate(Screen.Settings.route)
fun NavController.navigateToStatistics() = navigate(Screen.Statistics.route)
fun NavController.navigateToInstructions(page: Int? = null) =
    if (page != null) navigate(Screen.InstructionsShort.createRoute(page))
    else navigate(Screen.Instructions.route)

// Game Navigation
fun NavController.navigateToStart() = navigate(Screen.Start.route)
fun NavController.navigateToBoards() = navigate(Screen.Boards.route)
fun NavController.navigateToBoard(board: Screen.Board) = navigate(board.route)
fun NavController.navigateToVirtual() = navigate(Screen.Virtual.route)
fun NavController.exitGame() = navigate(Screen.Boards.route) {
    popUpTo(Screen.Boards.route) { inclusive = false }
    launchSingleTop = true
}
