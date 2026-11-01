package app.gossamer.ui.catching

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.gossamer.Caught
import app.gossamer.R
import app.gossamer.container
import app.gossamer.data.Kind
import app.gossamer.ru
import app.gossamer.ui.art.glyph
import app.gossamer.ui.art.rememberTime
import app.gossamer.ui.components.Btn
import app.gossamer.ui.components.Chip
import app.gossamer.ui.components.Field
import app.gossamer.ui.components.Flow
import app.gossamer.ui.theme.C
import app.gossamer.ui.theme.T
import app.gossamer.ui.web.Mic

object Speech {
    fun available(ctx: Context) = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).resolveActivity(ctx.packageManager) != null

    fun intent(ctx: Context) = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        .putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (ctx.ru) "ru-RU" else ctx.resources.configuration.locales[0].toLanguageTag())
        .putExtra(RecognizerIntent.EXTRA_PROMPT, ctx.getString(R.string.c_say_prompt))
}

fun kindName(k: Kind) = when (k) {
    Kind.IDEA -> R.string.k_idea
    Kind.QUESTION -> R.string.k_question
    Kind.NOTICE -> R.string.k_notice
    Kind.WORDS -> R.string.k_words
}

fun kindWhat(k: Kind) = when (k) {
    Kind.IDEA -> R.string.k_idea_b
    Kind.QUESTION -> R.string.k_question_b
    Kind.NOTICE -> R.string.k_notice_b
    Kind.WORDS -> R.string.k_words_b
}

@Composable
fun KindGlyph(k: Kind, size: Dp = 20.dp, night: Boolean = false) {
    val t by rememberTime()
    val u = with(LocalDensity.current) { 1.dp.toPx() } * size.value / 22f
    Canvas(Modifier.size(size)) { glyph(k, center, u, t, k.ordinal.toLong(), night = night) }
}

/**
 * Writing a thought down: one field, four kinds, a microphone. Nothing else to fill in.
 * [voice] opens the phone's speech input at once (from the widget or the shortcut).
 */
@Composable
fun CatchForm(initial: String = "", initialKind: Kind = Kind.IDEA, voice: Boolean = false, onCaught: (Caught) -> Unit) {
    val ctx = LocalContext.current
    val c = ctx.container
    var text by rememberSaveable { mutableStateOf(initial) }
    var kind by rememberSaveable { mutableStateOf(initialKind) }
    var busy by remember { mutableStateOf(false) }
    val focus = remember { FocusRequester() }
    val canSpeak = remember { Speech.available(ctx) }
    val speech = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
        val said = r.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        if (r.resultCode == Activity.RESULT_OK && !said.isNullOrBlank()) {
            text = if (text.isBlank()) said.replaceFirstChar { it.uppercase() } else text.trimEnd() + " " + said
        }
        runCatching { focus.requestFocus() }
    }
    LaunchedEffect(Unit) {
        if (voice && canSpeak) speech.launch(Speech.intent(ctx)) else runCatching { focus.requestFocus() }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.c_title), style = T.Title)
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Field(text, { text = it }, stringResource(R.string.c_hint), Modifier.weight(1f).heightIn(min = 110.dp), style = T.Thought, single = false, focus = focus, minLines = 3)
            if (canSpeak) {
                val label = stringResource(R.string.sc_say)
                Box(Modifier.size(48.dp).clip(CircleShape).background(C.deep).border(1.dp, C.line, CircleShape).clickable { speech.launch(Speech.intent(ctx)) }
                    .semantics { contentDescription = label }, contentAlignment = Alignment.Center) { Mic(Modifier.size(20.dp), C.silk) }
            }
        }
        Flow {
            for (k in Kind.entries) Chip(stringResource(kindName(k)), kind == k, { kind = k }, lead = { KindGlyph(k, 18.dp) })
        }
        Text(stringResource(kindWhat(kind)), style = T.Small)
        Btn(stringResource(R.string.catch_btn), {
            busy = true
            val t = text
            c.write {
                val r = catch(t, kind)
                if (r.echo != null) note(ctx.getString(R.string.n_echo), ctx.getString(R.string.n_echo_b, r.echo.text.take(60)))
                else note(ctx.getString(if (r.thought.night) R.string.n_caught_night else R.string.n_caught))
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { onCaught(r) }
            }
        }, Modifier.fillMaxWidth(), enabled = text.isNotBlank() && !busy)
    }
}
