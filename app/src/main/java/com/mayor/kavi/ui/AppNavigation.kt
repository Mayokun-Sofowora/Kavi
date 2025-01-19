package com.mayor.kavi.ui

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.*
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.mayor.kavi.ui.screens.modes.*
import com.mayor.kavi.ui.screens.gameboards.*
import com.mayor.kavi.ui.screens.main.*
import com.mayor.kavi.util.NavigationGraph
import com.mayor.kavi.ui.viewmodel.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import com.mayor.kavi.util.Screen

@Composable
fun AppNavigation() {
    val navController: NavHostController = rememberNavController()
    val appViewModel: AppViewModel = hiltViewModel()
    val gameViewModel: GameViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = NavigationGraph.Main.route
    ) {
        // Main Navigation Graph
        navigation(
            startDestination = Screen.MainMenu.route,
            route = NavigationGraph.Main.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { 1000 },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -1000 },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            composable(Screen.MainMenu.route) { MainMenuScreen(navController) }
            composable(Screen.Settings.route) { SettingsScreen(gameViewModel, navController) }
            composable(Screen.Statistics.route) { 
                StatisticsScreen(appViewModel, navController)
            }
            composable(Screen.Instructions.route) {
                InstructionsScreen(navController, startPage = 0, showOnlyPage = false)
            }
            composable(
                route = Screen.InstructionsShort.route,
                arguments = listOf(navArgument("page") { type = NavType.IntType })
            ) { backStackEntry ->
                val page = backStackEntry.arguments?.getInt("page") ?: 1
                InstructionsScreen(navController, startPage = page, showOnlyPage = true)
            }
        }

        // Game Navigation Graph
        navigation(
            startDestination = Screen.Start.route,
            route = NavigationGraph.Game.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { 1000 },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -1000 },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            composable(Screen.Start.route) { InterfaceModeScreen(navController, appViewModel) }
            composable(Screen.Boards.route) { BoardsScreen(gameViewModel, navController) }
            // Game Boards
            composable(Screen.Board.One.route) { BoardOneScreen(gameViewModel, navController) }
            composable(Screen.Board.Two.route) { BoardTwoScreen(gameViewModel, navController) }
            composable(Screen.Board.Three.route) { BoardThreeScreen(gameViewModel, navController) }
            composable(Screen.Board.Four.route) { BoardFourScreen(gameViewModel, navController) }
            // Virtual Mode
            composable(Screen.Virtual.route) {
                VirtualModeScreen(
                    onNavigateBack = {
                        navController.navigateUp()
                    }
                )
            }
        }
    }
}
