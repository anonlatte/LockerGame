package com.example.lockergame.feature.game

import com.example.lockergame.domain.model.LockDifficulty
import com.example.lockergame.domain.model.LockPhase
import com.example.lockergame.domain.model.LockProgress
import com.example.lockergame.domain.model.LockTheme
import com.example.lockergame.domain.model.UnlockAnimationPhase

data class GameUiState(
    val dialValue: Int = 0,
    val dialRotationDegrees: Float = 0f,
    val difficulty: LockDifficulty = LockDifficulty.Easy,
    val lockTheme: LockTheme = LockTheme.ClassicSilver,
    val phase: LockPhase = LockPhase.Idle,
    val progress: LockProgress = LockProgress(0, 0, LockPhase.Idle),
    val isUnlocked: Boolean = false,
    val unlockAnimationPhase: UnlockAnimationPhase = UnlockAnimationPhase.Locked,
    val tutorialHintsEnabled: Boolean = true,
    val dialDivisions: Int = 60,
    val canAutoConfirm: Boolean = false,
)

sealed interface GameUiEffect {
    data object PerformSubtleHaptic : GameUiEffect
    data object PerformUnlockHaptic : GameUiEffect
    data object PlayUnlockClick : GameUiEffect
    data object StartDoorOpenAnimation : GameUiEffect
    data object NavigateToUnlockResult : GameUiEffect
    data object FlashDialSuccess : GameUiEffect
    data object FlashDialFailure : GameUiEffect
}
