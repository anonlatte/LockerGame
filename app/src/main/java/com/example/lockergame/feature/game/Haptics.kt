package com.example.lockergame.feature.game

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.core.content.ContextCompat.getSystemService

interface HapticPerformer {
    fun performSubtleTick()
    fun performUnlock()
}

private class ComposeHapticPerformer(
    private val hapticFeedback: HapticFeedback,
    private val vibrator: Vibrator?,
) : HapticPerformer {
    override fun performSubtleTick() {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        vibrator?.vibrate(VibrationEffect.createOneShot(1L, 1))
    }

    override fun performUnlock() {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        vibrator?.vibrate(VibrationEffect.createOneShot(50L, 60))
    }
}

@Composable
fun rememberHapticPerformer(): HapticPerformer {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    return remember(context, haptic) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSystemService(context, VibratorManager::class.java)?.defaultVibrator
        } else {
            getSystemService(context, Vibrator::class.java)
        }
        ComposeHapticPerformer(haptic, vibrator)
    }
}
