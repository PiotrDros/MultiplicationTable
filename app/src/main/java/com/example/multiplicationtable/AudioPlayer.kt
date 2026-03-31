package com.example.multiplicationtable

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.util.Log

private const val AUDIO_TAG = "AudioPlayer"

class AudioPlayer(private val context: Context) {

    private var current: MediaPlayer? = null
    private val audioManager: AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun playSequence(resIds: List<Int>, onComplete: () -> Unit = {}) {
        stop()
        // Set device volume to maximum before playback
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0)
        playNext(resIds, 0, onComplete)
    }

    private fun playNext(resIds: List<Int>, index: Int, onComplete: () -> Unit) {
        if (index >= resIds.size) {
            onComplete()
            return
        }
        try {
            val player = MediaPlayer.create(context, resIds[index])
            if (player == null) {
                Log.w(AUDIO_TAG, "Missing audio resource id: ${resIds[index]}, skipping.")
                playNext(resIds, index + 1, onComplete)
                return
            }
            current = player
            // Set audio attributes for maximum volume
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            player.setAudioAttributes(audioAttributes)
            player.setVolume(1.0f, 1.0f)  // Max player volume
            player.setOnCompletionListener {
                it.release()
                if (current === it) current = null
                playNext(resIds, index + 1, onComplete)
            }
            player.start()
        } catch (e: Exception) {
            Log.e(AUDIO_TAG, "Playback error at index $index", e)
            playNext(resIds, index + 1, onComplete)
        }
    }

    fun stop() {
        current?.let {
            try { it.stop() } catch (_: Exception) {}
            it.release()
        }
        current = null
    }

    fun release() = stop()
}

/** Builds the list of raw resource IDs for the question "What is X times Y". */
fun questionAudioResIds(context: Context, left: Int, right: Int): List<Int> =
    listOfNotNull(
        rawResId(context, "phrase_what_is"),
        rawResId(context, "digit_$left"),
        rawResId(context, "phrase_times"),
        rawResId(context, "digit_$right"),
    )

/** Resource IDs for a correct-answer feedback sound. */
fun correctAudioResIds(context: Context): List<Int> =
    listOfNotNull(rawResId(context, "feedback_correct"))

/** Resource IDs for a wrong-answer feedback sequence: wrong → correct answer is → result. */
fun wrongAudioResIds(context: Context, product: Int): List<Int> =
    listOfNotNull(
        rawResId(context, "feedback_wrong"),
        rawResId(context, "feedback_correct_answer_is"),
        rawResId(context, "result_${product.toString().padStart(2, '0')}"),
    )

/** Returns the raw resource ID for [name], or null if the file is not present. */
private fun rawResId(context: Context, name: String): Int? {
    val id = context.resources.getIdentifier(name, "raw", context.packageName)
    return if (id != 0) id else null
}



