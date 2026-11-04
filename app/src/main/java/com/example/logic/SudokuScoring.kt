package com.example.logic

/**
 * Detailed scoring breakdown for a completed or attempted Sudoku match.
 */
data class ScoreResult(
    val basePoints: Int,
    val speedBonus: Int,
    val accuracyBonus: Int,
    val streakBonus: Int,
    val totalEarned: Int
)

/**
 * Transparent, rewarding Sudoku scoring engine.
 *
 * Scoring Formula:
 * - Base points awarded by puzzle difficulty (Easy: 600, Medium: 1200, Hard: 2000, Expert: 3200, Daily: 2400)
 * - Speed bonus for fast solves under difficulty benchmark (up to 800 bonus points)
 * - Accuracy bonus for low mistakes (0 mistakes / Flawless: +500 pts, 1 mistake: +250 pts, 2 mistakes: +100 pts)
 * - Streak multiplier bonus (+75 pts per active day of streak, capped at 750 pts)
 * - Partial completion / effort points (+100 pts) even on game over, ensuring every match updates score!
 */
object SudokuScoring {

    fun calculateScore(
        difficulty: Difficulty,
        timeSeconds: Long,
        mistakes: Int,
        streak: Int,
        isWon: Boolean
    ): ScoreResult {
        if (!isWon) {
            return ScoreResult(
                basePoints = 100,
                speedBonus = 0,
                accuracyBonus = 0,
                streakBonus = 0,
                totalEarned = 100
            )
        }

        val base = when (difficulty) {
            Difficulty.EASY -> 600
            Difficulty.MEDIUM -> 1200
            Difficulty.HARD -> 2000
            Difficulty.EXPERT -> 3200
            Difficulty.DAILY -> 2400
        }

        val benchmarkSeconds = when (difficulty) {
            Difficulty.EASY -> 420L   // 7 mins
            Difficulty.MEDIUM -> 720L // 12 mins
            Difficulty.HARD -> 1200L  // 20 mins
            Difficulty.EXPERT -> 1800L// 30 mins
            Difficulty.DAILY -> 900L  // 15 mins
        }

        val speedBonus = if (timeSeconds < benchmarkSeconds) {
            ((benchmarkSeconds - timeSeconds) * 2).coerceIn(0L, 800L).toInt()
        } else {
            0
        }

        val accuracyBonus = when (mistakes) {
            0 -> 500 // Flawless clean sweep
            1 -> 250
            2 -> 100
            else -> 0
        }

        val streakBonus = (streak.coerceAtLeast(1) * 75).coerceAtMost(750)
        val total = base + speedBonus + accuracyBonus + streakBonus

        return ScoreResult(
            basePoints = base,
            speedBonus = speedBonus,
            accuracyBonus = accuracyBonus,
            streakBonus = streakBonus,
            totalEarned = total
        )
    }
}
