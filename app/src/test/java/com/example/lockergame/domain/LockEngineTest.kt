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
        val firstHit = rotateTo(engine, 12, RotationDirection.CounterClockwise)
        val firstConfirm = confirm(engine)
        val secondHit = rotateTo(engine, 18, RotationDirection.Clockwise)
        val secondStepOutputs = confirm(engine)

        assertThat(firstHit).contains(LockEngineOutput.CorrectStepHit)
        assertThat(firstConfirm).contains(LockEngineOutput.InputConfirmed)
        assertThat(secondHit).contains(LockEngineOutput.CorrectStepHit)
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
        val wrongOutputs = rotateTo(engine, 50, RotationDirection.Clockwise)
        val confirmOutputs = confirm(engine)

        assertThat(wrongOutputs).contains(LockEngineOutput.WrongMove)
        assertThat(confirmOutputs).contains(LockEngineOutput.WrongMove)
        assertThat(engine.state.currentStepIndex).isEqualTo(0)
        assertThat(engine.state.phase).isEqualTo(LockPhase.Step1)
    }

    @Test
    fun hard_requires_clearing_turns() {
        val engine = LockEngine(
            combination = LockCombination(listOf(40, 20, 10)),
            config = DifficultyConfigs.forDifficulty(LockDifficulty.Hard, 60),
        )

        engine.dispatch(LockEngineEvent.StartAttempt)
        rotate(engine, RotationDirection.Clockwise, 10)

        assertThat(engine.state.phase).isEqualTo(LockPhase.Clearing)

        rotate(engine, RotationDirection.Clockwise, 120)
        rotateTo(engine, 40, RotationDirection.CounterClockwise)
        confirm(engine)

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
        val firstHit = rotateTo(engine, 50, RotationDirection.CounterClockwise)
        confirm(engine)
        rotateTo(engine, 55, RotationDirection.Clockwise)

        assertThat(firstHit).contains(LockEngineOutput.CorrectStepHit)
        assertThat(engine.state.currentStepIndex).isEqualTo(1)

        val outputs = rotate(engine, RotationDirection.Clockwise, 60)
        val confirmOutputs = confirm(engine)

        assertThat(outputs).contains(LockEngineOutput.CorrectStepHit)
        assertThat(confirmOutputs).contains(LockEngineOutput.InputConfirmed)
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
        rotateTo(engine, 50, RotationDirection.CounterClockwise)
        confirm(engine)
        val outputs = rotate(engine, RotationDirection.CounterClockwise, 1)

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
        val firstHit = rotateTo(engine, 50, RotationDirection.CounterClockwise)
        confirm(engine)
        val outputs = rotate(engine, RotationDirection.Clockwise, 90)
        val confirmOutputs = confirm(engine)

        assertThat(firstHit).contains(LockEngineOutput.CorrectStepHit)
        assertThat(outputs).contains(LockEngineOutput.CorrectStepHit)
        assertThat(confirmOutputs).contains(LockEngineOutput.InputConfirmed)
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
        rotateTo(engine, 58, RotationDirection.CounterClockwise)
        confirm(engine)
        rotateTo(engine, 5, RotationDirection.Clockwise)
        val firstUnlock = confirm(engine)
        val secondUnlock = rotate(engine, RotationDirection.Clockwise, 3)

        assertThat(firstUnlock.count { it == LockEngineOutput.Unlocked }).isEqualTo(1)
        assertThat(secondUnlock).doesNotContain(LockEngineOutput.Unlocked)
    }

    @Test
    fun confirm_is_required_for_correct_step_hit() {
        val engine = LockEngine(
            combination = LockCombination(listOf(10, 20)),
            config = DifficultyConfigs.forDifficulty(LockDifficulty.Easy, 60),
        )

        engine.dispatch(LockEngineEvent.StartAttempt)
        val rotateOutputs = rotateTo(engine, 10, RotationDirection.CounterClockwise)
        val confirmOutputs = confirm(engine)

        assertThat(rotateOutputs).contains(LockEngineOutput.CorrectStepHit)
        assertThat(confirmOutputs).contains(LockEngineOutput.InputConfirmed)
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

    private fun confirm(engine: LockEngine): List<LockEngineOutput> = engine.dispatch(LockEngineEvent.ConfirmStep)

    private fun Int.floorMod(divisor: Int): Int {
        val result = this % divisor
        return if (result < 0) result + divisor else result
    }
}
