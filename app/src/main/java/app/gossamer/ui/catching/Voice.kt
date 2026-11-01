package app.gossamer.ui.catching

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import app.gossamer.R
import app.gossamer.ru

/**
 * Speech to text inside the sheet, through whatever recognition service the phone has
 * (Google's, or the one on a phone without Google apps). Words appear in the field as you speak.
 * The system «speak now» window isn't used: on some phones it's registered by an app that just closes.
 */
@Stable
class Voice(private val ctx: Context, private val onText: (String) -> Unit, private val onFail: (Int) -> Unit) {
    var listening by mutableStateOf(false)
        private set
    var partial by mutableStateOf("")
        private set
    private var rec: SpeechRecognizer? = null

    val available: Boolean = runCatching { SpeechRecognizer.isRecognitionAvailable(ctx) }.getOrDefault(false)

    fun start() {
        if (listening) { stop(); return }
        val r = rec ?: runCatching { SpeechRecognizer.createSpeechRecognizer(ctx) }.getOrNull()?.also { rec = it } ?: return onFail(R.string.v_none)
        r.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { listening = true }
            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() = Unit
            override fun onEvent(eventType: Int, params: Bundle?) = Unit
            override fun onPartialResults(b: Bundle?) {
                partial = b?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
            }
            override fun onResults(b: Bundle?) {
                listening = false
                val said = b?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty().ifBlank { partial }
                partial = ""
                if (said.isNotBlank()) onText(said) else onFail(R.string.v_nothing)
            }
            override fun onError(error: Int) {
                listening = false
                val got = partial
                partial = ""
                if (got.isNotBlank()) { onText(got); return }
                onFail(
                    when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> R.string.v_nothing
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> R.string.v_perm
                        SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED, SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE -> R.string.v_lang
                        else -> R.string.v_none
                    },
                )
            }
        })
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (ctx.ru) "ru-RU" else ctx.resources.configuration.locales[0].toLanguageTag())
            .putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            .putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, ctx.packageName)
        listening = true
        runCatching { r.startListening(intent) }.onFailure { listening = false; onFail(R.string.v_none) }
    }

    fun stop() {
        runCatching { rec?.stopListening() }
    }

    fun release() {
        runCatching { rec?.destroy() }
        rec = null
    }
}

@Composable
fun rememberVoice(onText: (String) -> Unit, onFail: (Int) -> Unit): Voice {
    val ctx = LocalContext.current
    val v = remember { Voice(ctx, onText, onFail) }
    DisposableEffect(v) { onDispose { v.release() } }
    return v
}
