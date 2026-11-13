package com.example.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalThemePalette
import com.example.ui.theme.ObsidianWarning

@Composable
fun PersistentTimerComponent(
    seconds: Long,
    isPaused: Boolean,
    onTogglePause: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalThemePalette.current
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val accent by animateColorAsState(
        targetValue = if (isPaused) ObsidianWarning else palette.secondary,
        animationSpec = tween(220),
        label = "timerAccent"
    )
    val formattedTime = formatTimer(seconds)

    Row(
        modifier = modifier
            .bounceClick(pressedScale = 0.95f, onClick = onTogglePause)
            .clip(RoundedCornerShape(20.dp))
            .background(palette.surfaceElevated)
            .border(1.dp, palette.border, RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("timer_component"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Status indicator dot (breathes while the clock runs)
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (isPaused) accent else accent.copy(alpha = alpha))
        )

        Text(
            text = formattedTime,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.testTag("timer_display")
        )

        Crossfade(targetState = isPaused, animationSpec = tween(180), label = "timerIcon") { paused ->
            Icon(
                imageVector = if (paused) Icons.Default.PlayArrow else Icons.Default.Pause,
                contentDescription = if (paused) "Resume game" else "Pause game",
                tint = accent,
                modifier = Modifier
                    .size(18.dp)
                    .testTag("timer_pause_toggle")
            )
        }
    }
}

fun formatTimer(seconds: Long): String {
    val hrs = seconds / 3600
    val mins = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hrs > 0) {
        String.format("%02d:%02d:%02d", hrs, mins, secs)
    } else {
        String.format("%02d:%02d", mins, secs)
    }
}
