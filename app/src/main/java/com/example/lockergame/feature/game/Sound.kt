package com.example.lockergame.feature.game

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.media.ToneGenerator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

interface SoundPlayer {
    fun playConfirmClick()
    fun playUnlockClick()
}

@Singleton
class AndroidSoundPlayer @Inject constructor(
    @ApplicationContext private val context: Context,
) : SoundPlayer {
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(1)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()
    private val toneGenerator = ToneGenerator(android.media.AudioManager.STREAM_NOTIFICATION, 80)

    private var soundId = 0
    private var loaded = false

    init {
        val resourceId = context.resources.getIdentifier("unlock_click", "raw", context.packageName)
        if (resourceId != 0) {
            soundId = soundPool.load(context, resourceId, 1)
            soundPool.setOnLoadCompleteListener { _, sampleId, status ->
                loaded = status == 0 && sampleId == soundId
            }
        }
        // Place a short mechanical click sound at app/src/main/res/raw/unlock_click.wav if desired.
    }

    override fun playUnlockClick() {
        if (loaded && soundId != 0) {
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
        } else {
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 90)
        }
    }

    override fun playConfirmClick() {
        if (loaded && soundId != 0) {
            soundPool.play(soundId, 0.72f, 0.72f, 1, 0, 1.08f)
        } else {
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 45)
        }
    }
}

@Composable
fun rememberSoundPlayer(): SoundPlayer {
    val context = LocalContext.current.applicationContext
    return remember(context) { AndroidSoundPlayer(context) }
}
