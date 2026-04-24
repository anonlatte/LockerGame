package com.example.lockergame.domain.model

enum class LockDifficulty {
    Easy,
    Medium,
    Hard,
    Expert,
}

enum class LockTheme {
    ClassicSilver,
    MatteBlack,
    RetroBrass,
    MinimalWhite,
}

enum class RotationDirection {
    Clockwise,
    CounterClockwise,
}

enum class LockPhase {
    Idle,
    Clearing,
    Step1,
    Step2,
    Step3,
    Step4,
    ReadyToOpen,
    Unlocked,
    Failed,
}

enum class UnlockAnimationPhase {
    Locked,
    Click,
    DoorOpening,
    GoldRevealed,
    Completed,
}

data class DifficultyInputConfig(
    val rotaryPixelsPerStep: Float,
    val dialStepsPerInputStep: Int,
    val tolerance: Int,
    val requireClearing: Boolean,
    val requiredClearingTurns: Int,
    val strictDirection: Boolean,
    val strictPassCount: Boolean,
    val resetOnMistake: Boolean,
)

data class LockCombination(
    val values: List<Int>,
)

data class LockAttemptState(
    val phase: LockPhase,
    val dialValue: Int,
    val currentStepIndex: Int,
    val lastDirection: RotationDirection?,
    val fullTurnsInCurrentDirection: Int,
    val passedTargets: Map<Int, Int>,
    val isCurrentTargetAligned: Boolean,
    val currentTargetHitCount: Int,
    val openProgressSteps: Int,
    val isUnlocked: Boolean,
    val mistakeCount: Int,
)

data class LockProgress(
    val currentStepIndex: Int,
    val totalSteps: Int,
    val phase: LockPhase,
)

data class LockDifficultyConfig(
    val difficulty: LockDifficulty,
    val combinationLength: Int,
    val dialDivisions: Int,
    val tolerance: Int,
    val requireClearing: Boolean,
    val requiredClearingTurns: Int,
    val requireDirectionChanges: Boolean,
    val requirePassCount: Boolean,
    val resetOnWrongDirection: Boolean,
    val resetOnOvershoot: Boolean,
    val inputConfig: DifficultyInputConfig,
)

sealed interface LockEngineEvent {
    data class DialRotated(
        val direction: RotationDirection,
        val steps: Int,
    ) : LockEngineEvent

    data object ConfirmStep : LockEngineEvent
    data object StartAttempt : LockEngineEvent
    data object ResetAttempt : LockEngineEvent
}

sealed interface LockEngineOutput {
    data object None : LockEngineOutput
    data object InputConfirmed : LockEngineOutput
    data object CorrectStepHit : LockEngineOutput
    data object WrongMove : LockEngineOutput
    data object Unlocked : LockEngineOutput
    data object Reset : LockEngineOutput
}

data class RotaryStepResult(
    val direction: RotationDirection,
    val dialSteps: Int,
)
