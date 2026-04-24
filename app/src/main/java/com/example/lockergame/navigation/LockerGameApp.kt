package com.example.lockergame.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lockergame.feature.game.GameScreen
import com.example.lockergame.feature.game.GameViewModel
import com.example.lockergame.feature.home.HomeScreen
import com.example.lockergame.feature.settings.CustomizationScreen
import com.example.lockergame.feature.settings.DifficultyScreen
import com.example.lockergame.feature.settings.SettingsViewModel
import com.example.lockergame.ui.theme.LockerGameTheme

private object Routes {
    const val Home = "home"
    const val Difficulty = "difficulty"
    const val Customization = "customization"
    const val Game = "game"
}

@Composable
fun LockerGameApp() {
    val navController = rememberNavController()
    LockerGameTheme {
        NavHost(
            navController = navController,
            startDestination = Routes.Home,
        ) {
            composable(Routes.Home) {
                HomeScreen(
                    onStart = { navController.navigate(Routes.Game) },
                    onDifficulty = { navController.navigate(Routes.Difficulty) },
                    onCustomize = { navController.navigate(Routes.Customization) },
                )
            }
            composable(Routes.Difficulty) {
                val viewModel: SettingsViewModel = hiltViewModel()
                DifficultyScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onPlay = { navController.navigate(Routes.Game) },
                )
            }
            composable(Routes.Customization) {
                val viewModel: SettingsViewModel = hiltViewModel()
                CustomizationScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.Game) {
                val viewModel: GameViewModel = hiltViewModel()
                GameScreen(
                    viewModel = viewModel,
                    onExit = { navController.popBackStack() },
                    onGoToDifficulty = { navController.navigate(Routes.Difficulty) },
                )
            }
        }
    }
}
