package com.example.lockergame.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.lockergame.domain.model.LockDifficulty
import com.example.lockergame.domain.model.LockTheme
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "locker_game_settings")

@Singleton
class DataStoreSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : SettingsRepository {

    override val settings: Flow<GameSettings> = context.dataStore.data.map { preferences ->
        GameSettings(
            difficulty = preferences[Keys.DIFFICULTY]?.let(LockDifficulty::valueOf) ?: LockDifficulty.Easy,
            lockTheme = preferences[Keys.LOCK_THEME]?.let(LockTheme::valueOf) ?: LockTheme.ClassicSilver,
            soundEnabled = preferences[Keys.SOUND_ENABLED] ?: true,
            hapticsEnabled = preferences[Keys.HAPTICS_ENABLED] ?: true,
            tutorialHintsEnabled = preferences[Keys.TUTORIAL_HINTS_ENABLED] ?: true,
            totalLocksOpened = preferences[Keys.TOTAL_LOCKS_OPENED] ?: 0,
            bestUnlockTimesMillis = LockDifficulty.entries.associateWith { difficulty ->
                preferences[bestTimeKey(difficulty)] ?: Long.MAX_VALUE
            }.filterValues { it != Long.MAX_VALUE },
        )
    }

    override suspend fun setDifficulty(difficulty: LockDifficulty) {
        context.dataStore.edit { it[Keys.DIFFICULTY] = difficulty.name }
    }

    override suspend fun setLockTheme(lockTheme: LockTheme) {
        context.dataStore.edit { it[Keys.LOCK_THEME] = lockTheme.name }
    }

    override suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
    }

    override suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HAPTICS_ENABLED] = enabled }
    }

    override suspend fun setTutorialHintsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.TUTORIAL_HINTS_ENABLED] = enabled }
    }

    override suspend fun recordUnlock(difficulty: LockDifficulty, elapsedMillis: Long) {
        context.dataStore.edit { preferences ->
            preferences[Keys.TOTAL_LOCKS_OPENED] = (preferences[Keys.TOTAL_LOCKS_OPENED] ?: 0) + 1
            val key = bestTimeKey(difficulty)
            val best = preferences[key] ?: Long.MAX_VALUE
            if (elapsedMillis < best) {
                preferences[key] = elapsedMillis
            }
        }
    }

    private fun bestTimeKey(difficulty: LockDifficulty) = longPreferencesKey("best_unlock_time_${difficulty.name}")

    private object Keys {
        val DIFFICULTY = stringPreferencesKey("difficulty")
        val LOCK_THEME = stringPreferencesKey("lock_theme")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val TUTORIAL_HINTS_ENABLED = booleanPreferencesKey("tutorial_hints_enabled")
        val TOTAL_LOCKS_OPENED = intPreferencesKey("total_locks_opened")
    }
}
