package com.example.lockergame.feature.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import com.example.lockergame.domain.model.LockDifficulty

@Composable
fun DifficultyScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onPlay: () -> Unit,
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    Scaffold(timeText = { TimeText() }) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
            ScalingLazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
            ) {
                item {
                    Text("Difficulty", style = MaterialTheme.typography.title3)
                }
                LockDifficulty.entries.forEach { difficulty ->
                    item {
                        Chip(
                            onClick = { viewModel.selectDifficulty(difficulty) },
                            label = { Text(difficulty.name) },
                            secondaryLabel = { Text(descriptionFor(difficulty)) },
                            colors = if (settings.difficulty == difficulty) {
                                ChipDefaults.secondaryChipColors()
                            } else {
                                ChipDefaults.primaryChipColors()
                            },
                        )
                    }
                }
                item {
                    Button(onClick = onPlay) { Text("Play") }
                }
                item {
                    Button(onClick = onBack) { Text("Back") }
                }
            }
        }
    }
}

private fun descriptionFor(difficulty: LockDifficulty): String = when (difficulty) {
    LockDifficulty.Easy -> "2 numbers, forgiving"
    LockDifficulty.Medium -> "3 numbers, direction matters"
    LockDifficulty.Hard -> "Clearing and pass counts"
    LockDifficulty.Expert -> "Strict realistic entry"
}
