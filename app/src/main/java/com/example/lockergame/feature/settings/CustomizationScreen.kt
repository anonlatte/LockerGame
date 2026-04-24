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
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import com.example.lockergame.domain.model.LockTheme

@Composable
fun CustomizationScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
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
                    Text("Customization", style = MaterialTheme.typography.title3)
                }
                item {
                    Text("Lock theme", style = MaterialTheme.typography.caption1)
                }
                LockTheme.entries.forEach { lockTheme ->
                    item {
                        Chip(
                            onClick = { viewModel.selectTheme(lockTheme) },
                            label = { Text(lockTheme.name) },
                            secondaryLabel = { Text(themeDescription(lockTheme)) },
                        )
                    }
                }
                item {
                    Chip(
                        onClick = { viewModel.setSoundEnabled(!settings.soundEnabled) },
                        label = { Text("Sound") },
                        secondaryLabel = { Text(if (settings.soundEnabled) "Enabled" else "Disabled") },
                    )
                }
                item {
                    Chip(
                        onClick = { viewModel.setHapticsEnabled(!settings.hapticsEnabled) },
                        label = { Text("Haptics") },
                        secondaryLabel = { Text(if (settings.hapticsEnabled) "Enabled" else "Disabled") },
                    )
                }
                item {
                    Chip(
                        onClick = { viewModel.setTutorialHintsEnabled(!settings.tutorialHintsEnabled) },
                        label = { Text("Hints") },
                        secondaryLabel = { Text(if (settings.tutorialHintsEnabled) "Enabled" else "Disabled") },
                    )
                }
                item {
                    Button(onClick = onBack) { Text("Back") }
                }
            }
        }
    }
}

private fun themeDescription(theme: LockTheme): String = when (theme) {
    LockTheme.ClassicSilver -> "60-division steel dial"
    LockTheme.MatteBlack -> "60-division tactical finish"
    LockTheme.RetroBrass -> "100-division brass face"
    LockTheme.MinimalWhite -> "100-division clean contrast"
}
