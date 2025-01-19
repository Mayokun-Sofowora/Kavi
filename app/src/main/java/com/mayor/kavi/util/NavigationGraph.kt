package com.mayor.kavi.util

/**
 * Represents different navigation graphs in the application.
 *
 * @param route The unique route identifier for the graph.
 */
sealed class NavigationGraph(val route: String) {
    data object Main : NavigationGraph("main_graph")
    data object Game : NavigationGraph("game_graph")
}
