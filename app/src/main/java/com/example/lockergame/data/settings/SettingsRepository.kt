package com.example.lockergame.data.settings

import com.example.lockergame.domain.model.LockDifficulty
import com.example.lockergame.domain.model.LockTheme
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<GameSettings>

    suspend fun setDifficulty(difficulty: LockDifficulty)
    suspend fun setLockTheme(lockTheme: LockTheme)
    suspend fun setSoundEnabled(enabled: Boolean)
    suspend fun setHapticsEnabled(enabled: Boolean)
    suspend fun setTutorialHintsEnabled(enabled: Boolean)
    suspend fun recordUnlock(difficulty: LockDifficulty, elapsedMillis: Long)
}
