package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import kotlin.random.Random

/**
 * Press feedback used by every tappable control: the element springs down on touch,
 * bounces back on release and gives a light haptic tick. Apply it FIRST in a modifier
 * chain so the background, border and hit area all scale together.
 */
fun Modifier.bounceClick(
    enabled: Boolean = true,
    pressedScale: Float = 0.92f,
    haptics: Boolean = true,
    role: Role = Role.Button,
    onClick: () -> Unit
): Modifier = composed {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "pressScale"
    )
    val haptic = LocalHapticFeedback.current
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interaction,
            indication = null,
            enabled = enabled,
            role = role
        ) {
            if (haptics) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        }
}

private class ConfettiPiece(
    val vx: Float,
    val vy: Float,
    val size: Float,
    val rotation: Float,
    val spin: Float,
    val color: Color,
    val isCircle: Boolean
)

/**
 * One-shot confetti burst that fires from the upper middle of its bounds and falls with
 * gravity. Pure Canvas drawing, so it costs a single draw pass per frame.
 */
@Composable
fun ConfettiBurst(
    colors: List<Color>,
    modifier: Modifier = Modifier,
    pieceCount: Int = 110,
    durationMillis: Int = 3200
) {
    val pieces = remember {
        val rnd = Random(System.nanoTime())
        List(pieceCount) {
            ConfettiPiece(
                vx = (rnd.nextFloat() - 0.5f) * 1.5f,
                vy = -(0.25f + rnd.nextFloat() * 1.15f),
                size = 7f + rnd.nextFloat() * 8f,
                rotation = rnd.nextFloat() * 360f,
                spin = (rnd.nextFloat() - 0.5f) * 900f,
                color = colors[rnd.nextInt(colors.size)],
                isCircle = rnd.nextFloat() < 0.3f
            )
        }
    }
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(durationMillis, easing = LinearEasing))
    }

    Canvas(modifier = modifier) {
        val t = progress.value
        if (t >= 1f) return@Canvas
        val time = t * 1.7f
        val fade = (1f - ((t - 0.72f) / 0.28f)).coerceIn(0f, 1f)
        for (p in pieces) {
            val x = size.width * (0.5f + p.vx * time)
            val y = size.height * (0.30f + p.vy * time + 0.8f * 1.7f * time * time)
            val w = p.size * density
            val h = w * 0.55f
            rotate(degrees = p.rotation + p.spin * t, pivot = Offset(x, y)) {
                if (p.isCircle) {
                    drawCircle(
                        color = p.color.copy(alpha = fade),
                        radius = w * 0.4f,
                        center = Offset(x, y)
                    )
                } else {
                    drawRect(
                        color = p.color.copy(alpha = fade),
                        topLeft = Offset(x - w / 2f, y - h / 2f),
                        size = Size(w, h)
                    )
                }
            }
        }
    }
}
