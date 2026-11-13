package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.logic.ScoreResult
import com.example.ui.components.ConfettiBurst
import com.example.ui.components.DifficultyDialog
import com.example.ui.components.PersistentTimerComponent
import com.example.ui.components.SudokuControls
import com.example.ui.components.SudokuGridView
import com.example.ui.components.SudokuPauseOverlay
import com.example.ui.components.bounceClick
import com.example.ui.components.formatTimer
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.DigitGiven
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.LocalThemePalette
import com.example.ui.theme.WarmAmber
import com.example.ui.viewmodel.SudokuViewModel
import kotlinx.coroutines.delay

@Composable
fun PlayScreen(
    viewModel: SudokuViewModel,
    onGoHome: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val palette = LocalThemePalette.current
    var showDifficultyDialog by remember { mutableStateOf(false) }

    val selectedValue = run {
        val r = uiState.selectedRow
        val c = uiState.selectedCol
        if (r != null && c != null) uiState.board.getOrNull(r)?.getOrNull(c)?.value ?: 0 else 0
    }

    // Keep the last message around so the banner can fade out without going blank.
    var lastFeedback by remember { mutableStateOf("") }
    LaunchedEffect(uiState.feedbackMessage) {
        uiState.feedbackMessage?.let { lastFeedback = it }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar: difficulty picker, reset and new game
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .bounceClick(pressedScale = 0.95f) { showDifficultyDialog = true }
                        .clip(RoundedCornerShape(14.dp))
                        .background(palette.card)
                        .border(1.dp, palette.border, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .testTag("difficulty_selector"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = uiState.difficulty.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = palette.primaryLight
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "▼",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HeaderIconButton(
                        icon = Icons.Default.Refresh,
                        description = "Reset Board",
                        tint = DigitGiven,
                        testTag = "reset_board_button",
                        onClick = { viewModel.resetBoard() }
                    )
                    HeaderIconButton(
                        icon = Icons.Default.Add,
                        description = "New Game",
                        tint = palette.primaryLight,
                        testTag = "new_game_button",
                        onClick = { showDifficultyDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Timer and mistakes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PersistentTimerComponent(
                    seconds = uiState.timerSeconds,
                    isPaused = uiState.isPaused,
                    onTogglePause = { viewModel.onTogglePause() }
                )

                MistakesBadge(
                    mistakes = uiState.mistakes,
                    maxMistakes = uiState.maxMistakes
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Board with pause cover
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                SudokuGridView(
                    board = uiState.board,
                    selectedRow = uiState.selectedRow,
                    selectedCol = uiState.selectedCol,
                    onCellClick = { r, c -> viewModel.onCellClick(r, c) }
                )

                if (uiState.isPaused) {
                    SudokuPauseOverlay(
                        onResume = { viewModel.resumeGame() },
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(16.dp))
                    )
                }
            }

            // Feedback banner
            AnimatedVisibility(
                visible = uiState.feedbackMessage != null,
                enter = fadeIn(tween(200)) + expandVertically(tween(240)),
                exit = fadeOut(tween(200)) + shrinkVertically(tween(240))
            ) {
                val tone = when {
                    lastFeedback.startsWith("Incorrect") || lastFeedback.startsWith("Game Over") -> CrimsonError
                    lastFeedback.startsWith("Victory") -> EmeraldGreen
                    else -> palette.secondary
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(palette.card)
                        .border(1.dp, tone.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = tone,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = lastFeedback,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Controls and numpad
            SudokuControls(
                isNotesMode = uiState.isNotesMode,
                remainingCounts = uiState.remainingCounts,
                onNumberClick = { digit -> viewModel.onNumberInput(digit) },
                onUndoClick = { viewModel.onUndo() },
                onEraseClick = { viewModel.onErase() },
                onNotesToggle = { viewModel.onNotesToggle() },
                onHintClick = { viewModel.onHint() },
                canUndo = viewModel.canUndo,
                highlightDigit = selectedValue
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showDifficultyDialog) {
        DifficultyDialog(
            currentDifficulty = uiState.difficulty,
            onSelectDifficulty = { diff ->
                viewModel.startNewGame(diff, isDaily = false)
            },
            onDismiss = { showDifficultyDialog = false }
        )
    }

    if (uiState.isGameWon) {
        VictoryDialog(
            timeFormatted = formatTimer(uiState.timerSeconds),
            mistakes = uiState.mistakes,
            difficulty = uiState.difficulty.displayName,
            matchScore = uiState.lastMatchScore,
            totalScore = uiState.totalScore,
            onPlayAgain = {
                viewModel.startNewGame(uiState.difficulty, isDaily = false)
            },
            onGoHome = onGoHome
        )
    }

    if (uiState.isGameOver) {
        GameOverDialog(
            mistakes = uiState.mistakes,
            matchScore = uiState.lastMatchScore,
            totalScore = uiState.totalScore,
            onTryAgain = {
                viewModel.startNewGame(uiState.difficulty, isDaily = false)
            },
            onGoHome = onGoHome
        )
    }
}

@Composable
private fun HeaderIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    tint: Color,
    testTag: String,
    onClick: () -> Unit
) {
    val palette = LocalThemePalette.current
    Box(
        modifier = Modifier
            .bounceClick(pressedScale = 0.88f, onClick = onClick)
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(palette.card)
            .border(1.dp, palette.border, RoundedCornerShape(12.dp))
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
    }
}

/** Pip meter plus "n/max" counter; the badge shakes whenever a new mistake is made. */
@Composable
private fun MistakesBadge(mistakes: Int, maxMistakes: Int) {
    val palette = LocalThemePalette.current
    val shake = remember { Animatable(0f) }
    var previous by remember { mutableIntStateOf(mistakes) }
    LaunchedEffect(mistakes) {
        if (mistakes > previous) {
            for (target in listOf(-1f, 1f, -0.7f, 0.7f, -0.3f, 0f)) {
                shake.animateTo(target, tween(50))
            }
        }
        previous = mistakes
    }

    Row(
        modifier = Modifier
            .graphicsLayer { translationX = shake.value * 8.dp.toPx() }
            .clip(RoundedCornerShape(20.dp))
            .background(palette.card)
            .border(
                1.dp,
                if (mistakes > 0) CrimsonError.copy(alpha = 0.5f) else palette.border,
                RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("mistakes_display"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            for (i in 0 until maxMistakes) {
                val pipColor by animateColorAsState(
                    targetValue = if (i < mistakes) CrimsonError else palette.border,
                    animationSpec = tween(220),
                    label = "mistakePip"
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(pipColor)
                )
            }
        }
        Text(
            text = "$mistakes/$maxMistakes",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (mistakes > 0) CrimsonError else DigitGiven
        )
    }
}

@Composable
private fun AnimatedStar(filled: Boolean, delayMillis: Long) {
    val palette = LocalThemePalette.current
    val scale = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(delayMillis)
        scale.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = 300f))
    }
    Icon(
        imageVector = Icons.Default.Star,
        contentDescription = null,
        tint = if (filled) WarmAmber else palette.border,
        modifier = Modifier
            .size(34.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                rotationZ = (1f - scale.value) * -40f
            }
    )
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    val palette = LocalThemePalette.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(palette.card)
            .border(1.dp, palette.border, RoundedCornerShape(14.dp))
            .padding(vertical = 10.dp, horizontal = 6.dp)
    ) {
        Text(label, fontSize = 11.sp, color = palette.cellNoteText)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            maxLines = 1
        )
    }
}

@Composable
fun VictoryDialog(
    timeFormatted: String,
    mistakes: Int,
    difficulty: String,
    matchScore: ScoreResult?,
    totalScore: Int,
    onPlayAgain: () -> Unit,
    onGoHome: () -> Unit
) {
    val palette = LocalThemePalette.current
    val haptic = LocalHapticFeedback.current
    val earned = matchScore?.totalEarned ?: 600
    val stars = when {
        mistakes == 0 -> 3
        mistakes <= 2 -> 2
        else -> 1
    }

    val enter = remember { Animatable(0f) }
    val countUp = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        enter.animateTo(1f, spring(dampingRatio = 0.6f, stiffness = 260f))
    }
    LaunchedEffect(Unit) {
        delay(450)
        countUp.animateTo(1f, tween(1100, easing = FastOutSlowInEasing))
    }

    // Slow glow on the badge
    val glowTransition = rememberInfiniteTransition(label = "victoryGlow")
    val glow by glowTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Reverse),
        label = "victoryGlowAlpha"
    )

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag("victory_dialog"),
            contentAlignment = Alignment.Center
        ) {
            ConfettiBurst(
                colors = listOf(
                    palette.primary,
                    palette.primaryLight,
                    palette.secondary,
                    palette.tertiary,
                    EmeraldGreen,
                    WarmAmber
                ),
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
                    .graphicsLayer {
                        val s = 0.7f + 0.3f * enter.value
                        scaleX = s
                        scaleY = s
                        alpha = enter.value.coerceIn(0f, 1f)
                    }
                    .clip(RoundedCornerShape(24.dp))
                    .background(palette.surface)
                    .border(
                        1.5.dp,
                        Brush.linearGradient(listOf(EmeraldGreen, palette.primaryLight)),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(EmeraldGreen.copy(alpha = glow)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Text(
                    text = "Puzzle Solved!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (i in 1..3) {
                        AnimatedStar(filled = i <= stars, delayMillis = 350L + i * 160L)
                    }
                }

                // Score banner with count-up
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(palette.card)
                        .border(1.dp, palette.border, RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = WarmAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "+${(earned * countUp.value).toInt()} Points",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = WarmAmber
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Total Rating: $totalScore pts",
                            fontSize = 12.sp,
                            color = palette.cellNoteText
                        )
                        if (matchScore != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Base: ${matchScore.basePoints}", fontSize = 11.sp, color = palette.cellNoteText)
                                if (matchScore.speedBonus > 0) {
                                    Text("⚡+${matchScore.speedBonus}", fontSize = 11.sp, color = palette.secondaryLight)
                                }
                                if (matchScore.accuracyBonus > 0) {
                                    Text("🎯+${matchScore.accuracyBonus}", fontSize = 11.sp, color = EmeraldGreen)
                                }
                                if (matchScore.streakBonus > 0) {
                                    Text("🔥+${matchScore.streakBonus}", fontSize = 11.sp, color = WarmAmber)
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatTile("Time", timeFormatted, palette.secondaryLight, Modifier.weight(1f))
                    StatTile(
                        "Mistakes",
                        "$mistakes",
                        if (mistakes == 0) EmeraldGreen else WarmAmber,
                        Modifier.weight(1f)
                    )
                    StatTile("Difficulty", difficulty, palette.primaryLight, Modifier.weight(1f))
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onPlayAgain,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.primary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("victory_play_again_button")
                    ) {
                        Text("Play Again", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    OutlinedButton(
                        onClick = onGoHome,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, palette.border),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("victory_home_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Back to Home Screen", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun GameOverDialog(
    mistakes: Int,
    matchScore: ScoreResult?,
    totalScore: Int,
    onTryAgain: () -> Unit,
    onGoHome: () -> Unit
) {
    val palette = LocalThemePalette.current
    val haptic = LocalHapticFeedback.current

    val enter = remember { Animatable(0f) }
    val wobble = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        enter.animateTo(1f, spring(dampingRatio = 0.7f, stiffness = 300f))
    }
    LaunchedEffect(Unit) {
        delay(250)
        for (target in listOf(-12f, 10f, -7f, 5f, -2f, 0f)) {
            wobble.animateTo(target, tween(70))
        }
    }

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag("game_over_dialog"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
                    .graphicsLayer {
                        val s = 0.8f + 0.2f * enter.value
                        scaleX = s
                        scaleY = s
                        alpha = enter.value.coerceIn(0f, 1f)
                    }
                    .clip(RoundedCornerShape(24.dp))
                    .background(palette.surface)
                    .border(1.5.dp, CrimsonError, RoundedCornerShape(24.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .graphicsLayer { rotationZ = wobble.value }
                        .clip(CircleShape)
                        .background(CrimsonError.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = CrimsonError,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Text(
                    text = "Game Over",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "You made $mistakes mistakes. Focus sharpens through practice.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.cellNoteText,
                    fontSize = 13.sp
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(palette.card)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+${matchScore?.totalEarned ?: 100} Match Effort Points  •  Total: $totalScore pts",
                        fontSize = 12.sp,
                        color = Color.LightGray,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onTryAgain,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.primary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("try_again_button")
                    ) {
                        Text("Try Again", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    OutlinedButton(
                        onClick = onGoHome,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, palette.border),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("game_over_home_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Back to Home Screen", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
