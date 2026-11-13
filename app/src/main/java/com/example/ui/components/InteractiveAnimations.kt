package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Tactile spring-physics press animation inspired by recent.design micro-interactions.
 * Scales down smoothly when pressed and rebounds with organic bouncy spring physics.
 */
fun Modifier.bounceClick(
    scaleDown: Float = 0.94f,
    tiltDegrees: Float = 0f,
    enabled: Boolean = true
): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "bounce_scale"
    )
    val rotationZ by animateFloatAsState(
        targetValue = if (isPressed && enabled) tiltDegrees else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bounce_tilt"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            this.rotationZ = rotationZ
        }
        .pointerInput(enabled) {
            if (!enabled) return@pointerInput
            awaitPointerEventScope {
                while (true) {
                    awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    isPressed = true
                    waitForUpOrCancellation(pass = PointerEventPass.Initial)
                    isPressed = false
                }
            }
        }
}

/**
 * Staggered entrance reveal wrapper that springs elements into view with vertical slide + scale + fade.
 */
@Composable
fun StaggeredEntrance(
    index: Int,
    modifier: Modifier = Modifier,
    baseDelayMs: Int = 55,
    content: @Composable () -> Unit
) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay((index * baseDelayMs).toLong())
        progress.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = 0.72f,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Box(
        modifier = modifier.graphicsLayer {
            val p = progress.value
            alpha = p.coerceIn(0f, 1f)
            translationY = (1f - p) * 36f
            val s = 0.92f + (0.08f * p)
            scaleX = s
            scaleY = s
        }
    ) {
        content()
    }
}

/**
 * Interactive 3D perspective & spotlight card inspired by recent.design.
 * Responds to pointer/touch position with subtle 3D tilt and a radial spotlight glow.
 */
@Composable
fun InteractiveSpotlightBox(
    modifier: Modifier = Modifier,
    spotlightColor: Color = Color.White.copy(alpha = 0.14f),
    maxTiltDegrees: Float = 4.5f,
    content: @Composable BoxScope.() -> Unit
) {
    var touchOffset by remember { mutableStateOf<Offset?>(null) }
    var normalizedX by remember { mutableFloatStateOf(0f) }
    var normalizedY by remember { mutableFloatStateOf(0f) }

    val animatedRotX by animateFloatAsState(
        targetValue = if (touchOffset != null) -normalizedY * maxTiltDegrees else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "tilt_x"
    )
    val animatedRotY by animateFloatAsState(
        targetValue = if (touchOffset != null) normalizedX * maxTiltDegrees else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "tilt_y"
    )
    val animatedScale by animateFloatAsState(
        targetValue = if (touchOffset != null) 0.985f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "tilt_scale"
    )
    val spotlightAlpha by animateFloatAsState(
        targetValue = if (touchOffset != null) 1f else 0f,
        animationSpec = tween(220),
        label = "spotlight_alpha"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                rotationX = animatedRotX
                rotationY = animatedRotY
                scaleX = animatedScale
                scaleY = animatedScale
                cameraDistance = 14f * density
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val change = event.changes.firstOrNull()
                        if (change != null && change.pressed) {
                            val pos = change.position
                            touchOffset = pos
                            val w = size.width.coerceAtLeast(1)
                            val h = size.height.coerceAtLeast(1)
                            normalizedX = ((pos.x / w) - 0.5f) * 2f
                            normalizedY = ((pos.y / h) - 0.5f) * 2f
                        } else {
                            touchOffset = null
                            normalizedX = 0f
                            normalizedY = 0f
                        }
                    }
                }
            }
            .drawWithContent {
                drawContent()
                val currentTouch = touchOffset
                if (currentTouch != null && spotlightAlpha > 0.01f) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                spotlightColor.copy(alpha = spotlightColor.alpha * spotlightAlpha),
                                Color.Transparent
                            ),
                            center = currentTouch,
                            radius = size.maxDimension * 0.65f
                        ),
                        center = currentTouch,
                        radius = size.maxDimension * 0.65f
                    )
                }
            },
        content = content
    )
}

/**
 * Slot-machine / Odometer style rolling number animation for scores, streaks, and stats.
 */
@Composable
fun AnimatedRollingNumber(
    value: Int,
    modifier: Modifier = Modifier,
    prefix: String = "",
    suffix: String = "",
    color: Color = Color.White,
    fontSize: TextUnit = 15.sp,
    fontWeight: FontWeight = FontWeight.ExtraBold
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (prefix.isNotEmpty()) {
            Text(
                text = prefix,
                color = color,
                fontSize = fontSize,
                fontWeight = fontWeight
            )
        }
        AnimatedContent(
            targetState = value,
            transitionSpec = {
                if (targetState > initialState) {
                    (slideInVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) { height -> height } + fadeIn(tween(180))) togetherWith
                        (slideOutVertically(tween(180)) { height -> -height } + fadeOut(tween(150)))
                } else {
                    (slideInVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) { height -> -height } + fadeIn(tween(180))) togetherWith
                        (slideOutVertically(tween(180)) { height -> height } + fadeOut(tween(150)))
                }.using(SizeTransform(clip = false))
            },
            label = "rolling_number"
        ) { targetVal ->
            Text(
                text = "$targetVal",
                color = color,
                fontSize = fontSize,
                fontWeight = fontWeight
            )
        }
        if (suffix.isNotEmpty()) {
            Text(
                text = suffix,
                color = color,
                fontSize = fontSize,
                fontWeight = fontWeight
            )
        }
    }
}

/**
 * Animated liquid shimmer border modifier for hero cards and interactive highlights.
 */
@Composable
fun Modifier.animatedGlowingBorder(
    primaryColor: Color,
    secondaryColor: Color,
    cornerRadius: Dp = 20.dp,
    borderWidth: Dp = 1.5.dp
): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "liquid_border")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "border_angle"
    )

    return this.drawWithContent {
        drawContent()
        val strokePx = borderWidth.toPx()
        val radiusPx = cornerRadius.toPx()
        rotate(degrees = angle) {
            // Subtle sweep highlight drawn along the border
        }
        val rad = Math.toRadians(angle.toDouble())
        val cx = size.width / 2f + (cos(rad).toFloat() * size.width * 0.4f)
        val cy = size.height / 2f + (sin(rad).toFloat() * size.height * 0.4f)
        drawRoundRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.85f),
                    secondaryColor.copy(alpha = 0.55f),
                    primaryColor.copy(alpha = 0.2f)
                ),
                center = Offset(cx, cy),
                radius = size.maxDimension * 0.85f
            ),
            cornerRadius = CornerRadius(radiusPx, radiusPx),
            style = Stroke(width = strokePx)
        )
    }
}

/**
 * Interactive particle model for joyful tap bursts and victory celebrations.
 */
data class InteractiveParticle(
    val startX: Float,
    val startY: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val sizeDp: Float,
    val createdAtMs: Long = System.currentTimeMillis()
)

/**
 * Celebration confetti & particle burst canvas for Victory Dialog and interactive matrix taps.
 */
@Composable
fun CelebrationParticlesCanvas(
    primaryColor: Color,
    secondaryColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "celebration_particles")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_phase"
    )

    val specs = remember(primaryColor, secondaryColor, accentColor) {
        val colors = listOf(primaryColor, secondaryColor, accentColor, Color(0xFFF59E0B), Color(0xFF10B981))
        List(28) { idx ->
            val angle = (idx / 28f) * (2 * Math.PI) + Random.nextDouble(-0.2, 0.2)
            val speed = Random.nextFloat() * 0.42f + 0.16f
            Triple(
                Offset(cos(angle).toFloat() * speed, sin(angle).toFloat() * speed),
                colors[idx % colors.size],
                Random.nextFloat() * 4.5f + 2.5f
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height * 0.28f)
        val maxRadius = size.minDimension * 0.75f
        val fade = (1f - phase).coerceIn(0f, 1f)

        specs.forEachIndexed { idx, (vel, color, radiusDp) ->
            val localPhase = ((phase + (idx % 5) * 0.12f) % 1f)
            val localFade = (1f - localPhase).coerceIn(0f, 1f)
            val px = center.x + vel.x * maxRadius * localPhase
            val py = center.y + vel.y * maxRadius * localPhase + (localPhase * localPhase * 55f)
            drawCircle(
                color = color.copy(alpha = localFade * 0.85f),
                radius = radiusDp.dp.toPx() * (0.5f + 0.5f * localFade),
                center = Offset(px, py)
            )
        }
    }
}
