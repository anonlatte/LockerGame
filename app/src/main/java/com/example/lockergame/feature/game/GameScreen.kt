package com.example.lockergame.feature.game

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.MaterialTheme
import androidx.compose.ui.unit.dp
import kotlin.math.hypot
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GameScreen(
    onExit: () -> Unit,
    onGoToDifficulty: () -> Unit,
    onShowResult: (GameUiState) -> Unit,
    viewModel: GameViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val haptics = rememberHapticPerformer()
    val soundPlayer = rememberSoundPlayer()
    val soundEnabled by rememberUpdatedState(settings.soundEnabled)
    val hapticsEnabled by rememberUpdatedState(settings.hapticsEnabled)
    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()
    var knobPressed by remember { mutableStateOf(false) }
    var dialFlashColor by remember { mutableStateOf(Color.Transparent) }
    val knobScale by animateFloatAsState(
        targetValue = if (knobPressed) 0.1f else 1f,
        label = "knob_scale",
    )
    val dialFlashAlpha by animateFloatAsState(
        targetValue = if (dialFlashColor == Color.Transparent) 0f else 1f,
        label = "dial_flash_alpha",
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

    LaunchedEffect(uiState.canAutoConfirm, uiState.dialValue, uiState.progress.currentStepIndex, uiState.phase) {
        if (!uiState.canAutoConfirm || uiState.isUnlocked) return@LaunchedEffect
        delay(500)
        if (viewModel.uiState.value.canAutoConfirm &&
            viewModel.uiState.value.dialValue == uiState.dialValue &&
            viewModel.uiState.value.progress.currentStepIndex == uiState.progress.currentStepIndex
        ) {
            viewModel.onConfirmInput()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                GameUiEffect.PerformSubtleHaptic -> if (hapticsEnabled) haptics.performSubtleTick()
                GameUiEffect.PerformUnlockHaptic -> if (hapticsEnabled) haptics.performUnlock()
                GameUiEffect.PlayUnlockClick -> if (soundEnabled) soundPlayer.playUnlockClick()
                GameUiEffect.NavigateToUnlockResult -> onShowResult(uiState)
                GameUiEffect.StartDoorOpenAnimation -> Unit
                GameUiEffect.FlashDialSuccess -> {
                    dialFlashColor = Color(0xFF38D26E)
                    coroutineScope.launch {
                        delay(140)
                        dialFlashColor = Color.Transparent
                    }
                }
                GameUiEffect.FlashDialFailure -> {
                    dialFlashColor = Color(0xFFE34B4B)
                    coroutineScope.launch {
                        delay(170)
                        dialFlashColor = Color.Transparent
                    }
                }
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
        onKnobTap = {
            if (soundEnabled) {
                soundPlayer.playConfirmClick()
            }
            coroutineScope.launch {
                knobPressed = true
                delay(70)
                knobPressed = false
            }
            if (uiState.isUnlocked) {
                viewModel.startNewGame()
            } else {
                viewModel.onConfirmInput()
            }
        },
        onBackgroundTap = {
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
        doorOffsetProgress = 0f,
        goldRevealProgress = 0f,
        knobScale = knobScale,
        dialFlashColor = dialFlashColor.copy(alpha = 0.8f * dialFlashAlpha),
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GameScreenContent(
    uiState: GameUiState,
    onRotary: (Float) -> Unit,
    onDragDelta: (Float) -> Unit,
    onKnobTap: () -> Unit,
    onBackgroundTap: () -> Unit,
    onLongPress: () -> Unit,
    focusRequester: FocusRequester,
    doorOffsetProgress: Float,
    goldRevealProgress: Float,
    knobScale: Float,
    dialFlashColor: Color,
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
                .pointerInput(onKnobTap, onBackgroundTap, onLongPress, uiState.isUnlocked) {
                    detectTapGestures(
                        onTap = { offset ->
                            val centerX = size.width / 2f
                            val centerY = size.height / 2f
                            val knobTapRadius = minOf(size.width, size.height) * 0.16f
                            val distance = hypot(offset.x - centerX, offset.y - centerY)
                            if (distance <= knobTapRadius) {
                                onKnobTap()
                            } else if (uiState.isUnlocked) {
                                onBackgroundTap()
                            }
                        },
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
                        knobScale = knobScale,
                        feedbackRingColor = dialFlashColor,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}
