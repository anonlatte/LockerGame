package com.example.lockergame.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lockergame.feature.game.GameScreen
import com.example.lockergame.feature.game.UnlockResultScreen
import com.example.lockergame.feature.game.GameViewModel
import com.example.lockergame.feature.home.HomeScreen
import com.example.lockergame.feature.settings.CustomizationScreen
import com.example.lockergame.feature.settings.DifficultyScreen
import com.example.lockergame.feature.settings.SettingsViewModel
import com.example.lockergame.domain.model.LockDifficulty
import com.example.lockergame.domain.model.LockTheme
import androidx.navigation.navArgument
import com.example.lockergame.ui.theme.LockerGameTheme

private object Routes {
    const val Home = "home"
    const val Difficulty = "difficulty"
    const val Customization = "customization"
    const val Game = "game"
    const val UnlockResult = "unlock_result/{theme}/{difficulty}"

    fun unlockResult(theme: LockTheme, difficulty: LockDifficulty): String =
        "unlock_result/${theme.name}/${difficulty.name}"
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
                )
            }
            composable(Routes.Customization) {
                val viewModel: SettingsViewModel = hiltViewModel()
                CustomizationScreen(
                    viewModel = viewModel,
                )
            }
            composable(Routes.Game) {
                val viewModel: GameViewModel = hiltViewModel()
                GameScreen(
                    viewModel = viewModel,
                    onExit = { navController.popBackStack() },
                    onGoToDifficulty = { navController.navigate(Routes.Difficulty) },
                    onShowResult = { state ->
                        navController.navigate(Routes.unlockResult(state.lockTheme, state.difficulty))
                    },
                )
            }
            composable(
                route = Routes.UnlockResult,
                arguments = listOf(
                    navArgument("theme") { type = NavType.StringType },
                    navArgument("difficulty") { type = NavType.StringType },
                ),
            ) { backStackEntry ->
                val lockTheme = LockTheme.valueOf(backStackEntry.arguments?.getString("theme") ?: LockTheme.ClassicSilver.name)
                val difficulty = LockDifficulty.valueOf(backStackEntry.arguments?.getString("difficulty") ?: LockDifficulty.Easy.name)
                UnlockResultScreen(
                    lockTheme = lockTheme,
                    difficulty = difficulty,
                    onReplay = {
                        navController.popBackStack(Routes.Game, inclusive = true)
                        navController.navigate(Routes.Game)
                    },
                    onNextDifficulty = {
                        navController.popBackStack(Routes.Game, inclusive = true)
                        navController.navigate(Routes.Difficulty)
                    },
                    onExit = {
                        navController.popBackStack(Routes.Game, inclusive = true)
                    },
                )
            }
        }
    }
}
