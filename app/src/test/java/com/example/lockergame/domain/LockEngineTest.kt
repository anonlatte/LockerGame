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
        val firstHit = rotateTo(engine, 12, RotationDirection.Clockwise)
        val firstConfirm = confirm(engine)
        val secondHit = rotateTo(engine, 18, RotationDirection.CounterClockwise)
        val secondConfirm = confirm(engine)
        val unlockOutputs = rotate(engine, RotationDirection.Clockwise, 8)

        assertThat(firstHit).contains(LockEngineOutput.CorrectStepHit)
        assertThat(firstConfirm).contains(LockEngineOutput.InputConfirmed)
        assertThat(secondHit).contains(LockEngineOutput.CorrectStepHit)
        assertThat(secondConfirm).contains(LockEngineOutput.InputConfirmed)
        assertThat(unlockOutputs).contains(LockEngineOutput.Unlocked)
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

        assertThat(wrongOutputs).contains(LockEngineOutput.Reset)
        assertThat(engine.state.currentStepIndex).isEqualTo(0)
        assertThat(engine.state.phase).isEqualTo(LockPhase.Step1)
    }

    @Test
    fun hard_requires_clearing_turns() {
        val engine = LockEngine(
            combination = LockCombination(listOf(40, 20, 10, 5)),
            config = DifficultyConfigs.forDifficulty(LockDifficulty.Hard, 60),
        )

        engine.dispatch(LockEngineEvent.StartAttempt)
        rotate(engine, RotationDirection.Clockwise, 119)
        assertThat(engine.state.phase).isEqualTo(LockPhase.Clearing)

        rotate(engine, RotationDirection.Clockwise, 1)
        assertThat(engine.state.phase).isEqualTo(LockPhase.Step1)
    }

    @Test
    fun hard_requires_repeated_hits_before_advancing() {
        val engine = LockEngine(
            combination = LockCombination(listOf(50, 20, 10, 5)),
            config = DifficultyConfigs.forDifficulty(LockDifficulty.Hard, 60),
        )

        engine.dispatch(LockEngineEvent.StartAttempt)
        rotate(engine, RotationDirection.Clockwise, 120)

        val firstHits = hitTarget(engine, 50, RotationDirection.Clockwise, hits = 4)
        val firstConfirm = confirm(engine)
        val secondHits = hitTarget(engine, 20, RotationDirection.CounterClockwise, hits = 3)
        val secondConfirm = confirm(engine)

        assertThat(firstHits.count { it == LockEngineOutput.CorrectStepHit }).isEqualTo(1)
        assertThat(firstConfirm).contains(LockEngineOutput.InputConfirmed)
        assertThat(secondHits.count { it == LockEngineOutput.CorrectStepHit }).isEqualTo(1)
        assertThat(secondConfirm).contains(LockEngineOutput.InputConfirmed)
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
        val outputs = rotate(engine, RotationDirection.CounterClockwise, 1)

        assertThat(outputs).contains(LockEngineOutput.Reset)
        assertThat(engine.state.phase).isEqualTo(LockPhase.Clearing)
    }

    @Test
    fun engine_processes_multi_step_rotary_input_one_step_at_a_time() {
        val engine = LockEngine(
            combination = LockCombination(listOf(50, 20, 10)),
            config = DifficultyConfigs.forDifficulty(LockDifficulty.Medium, 60),
        )

        engine.dispatch(LockEngineEvent.StartAttempt)
        val outputs = rotate(engine, RotationDirection.Clockwise, 130)

        assertThat(outputs.count { it == LockEngineOutput.CorrectStepHit }).isEqualTo(1)
        assertThat(engine.state.currentTargetHitCount).isEqualTo(3)

        val confirmOutputs = confirm(engine)

        assertThat(confirmOutputs).contains(LockEngineOutput.InputConfirmed)
        assertThat(engine.state.phase).isEqualTo(LockPhase.Step2)
        assertThat(engine.state.dialValue).isEqualTo(50)
    }

    @Test
    fun unlock_event_emitted_once() {
        val engine = LockEngine(
            combination = LockCombination(listOf(58, 5)),
            config = DifficultyConfigs.forDifficulty(LockDifficulty.Easy, 60),
        )

        engine.dispatch(LockEngineEvent.StartAttempt)
        rotateTo(engine, 58, RotationDirection.Clockwise)
        confirm(engine)
        rotateTo(engine, 5, RotationDirection.CounterClockwise)
        confirm(engine)
        val firstUnlock = rotate(engine, RotationDirection.Clockwise, 8)
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
        val rotateOutputs = rotateTo(engine, 10, RotationDirection.Clockwise)
        val confirmOutputs = confirm(engine)

        assertThat(rotateOutputs).contains(LockEngineOutput.CorrectStepHit)
        assertThat(confirmOutputs).contains(LockEngineOutput.InputConfirmed)
    }

    private fun hitTarget(
        engine: LockEngine,
        target: Int,
        direction: RotationDirection,
        hits: Int,
    ): List<LockEngineOutput> {
        val outputs = mutableListOf<LockEngineOutput>()
        repeat(hits) { index ->
            outputs += if (index == 0) {
                rotateTo(engine, target, direction)
            } else {
                rotate(engine, direction, 60)
            }
        }
        return outputs
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
