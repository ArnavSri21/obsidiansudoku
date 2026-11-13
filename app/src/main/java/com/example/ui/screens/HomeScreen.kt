package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.logic.Difficulty
import com.example.ui.components.AnimatedRollingNumber
import com.example.ui.components.InteractiveSpotlightBox
import com.example.ui.components.StaggeredEntrance
import com.example.ui.components.animatedGlowingBorder
import com.example.ui.components.bounceClick
import com.example.ui.theme.LocalThemePalette
import com.example.ui.viewmodel.SocialViewModel
import com.example.ui.viewmodel.SudokuViewModel
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

enum class MatrixAnimationMode(val label: String) {
    WAVE("Wave"),
    VORTEX("Vortex"),
    QUANTUM("Quantum")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    sudokuViewModel: SudokuViewModel,
    socialViewModel: SocialViewModel,
    onStartMatch: (Difficulty) -> Unit,
    onNavigateToRank: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToDaily: () -> Unit,
    onNavigateToStickers: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalThemePalette.current
    val socialUiState by socialViewModel.uiState.collectAsState()
    val sudokuUiState by sudokuViewModel.uiState.collectAsState()

    var selectedDifficulty by remember { mutableStateOf(Difficulty.EASY) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hero Player Banner with 3D Interactive Tilt & Spotlight
        StaggeredEntrance(index = 0) {
            PlayerHeroCard(
                username = socialUiState.publicUsername,
                totalScore = sudokuUiState.totalScore,
                streak = socialUiState.currentStreak,
                totalVictories = socialUiState.totalGamesWon,
                palette = palette
            )
        }

        // Cool Interactive Animated Sudoku Showcase
        StaggeredEntrance(index = 1) {
            SudokuMatrixAnimationCard(palette = palette)
        }

        // 1. Play Match Hero Action Card
        StaggeredEntrance(index = 2) {
            InteractiveSpotlightBox(
                spotlightColor = palette.primary.copy(alpha = 0.16f),
                maxTiltDegrees = 3.5f
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animatedGlowingBorder(
                            primaryColor = palette.primary,
                            secondaryColor = palette.secondary,
                            cornerRadius = 20.dp,
                            borderWidth = 1.5.dp
                        )
                        .testTag("play_match_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = palette.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            Brush.linearGradient(
                                                listOf(
                                                    palette.primary.copy(alpha = 0.3f),
                                                    palette.secondary.copy(alpha = 0.15f)
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = palette.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Play Match",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = palette.cellGivenText
                                    )
                                    Text(
                                        text = "Choose difficulty & test your logic",
                                        fontSize = 12.sp,
                                        color = palette.cellNoteText
                                    )
                                }
                            }

                            // Animated rolling score multiplier badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(palette.secondary.copy(alpha = 0.16f))
                                    .border(1.dp, palette.secondary.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                AnimatedRollingNumber(
                                    value = selectedDifficulty.stars * 500,
                                    prefix = "+",
                                    suffix = " pts",
                                    color = palette.secondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Difficulty Chips with Spring Bounce
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                Difficulty.EASY,
                                Difficulty.MEDIUM,
                                Difficulty.HARD,
                                Difficulty.EXPERT
                            ).forEach { diff ->
                                val isSelected = (selectedDifficulty == diff)
                                val chipScale by animateFloatAsState(
                                    targetValue = if (isSelected) 1.05f else 1f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    ),
                                    label = "diff_chip_scale"
                                )
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedDifficulty = diff },
                                    label = {
                                        Text(
                                            text = diff.displayName,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = palette.primary.copy(alpha = 0.25f),
                                        selectedLabelColor = palette.primary,
                                        containerColor = palette.card,
                                        labelColor = palette.cellNoteText
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        selectedBorderColor = palette.primary,
                                        borderColor = palette.border
                                    ),
                                    modifier = Modifier
                                        .scale(chipScale)
                                        .bounceClick(scaleDown = 0.92f)
                                        .testTag("home_difficulty_${diff.name.lowercase()}")
                                )
                            }
                        }

                        // Launch Button with Spring Bounce
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { onStartMatch(selectedDifficulty) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .bounceClick(scaleDown = 0.95f)
                                    .testTag("play_match_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = palette.primary,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                AnimatedContent(
                                    targetState = selectedDifficulty.displayName,
                                    transitionSpec = {
                                        (scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn()) togetherWith
                                            (scaleOut() + fadeOut())
                                    },
                                    label = "play_btn_label"
                                ) { diffName ->
                                    Text(
                                        text = "Play $diffName",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Rank & Dashboard Action Cards (Side by Side with Interactive Tilt & Bounce)
        StaggeredEntrance(index = 3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Rank / Leaderboard Option
                InteractiveSpotlightBox(
                    modifier = Modifier.weight(1f),
                    spotlightColor = palette.secondary.copy(alpha = 0.18f),
                    maxTiltDegrees = 5f
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .bounceClick(scaleDown = 0.94f, tiltDegrees = -1.2f)
                            .clickable { onNavigateToRank() }
                            .testTag("rank_button"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = palette.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, palette.borderStrong)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(palette.secondary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Leaderboard,
                                    contentDescription = null,
                                    tint = palette.secondary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Rankings",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.cellGivenText
                                )
                                Text(
                                    text = "Live realtime leaderboard",
                                    fontSize = 11.sp,
                                    color = palette.cellNoteText
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "View Rank",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.secondary
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = palette.secondary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                // Dashboard Option
                InteractiveSpotlightBox(
                    modifier = Modifier.weight(1f),
                    spotlightColor = palette.primary.copy(alpha = 0.18f),
                    maxTiltDegrees = 5f
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .bounceClick(scaleDown = 0.94f, tiltDegrees = 1.2f)
                            .clickable { onNavigateToDashboard() }
                            .testTag("dashboard_button"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = palette.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, palette.borderStrong)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(palette.primary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BarChart,
                                    contentDescription = null,
                                    tint = palette.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Dashboard",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.cellGivenText
                                )
                                Text(
                                    text = "Stats of every solved puzzle",
                                    fontSize = 11.sp,
                                    color = palette.cellNoteText
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Open Stats",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.primary
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = palette.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Quick Daily & Trophy shortcuts with Spring Bounce
        StaggeredEntrance(index = 4) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Daily Challenge shortcut
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .bounceClick(scaleDown = 0.94f)
                        .clickable { onNavigateToDaily() }
                        .testTag("daily_challenge_shortcut"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = palette.card),
                    border = androidx.compose.foundation.BorderStroke(1.dp, palette.border)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = palette.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Daily Puzzle",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = palette.cellGivenText
                            )
                            Text(
                                text = "Keep streak alive",
                                fontSize = 11.sp,
                                color = palette.cellNoteText
                            )
                        }
                    }
                }

                // Stickers shortcut
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .bounceClick(scaleDown = 0.94f)
                        .clickable { onNavigateToStickers() }
                        .testTag("stickers_shortcut"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = palette.card),
                    border = androidx.compose.foundation.BorderStroke(1.dp, palette.border)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = palette.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Stickers",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = palette.cellGivenText
                            )
                            Text(
                                text = "Milestones & Shards",
                                fontSize = 11.sp,
                                color = palette.cellNoteText
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
fun PlayerHeroCard(
    username: String,
    totalScore: Int,
    streak: Int,
    totalVictories: Int,
    palette: com.example.ui.theme.ThemePalette
) {
    val infiniteTransition = rememberInfiniteTransition(label = "flame_pulse")
    val flameScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame_scale"
    )

    InteractiveSpotlightBox(
        spotlightColor = palette.primary.copy(alpha = 0.18f),
        maxTiltDegrees = 4f
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .animatedGlowingBorder(
                    primaryColor = palette.primary,
                    secondaryColor = palette.secondary,
                    cornerRadius = 22.dp,
                    borderWidth = 1.2.dp
                )
                .testTag("player_hero_card"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = palette.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Obsidian Solver",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = palette.secondary
                        )
                        Text(
                            text = "@$username",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = palette.cellGivenText
                        )
                    }

                    // Streak flame badge with pulse animation & rolling number
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(palette.secondary.copy(alpha = 0.18f))
                            .border(1.dp, palette.secondary.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("home_streak_badge"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = palette.secondary,
                            modifier = Modifier
                                .size(20.dp)
                                .scale(flameScale)
                        )
                        AnimatedRollingNumber(
                            value = streak,
                            suffix = " Days",
                            color = palette.secondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Stats row with odometer rolling counters
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(palette.card)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = palette.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text("Rating Score", fontSize = 10.sp, color = palette.cellNoteText)
                            AnimatedRollingNumber(
                                value = totalScore,
                                suffix = " pts",
                                color = palette.cellGivenText,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(width = 1.dp, height = 28.dp)
                            .background(palette.border)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = palette.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text("Matches Won", fontSize = 10.sp, color = palette.cellNoteText)
                            AnimatedRollingNumber(
                                value = totalVictories,
                                color = palette.cellGivenText,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Interactive recent.design-inspired Sudoku Matrix Playground.
 * Supports:
 * - Tapping any individual cell (r, c) to spawn a radial shockwave ripple from that exact coordinate,
 *   morph the digit with spring physics, and emit a burst of glowing particles.
 * - Switching between 3 procedural animation modes: Wave, Vortex, and Quantum.
 */
@Composable
fun SudokuMatrixAnimationCard(
    palette: com.example.ui.theme.ThemePalette
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sudoku_wave")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 9f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_offset"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    var animMode by remember { mutableStateOf(MatrixAnimationMode.WAVE) }
    var lastTappedCell by remember { mutableStateOf(4 to 4) }
    var tapCount by remember { mutableIntStateOf(0) }
    val shockwaveProgress = remember { Animatable(1f) }
    val interactiveDigits = remember { mutableStateMapOf<Pair<Int, Int>, Int>() }

    LaunchedEffect(tapCount) {
        if (tapCount > 0) {
            shockwaveProgress.snapTo(0f)
            shockwaveProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(750, easing = FastOutSlowInEasing)
            )
        }
    }

    val sampleBoard = remember {
        listOf(
            listOf(5, 3, 0,  0, 7, 0,  0, 0, 0),
            listOf(6, 0, 0,  1, 9, 5,  0, 0, 0),
            listOf(0, 9, 8,  0, 0, 0,  0, 6, 0),

            listOf(8, 0, 0,  0, 6, 0,  0, 0, 3),
            listOf(4, 0, 0,  8, 0, 3,  0, 0, 1),
            listOf(7, 0, 0,  0, 2, 0,  0, 0, 6),

            listOf(0, 6, 0,  0, 0, 0,  2, 8, 0),
            listOf(0, 0, 0,  4, 1, 9,  0, 0, 5),
            listOf(0, 0, 0,  0, 8, 0,  0, 7, 9)
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sudoku_animation_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = palette.card),
        border = androidx.compose.foundation.BorderStroke(1.dp, palette.borderStrong)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = palette.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Interactive Matrix",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.cellGivenText
                    )
                }

                // Mode Switcher Pills
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MatrixAnimationMode.entries.forEach { mode ->
                        val selected = (animMode == mode)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (selected) palette.primary.copy(alpha = 0.25f)
                                    else palette.surface
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (selected) palette.primary else palette.border,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .bounceClick(scaleDown = 0.9f)
                                .clickable {
                                    animMode = mode
                                    tapCount++
                                }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = mode.label,
                                fontSize = 10.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) palette.primary else palette.cellNoteText
                            )
                        }
                    }
                }
            }

            // 9x9 Interactive Animated Grid
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.22f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(palette.surface)
                    .border(1.dp, palette.border, RoundedCornerShape(16.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background scanline & radial shockwave canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val scanY = (waveOffset / 9f) * h

                    // Draw subtle laser scanline
                    drawLine(
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                palette.primary.copy(alpha = glowAlpha * 0.7f),
                                palette.secondary.copy(alpha = glowAlpha * 0.95f),
                                palette.primary.copy(alpha = glowAlpha * 0.7f),
                                Color.Transparent
                            )
                        ),
                        start = Offset(0f, scanY),
                        end = Offset(w, scanY),
                        strokeWidth = 2.5.dp.toPx()
                    )

                    // Draw expanding shockwave ring + particles from last tapped cell
                    val progress = shockwaveProgress.value
                    if (progress < 0.99f) {
                        val centerX = ((lastTappedCell.second + 0.5f) / 9f) * w
                        val centerY = ((lastTappedCell.first + 0.5f) / 9f) * h
                        val centerOffset = Offset(centerX, centerY)
                        val maxRadius = hypot(w, h) * 0.65f
                        val currentRadius = maxRadius * progress
                        val ringAlpha = (1f - progress).coerceIn(0f, 1f)

                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    palette.primary.copy(alpha = ringAlpha * 0.45f),
                                    palette.secondary.copy(alpha = ringAlpha * 0.7f),
                                    Color.Transparent
                                ),
                                center = centerOffset,
                                radius = currentRadius.coerceAtLeast(1f)
                            ),
                            center = centerOffset,
                            radius = currentRadius
                        )

                        // Burst of 12 glowing particles around tapped cell
                        for (i in 0 until 12) {
                            val angle = (i / 12f) * (2f * Math.PI.toFloat()) + (tapCount * 0.3f)
                            val dist = progress * 56.dp.toPx()
                            val px = centerX + cos(angle) * dist
                            val py = centerY + sin(angle) * dist
                            drawCircle(
                                color = if (i % 2 == 0) palette.secondary.copy(alpha = ringAlpha)
                                else palette.primary.copy(alpha = ringAlpha),
                                radius = (3.5f * (1f - progress * 0.6f)).dp.toPx(),
                                center = Offset(px, py)
                            )
                        }
                    }
                }

                // Interactive Grid Cells
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (r in 0 until 9) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (c in 0 until 9) {
                                val baseDigit = sampleBoard[r][c]
                                val customDigit = interactiveDigits[r to c]
                                val digit = customDigit ?: baseDigit

                                // Calculate cell excitation based on current mode + radial distance to tapped cell
                                val radialDist = hypot(
                                    (r - lastTappedCell.first).toFloat(),
                                    (c - lastTappedCell.second).toFloat()
                                )
                                val shockwaveRingDist = abs(radialDist - shockwaveProgress.value * 7.5f)
                                val isShockwaveHit = shockwaveProgress.value < 0.95f && shockwaveRingDist < 1.15f

                                val isWaveActive = when (animMode) {
                                    MatrixAnimationMode.WAVE -> ((r + c + tapCount) % 9 == waveOffset.toInt() % 9)
                                    MatrixAnimationMode.VORTEX -> {
                                        val angle = ((atan2((r - 4).toDouble(), (c - 4).toDouble()) + Math.PI) / (2 * Math.PI) * 9).toInt()
                                        ((angle + radialDist.toInt()) % 9 == waveOffset.toInt() % 9)
                                    }
                                    MatrixAnimationMode.QUANTUM -> {
                                        ((r * 3 + c * 7 + tapCount) % 9 == waveOffset.toInt() % 9)
                                    }
                                }

                                val isExcited = isWaveActive || isShockwaveHit || (lastTappedCell == (r to c) && shockwaveProgress.value < 0.5f)

                                val cellScale by animateFloatAsState(
                                    targetValue = when {
                                        lastTappedCell == (r to c) && shockwaveProgress.value < 0.35f -> 1.28f
                                        isShockwaveHit -> 1.15f
                                        isWaveActive -> 1.07f
                                        else -> 1f
                                    },
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    ),
                                    label = "matrix_cell_scale"
                                )

                                val cellBg = when {
                                    isShockwaveHit -> palette.secondary.copy(alpha = 0.42f)
                                    isWaveActive -> palette.primary.copy(alpha = 0.35f)
                                    customDigit != null -> palette.primary.copy(alpha = 0.18f)
                                    (r in 3..5 && c in 3..5) -> palette.surfaceElevated.copy(alpha = 0.7f)
                                    else -> Color.Transparent
                                }

                                Box(
                                    modifier = Modifier
                                        .size(25.dp)
                                        .graphicsLayer {
                                            scaleX = cellScale
                                            scaleY = cellScale
                                        }
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(cellBg)
                                        .clickable {
                                            lastTappedCell = r to c
                                            val nextNum = ((digit % 9) + 1)
                                            interactiveDigits[r to c] = nextNum
                                            tapCount++
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (digit != 0) {
                                        Text(
                                            text = "$digit",
                                            fontSize = 11.sp,
                                            fontWeight = if (isExcited || customDigit != null) FontWeight.ExtraBold else FontWeight.SemiBold,
                                            color = when {
                                                isShockwaveHit -> Color.White
                                                isWaveActive -> palette.secondary
                                                customDigit != null -> palette.primary
                                                else -> palette.cellGivenText
                                            }
                                        )
                                    } else if (isExcited) {
                                        Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .clip(CircleShape)
                                                .background(palette.primary.copy(alpha = glowAlpha))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Text(
                text = "Tap any cell to spawn a quantum ripple & morph digits",
                fontSize = 11.sp,
                color = palette.cellNoteText
            )
        }
    }
}
