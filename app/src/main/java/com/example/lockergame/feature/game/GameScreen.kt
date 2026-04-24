package com.example.lockergame.feature.game

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.MaterialTheme
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GameScreen(
    onExit: () -> Unit,
    onGoToDifficulty: () -> Unit,
    viewModel: GameViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val haptics = rememberHapticPerformer()
    val soundPlayer = rememberSoundPlayer()
    val soundEnabled by rememberUpdatedState(settings.soundEnabled)
    val hapticsEnabled by rememberUpdatedState(settings.hapticsEnabled)
    val focusRequester = remember { FocusRequester() }
    var doorAnimationStarted by remember { mutableStateOf(false) }
    val doorOffsetProgress by animateFloatAsState(
        targetValue = if (doorAnimationStarted) 1f else 0f,
        label = "door_offset",
    )
    val goldRevealProgress by animateFloatAsState(
        targetValue = when (uiState.unlockAnimationPhase) {
            com.example.lockergame.domain.model.UnlockAnimationPhase.GoldRevealed,
            com.example.lockergame.domain.model.UnlockAnimationPhase.Completed,
            -> 1f

            else -> 0f
        },
        label = "gold_reveal",
    )
    var dragAccumulator by remember { mutableFloatStateOf(0f) }
    val touchStepThreshold = with(LocalDensity.current) { 10.dp.toPx() }

    LaunchedEffect(Unit) {
        withFrameNanos { }
        focusRequester.requestFocus()
        viewModel.ensureGameStarted()
    }

    LaunchedEffect(uiState.phase, uiState.isUnlocked) {
        withFrameNanos { }
        focusRequester.requestFocus()
    }

    LaunchedEffect(uiState.isUnlocked) {
        if (!uiState.isUnlocked) {
            doorAnimationStarted = false
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                GameUiEffect.PerformSubtleHaptic -> if (hapticsEnabled) haptics.performSubtleTick()
                GameUiEffect.PerformUnlockHaptic -> if (hapticsEnabled) haptics.performUnlock()
                GameUiEffect.PlayUnlockClick -> if (soundEnabled) soundPlayer.playUnlockClick()
                GameUiEffect.StartDoorOpenAnimation -> doorAnimationStarted = true
            }
        }
    }

    GameScreenContent(
        uiState = uiState,
        onRotary = viewModel::onRotaryInput,
        onDragDelta = { dragDelta ->
            dragAccumulator += dragDelta
            while (kotlin.math.abs(dragAccumulator) >= touchStepThreshold) {
                val consume = if (dragAccumulator > 0) touchStepThreshold else -touchStepThreshold
                dragAccumulator -= consume
                viewModel.onRotaryInput(consume)
            }
        },
        onTap = {
            if (uiState.isUnlocked) {
                viewModel.startNewGame()
            }
        },
        onLongPress = {
            if (uiState.isUnlocked) {
                onGoToDifficulty()
            } else {
                viewModel.resetAttempt()
            }
        },
        focusRequester = focusRequester,
        doorOffsetProgress = doorOffsetProgress,
        goldRevealProgress = goldRevealProgress,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GameScreenContent(
    uiState: GameUiState,
    onRotary: (Float) -> Unit,
    onDragDelta: (Float) -> Unit,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    focusRequester: FocusRequester,
    doorOffsetProgress: Float,
    goldRevealProgress: Float,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .onRotaryScrollEvent {
                onRotary(it.verticalScrollPixels)
                true
            }
            .focusRequester(focusRequester)
            .focusable(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(onTap, onLongPress) {
                    detectTapGestures(
                        onTap = { onTap() },
                        onLongPress = { onLongPress() },
                    )
                }
                .pointerInput(onDragDelta) {
                    detectDragGestures { _, dragAmount ->
                        onDragDelta(dragAmount.x + dragAmount.y)
                    }
                },
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LockBody(
                    lockTheme = uiState.lockTheme,
                    doorOffsetProgress = doorOffsetProgress,
                    goldRevealProgress = goldRevealProgress,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    LockDial(
                        dialValue = uiState.dialValue,
                        dialRotationDegrees = uiState.dialRotationDegrees,
                        dialDivisions = uiState.dialDivisions,
                        lockTheme = uiState.lockTheme,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}
