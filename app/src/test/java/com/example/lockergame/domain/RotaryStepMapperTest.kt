package com.example.lockergame.domain

import com.example.lockergame.domain.config.DifficultyConfigs
import com.example.lockergame.domain.engine.RotaryStepMapper
import com.example.lockergame.domain.model.LockDifficulty
import com.example.lockergame.domain.model.RotationDirection
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RotaryStepMapperTest {

    @Test
    fun rotary_step_mapper_accumulates_deltas_by_difficulty() {
        val mapper = RotaryStepMapper(
            DifficultyConfigs.forDifficulty(LockDifficulty.Expert, 60).inputConfig,
        )

        assertThat(mapper.push(8f)).isNull()

        val first = mapper.push(8f)
        assertThat(first?.dialSteps).isEqualTo(1)
        assertThat(first?.direction).isEqualTo(RotationDirection.CounterClockwise)

        val second = mapper.push(-32f)
        assertThat(second?.dialSteps).isEqualTo(2)
        assertThat(second?.direction).isEqualTo(RotationDirection.Clockwise)
    }
}
