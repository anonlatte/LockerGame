package com.example.lockergame.feature.game

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.example.lockergame.domain.model.LockTheme
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

data class LockPalette(
    val body: Color,
    val bodyShadow: Color,
    val dial: Color,
    val dialInner: Color,
    val tick: Color,
    val number: Color,
    val accent: Color,
    val gold: Color,
    val goldGlow: Color,
)

@Composable
fun lockPalette(theme: LockTheme): LockPalette = when (theme) {
    LockTheme.ClassicSilver -> LockPalette(
        body = Color(0xFF161A1D),
        bodyShadow = Color(0xFF0A0C0D),
        dial = Color(0xFFC5CCD5),
        dialInner = Color(0xFF8C949E),
        tick = Color(0xFFE8ECEF),
        number = Color(0xFFF7F8FA),
        accent = Color(0xFF9AA4B2),
        gold = Color(0xFFD7AB2E),
        goldGlow = Color(0x66FFD76A),
    )

    LockTheme.MatteBlack -> LockPalette(
        body = Color(0xFF0D0F12),
        bodyShadow = Color(0xFF040506),
        dial = Color(0xFF30353B),
        dialInner = Color(0xFF15191E),
        tick = Color(0xFFCDD2D8),
        number = Color(0xFFE5E8EC),
        accent = Color(0xFF7E8792),
        gold = Color(0xFFC89D29),
        goldGlow = Color(0x55FFD35D),
    )

    LockTheme.RetroBrass -> LockPalette(
        body = Color(0xFF15110C),
        bodyShadow = Color(0xFF070503),
        dial = Color(0xFFC09A57),
        dialInner = Color(0xFF785B2E),
        tick = Color(0xFFF3E6C6),
        number = Color(0xFFFFF1CF),
        accent = Color(0xFF8F6D34),
        gold = Color(0xFFFFC841),
        goldGlow = Color(0x77FFD76A),
    )

    LockTheme.MinimalWhite -> LockPalette(
        body = Color(0xFF101214),
        bodyShadow = Color(0xFF050607),
        dial = Color(0xFFF4F5F7),
        dialInner = Color(0xFFC7CCD4),
        tick = Color(0xFF1A1E23),
        number = Color(0xFF0B0E12),
        accent = Color(0xFFDEE3EA),
        gold = Color(0xFFD7A92F),
        goldGlow = Color(0x55FFE07A),
    )
}

@Composable
fun LockBody(
    lockTheme: LockTheme,
    doorOffsetProgress: Float,
    goldRevealProgress: Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val palette = lockPalette(lockTheme)
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        GoldReveal(
            goldRevealProgress = goldRevealProgress,
            palette = palette,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = size.width * 0.24f * doorOffsetProgress
                    rotationZ = 8f * doorOffsetProgress
                },
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = palette.bodyShadow,
                    radius = size.minDimension * 0.54f,
                    center = center + Offset(0f, size.minDimension * 0.02f),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(palette.body, palette.bodyShadow),
                        center = center,
                        radius = size.minDimension * 0.58f,
                    ),
                    radius = size.minDimension * 0.52f,
                )
                drawCircle(
                    color = palette.accent.copy(alpha = 0.20f),
                    radius = size.minDimension * 0.47f,
                    style = Stroke(width = size.minDimension * 0.01f),
                )
            }
            content()
        }
    }
}

@Composable
fun LockDial(
    dialValue: Int,
    dialRotationDegrees: Float,
    dialDivisions: Int,
    lockTheme: LockTheme,
    knobScale: Float = 1f,
    feedbackRingColor: Color = Color.Transparent,
    modifier: Modifier = Modifier,
) {
    val palette = lockPalette(lockTheme)
    val textPaint = remember(palette) {
        Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            textSize = 20f
            color = palette.number.toArgbCompat()
            typeface = android.graphics.Typeface.MONOSPACE
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension * 0.42f
            val center = center

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(palette.dial, palette.dialInner),
                    center = center - Offset(radius * 0.2f, radius * 0.2f),
                    radius = radius * 1.3f,
                ),
                radius = radius,
                center = center,
            )
            drawCircle(
                color = palette.accent.copy(alpha = 0.35f),
                radius = radius,
                style = Stroke(width = size.minDimension * 0.012f),
            )
            if (feedbackRingColor.alpha > 0f) {
                drawCircle(
                    color = feedbackRingColor,
                    radius = radius * 1.01f,
                    style = Stroke(width = size.minDimension * 0.02f),
                )
            }

            rotate(dialRotationDegrees, center) {
                val outerTickRadius = radius * 0.94f
                val innerTickLong = radius * 0.72f
                val innerTickShort = radius * 0.80f
                repeat(dialDivisions) { index ->
                    val angle = Math.toRadians((index.toFloat() / dialDivisions) * 360f - 90f.toDouble())
                    val start = Offset(
                        x = center.x + outerTickRadius * cos(angle).toFloat(),
                        y = center.y + outerTickRadius * sin(angle).toFloat(),
                    )
                    val endRadius = if (index % 5 == 0) innerTickLong else innerTickShort
                    val end = Offset(
                        x = center.x + endRadius * cos(angle).toFloat(),
                        y = center.y + endRadius * sin(angle).toFloat(),
                    )
                    drawLine(
                        color = palette.tick.copy(alpha = if (index % 5 == 0) 1f else 0.55f),
                        start = start,
                        end = end,
                        strokeWidth = if (index % 5 == 0) size.minDimension * 0.010f else size.minDimension * 0.005f,
                        cap = StrokeCap.Round,
                    )
                }

                val labelStep = (dialDivisions / 10).coerceAtLeast(5)
                for (value in 0 until dialDivisions step labelStep) {
                    val angle = Math.toRadians((value.toFloat() / dialDivisions) * 360f - 90f.toDouble())
                    val textRadius = radius * 0.62f
                    val x = center.x + textRadius * cos(angle).toFloat()
                    val y = center.y + textRadius * sin(angle).toFloat() + size.minDimension * 0.022f
                    drawContext.canvas.nativeCanvas.drawText(value.toString(), x, y, textPaint)
                }
            }

            drawCircle(
                color = Color(0xFF050607),
                radius = radius * 0.24f,
            )
            drawCircle(
                color = palette.accent.copy(alpha = 0.5f),
                radius = radius * 0.24f,
                style = Stroke(width = size.minDimension * 0.012f),
            )
            scale(scale = knobScale, pivot = center) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(palette.accent.copy(alpha = 0.26f), Color.Transparent),
                        center = center,
                        radius = radius * 0.38f,
                    ),
                    radius = radius * 0.32f,
                )

                val handleRadius = radius * 0.13f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF161A1D), Color(0xFF050607)),
                        center = center - Offset(handleRadius * 0.25f, handleRadius * 0.25f),
                        radius = handleRadius * 1.8f,
                    ),
                    radius = handleRadius,
                )
                drawCircle(
                    color = palette.accent.copy(alpha = 0.65f),
                    radius = handleRadius,
                    style = Stroke(width = size.minDimension * 0.01f),
                )
                drawLine(
                    color = palette.tick.copy(alpha = 0.92f),
                    start = Offset(center.x, center.y - handleRadius * 0.58f),
                    end = Offset(center.x, center.y + handleRadius * 0.58f),
                    strokeWidth = size.minDimension * 0.012f,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = palette.tick.copy(alpha = 0.92f),
                    start = Offset(center.x - handleRadius * 0.58f, center.y),
                    end = Offset(center.x + handleRadius * 0.58f, center.y),
                    strokeWidth = size.minDimension * 0.012f,
                    cap = StrokeCap.Round,
                )
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val tipX = size.width * 0.5f
            val top = size.height * 0.04f
            drawLine(
                color = palette.gold,
                start = Offset(tipX, top),
                end = Offset(tipX, top + size.minDimension * 0.16f),
                strokeWidth = size.minDimension * 0.014f,
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
private fun GoldReveal(
    goldRevealProgress: Float,
    palette: LockPalette,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val alpha = goldRevealProgress.coerceIn(0f, 1f)
            if (alpha <= 0f) return@Canvas
            drawCircle(
                color = palette.goldGlow.copy(alpha = alpha),
                radius = size.minDimension * (0.16f + alpha * 0.18f),
            )
            repeat(6) { index ->
                val row = index / 3
                val col = index % 3
                val coinSize = size.minDimension * 0.08f
                val x = center.x - coinSize + col * coinSize * 0.95f
                val y = center.y + row * coinSize * 0.7f
                drawOval(
                    color = palette.gold.copy(alpha = alpha),
                    topLeft = Offset(x, y),
                    size = Size(coinSize, coinSize * 0.45f),
                )
            }
        }
    }
}

@Composable
fun DialKnob(lockTheme: LockTheme) {
    val palette = lockPalette(lockTheme)
    Box(
        modifier = Modifier
            .size(26.dp)
            .background(palette.bodyShadow, CircleShape),
    )
}

private fun Color.toArgbCompat(): Int = android.graphics.Color.argb(
    (alpha * 255).roundToInt(),
    (red * 255).roundToInt(),
    (green * 255).roundToInt(),
    (blue * 255).roundToInt(),
)
