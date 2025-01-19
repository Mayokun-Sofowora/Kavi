package com.mayor.kavi.util

/**
 * Represents different screens in the application.
 *
 * @param route The unique route identifier for the screen.
 */
sealed class Screen(val route: String) {

    // Main Screens
    object MainMenu : Screen("mainMenu")
    object Settings : Screen("settings")
    object Statistics : Screen("statistics")
    object Instructions : Screen("instructions/full")
    object InstructionsShort : Screen("instructions/short/{page}") {
        fun createRoute(page: Int) = "instructions/short/$page"
    }

    // Game Screens
    object Start : Screen("start")
    object Boards : Screen("classicBoards")
    object Virtual : Screen("virtual")

    // Game Board Screens
    sealed class Board(route: String) : Screen(route) {
        object One : Board("boardOne")
        object Two : Board("boardTwo")
        object Three : Board("boardThree")
        object Four : Board("boardFour")
    }
}
