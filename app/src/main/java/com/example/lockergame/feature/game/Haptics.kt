package com.example.lockergame.feature.game

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback

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
    }

    override fun performUnlock() {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        vibrator ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(50L, 180))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(50L)
        }
    }
}

@Composable
fun rememberHapticPerformer(): HapticPerformer {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    return remember(context, haptic) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        ComposeHapticPerformer(haptic, vibrator)
    }
}
