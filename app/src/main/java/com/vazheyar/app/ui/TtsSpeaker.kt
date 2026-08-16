package com.vazheyar.app.ui

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

class TtsSpeaker(context: Context) : TextToSpeech.OnInitListener {
    private val locale = Locale.US
    private val tts = TextToSpeech(context.applicationContext, this)
    private var ready = false
    private var pendingSpeech: PendingSpeech? = null

    private data class PendingSpeech(
        val text: String,
        val rate: Float,
        val utteranceId: String
    )

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) return
        val availability = tts.isLanguageAvailable(locale)
        ready = availability >= TextToSpeech.LANG_AVAILABLE
        if (!ready) return
        tts.language = locale
        pendingSpeech?.let {
            pendingSpeech = null
            speakInternal(it.text, it.rate, it.utteranceId)
        }
    }

    fun speakWord(text: String) {
        speak(text = text, rate = 0.92f, utteranceId = "word-${text.hashCode()}")
    }

    fun speakExample(text: String) {
        speak(text = text, rate = 0.86f, utteranceId = "example-${text.hashCode()}")
    }

    private fun speak(text: String, rate: Float, utteranceId: String) {
        val clean = text.trim()
        if (clean.isBlank()) return
        if (!ready) {
            pendingSpeech = PendingSpeech(clean, rate, utteranceId)
            return
        }
        speakInternal(clean, rate, utteranceId)
    }

    private fun speakInternal(text: String, rate: Float, utteranceId: String) {
        tts.language = locale
        tts.setSpeechRate(rate)
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun close() {
        pendingSpeech = null
        tts.stop()
        tts.shutdown()
    }
}

@Composable
fun rememberTtsSpeaker(): TtsSpeaker {
    val context = LocalContext.current
    val speaker = remember { TtsSpeaker(context) }
    DisposableEffect(Unit) {
        onDispose { speaker.close() }
    }
    return speaker
}
