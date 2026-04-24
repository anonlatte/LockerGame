package com.example.lockergame.feature.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ListHeaderDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.ScrollIndicator
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.example.lockergame.domain.model.LockTheme

@Composable
fun CustomizationScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val columnState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()

    ScreenScaffold(
        scrollState = columnState,
        scrollIndicator = { ScrollIndicator(state = columnState) },
    ) { contentPadding ->
        TransformingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = columnState,
            contentPadding = contentPadding,
        ) {
            item {
                ListHeader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, transformationSpec)
                        .minimumVerticalContentPadding(ListHeaderDefaults.minimumTopListContentPadding),
                    transformation = SurfaceTransformation(transformationSpec),
                ) {
                    Text(
                        text = "Customization",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            item {
                Text(
                    text = "Lock theme",
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, transformationSpec),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            LockTheme.entries.forEach { lockTheme ->
                item {
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                        transformation = SurfaceTransformation(transformationSpec),
                        onClick = { viewModel.selectTheme(lockTheme) },
                    ) {
                        Text(
                            text = if (settings.lockTheme == lockTheme) {
                                "✓ ${lockTheme.name}"
                            } else {
                                lockTheme.name
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                item {
                    Text(
                        text = themeDescription(lockTheme),
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            item {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, transformationSpec),
                    transformation = SurfaceTransformation(transformationSpec),
                    onClick = { viewModel.setSoundEnabled(!settings.soundEnabled) },
                ) {
                    Text("Sound: ${if (settings.soundEnabled) "Enabled" else "Disabled"}")
                }
            }

            item {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, transformationSpec),
                    transformation = SurfaceTransformation(transformationSpec),
                    onClick = { viewModel.setHapticsEnabled(!settings.hapticsEnabled) },
                ) {
                    Text("Haptics: ${if (settings.hapticsEnabled) "Enabled" else "Disabled"}")
                }
            }

            item {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, transformationSpec),
                    transformation = SurfaceTransformation(transformationSpec),
                    onClick = { viewModel.setTutorialHintsEnabled(!settings.tutorialHintsEnabled) },
                ) {
                    Text("Hints: ${if (settings.tutorialHintsEnabled) "Enabled" else "Disabled"}")
                }
            }

            item {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, transformationSpec),
                    transformation = SurfaceTransformation(transformationSpec),
                    onClick = onBack,
                ) {
                    Text("Back")
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
