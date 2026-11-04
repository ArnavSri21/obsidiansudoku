package com.example.ui.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GameStatsEntity
import com.example.data.model.SolvedPuzzleEntity
import com.example.ui.components.formatTimer
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.LocalThemePalette
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.WarmAmber
import com.example.ui.viewmodel.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToPlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val palette = LocalThemePalette.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Player Dashboard",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = palette.cellGivenText
                )
                Text(
                    text = "Performance overview and complete history of every puzzle solved",
                    fontSize = 12.sp,
                    color = palette.cellNoteText
                )
            }
        }

        // Overview KPI Cards
        item {
            PerformanceSummaryGrid(uiState = uiState, palette = palette)
        }

        // Difficulty Breakdown Section
        item {
            DifficultyBreakdownCard(statsList = uiState.statsList, palette = palette)
        }

        // Every Puzzle Solved History Header & Filters
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = palette.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Every Solved Puzzle",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.cellGivenText
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(palette.primary.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${uiState.allSolvedPuzzles.size} Solved",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.primary
                        )
                    }
                }

                // Filter Chips
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("ALL", "EASY", "MEDIUM", "HARD", "EXPERT", "DAILY", "FLAWLESS").forEach { filterKey ->
                        val isSelected = (uiState.selectedFilter.equals(filterKey, ignoreCase = true))
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setFilter(filterKey) },
                            label = {
                                Text(
                                    text = if (filterKey == "ALL") "All (${uiState.allSolvedPuzzles.size})"
                                    else if (filterKey == "FLAWLESS") "Flawless ⭐"
                                    else filterKey.lowercase().replaceFirstChar { it.uppercase() },
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
                            modifier = Modifier.testTag("filter_chip_${filterKey.lowercase()}")
                        )
                    }
                }
            }
        }

        // Solved Puzzle Items or Empty State
        if (uiState.filteredSolvedPuzzles.isEmpty()) {
            item {
                EmptySolvedPuzzlesCard(
                    palette = palette,
                    onPlayMatch = onNavigateToPlay
                )
            }
        } else {
            items(uiState.filteredSolvedPuzzles, key = { it.id }) { puzzle ->
                SolvedPuzzleItemCard(puzzle = puzzle, palette = palette)
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun PerformanceSummaryGrid(
    uiState: com.example.ui.viewmodel.DashboardUiState,
    palette: com.example.ui.theme.ThemePalette
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("kpi_summary_grid"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = palette.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, palette.borderStrong)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Row 1: Solved & Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiItemBox(
                    title = "Puzzles Solved",
                    value = "${uiState.totalSolved}",
                    icon = Icons.Default.CheckCircle,
                    accentColor = EmeraldGreen,
                    palette = palette,
                    modifier = Modifier.weight(1f)
                )

                KpiItemBox(
                    title = "Total Rating",
                    value = "${uiState.totalScore} pts",
                    icon = Icons.Default.Star,
                    accentColor = WarmAmber,
                    palette = palette,
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 2: Win Rate & Best Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiItemBox(
                    title = "Win Rate",
                    value = String.format(Locale.US, "%.1f%%", uiState.winRatePercent),
                    icon = Icons.Default.EmojiEvents,
                    accentColor = palette.primary,
                    palette = palette,
                    modifier = Modifier.weight(1f)
                )

                KpiItemBox(
                    title = "Fastest Solve",
                    value = uiState.bestTimeFormatted,
                    icon = Icons.Default.Bolt,
                    accentColor = NeonCyan,
                    palette = palette,
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 3: Streak & Flawless
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiItemBox(
                    title = "Current Streak",
                    value = "🔥 ${uiState.currentStreak} Days",
                    icon = Icons.Default.LocalFireDepartment,
                    accentColor = palette.secondary,
                    palette = palette,
                    modifier = Modifier.weight(1f)
                )

                KpiItemBox(
                    title = "Flawless Solves",
                    value = "${uiState.flawlessCount}",
                    icon = Icons.Default.CheckCircle,
                    accentColor = EmeraldGreen,
                    palette = palette,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun KpiItemBox(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    palette: com.example.ui.theme.ThemePalette,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(palette.card)
            .border(1.dp, palette.border, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = palette.cellNoteText
                )
            }
            Text(
                text = value,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = palette.cellGivenText
            )
        }
    }
}

@Composable
fun DifficultyBreakdownCard(
    statsList: List<GameStatsEntity>,
    palette: com.example.ui.theme.ThemePalette
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("difficulty_breakdown_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = palette.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, palette.borderStrong)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Difficulty Performance Matrix",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = palette.cellGivenText
            )

            val difficulties = listOf("EASY", "MEDIUM", "HARD", "EXPERT", "DAILY")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                difficulties.forEach { diff ->
                    val stat = statsList.firstOrNull { it.difficulty.equals(diff, ignoreCase = true) }
                    val played = stat?.gamesPlayed ?: 0
                    val won = stat?.gamesWon ?: 0
                    val bestSecs = stat?.bestTimeSeconds ?: 0L
                    val bestFormatted = if (bestSecs > 0) formatTimer(bestSecs) else "--:--"

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(palette.card)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (diff) {
                                            "EASY" -> EmeraldGreen
                                            "MEDIUM" -> WarmAmber
                                            "HARD" -> palette.primary
                                            "EXPERT" -> CrimsonError
                                            else -> NeonCyan
                                        }
                                    )
                            )
                            Text(
                                text = diff.lowercase().replaceFirstChar { it.uppercase() },
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = palette.cellGivenText
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$won / $played won",
                                fontSize = 12.sp,
                                color = palette.cellNoteText
                            )
                            Text(
                                text = "Best: $bestFormatted",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = palette.secondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SolvedPuzzleItemCard(
    puzzle: SolvedPuzzleEntity,
    palette: com.example.ui.theme.ThemePalette
) {
    val diffColor = when (puzzle.difficulty.uppercase(Locale.US)) {
        "EASY" -> EmeraldGreen
        "MEDIUM" -> WarmAmber
        "HARD" -> palette.primary
        "EXPERT" -> CrimsonError
        else -> NeonCyan
    }

    val dateFormatted = remember(puzzle.solvedAt) {
        val sdf = SimpleDateFormat("MMM d, yyyy  h:mm a", Locale.US)
        sdf.format(Date(puzzle.solvedAt))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("solved_puzzle_item_${puzzle.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = palette.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, palette.border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(diffColor.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = puzzle.difficulty.uppercase(Locale.US),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = diffColor
                        )
                    }

                    if (puzzle.isDaily) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(palette.secondary.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Daily",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.secondary
                            )
                        }
                    }

                    if (puzzle.mistakes == 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(EmeraldGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Flawless ⭐",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreen
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = formatTimer(puzzle.timeSpentSeconds),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = palette.cellGivenText
                        )
                    }

                    Text(
                        text = "•",
                        color = palette.cellNoteText,
                        fontSize = 12.sp
                    )

                    Text(
                        text = "${puzzle.mistakes} mistakes",
                        fontSize = 12.sp,
                        color = if (puzzle.mistakes == 0) EmeraldGreen else palette.cellNoteText
                    )
                }

                Text(
                    text = dateFormatted,
                    fontSize = 11.sp,
                    color = palette.cellNoteText
                )
            }

            // Score Earned Badge
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "+${puzzle.scoreEarned} pts",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = WarmAmber
                )
                Text(
                    text = "Score Earned",
                    fontSize = 10.sp,
                    color = palette.cellNoteText
                )
            }
        }
    }
}

@Composable
fun EmptySolvedPuzzlesCard(
    palette: com.example.ui.theme.ThemePalette,
    onPlayMatch: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("empty_solved_puzzles_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = palette.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, palette.border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(palette.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = palette.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                text = "No Solved Puzzles Recorded Yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = palette.cellGivenText
            )

            Text(
                text = "Solve your first Sudoku match to see detailed stats, speed bonus breakdowns, and your complete solving history here!",
                fontSize = 12.sp,
                color = palette.cellNoteText,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Button(
                onClick = onPlayMatch,
                colors = ButtonDefaults.buttonColors(containerColor = palette.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("empty_dashboard_play_button")
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Play a Match Now", fontWeight = FontWeight.Bold)
            }
        }
    }
}
