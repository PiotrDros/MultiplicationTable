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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.multiplicationtable.ui.theme.MultiplicationTableTheme
import kotlinx.coroutines.delay

private enum class AppScreen { Start, Game, Summary }

private val screenPadding = 32.dp
private val sectionSpacing = 20.dp
private val buttonHeight = 64.dp
private val answerFieldWidth = 140.dp
private val answerFieldHeight = 64.dp

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
    val context = LocalContext.current
    val audioPlayer = remember { AudioPlayer(context) }
    DisposableEffect(Unit) {
        onDispose { audioPlayer.release() }
    }

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
            audioPlayer = audioPlayer,
            onSubmitAnswer = { input ->
                val answer = input.toIntOrNull() ?: return@GameScreen false
                val updatedSession = submitAnswer(gameSession, answer)
                gameSession = updatedSession
                if (updatedSession.isFinished) screen = AppScreen.Summary
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
            onChangeRangeClicked = { screen = AppScreen.Start },
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
        modifier = modifier.fillMaxSize().padding(screenPadding),
        verticalArrangement = Arrangement.spacedBy(sectionSpacing),
    ) {
        Text(text = stringResource(R.string.start_title), style = MaterialTheme.typography.headlineLarge)
        Text(text = stringResource(R.string.range_label, selectedMinimum), style = MaterialTheme.typography.titleLarge)
        Slider(
            value = selectedMinimum.toFloat(),
            onValueChange = { onMinimumSelected(it.toInt().coerceIn(2, 9)) },
            valueRange = 2f..9f,
            steps = 6,
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = onStartClicked,
            modifier = Modifier.fillMaxWidth().height(buttonHeight),
        ) {
            Text(text = stringResource(R.string.start_button), style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun GameScreen(
    session: GameSession,
    onSubmitAnswer: (String) -> Boolean,
    audioPlayer: AudioPlayer,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var answerInput by remember { mutableStateOf("") }
    var showInputError by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var isCorrect by remember { mutableStateOf(false) }
    var feedbackDurationMs by remember { mutableStateOf(0L) }

    val currentRound = session.currentRound
    val feedbackCorrect = stringResource(R.string.feedback_correct)
    val feedbackIncorrectWithResult = stringResource(R.string.feedback_incorrect_with_result, currentRound.product)

    // Stop audio when navigating away from this screen.
    DisposableEffect(Unit) {
        onDispose { audioPlayer.stop() }
    }

    // Play question audio when feedback is dismissed (or on first load of this screen).
    LaunchedEffect(feedbackMessage) {
        if (feedbackMessage == null && !session.isFinished) {
            audioPlayer.playSequence(questionAudioResIds(context, currentRound.left, currentRound.right))
        }
    }

    // Hide feedback card after the configured delay, then clear input.
    LaunchedEffect(feedbackMessage, feedbackDurationMs) {
        if (feedbackMessage != null) {
            delay(feedbackDurationMs)
            feedbackMessage = null
            answerInput = ""
            showInputError = false
        }
    }

    Column(
        modifier = modifier.fillMaxSize().padding(screenPadding),
        verticalArrangement = Arrangement.spacedBy(sectionSpacing),
    ) {
        Text(
            text = stringResource(R.string.round_progress, session.roundNumber, session.totalRounds),
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(text = stringResource(R.string.score_label, session.score), style = MaterialTheme.typography.titleLarge)
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.question_format, currentRound.left, currentRound.right),
                style = MaterialTheme.typography.headlineMedium,
            )
            OutlinedTextField(
                value = answerInput,
                onValueChange = {
                    answerInput = it.filter(Char::isDigit).take(2)
                    showInputError = false
                },
                label = { Text(text = stringResource(R.string.answer_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = showInputError,
                modifier = Modifier.width(answerFieldWidth).height(answerFieldHeight),
                enabled = feedbackMessage == null,
                textStyle = MaterialTheme.typography.titleLarge,
            )
        }
        if (showInputError) {
            Text(
                text = stringResource(R.string.answer_error),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.titleMedium,
            )
        }
        Button(
            onClick = {
                val submittedAnswer = answerInput.toIntOrNull()
                val capturedRound = currentRound
                val accepted = onSubmitAnswer(answerInput)
                if (!accepted) {
                    showInputError = true
                } else {
                    val isAnswerCorrect = submittedAnswer == capturedRound.product
                    isCorrect = isAnswerCorrect
                    answerInput = ""
                    showInputError = false
                    if (isAnswerCorrect) {
                        feedbackDurationMs = 1500
                        feedbackMessage = feedbackCorrect
                        audioPlayer.playSequence(correctAudioResIds(context))
                    } else {
                        feedbackDurationMs = 8000
                        feedbackMessage = feedbackIncorrectWithResult
                        audioPlayer.playSequence(wrongAudioResIds(context, capturedRound.product))
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(buttonHeight),
            enabled = feedbackMessage == null,
        ) {
            Text(text = stringResource(R.string.submit_button), style = MaterialTheme.typography.titleLarge)
        }

        if (feedbackMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFf44336),
                ),
            ) {
                Text(
                    text = feedbackMessage ?: "",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
private fun SummaryScreen(
    score: Int = 0,
    rounds: Int = 10,
    onPlayAgainClicked: () -> Unit = {},
    onChangeRangeClicked: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(screenPadding),
        verticalArrangement = Arrangement.spacedBy(sectionSpacing),
    ) {
        Text(text = stringResource(R.string.summary_title), style = MaterialTheme.typography.headlineLarge)
        Text(text = stringResource(R.string.summary_score, score, rounds), style = MaterialTheme.typography.headlineMedium)
        Button(
            onClick = onPlayAgainClicked,
            modifier = Modifier.fillMaxWidth().height(buttonHeight),
        ) {
            Text(text = stringResource(R.string.play_again_button), style = MaterialTheme.typography.titleLarge)
        }
        Button(
            onClick = onChangeRangeClicked,
            modifier = Modifier.fillMaxWidth().height(buttonHeight),
        ) {
            Text(text = stringResource(R.string.change_range_button), style = MaterialTheme.typography.titleLarge)
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