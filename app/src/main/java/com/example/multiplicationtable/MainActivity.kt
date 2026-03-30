package com.example.multiplicationtable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.multiplicationtable.ui.theme.MultiplicationTableTheme

private enum class AppScreen {
    Start,
    Game,
    Summary,
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MultiplicationTableTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MultiplicationGameApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
private fun MultiplicationGameApp(modifier: Modifier = Modifier) {
    var screen by remember { mutableStateOf(AppScreen.Start) }
    var selectedMinimum by remember { mutableStateOf(2) }
    var gameSession by remember { mutableStateOf(startSession(selectedMinimum)) }

    when (screen) {
        AppScreen.Start -> StartScreen(
            selectedMinimum = selectedMinimum,
            onMinimumSelected = { selectedMinimum = it },
            onStartClicked = {
                gameSession = startSession(selectedMinimum)
                screen = AppScreen.Game
            },
            modifier = modifier,
        )

        AppScreen.Game -> GameScreen(
            session = gameSession,
            onSubmitAnswer = { input ->
                val answer = input.toIntOrNull()
                if (answer == null) {
                    return@GameScreen false
                }

                val updatedSession = submitAnswer(gameSession, answer)
                gameSession = updatedSession
                if (updatedSession.isFinished) {
                    screen = AppScreen.Summary
                }
                true
            },
            modifier = modifier,
        )

        AppScreen.Summary -> SummaryScreen(
            score = gameSession.score,
            rounds = gameSession.totalRounds,
            onPlayAgainClicked = {
                gameSession = startSession(selectedMinimum)
                screen = AppScreen.Game
            },
            onChangeRangeClicked = {
                screen = AppScreen.Start
            },
            modifier = modifier,
        )
    }
}

@Composable
private fun StartScreen(
    selectedMinimum: Int,
    onMinimumSelected: (Int) -> Unit,
    onStartClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.start_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(text = stringResource(R.string.range_label, selectedMinimum))
        Slider(
            value = selectedMinimum.toFloat(),
            onValueChange = { onMinimumSelected(it.toInt().coerceIn(2, 9)) },
            valueRange = 2f..9f,
            steps = 6,
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = onStartClicked,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.start_button))
        }
    }
}

@Composable
private fun GameScreen(
    session: GameSession,
    onSubmitAnswer: (String) -> Boolean,
    modifier: Modifier = Modifier,
) {
    var answerInput by remember(session.roundNumber) { mutableStateOf("") }
    var showInputError by remember(session.roundNumber) { mutableStateOf(false) }

    val currentRound = session.currentRound

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(
                R.string.round_progress,
                session.roundNumber,
                session.totalRounds,
            ),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = stringResource(R.string.score_label, session.score),
            style = MaterialTheme.typography.titleMedium,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(
                    R.string.question_format,
                    currentRound.left,
                    currentRound.right,
                ),
                style = MaterialTheme.typography.headlineSmall,
            )
            OutlinedTextField(
                value = answerInput,
                onValueChange = {
                    answerInput = it
                    showInputError = false
                },
                label = { Text(text = stringResource(R.string.answer_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = showInputError,
                modifier = Modifier.width(100.dp),
            )
        }
        if (showInputError) {
            Text(
                text = stringResource(R.string.answer_error),
                color = MaterialTheme.colorScheme.error,
            )
        }
        Button(
            onClick = {
                val accepted = onSubmitAnswer(answerInput)
                if (!accepted) {
                    showInputError = true
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.submit_button))
        }
    }
}

@Composable
private fun SummaryScreen(
    score: Int,
    rounds: Int,
    onPlayAgainClicked: () -> Unit,
    onChangeRangeClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.summary_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = stringResource(R.string.summary_score, score, rounds),
            style = MaterialTheme.typography.headlineSmall,
        )
        Button(
            onClick = onPlayAgainClicked,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.play_again_button))
        }
        Button(
            onClick = onChangeRangeClicked,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.change_range_button))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MultiplicationGamePreview() {
    MultiplicationTableTheme {
        Scaffold(modifier = Modifier.height(640.dp)) { innerPadding ->
            MultiplicationGameApp(modifier = Modifier.padding(innerPadding))
        }
    }
}