package com.example.lockergame.feature.game

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lockergame.data.settings.GameSettings
import com.example.lockergame.data.settings.SettingsRepository
import com.example.lockergame.domain.config.DifficultyConfigs
import com.example.lockergame.domain.engine.CombinationGenerator
import com.example.lockergame.domain.engine.DialInputController
import com.example.lockergame.domain.engine.LockEngine
import com.example.lockergame.domain.engine.RotaryStepMapper
import com.example.lockergame.domain.model.LockEngineEvent
import com.example.lockergame.domain.model.LockEngineOutput
import com.example.lockergame.domain.model.LockPhase
import com.example.lockergame.domain.model.UnlockAnimationPhase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class GameViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val combinationGenerator: CombinationGenerator,
) : ViewModel() {
    private companion object {
        const val TAG = "LockerGame"
    }

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<GameUiEffect>(extraBufferCapacity = 8)
    val effects = _effects.asSharedFlow()

    val settings: StateFlow<GameSettings> = settingsRepository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GameSettings(),
    )

    private var engine: LockEngine? = null
    private var inputController: DialInputController? = null
    private var gameStartedAtMillis: Long = 0L

    init {
        viewModelScope.launch {
            settings.collect { currentSettings ->
                _uiState.value = _uiState.value.copy(
                    difficulty = currentSettings.difficulty,
                    lockTheme = currentSettings.lockTheme,
                    tutorialHintsEnabled = currentSettings.tutorialHintsEnabled,
                )
            }
        }
    }

    fun ensureGameStarted() {
        if (engine == null) {
            startNewGame()
        }
    }

    fun startNewGame() {
        viewModelScope.launch {
            val currentSettings = settings.first()
            val dialDivisions = DifficultyConfigs.dialDivisionsForTheme(currentSettings.lockTheme)
            val difficultyConfig = DifficultyConfigs.forDifficulty(currentSettings.difficulty, dialDivisions)
            val combination = combinationGenerator.generate(
                length = difficultyConfig.combinationLength,
                dialDivisions = dialDivisions,
            )
            Log.d(TAG, "Generated combination=${combination.values.joinToString(",")}")
            engine = LockEngine(combination, difficultyConfig).also {
                it.dispatch(LockEngineEvent.StartAttempt)
            }
            inputController = DialInputController(RotaryStepMapper(difficultyConfig.inputConfig))
            gameStartedAtMillis = System.currentTimeMillis()
            syncState(unlockAnimationPhase = UnlockAnimationPhase.Locked)
        }
    }

    fun resetAttempt() {
        engine?.dispatch(LockEngineEvent.ResetAttempt)
        inputController?.reset()
        Log.w(TAG, "Input reset")
        syncState(unlockAnimationPhase = UnlockAnimationPhase.Locked)
    }

    fun onRotaryInput(deltaPixels: Float) {
        val previousDirection = engine?.state?.lastDirection
        val stepResult = inputController?.onRotary(deltaPixels) ?: return
        val outputs = engine?.dispatch(
            LockEngineEvent.DialRotated(
                direction = stepResult.direction,
                steps = stepResult.dialSteps,
            ),
        ).orEmpty()
        syncState()
        val currentDirection = engine?.state?.lastDirection
        if (currentDirection != null && currentDirection != previousDirection) {
            Log.d(TAG, "Rotation direction=$currentDirection")
        }
        handleOutputs(outputs)
    }

    fun onConfirmInput() {
        val previousState = engine?.state
        val outputs = engine?.dispatch(LockEngineEvent.ConfirmStep).orEmpty()
        syncState()
        val currentState = engine?.state

        currentState?.let {
            Log.d(TAG, "Selected value=${it.dialValue} phase=${it.phase}")
        }

        if (previousState != null && currentState != null) {
            val success = currentState.currentStepIndex > previousState.currentStepIndex || currentState.isUnlocked
            val failure = outputs.any { it == LockEngineOutput.WrongMove || it == LockEngineOutput.Reset }
            when {
                success -> emitEffect(GameUiEffect.FlashDialSuccess)
                failure || outputs.any { it == LockEngineOutput.InputConfirmed } -> emitEffect(GameUiEffect.FlashDialFailure)
            }
        }

        handleOutputs(outputs)
    }

    private fun handleOutputs(outputs: List<LockEngineOutput>) {
        outputs.forEach { output ->
            when (output) {
                LockEngineOutput.InputConfirmed -> Unit
                LockEngineOutput.CorrectStepHit -> emitEffect(GameUiEffect.PerformSubtleHaptic)
                LockEngineOutput.Unlocked -> onUnlocked()
                LockEngineOutput.Reset -> {
                    Log.w(TAG, "Input reset")
                    Log.e(TAG, "Lock input error: reset")
                }
                LockEngineOutput.WrongMove -> {
                    Log.e(TAG, "Lock input error: wrong move")
                }
                LockEngineOutput.None,
                -> Unit
            }
        }
    }

    private fun onUnlocked() {
        val currentSettings = settings.value
        if (currentSettings.hapticsEnabled) emitEffect(GameUiEffect.PerformUnlockHaptic)
        if (currentSettings.soundEnabled) emitEffect(GameUiEffect.PlayUnlockClick)

        viewModelScope.launch {
            val elapsed = System.currentTimeMillis() - gameStartedAtMillis
            settingsRepository.recordUnlock(currentSettings.difficulty, elapsed)
            _uiState.value = _uiState.value.copy(unlockAnimationPhase = UnlockAnimationPhase.Click)
            delay(140)
            _effects.emit(GameUiEffect.NavigateToUnlockResult)
        }
    }

    private fun emitEffect(effect: GameUiEffect) {
        viewModelScope.launch {
            _effects.emit(effect)
        }
    }

    private fun syncState(unlockAnimationPhase: UnlockAnimationPhase? = null) {
        val currentEngine = engine ?: return
        val currentSettings = settings.value
        val currentState = currentEngine.state
        val currentDialDivisions = DifficultyConfigs.dialDivisionsForTheme(currentSettings.lockTheme)
        _uiState.value = _uiState.value.copy(
            dialValue = currentState.dialValue,
            dialRotationDegrees = -(currentState.dialValue * 360f / currentDialDivisions),
            difficulty = currentSettings.difficulty,
            lockTheme = currentSettings.lockTheme,
            phase = currentState.phase,
            progress = currentEngine.progress(),
            isUnlocked = currentState.isUnlocked,
            unlockAnimationPhase = unlockAnimationPhase ?: _uiState.value.unlockAnimationPhase,
            tutorialHintsEnabled = currentSettings.tutorialHintsEnabled,
            dialDivisions = currentDialDivisions,
        )
    }
}
