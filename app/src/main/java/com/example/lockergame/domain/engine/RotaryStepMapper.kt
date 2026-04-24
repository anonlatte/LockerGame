package com.example.lockergame.domain.engine

import com.example.lockergame.domain.model.DifficultyInputConfig
import com.example.lockergame.domain.model.RotaryStepResult
import com.example.lockergame.domain.model.RotationDirection
import kotlin.math.abs
import kotlin.math.sign

class RotaryStepMapper(
    private val config: DifficultyInputConfig,
) {
    private var accumulatedDelta = 0f

    fun push(deltaPixels: Float): RotaryStepResult? {
        accumulatedDelta += deltaPixels
        if (abs(accumulatedDelta) < config.rotaryPixelsPerStep) return null

        val inputSteps = (abs(accumulatedDelta) / config.rotaryPixelsPerStep).toInt()
        accumulatedDelta -= sign(accumulatedDelta) * inputSteps * config.rotaryPixelsPerStep

        val direction = if (deltaPixels < 0f) {
            RotationDirection.Clockwise
        } else {
            RotationDirection.CounterClockwise
        }
        return RotaryStepResult(
            direction = direction,
            dialSteps = inputSteps * config.dialStepsPerInputStep,
        )
    }

    fun reset() {
        accumulatedDelta = 0f
    }
}

class DialInputController(
    private val mapper: RotaryStepMapper,
) {
    fun onRotary(deltaPixels: Float): RotaryStepResult? = mapper.push(deltaPixels)

    fun reset() {
        mapper.reset()
    }
}
