package com.example.lockergame.domain.engine

import com.example.lockergame.domain.model.LockAttemptState
import com.example.lockergame.domain.model.LockCombination
import com.example.lockergame.domain.model.LockDifficultyConfig
import com.example.lockergame.domain.model.LockEngineEvent
import com.example.lockergame.domain.model.LockEngineOutput
import com.example.lockergame.domain.model.LockPhase
import com.example.lockergame.domain.model.LockProgress
import com.example.lockergame.domain.model.RotationDirection

class LockEngine(
    private val combination: LockCombination,
    private val config: LockDifficultyConfig,
) {
    private var stepsInCurrentDirection = 0
    private var unlockEmitted = false

    var state: LockAttemptState = initialState()
        private set

    fun progress(): LockProgress = LockProgress(
        currentStepIndex = state.currentStepIndex,
        totalSteps = combination.values.size,
        phase = state.phase,
    )

    fun dispatch(event: LockEngineEvent): List<LockEngineOutput> = when (event) {
        LockEngineEvent.StartAttempt -> {
            resetToStart()
            listOf(LockEngineOutput.None)
        }

        LockEngineEvent.ResetAttempt -> {
            resetToStart()
            listOf(LockEngineOutput.Reset)
        }

        is LockEngineEvent.DialRotated -> processDialRotation(event.direction, event.steps)
    }

    private fun processDialRotation(
        direction: RotationDirection,
        steps: Int,
    ): List<LockEngineOutput> {
        if (steps <= 0 || state.isUnlocked) return listOf(LockEngineOutput.None)
        val outputs = mutableListOf<LockEngineOutput>()
        repeat(steps) {
            val stepOutput = processSingleDialStep(direction)
            if (stepOutput != LockEngineOutput.None) outputs += stepOutput
        }
        return if (outputs.isEmpty()) listOf(LockEngineOutput.None) else outputs
    }

    private fun processSingleDialStep(direction: RotationDirection): LockEngineOutput {
        val previousValue = state.dialValue
        val directionChanged = state.lastDirection != null && state.lastDirection != direction
        if (directionChanged) {
            stepsInCurrentDirection = 0
            state = state.copy(fullTurnsInCurrentDirection = 0)
        }

        val newValue = rotate(previousValue, direction)
        stepsInCurrentDirection += 1
        state = state.copy(
            dialValue = newValue,
            lastDirection = direction,
            fullTurnsInCurrentDirection = stepsInCurrentDirection / config.dialDivisions,
        )

        if (state.phase == LockPhase.Clearing) {
            return handleClearing(direction)
        }

        val stepIndex = state.currentStepIndex
        if (stepIndex >= combination.values.size) return LockEngineOutput.None

        if (config.requireDirectionChanges && direction != expectedDirection(stepIndex)) {
            return onWrongMove(fullReset = config.resetOnWrongDirection)
        }

        if (stepIndex == 1 && config.requirePassCount) {
            val firstTarget = combination.values.first()
            if (matchesTarget(newValue, firstTarget, config.tolerance)) {
                incrementPassedTarget(firstTarget)
            }
        }

        return maybeCompleteCurrentStep(direction)
    }

    private fun handleClearing(direction: RotationDirection): LockEngineOutput {
        if (direction != RotationDirection.Clockwise) {
            return onWrongMove(fullReset = true)
        }
        if (state.fullTurnsInCurrentDirection >= config.requiredClearingTurns) {
            state = state.copy(
                phase = phaseForStepIndex(0),
                currentStepIndex = 0,
            )
            return maybeCompleteCurrentStep(direction)
        }
        return LockEngineOutput.None
    }

    private fun maybeCompleteCurrentStep(direction: RotationDirection): LockEngineOutput {
        val stepIndex = state.currentStepIndex
        val target = combination.values[stepIndex]
        if (!matchesTarget(state.dialValue, target, config.tolerance)) return LockEngineOutput.None

        if (config.requireDirectionChanges && direction != expectedDirection(stepIndex)) {
            return onWrongMove(fullReset = config.resetOnWrongDirection)
        }

        if (stepIndex == 1 && config.requirePassCount) {
            val firstTarget = combination.values.first()
            if ((state.passedTargets[firstTarget] ?: 0) < 1) {
                return LockEngineOutput.None
            }
        }

        val nextStepIndex = stepIndex + 1
        if (nextStepIndex >= combination.values.size) {
            if (!unlockEmitted) {
                unlockEmitted = true
                state = state.copy(
                    phase = LockPhase.Unlocked,
                    currentStepIndex = combination.values.size,
                    isUnlocked = true,
                )
                return LockEngineOutput.Unlocked
            }
            return LockEngineOutput.None
        }

        state = state.copy(
            phase = phaseForStepIndex(nextStepIndex),
            currentStepIndex = nextStepIndex,
        )
        if (nextStepIndex == 1) {
            state = state.copy(passedTargets = emptyMap())
        }
        return LockEngineOutput.CorrectStepHit
    }

    private fun onWrongMove(fullReset: Boolean): LockEngineOutput {
        val mistakeCount = state.mistakeCount + 1
        if (fullReset) {
            resetToStart(mistakeCount)
            return LockEngineOutput.Reset
        }

        state = state.copy(
            phase = phaseForStepIndex(state.currentStepIndex),
            passedTargets = emptyMap(),
            mistakeCount = mistakeCount,
        )
        return LockEngineOutput.WrongMove
    }

    private fun incrementPassedTarget(target: Int) {
        val updated = state.passedTargets.toMutableMap()
        updated[target] = (updated[target] ?: 0) + 1
        state = state.copy(passedTargets = updated)
    }

    private fun rotate(value: Int, direction: RotationDirection): Int = when (direction) {
        RotationDirection.Clockwise -> (value - 1).floorMod(config.dialDivisions)
        RotationDirection.CounterClockwise -> (value + 1).floorMod(config.dialDivisions)
    }

    private fun matchesTarget(current: Int, target: Int, tolerance: Int): Boolean {
        val direct = kotlin.math.abs(current - target)
        val wrapped = config.dialDivisions - direct
        return minOf(direct, wrapped) <= tolerance
    }

    private fun expectedDirection(stepIndex: Int): RotationDirection = when (stepIndex % 2) {
        0 -> RotationDirection.Clockwise
        else -> RotationDirection.CounterClockwise
    }

    private fun resetToStart(mistakes: Int = state.mistakeCount) {
        stepsInCurrentDirection = 0
        unlockEmitted = false
        state = initialState().copy(mistakeCount = mistakes)
    }

    private fun initialState(): LockAttemptState = LockAttemptState(
        phase = if (config.requireClearing) LockPhase.Clearing else phaseForStepIndex(0),
        dialValue = 0,
        currentStepIndex = 0,
        lastDirection = null,
        fullTurnsInCurrentDirection = 0,
        passedTargets = emptyMap(),
        isUnlocked = false,
        mistakeCount = 0,
    )

    private fun phaseForStepIndex(stepIndex: Int): LockPhase = when (stepIndex) {
        0 -> LockPhase.Step1
        1 -> LockPhase.Step2
        2 -> LockPhase.Step3
        3 -> LockPhase.Step4
        else -> LockPhase.ReadyToOpen
    }

    private fun Int.floorMod(divisor: Int): Int {
        val result = this % divisor
        return if (result < 0) result + divisor else result
    }
}
