package com.example.multiplicationtable

import kotlin.random.Random

const val maxMultiplier = 9
const val totalRounds = 10

val random = Random.Default

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

fun startSession(minValue: Int, maxValue: Int): GameSession {
    if (minValue < 2 || maxValue > 9 || minValue > maxValue) {
        throw IllegalArgumentException("Invalid multiplier range: $minValue to $maxValue")
    }
    return GameSession(
        minMultiplier = minValue,
        maxMultiplier = maxValue,
        score = 0,
        roundNumber = 1,
        totalRounds = totalRounds,
        currentRound = generateRound(minValue, maxValue),
    )
}

fun submitAnswer(session: GameSession, answer: Int): GameSession {
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
            currentRound = generateRound(session.minMultiplier, session.maxMultiplier),
        )
    }
}

fun generateRound(minValue: Int, maxValue: Int): Round {
    return Round(
        left = random.nextInt(minValue, maxValue + 1),
        right = random.nextInt(minValue, maxValue + 1),
    )
}


