package com.example.lockergame.domain.engine

import com.example.lockergame.domain.model.LockCombination
import javax.inject.Inject
import kotlin.random.Random

class CombinationGenerator @Inject constructor() {
    fun generate(length: Int, dialDivisions: Int): LockCombination {
        val values = mutableSetOf<Int>()
        while (values.size < length) {
            values += Random.nextInt(0, dialDivisions)
        }
        return LockCombination(values.toList())
    }
}
