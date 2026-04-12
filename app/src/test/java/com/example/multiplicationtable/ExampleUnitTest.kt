package com.example.multiplicationtable

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun generateRound_usesSelectedRange() {
        val random = Random(1234)

        repeat(200) {
            val round = generateRound(minValue = 6, maxValue = 9, random = random)
            assertTrue(round.left in 6..9)
            assertTrue(round.right in 6..9)
        }
    }

    @Test
    fun submitAnswer_addsPointForCorrectAnswer() {
        val session = GameSession(
            minMultiplier = 2,
            maxMultiplier = 9,
            score = 0,
            roundNumber = 1,
            totalRounds = totalRounds,
            currentRound = Round(left = 3, right = 4),
        )

        val updated = submitAnswer(session, answer = 12, random = Random(11))

        assertEquals(1, updated.score)
        assertEquals(2, updated.roundNumber)
    }

    @Test
    fun submitAnswer_finishesAfterTenRounds() {
        var session = startSession(minValue = 2, maxValue = 9, random = Random(1))

        repeat(totalRounds) {
            session = submitAnswer(session, answer = -1, random = Random(it + 2))
        }

        assertTrue(session.isFinished)
        assertEquals(totalRounds + 1, session.roundNumber)
    }
}