package com.example.lockergame.feature.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButton
import androidx.wear.compose.material3.MaterialTheme
import com.example.lockergame.domain.config.DifficultyConfigs
import com.example.lockergame.domain.model.LockDifficulty
import com.example.lockergame.domain.model.LockTheme
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UnlockResultScreen(
    lockTheme: LockTheme,
    difficulty: LockDifficulty,
    onReplay: () -> Unit,
    onNextDifficulty: () -> Unit,
    onExit: () -> Unit,
) {
    val doorOffset = remember { Animatable(0f) }
    val goldReveal = remember { Animatable(0f) }
    var actionsVisible by remember { mutableStateOf(false) }
    val dialDivisions = DifficultyConfigs.dialDivisionsForTheme(lockTheme)

    LaunchedEffect(Unit) {
        delay(40)
        doorOffset.animateTo(
            targetValue = 1.28f,
            animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        )
        goldReveal.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        )
        actionsVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(onReplay, onNextDifficulty) {
                detectTapGestures(
                    onTap = { onReplay() },
                    onLongPress = { onNextDifficulty() },
                )
            },
    ) {
        LockBody(
            lockTheme = lockTheme,
            doorOffsetProgress = doorOffset.value,
            goldRevealProgress = goldReveal.value,
            modifier = Modifier.fillMaxSize(),
        ) {
            LockDial(
                dialValue = 0,
                dialRotationDegrees = 0f,
                dialDivisions = dialDivisions,
                lockTheme = lockTheme,
                knobScale = 1f,
                modifier = Modifier.fillMaxSize(),
            )
        }

        if (actionsVisible) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp),
            ) {
                IconButton(onClick = onReplay, modifier = Modifier.padding(horizontal = 4.dp)) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_revert),
                        contentDescription = "Replay",
                    )
                }
                IconButton(onClick = onNextDifficulty, modifier = Modifier.padding(horizontal = 4.dp)) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_more),
                        contentDescription = "Difficulty ${difficulty.name}",
                    )
                }
                IconButton(onClick = onExit, modifier = Modifier.padding(horizontal = 4.dp)) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
                        contentDescription = "Exit",
                    )
                }
            }
        }
    }
}
