package com.example.lockergame.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lockergame.data.settings.GameSettings
import com.example.lockergame.data.settings.SettingsRepository
import com.example.lockergame.domain.model.LockDifficulty
import com.example.lockergame.domain.model.LockTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
) : ViewModel() {
    val settings: StateFlow<GameSettings> = repository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GameSettings(),
    )

    fun selectDifficulty(difficulty: LockDifficulty) {
        viewModelScope.launch { repository.setDifficulty(difficulty) }
    }

    fun selectTheme(lockTheme: LockTheme) {
        viewModelScope.launch { repository.setLockTheme(lockTheme) }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setSoundEnabled(enabled) }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setHapticsEnabled(enabled) }
    }

    fun setTutorialHintsEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setTutorialHintsEnabled(enabled) }
    }
}
