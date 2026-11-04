package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.logic.Difficulty
import com.example.ui.theme.LocalThemePalette
import com.example.ui.viewmodel.SocialViewModel
import com.example.ui.viewmodel.SudokuViewModel
import kotlin.math.sin

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
        // Hero Player Banner
        PlayerHeroCard(
            username = socialUiState.publicUsername,
            totalScore = sudokuUiState.totalScore,
            streak = socialUiState.currentStreak,
            totalVictories = socialUiState.totalGamesWon,
            palette = palette
        )

        // Cool Animated Sudoku Showcase
        SudokuMatrixAnimationCard(palette = palette)

        // 1. Play Match Hero Action Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("play_match_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = palette.surface),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, palette.primary.copy(alpha = 0.5f))
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
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(palette.primary.copy(alpha = 0.2f)),
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

                    // Score multiplier badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(palette.secondary.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "+${selectedDifficulty.stars * 500} pts",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.secondary
                        )
                    }
                }

                // Difficulty Chips
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
                            modifier = Modifier.testTag("home_difficulty_${diff.name.lowercase()}")
                        )
                    }
                }

                // Launch Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onStartMatch(selectedDifficulty) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("play_match_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.primary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Play ${selectedDifficulty.displayName}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }

        // 2. Rank & Dashboard Action Cards (Side by Side or 2 Rows)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Rank / Leaderboard Option
            Card(
                modifier = Modifier
                    .weight(1f)
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

            // Dashboard Option
            Card(
                modifier = Modifier
                    .weight(1f)
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

        // 3. Quick Daily & Trophy shortcuts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Daily Challenge shortcut
            Card(
                modifier = Modifier
                    .weight(1f)
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
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("player_hero_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = palette.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, palette.borderStrong)
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

                // Streak flame badge with pulse animation
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
                    Text(
                        text = "$streak Days",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.secondary
                    )
                }
            }

            // Stats row
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
                        Text(
                            text = "$totalScore pts",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = palette.cellGivenText
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
                        Text(
                            text = "$totalVictories",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = palette.cellGivenText
                        )
                    }
                }
            }
        }
    }
}

/**
 * Animated dynamic Sudoku showcase with live solving wave, neon scanlines, and reactive ripple.
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
            animation = tween(3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_offset"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    var rippleSeed by remember { mutableIntStateOf(0) }

    // Sample stylized Sudoku digits for animation
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
                        text = "Obsidian Matrix Engine",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.cellGivenText
                    )
                }

                Text(
                    text = "Tap to Pulse Ripple",
                    fontSize = 11.sp,
                    color = palette.cellNoteText
                )
            }

            // 9x9 Animated Grid
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.25f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(palette.surface)
                    .border(1.dp, palette.border, RoundedCornerShape(16.dp))
                    .clickable { rippleSeed++ }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background scanline canvas
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
                                palette.secondary.copy(alpha = glowAlpha * 0.9f),
                                palette.primary.copy(alpha = glowAlpha * 0.7f),
                                Color.Transparent
                            )
                        ),
                        start = Offset(0f, scanY),
                        end = Offset(w, scanY),
                        strokeWidth = 3.dp.toPx()
                    )
                }

                // Grid layout
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
                                val digit = sampleBoard[r][c]
                                val dist = (r + c + rippleSeed * 2) % 9
                                val isWaveActive = (dist == waveOffset.toInt() % 9)
                                val cellBg = when {
                                    isWaveActive -> palette.primary.copy(alpha = 0.35f)
                                    (r in 3..5 && c in 3..5) -> palette.surfaceElevated.copy(alpha = 0.7f)
                                    else -> Color.Transparent
                                }

                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(cellBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (digit != 0) {
                                        Text(
                                            text = "$digit",
                                            fontSize = 11.sp,
                                            fontWeight = if (isWaveActive) FontWeight.ExtraBold else FontWeight.SemiBold,
                                            color = if (isWaveActive) palette.secondary else palette.cellGivenText
                                        )
                                    } else if (isWaveActive) {
                                        // Small energy pulse dot
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
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
        }
    }
}
