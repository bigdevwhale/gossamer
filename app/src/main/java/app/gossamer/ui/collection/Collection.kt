package app.gossamer.ui.collection

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.gossamer.R
import app.gossamer.container
import app.gossamer.data.Cocoon
import app.gossamer.data.DAY
import app.gossamer.domain.Web
import app.gossamer.ui.Home
import app.gossamer.ui.Nav
import app.gossamer.ui.art.butterfly
import app.gossamer.ui.art.cocoon
import app.gossamer.ui.art.rememberTime
import app.gossamer.ui.catching.KindGlyph
import app.gossamer.ui.components.Btn
import app.gossamer.ui.components.Card
import app.gossamer.ui.components.Confirm
import app.gossamer.ui.components.Field
import app.gossamer.ui.components.Kind as BtnKind
import app.gossamer.ui.components.Page
import app.gossamer.ui.components.Section
import app.gossamer.ui.components.when_
import app.gossamer.ui.theme.C
import app.gossamer.ui.theme.T
import app.gossamer.ui.thought.ThoughtRow

private fun daysTo(at: Long, now: Long) = ((at - now + DAY - 1) / DAY).toInt().coerceAtLeast(1)

/** Cocoons ripening, ripe ones to open, and the butterflies: thoughts that grew into something. */
@Composable
fun CollectionScreen(h: Home, nav: Nav) {
    val time by rememberTime()
    val u = with(LocalDensity.current) { 1.dp.toPx() }
    Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, top = 64.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (h.indian || h.yearPicks.isNotEmpty()) Card(Modifier.fillMaxWidth(), onClick = nav::year, color = C.panelL) {
            Text(stringResource(R.string.year_title, h.yearNow), style = T.Heading)
            Text(if (h.yearPicks.isEmpty()) stringResource(R.string.year_card) else pluralStringResource(R.plurals.year_picked, h.yearPicks.size, h.yearPicks.size), style = T.Small)
        }
        if (h.ripe.isNotEmpty() || h.ripening.isNotEmpty()) Section(stringResource(R.string.cocoons))
        for (cc in h.ripe + h.ripening) {
            val ripe = cc in h.ripe
            Card(Modifier.fillMaxWidth(), onClick = { if (ripe) nav.hatch(cc.id) else nav.cocoon(cc.id) }) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Canvas(Modifier.size(width = 34.dp, height = 56.dp)) { cocoon(Offset(size.width / 2, size.height * .58f), u, time, ripe, .9f) }
                    Column(Modifier.weight(1f)) {
                        Text(cc.name, style = T.Heading)
                        Text(if (ripe) stringResource(R.string.ripe_now) else pluralStringResource(R.plurals.ripe_in, daysTo(cc.ripe, h.now), daysTo(cc.ripe, h.now)), style = T.Small.copy(color = if (ripe) C.amber else C.muted))
                    }
                }
            }
        }
        Section(stringResource(R.string.butterflies))
        if (h.butterflies.isEmpty()) Text(stringResource(R.string.butterflies_empty, Web.COCOON_MIN), style = T.Body.copy(color = C.silkD))
        for (row in h.butterflies.chunked(2)) Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            for (b in row) Card(Modifier.weight(1f), onClick = { nav.cocoon(b.id) }, pad = 10.dp) {
                Canvas(Modifier.fillMaxWidth().aspectRatio(1.2f)) { butterfly(Offset(size.width / 2, size.height * .5f), size.width * .9f, b.seed, h.kinds(b), time) }
                Text(b.name, style = T.Heading, maxLines = 2)
                Text(when_(b.hatched ?: b.at, h.now), style = T.Small)
            }
            if (row.size == 1) Spacer(Modifier.weight(1f))
        }
    }
}

/** Opening a ripe cocoon: reread what is inside and sum it up in one sentence. Then the butterfly flies out. */
@Composable
fun HatchPage(h: Home, cc: Cocoon, nav: Nav) {
    val c = LocalContext.current.container
    val time by rememberTime()
    val u = with(LocalDensity.current) { 1.dp.toPx() }
    var verdict by rememberSaveable { mutableStateOf("") }
    var asking by remember { mutableStateOf(false) }
    val inside = h.inside(cc)
    if (cc.hatched != null) { CocoonPage(h, cc, nav); return }
    Page(cc.name, nav::close) {
        Canvas(Modifier.fillMaxWidth().height(150.dp)) { cocoon(Offset(size.width / 2, size.height * .58f), u, time, ripe = true, scale = 2.2f) }
        Text(stringResource(R.string.hatch_b), style = T.Body.copy(color = C.silkD))
        for (t in inside) Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            KindGlyph(t.kind, 18.dp); Text(t.text, style = T.ThoughtSmall)
        }
        Field(verdict, { verdict = it }, stringResource(R.string.hatch_hint), Modifier.fillMaxWidth(), style = T.Thought, single = false, minLines = 2)
        Text(stringResource(R.string.hatch_examples), style = T.Small)
        Btn(stringResource(R.string.hatch_go), { c.write { hatch(cc, verdict) } }, Modifier.fillMaxWidth(), enabled = verdict.isNotBlank())
        Btn(stringResource(R.string.unspin), { asking = true }, Modifier.fillMaxWidth(), kind = BtnKind.QUIET)
    }
    if (asking) Confirm(stringResource(R.string.unspin_q), stringResource(R.string.unspin_b), stringResource(R.string.unspin), { c.write { unspin(cc) }; nav.close() }, { asking = false })
}

/** A cocoon still ripening (no peeking inside), or a butterfly with the sentence it carries. */
@Composable
fun CocoonPage(h: Home, cc: Cocoon, nav: Nav) {
    val c = LocalContext.current.container
    val time by rememberTime()
    val u = with(LocalDensity.current) { 1.dp.toPx() }
    var asking by remember { mutableStateOf(false) }
    val inside = h.inside(cc)
    Page(cc.name, nav::close) {
        if (cc.hatched == null) {
            Canvas(Modifier.fillMaxWidth().height(170.dp)) { cocoon(Offset(size.width / 2, size.height * .58f), u, time, ripe = cc.ripe <= h.now, scale = 2.4f) }
            Text(pluralStringResource(R.plurals.cocoon_inside, inside.size, inside.size), style = T.Italic, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Text(if (cc.ripe <= h.now) stringResource(R.string.ripe_now) else pluralStringResource(R.plurals.ripe_in, daysTo(cc.ripe, h.now), daysTo(cc.ripe, h.now)), style = T.Body.copy(color = C.silkD), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Text(stringResource(R.string.cocoon_wait), style = T.Small)
            if (cc.ripe <= h.now) Btn(stringResource(R.string.hatch_open), { nav.hatch(cc.id) }, Modifier.fillMaxWidth())
            Btn(stringResource(R.string.unspin), { asking = true }, Modifier.fillMaxWidth(), kind = BtnKind.QUIET)
        } else {
            Box(Modifier.fillMaxWidth().height(230.dp)) {
                Canvas(Modifier.fillMaxSize()) { butterfly(Offset(size.width / 2, size.height * .5f + kotlin.math.sin(time * 1.3f) * 4 * u), size.height * 1.05f, cc.seed, h.kinds(cc), time, flap = true) }
            }
            Text("«" + cc.verdict + "»", style = T.Thought.copy(color = C.amber), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Text(stringResource(R.string.flew_out, when_(cc.hatched, h.now), when_(cc.at, h.now)), style = T.Small, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Section(pluralStringResource(R.plurals.cocoon_was, inside.size, inside.size))
            for (t in inside) ThoughtRow(h, t, onClick = { nav.thought(t.id) })
        }
    }
    if (asking) Confirm(stringResource(R.string.unspin_q), stringResource(R.string.unspin_b), stringResource(R.string.unspin), { c.write { unspin(cc) }; nav.close() }, { asking = false })
}

/** Indian summer: the year's twelve thoughts, picked once a year, sent off on a flying thread. */
@Composable
fun YearPage(h: Home, nav: Nav) {
    val ctx = LocalContext.current
    val c = ctx.container
    var picks by remember { mutableStateOf(h.yearPicks.map { it.id }) }
    fun save(p: List<Long>) { picks = p; c.write { settings.update { it.copy(year = h.yearNow, yearPicks = p) } } }
    Page(stringResource(R.string.year_title, h.yearNow), nav::close) {
        Text(stringResource(R.string.year_b, Web.YEAR_PICKS), style = T.Body.copy(color = C.silkD))
        if (picks.isNotEmpty()) {
            Section(stringResource(R.string.year_mine, picks.size, Web.YEAR_PICKS))
            picks.mapNotNull { h.byId[it] }.forEachIndexed { i, t ->
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("${i + 1}.", style = T.Heading.copy(color = C.amber))
                    Text(t.text, style = T.ThoughtSmall, modifier = Modifier.weight(1f))
                    Text("✕", style = T.Body.copy(color = C.muted), modifier = Modifier.clickable { save(picks - t.id) }.padding(4.dp))
                }
            }
            Btn(stringResource(R.string.year_share), {
                val text = ctx.getString(R.string.year_share_head, h.yearNow) + "\n\n" + picks.mapNotNull { h.byId[it] }.mapIndexed { i, t -> "${i + 1}. ${t.text}" }.joinToString("\n")
                runCatching { ctx.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text), null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
            }, Modifier.fillMaxWidth(), kind = BtnKind.PLAIN)
        }
        Section(stringResource(R.string.year_pool))
        for (t in h.yearPool.filter { it.id !in picks }.take(150)) ThoughtRow(h, t, onClick = { if (picks.size < Web.YEAR_PICKS) save(picks + t.id) })
    }
}
