package com.example.multiplicationtable

import kotlin.random.Random

const val maxMultiplier = 9
const val totalRounds = 10

data class Round(
    val left: Int,
    val right: Int,
) {
    val product: Int = left * right
}

data class GameSession(
    val minMultiplier: Int,
    val maxMultiplier: Int,
    val score: Int,
    val roundNumber: Int,
    val totalRounds: Int,
    val currentRound: Round,
) {
    val isFinished: Boolean = roundNumber > totalRounds
}

fun startSession(minValue: Int, maxValue: Int, random: Random = Random.Default): GameSession {
    val (safeMin, safeMax) = normalizeRange(minValue, maxValue)
    return GameSession(
        minMultiplier = safeMin,
        maxMultiplier = safeMax,
        score = 0,
        roundNumber = 1,
        totalRounds = totalRounds,
        currentRound = generateRound(safeMin, safeMax, random),
    )
}

fun submitAnswer(session: GameSession, answer: Int, random: Random = Random.Default): GameSession {
    if (session.isFinished) {
        return session
    }

    val roundPoint = if (answer == session.currentRound.product) 1 else 0
    val nextRoundNumber = session.roundNumber + 1

    return if (nextRoundNumber > session.totalRounds) {
        session.copy(
            score = session.score + roundPoint,
            roundNumber = nextRoundNumber,
        )
    } else {
        session.copy(
            score = session.score + roundPoint,
            roundNumber = nextRoundNumber,
            currentRound = generateRound(session.minMultiplier, session.maxMultiplier, random),
        )
    }
}

fun generateRound(minValue: Int, maxValue: Int, random: Random = Random.Default): Round {
    val (safeMin, safeMax) = normalizeRange(minValue, maxValue)
    return Round(
        left = random.nextInt(safeMin, safeMax + 1),
        right = random.nextInt(safeMin, safeMax + 1),
    )
}

private fun normalizeRange(minValue: Int, maxValue: Int): Pair<Int, Int> {
    val safeMin = minValue.coerceIn(2, maxMultiplier)
    val safeMax = maxValue.coerceIn(2, maxMultiplier)
    return if (safeMin <= safeMax) {
        safeMin to safeMax
    } else {
        safeMax to safeMin
    }
}

