package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.model.Cell
import com.example.ui.theme.CellErrorText
import com.example.ui.theme.LocalThemePalette
import com.example.ui.theme.ObsidianErrorBg
import kotlin.math.abs
import kotlin.math.max

// ---------------------------------------------------------------------------------------
// Grid
// ---------------------------------------------------------------------------------------

@Composable
fun SudokuGridView(
    board: List<List<Cell>>,
    selectedRow: Int?,
    selectedCol: Int?,
    onCellClick: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalThemePalette.current

    if (board.size != 9 || board.any { it.size != 9 }) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(palette.card, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("Preparing Board...", color = Color.White)
        }
        return
    }

    val haptic = LocalHapticFeedback.current
    val shape = RoundedCornerShape(16.dp)

    val selectedValue = if (selectedRow != null && selectedCol != null) {
        board[selectedRow][selectedCol].value
    } else 0

    // Identity of the current puzzle (its given clues). A new puzzle replays the intro.
    val puzzleKey = remember(board) {
        var h = 17
        for (row in board) for (cell in row) {
            if (cell.isGiven) h = h * 31 + (cell.row * 9 + cell.col) * 10 + cell.value
        }
        h
    }

    // Intro: cells fade and scale in as a diagonal ripple from the top-left corner.
    val intro = remember { Animatable(0f) }
    LaunchedEffect(puzzleKey) {
        intro.snapTo(0f)
        intro.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
    }

    // Completion wave: when a row, column or box becomes complete a light front sweeps
    // out of the cell the player just filled and passes through the completed cells.
    val completed = remember(board) { completedUnits(board) }
    var lastCompleted by remember(puzzleKey) { mutableStateOf(completed) }
    var waveCells by remember { mutableStateOf(emptySet<Int>()) }
    var waveOrigin by remember { mutableStateOf(4 to 4) }
    val wave = remember { Animatable(0f) }
    LaunchedEffect(completed) {
        val fresh = completed - lastCompleted
        lastCompleted = completed
        if (fresh.isNotEmpty()) {
            waveCells = fresh.flatMap { unitCells(it) }.toSet()
            waveOrigin = (selectedRow ?: 4) to (selectedCol ?: 4)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            wave.snapTo(0f)
            wave.animateTo(1f, tween(850, easing = LinearEasing))
            waveCells = emptySet()
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .drawBehind {
                // Soft ambient glow behind the board
                val spread = 10.dp.toPx()
                drawRoundRect(
                    brush = Brush.radialGradient(
                        colors = listOf(palette.primary.copy(alpha = 0.28f), Color.Transparent),
                        center = center,
                        radius = size.maxDimension * 0.78f
                    ),
                    topLeft = Offset(-spread, -spread),
                    size = Size(size.width + spread * 2, size.height + spread * 2),
                    cornerRadius = CornerRadius(26.dp.toPx())
                )
            }
            .clip(shape)
            .background(palette.surface)
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    listOf(
                        palette.primaryLight.copy(alpha = 0.85f),
                        palette.borderStrong,
                        palette.secondary.copy(alpha = 0.85f)
                    )
                ),
                shape = shape
            )
            .padding(3.dp)
            .testTag("sudoku_grid")
    ) {
        val cellDp = maxWidth / 9
        val density = LocalDensity.current
        val digitSize = with(density) { (cellDp * 0.58f).toSp() }
        val noteSize = with(density) { (cellDp * 0.25f).toSp() }

        val thinColor = palette.border.copy(alpha = 0.55f)
        val blockGlow = palette.primary.copy(alpha = 0.20f)
        val blockLine = palette.borderStrong

        Column(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    drawContent()
                    val w = size.width
                    val h = size.height
                    val thin = 0.7f.dp.toPx()
                    for (i in 1..8) {
                        if (i % 3 == 0) continue
                        val x = w * i / 9f
                        val y = h * i / 9f
                        drawLine(thinColor, Offset(x, 0f), Offset(x, h), thin)
                        drawLine(thinColor, Offset(0f, y), Offset(w, y), thin)
                    }
                    for (i in intArrayOf(3, 6)) {
                        val x = w * i / 9f
                        val y = h * i / 9f
                        drawLine(blockGlow, Offset(x, 0f), Offset(x, h), 5.dp.toPx())
                        drawLine(blockGlow, Offset(0f, y), Offset(w, y), 5.dp.toPx())
                        drawLine(blockLine, Offset(x, 0f), Offset(x, h), 1.8f.dp.toPx())
                        drawLine(blockLine, Offset(0f, y), Offset(w, y), 1.8f.dp.toPx())
                    }
                }
        ) {
            for (r in 0 until 9) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    for (c in 0 until 9) {
                        val cell = board[r][c]
                        val isSelected = (r == selectedRow && c == selectedCol)
                        val isRelated = (r == selectedRow || c == selectedCol ||
                                (selectedRow != null && selectedCol != null &&
                                        (r / 3 == selectedRow / 3) && (c / 3 == selectedCol / 3)))
                        val isSameNum = (selectedValue != 0 && cell.value == selectedValue)

                        val inWave = waveCells.contains(r * 9 + c)
                        val dist = max(abs(r - waveOrigin.first), abs(c - waveOrigin.second))
                        val glow: () -> Float = {
                            if (inWave) waveBump(wave.value, dist) else 0f
                        }

                        SudokuCellView(
                            cell = cell,
                            isSelected = isSelected,
                            isRelated = isRelated,
                            isSameNumber = isSameNum,
                            onClick = { onCellClick(r, c) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            digitSize = digitSize,
                            noteSize = noteSize,
                            highlightDigit = selectedValue,
                            introProgress = { intro.value },
                            introOrder = (r + c) / 16f,
                            waveGlow = glow
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------
// Cell
// ---------------------------------------------------------------------------------------

@Composable
fun SudokuCellView(
    cell: Cell,
    isSelected: Boolean,
    isRelated: Boolean,
    isSameNumber: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    digitSize: TextUnit = 20.sp,
    noteSize: TextUnit = 8.sp,
    highlightDigit: Int = 0,
    introProgress: () -> Float = { 1f },
    introOrder: Float = 0f,
    waveGlow: () -> Float = { 0f }
) {
    val palette = LocalThemePalette.current
    val haptic = LocalHapticFeedback.current

    val targetBgColor = when {
        cell.isError -> ObsidianErrorBg
        isSelected -> palette.cellSelected
        isSameNumber -> palette.cellSameNumber
        cell.isHint -> palette.secondary.copy(alpha = 0.22f)
        isRelated -> palette.cellRelated
        else -> Color.Transparent
    }
    val animatedBgColor by animateColorAsState(
        targetValue = targetBgColor,
        animationSpec = tween(160),
        label = "cellBg"
    )

    // Selection ring fades in quickly and trails out slowly as the selection moves on.
    val selectionAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(if (isSelected) 120 else 260),
        label = "cellSelection"
    )

    // The first composition of a cell (new puzzle, restored session) must stay silent.
    var ready by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { ready = true }

    // A digit springs in with a little overshoot when the player places it.
    val pop = remember { Animatable(1f) }
    LaunchedEffect(cell.value) {
        if (ready && cell.value != 0 && !cell.isGiven) {
            pop.snapTo(0.45f)
            pop.animateTo(1f, spring(dampingRatio = 0.42f, stiffness = 520f))
        } else {
            pop.snapTo(1f)
        }
    }

    // A wrong digit shakes the cell and gives a firmer haptic.
    val shake = remember { Animatable(0f) }
    LaunchedEffect(cell.isError, cell.value) {
        if (ready && cell.isError && cell.value != 0) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            for (target in listOf(-1f, 1f, -0.7f, 0.7f, -0.35f, 0.35f, 0f)) {
                shake.animateTo(target, tween(48))
            }
        } else {
            shake.snapTo(0f)
        }
    }

    val description = buildString {
        append("Row ${cell.row + 1}, column ${cell.col + 1}, ")
        when {
            cell.value != 0 -> append("value ${cell.value}")
            cell.notes.isNotEmpty() -> append("notes ${cell.notes.sorted().joinToString(" ")}")
            else -> append("empty")
        }
        if (cell.isGiven) append(", given")
        if (cell.isError) append(", incorrect")
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                val local = ((introProgress() - introOrder * 0.5f) / 0.5f).coerceIn(0f, 1f)
                val g = waveGlow()
                val s = (0.7f + 0.3f * local) * (1f + 0.14f * g)
                scaleX = s
                scaleY = s
                alpha = local
                translationX = shake.value * size.width * 0.14f
            }
            .background(animatedBgColor)
            .drawBehind {
                val g = waveGlow()
                if (g > 0f) {
                    drawRect(palette.secondaryLight.copy(alpha = 0.5f * g))
                }
                if (selectionAlpha > 0.01f) {
                    drawRect(palette.primary.copy(alpha = 0.16f * selectionAlpha))
                    val sw = 2.dp.toPx()
                    drawRect(
                        color = palette.primaryLight.copy(alpha = selectionAlpha),
                        topLeft = Offset(sw / 2f, sw / 2f),
                        size = Size(size.width - sw, size.height - sw),
                        style = Stroke(sw)
                    )
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .semantics {
                contentDescription = description
                selected = isSelected
            }
            .testTag("cell_${cell.row}_${cell.col}"),
        contentAlignment = Alignment.Center
    ) {
        if (cell.value != 0) {
            val textColor = when {
                cell.isError -> CellErrorText
                cell.isGiven -> palette.cellGivenText
                cell.isHint -> palette.primaryLight
                else -> palette.cellUserText
            }

            Text(
                text = cell.value.toString(),
                color = textColor,
                fontSize = digitSize,
                fontWeight = if (cell.isGiven) FontWeight.Bold else FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer {
                    val p = pop.value
                    scaleX = p
                    scaleY = p
                }
            )
        } else if (cell.notes.isNotEmpty()) {
            NotesGridView(
                notes = cell.notes,
                fontSize = noteSize,
                highlightDigit = highlightDigit
            )
        }
    }
}

@Composable
fun NotesGridView(
    notes: Set<Int>,
    fontSize: TextUnit = 8.sp,
    highlightDigit: Int = 0
) {
    val palette = LocalThemePalette.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(1.5.dp)
    ) {
        for (row in 0 until 3) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                for (col in 0 until 3) {
                    val digit = row * 3 + col + 1
                    val isHighlighted = digit == highlightDigit
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (notes.contains(digit)) {
                            Text(
                                text = digit.toString(),
                                color = if (isHighlighted) palette.primaryLight else palette.cellNoteText,
                                fontSize = fontSize,
                                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------------------

/** Brightness (0..1) of the completion wave at a cell [dist] steps from the wave origin. */
private fun waveBump(progress: Float, dist: Int): Float {
    val front = progress * 11f
    val x = (1f - abs(front - dist) / 2.4f).coerceIn(0f, 1f)
    return x * x * (3f - 2f * x)
}

/** Units are numbered rows 0..8, columns 9..17 and boxes 18..26. */
private fun completedUnits(board: List<List<Cell>>): Set<Int> {
    fun ok(c: Cell) = c.value != 0 && !c.isError
    val out = HashSet<Int>()
    for (i in 0 until 9) {
        if ((0 until 9).all { ok(board[i][it]) }) out.add(i)
        if ((0 until 9).all { ok(board[it][i]) }) out.add(9 + i)
        val br = (i / 3) * 3
        val bc = (i % 3) * 3
        var full = true
        for (dr in 0 until 3) for (dc in 0 until 3) {
            if (!ok(board[br + dr][bc + dc])) full = false
        }
        if (full) out.add(18 + i)
    }
    return out
}

/** Cell indices (row * 9 + col) belonging to a unit from [completedUnits]. */
private fun unitCells(unit: Int): List<Int> = when {
    unit < 9 -> (0 until 9).map { unit * 9 + it }
    unit < 18 -> (0 until 9).map { it * 9 + (unit - 9) }
    else -> {
        val b = unit - 18
        val br = (b / 3) * 3
        val bc = (b % 3) * 3
        (0 until 9).map { (br + it / 3) * 9 + (bc + it % 3) }
    }
}
