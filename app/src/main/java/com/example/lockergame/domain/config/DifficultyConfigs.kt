package com.example.lockergame.domain.config

import com.example.lockergame.domain.model.DifficultyInputConfig
import com.example.lockergame.domain.model.LockDifficulty
import com.example.lockergame.domain.model.LockDifficultyConfig
import com.example.lockergame.domain.model.LockTheme

object DifficultyConfigs {
    fun dialDivisionsForTheme(theme: LockTheme): Int = when (theme) {
        LockTheme.ClassicSilver,
        LockTheme.MatteBlack,
        -> 60

        LockTheme.RetroBrass,
        LockTheme.MinimalWhite,
        -> 100
    }

    fun forDifficulty(difficulty: LockDifficulty, dialDivisions: Int): LockDifficultyConfig = when (difficulty) {
        LockDifficulty.Easy -> LockDifficultyConfig(
            difficulty = difficulty,
            combinationLength = 2,
            dialDivisions = dialDivisions,
            tolerance = 2,
            requireClearing = false,
            requiredClearingTurns = 0,
            requireDirectionChanges = false,
            requirePassCount = false,
            resetOnWrongDirection = false,
            resetOnOvershoot = false,
            inputConfig = DifficultyInputConfig(
                rotaryPixelsPerStep = 18f,
                dialStepsPerInputStep = 1,
                tolerance = 2,
                requireClearing = false,
                requiredClearingTurns = 0,
                strictDirection = false,
                strictPassCount = false,
                resetOnMistake = false,
            ),
        )

        LockDifficulty.Medium -> LockDifficultyConfig(
            difficulty = difficulty,
            combinationLength = 3,
            dialDivisions = dialDivisions,
            tolerance = 1,
            requireClearing = false,
            requiredClearingTurns = 0,
            requireDirectionChanges = true,
            requirePassCount = false,
            resetOnWrongDirection = false,
            resetOnOvershoot = false,
            inputConfig = DifficultyInputConfig(
                rotaryPixelsPerStep = 22f,
                dialStepsPerInputStep = 1,
                tolerance = 1,
                requireClearing = false,
                requiredClearingTurns = 0,
                strictDirection = true,
                strictPassCount = false,
                resetOnMistake = false,
            ),
        )

        LockDifficulty.Hard -> LockDifficultyConfig(
            difficulty = difficulty,
            combinationLength = 3,
            dialDivisions = dialDivisions,
            tolerance = 0,
            requireClearing = true,
            requiredClearingTurns = 2,
            requireDirectionChanges = true,
            requirePassCount = true,
            resetOnWrongDirection = true,
            resetOnOvershoot = true,
            inputConfig = DifficultyInputConfig(
                rotaryPixelsPerStep = 26f,
                dialStepsPerInputStep = 1,
                tolerance = 0,
                requireClearing = true,
                requiredClearingTurns = 2,
                strictDirection = true,
                strictPassCount = true,
                resetOnMistake = true,
            ),
        )

        LockDifficulty.Expert -> LockDifficultyConfig(
            difficulty = difficulty,
            combinationLength = 4,
            dialDivisions = dialDivisions,
            tolerance = 0,
            requireClearing = true,
            requiredClearingTurns = 3,
            requireDirectionChanges = true,
            requirePassCount = true,
            resetOnWrongDirection = true,
            resetOnOvershoot = true,
            inputConfig = DifficultyInputConfig(
                rotaryPixelsPerStep = 32f,
                dialStepsPerInputStep = 1,
                tolerance = 0,
                requireClearing = true,
                requiredClearingTurns = 3,
                strictDirection = true,
                strictPassCount = true,
                resetOnMistake = true,
            ),
        )
    }
}
