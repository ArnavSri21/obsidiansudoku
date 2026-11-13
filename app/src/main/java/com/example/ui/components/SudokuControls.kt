package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalThemePalette

@Composable
fun SudokuControls(
    isNotesMode: Boolean,
    remainingCounts: Map<Int, Int>,
    onNumberClick: (Int) -> Unit,
    onUndoClick: () -> Unit,
    onEraseClick: () -> Unit,
    onNotesToggle: () -> Unit,
    onHintClick: () -> Unit,
    canUndo: Boolean,
    modifier: Modifier = Modifier,
    highlightDigit: Int = 0
) {
    val palette = LocalThemePalette.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Action buttons share the row equally, so every one is a large touch target.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ActionButton(
                icon = Icons.AutoMirrored.Filled.Undo,
                label = "Undo",
                enabled = canUndo,
                onClick = onUndoClick,
                testTag = "btn_undo",
                modifier = Modifier.weight(1f)
            )
            ActionButton(
                icon = Icons.Default.Delete,
                label = "Erase",
                enabled = true,
                onClick = onEraseClick,
                testTag = "btn_erase",
                modifier = Modifier.weight(1f)
            )
            ActionButton(
                icon = Icons.Default.Edit,
                label = if (isNotesMode) "Notes ON" else "Notes",
                isActive = isNotesMode,
                enabled = true,
                onClick = onNotesToggle,
                testTag = "btn_notes",
                modifier = Modifier.weight(1f)
            )
            ActionButton(
                icon = Icons.Default.Lightbulb,
                label = "Hint",
                enabled = true,
                onClick = onHintClick,
                tint = palette.secondaryLight,
                testTag = "btn_hint",
                modifier = Modifier.weight(1f)
            )
        }

        // Numpad row 1..9
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (digit in 1..9) {
                val remaining = remainingCounts[digit] ?: 9
                NumpadButton(
                    digit = digit,
                    remaining = remaining,
                    isCompleted = remaining <= 0,
                    isHighlighted = digit == highlightDigit,
                    isNotesMode = isNotesMode,
                    onClick = { onNumberClick(digit) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun ActionButton(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    tint: Color = Color.Unspecified
) {
    val palette = LocalThemePalette.current
    val baseTint = if (tint == Color.Unspecified) palette.primaryLight else tint
    val shape = RoundedCornerShape(14.dp)

    val bgColor by animateColorAsState(
        targetValue = if (isActive) palette.secondary.copy(alpha = 0.22f) else palette.card,
        animationSpec = tween(180),
        label = "actionBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isActive) palette.secondary else palette.border,
        animationSpec = tween(180),
        label = "actionBorder"
    )
    val contentColor by animateColorAsState(
        targetValue = when {
            !enabled -> palette.cellNoteText
            isActive -> palette.secondaryLight
            else -> baseTint
        },
        animationSpec = tween(180),
        label = "actionContent"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .bounceClick(enabled = enabled, onClick = onClick)
            .alpha(if (enabled) 1f else 0.45f)
            .height(60.dp)
            .clip(shape)
            .background(bgColor)
            .border(1.dp, borderColor, shape)
            .padding(horizontal = 4.dp, vertical = 6.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            color = if (enabled) Color.White else palette.cellNoteText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Composable
fun NumpadButton(
    digit: Int,
    remaining: Int,
    isCompleted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false,
    isNotesMode: Boolean = false
) {
    val palette = LocalThemePalette.current
    val shape = RoundedCornerShape(12.dp)

    val bgColor by animateColorAsState(
        targetValue = when {
            isCompleted -> palette.card.copy(alpha = 0.4f)
            isHighlighted -> palette.primary.copy(alpha = 0.28f)
            else -> palette.surfaceElevated
        },
        animationSpec = tween(180),
        label = "numpadBg"
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            isCompleted -> Color.Transparent
            isHighlighted -> palette.primaryLight
            isNotesMode -> palette.secondary.copy(alpha = 0.6f)
            else -> palette.border
        },
        animationSpec = tween(180),
        label = "numpadBorder"
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (isCompleted) 0.35f else 1f,
        animationSpec = tween(260),
        label = "numpadAlpha"
    )
    // How much of this digit is already on the board (0..1), drawn as a thin meter.
    val placed by animateFloatAsState(
        targetValue = ((9 - remaining).coerceIn(0, 9)) / 9f,
        animationSpec = tween(320),
        label = "numpadProgress"
    )

    Box(
        modifier = modifier
            .bounceClick(enabled = !isCompleted, pressedScale = 0.88f, onClick = onClick)
            .height(64.dp)
            .clip(shape)
            .background(bgColor)
            .border(1.dp, borderColor, shape)
            .testTag("numpad_$digit"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.alpha(contentAlpha)
        ) {
            Text(
                text = digit.toString(),
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (isCompleted) "✓" else "$remaining",
                color = if (isCompleted) palette.secondaryLight else palette.cellNoteText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Progress meter along the bottom edge
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 6.dp, vertical = 4.dp)
                .fillMaxWidth(placed)
                .height(2.dp)
                .clip(CircleShape)
                .background(palette.primaryLight.copy(alpha = 0.8f))
        )
    }
}
