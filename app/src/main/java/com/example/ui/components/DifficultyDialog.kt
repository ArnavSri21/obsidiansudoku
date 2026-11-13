package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.logic.Difficulty
import com.example.ui.theme.LocalThemePalette
import com.example.ui.theme.ObsidianWarning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DifficultyDialog(
    currentDifficulty: Difficulty,
    onSelectDifficulty: (Difficulty) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalThemePalette.current

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(palette.surface)
            .border(1.dp, palette.borderStrong, RoundedCornerShape(24.dp))
            .padding(20.dp)
            .testTag("difficulty_dialog")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Select Difficulty",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Choose your level of challenge",
                style = MaterialTheme.typography.bodyMedium,
                color = palette.cellNoteText
            )

            Spacer(modifier = Modifier.height(4.dp))

            val difficulties = listOf(
                Difficulty.EASY,
                Difficulty.MEDIUM,
                Difficulty.HARD,
                Difficulty.EXPERT
            )

            difficulties.forEachIndexed { index, diff ->
                // Each option slides up and fades in a beat after the one above it.
                val enter = remember { Animatable(0f) }
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(60L * index)
                    enter.animateTo(1f, tween(320, easing = FastOutSlowInEasing))
                }

                val isSelected = (diff == currentDifficulty)
                val shape = RoundedCornerShape(14.dp)
                Row(
                    modifier = Modifier
                        .graphicsLayer {
                            alpha = enter.value
                            translationY = (1f - enter.value) * 24.dp.toPx()
                        }
                        .bounceClick(pressedScale = 0.97f) {
                            onSelectDifficulty(diff)
                            onDismiss()
                        }
                        .fillMaxWidth()
                        .clip(shape)
                        .background(if (isSelected) palette.primary.copy(alpha = 0.2f) else palette.card)
                        .border(
                            1.dp,
                            if (isSelected) palette.primary else palette.border,
                            shape
                        )
                        .padding(16.dp)
                        .testTag("difficulty_${diff.name.lowercase()}"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = diff.displayName,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = diff.description,
                            color = palette.cellNoteText,
                            fontSize = 12.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        for (i in 1..diff.stars) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = ObsidianWarning,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
