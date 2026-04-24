package com.example.lockergame.data.settings

import com.example.lockergame.domain.model.LockDifficulty
import com.example.lockergame.domain.model.LockTheme

data class GameSettings(
    val difficulty: LockDifficulty = LockDifficulty.Easy,
    val lockTheme: LockTheme = LockTheme.ClassicSilver,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val tutorialHintsEnabled: Boolean = true,
    val totalLocksOpened: Int = 0,
    val bestUnlockTimesMillis: Map<LockDifficulty, Long> = emptyMap(),
)
