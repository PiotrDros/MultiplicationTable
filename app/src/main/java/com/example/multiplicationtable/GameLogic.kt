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
    val minValue: Int,
    val score: Int,
    val roundNumber: Int,
    val totalRounds: Int,
    val currentRound: Round,
) {
    val isFinished: Boolean = roundNumber > totalRounds
}

fun startSession(minValue: Int, random: Random = Random.Default): GameSession {
    val safeMin = minValue.coerceIn(2, maxMultiplier)
    return GameSession(
        minValue = safeMin,
        score = 0,
        roundNumber = 1,
        totalRounds = totalRounds,
        currentRound = generateRound(safeMin, random),
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
            currentRound = generateRound(session.minValue, random),
        )
    }
}

fun generateRound(minValue: Int, random: Random = Random.Default): Round {
    val safeMin = minValue.coerceIn(2, maxMultiplier)
    return Round(
        left = random.nextInt(safeMin, maxMultiplier + 1),
        right = random.nextInt(safeMin, maxMultiplier + 1),
    )
}

