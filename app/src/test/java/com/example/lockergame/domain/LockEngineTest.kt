package com.example.lockergame.domain

import com.example.lockergame.domain.config.DifficultyConfigs
import com.example.lockergame.domain.engine.LockEngine
import com.example.lockergame.domain.model.LockCombination
import com.example.lockergame.domain.model.LockDifficulty
import com.example.lockergame.domain.model.LockEngineEvent
import com.example.lockergame.domain.model.LockEngineOutput
import com.example.lockergame.domain.model.LockPhase
import com.example.lockergame.domain.model.RotationDirection
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LockEngineTest {

    @Test
    fun easy_unlock_with_tolerance() {
        val engine = LockEngine(
            combination = LockCombination(listOf(10, 20)),
            config = DifficultyConfigs.forDifficulty(LockDifficulty.Easy, 60),
        )

        engine.dispatch(LockEngineEvent.StartAttempt)
        rotateTo(engine, 12, RotationDirection.CounterClockwise)
        val secondStepOutputs = rotateTo(engine, 18, RotationDirection.CounterClockwise)

        assertThat(secondStepOutputs).contains(LockEngineOutput.Unlocked)
        assertThat(engine.state.isUnlocked).isTrue()
    }

    @Test
    fun medium_requires_direction_changes() {
        val engine = LockEngine(
            combination = LockCombination(listOf(50, 10, 55)),
            config = DifficultyConfigs.forDifficulty(LockDifficulty.Medium, 60),
        )

        engine.dispatch(LockEngineEvent.StartAttempt)
        val wrongOutputs = rotateTo(engine, 50, RotationDirection.CounterClockwise)

        assertThat(wrongOutputs).contains(LockEngineOutput.WrongMove)
        assertThat(engine.state.currentStepIndex).isEqualTo(0)
        assertThat(engine.state.phase).isEqualTo(LockPhase.Step1)
    }

    @Test
    fun hard_requires_clearing_turns() {
        val engine = LockEngine(
            combination = LockCombination(listOf(50, 20, 10)),
            config = DifficultyConfigs.forDifficulty(LockDifficulty.Hard, 60),
        )

        engine.dispatch(LockEngineEvent.StartAttempt)
        rotate(engine, RotationDirection.Clockwise, 10)

        assertThat(engine.state.phase).isEqualTo(LockPhase.Clearing)

        rotate(engine, RotationDirection.Clockwise, 120)
        rotateTo(engine, 50, RotationDirection.Clockwise)

        assertThat(engine.state.phase).isEqualTo(LockPhase.Step2)
    }

    @Test
    fun hard_requires_passing_first_number_before_second() {
        val engine = LockEngine(
            combination = LockCombination(listOf(50, 55, 10)),
            config = DifficultyConfigs.forDifficulty(LockDifficulty.Hard, 60),
        )

        engine.dispatch(LockEngineEvent.StartAttempt)
        rotate(engine, RotationDirection.Clockwise, 120)
        rotateTo(engine, 50, RotationDirection.Clockwise)
        rotateTo(engine, 55, RotationDirection.CounterClockwise)

        assertThat(engine.state.currentStepIndex).isEqualTo(1)

        val outputs = rotate(engine, RotationDirection.CounterClockwise, 60)

        assertThat(outputs).contains(LockEngineOutput.CorrectStepHit)
        assertThat(engine.state.currentStepIndex).isEqualTo(2)
    }

    @Test
    fun expert_resets_on_wrong_direction() {
        val engine = LockEngine(
            combination = LockCombination(listOf(50, 20, 10, 5)),
            config = DifficultyConfigs.forDifficulty(LockDifficulty.Expert, 60),
        )

        engine.dispatch(LockEngineEvent.StartAttempt)
        rotate(engine, RotationDirection.Clockwise, 180)
        rotateTo(engine, 50, RotationDirection.Clockwise)
        val outputs = rotate(engine, RotationDirection.Clockwise, 1)

        assertThat(outputs).contains(LockEngineOutput.Reset)
        assertThat(engine.state.phase).isEqualTo(LockPhase.Clearing)
    }

    @Test
    fun engine_processes_multi_step_rotary_input_one_step_at_a_time() {
        val engine = LockEngine(
            combination = LockCombination(listOf(50, 20, 10)),
            config = DifficultyConfigs.forDifficulty(LockDifficulty.Hard, 60),
        )

        engine.dispatch(LockEngineEvent.StartAttempt)
        rotate(engine, RotationDirection.Clockwise, 120)
        rotateTo(engine, 50, RotationDirection.Clockwise)
        val outputs = rotate(engine, RotationDirection.CounterClockwise, 90)

        assertThat(outputs).contains(LockEngineOutput.CorrectStepHit)
        assertThat(engine.state.phase).isEqualTo(LockPhase.Step3)
        assertThat(engine.state.dialValue).isEqualTo(20)
    }

    @Test
    fun unlock_event_emitted_once() {
        val engine = LockEngine(
            combination = LockCombination(listOf(58, 5)),
            config = DifficultyConfigs.forDifficulty(LockDifficulty.Easy, 60),
        )

        engine.dispatch(LockEngineEvent.StartAttempt)
        rotateTo(engine, 58, RotationDirection.Clockwise)
        val firstUnlock = rotateTo(engine, 5, RotationDirection.CounterClockwise)
        val secondUnlock = rotate(engine, RotationDirection.CounterClockwise, 3)

        assertThat(firstUnlock.count { it == LockEngineOutput.Unlocked }).isEqualTo(1)
        assertThat(secondUnlock).doesNotContain(LockEngineOutput.Unlocked)
    }

    private fun rotateTo(
        engine: LockEngine,
        target: Int,
        direction: RotationDirection,
    ): List<LockEngineOutput> {
        val current = engine.state.dialValue
        val steps = when (direction) {
            RotationDirection.Clockwise -> (current - target).floorMod(60)
            RotationDirection.CounterClockwise -> (target - current).floorMod(60)
        }
        return rotate(engine, direction, steps)
    }

    private fun rotate(
        engine: LockEngine,
        direction: RotationDirection,
        steps: Int,
    ): List<LockEngineOutput> = engine.dispatch(LockEngineEvent.DialRotated(direction, steps))

    private fun Int.floorMod(divisor: Int): Int {
        val result = this % divisor
        return if (result < 0) result + divisor else result
    }
}
