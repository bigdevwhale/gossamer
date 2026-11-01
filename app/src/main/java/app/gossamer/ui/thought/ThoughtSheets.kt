package app.gossamer.ui.thought

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.gossamer.R
import app.gossamer.container
import app.gossamer.data.Kind
import app.gossamer.data.Thought
import app.gossamer.domain.Web
import app.gossamer.ui.Home
import app.gossamer.ui.Nav
import app.gossamer.ui.catching.KindGlyph
import app.gossamer.ui.catching.kindName
import app.gossamer.ui.components.Btn
import app.gossamer.ui.components.Card
import app.gossamer.ui.components.Chip
import app.gossamer.ui.components.Confirm
import app.gossamer.ui.components.Field
import app.gossamer.ui.components.Flow
import app.gossamer.ui.components.Kind as BtnKind
import app.gossamer.ui.components.Page
import app.gossamer.ui.components.Section
import app.gossamer.ui.components.Sheet
import app.gossamer.ui.components.when_
import app.gossamer.ui.theme.C
import app.gossamer.ui.theme.T

/** A line in a list: the kind, the words in handwriting, and when. */
@Composable
fun ThoughtRow(h: Home, t: Thought, onClick: () -> Unit, trailing: (@Composable () -> Unit)? = null) {
    Card(Modifier.fillMaxWidth(), onClick = onClick, pad = 12.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            KindGlyph(t.kind, 22.dp, night = t.night)
            Column(Modifier.weight(1f)) {
                Text(t.text, style = T.ThoughtSmall, maxLines = 3)
                val bits = buildList {
                    add(when_(t.at, h.now))
                    val d = h.degree(t.id)
                    if (t.live && d > 0) add(LocalContext.current.resources.getQuantityString(R.plurals.threads, d, d))
                    if (t.echoes > 0) add(LocalContext.current.resources.getQuantityString(R.plurals.echoes, t.echoes, t.echoes))
                }
                Text(bits.joinToString(" · "), style = T.Small)
            }
            trailing?.invoke()
        }
    }
}

/** One thought up close: read it (that counts as touching it), tie it, hang it as a lure, spin its cluster. */
@Composable
fun ThoughtSheet(h: Home, t: Thought, nav: Nav) {
    val ctx = LocalContext.current
    val c = ctx.container
    var editing by remember { mutableStateOf(false) }
    var text by rememberSaveable(t.id) { mutableStateOf(t.text) }
    var kind by rememberSaveable(t.id) { mutableStateOf(t.kind) }
    var asking by remember { mutableStateOf<String?>(null) }
    val wasDew = t.id in h.dew
    LaunchedEffect(t.id) {
        if (t.live) {
            if (wasDew) c.note(ctx.getString(R.string.n_dew))
            c.write { touch(t.id) }
        }
    }
    Sheet(nav::close) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            KindGlyph(t.kind, 26.dp, night = t.night)
            Text(stringResource(kindName(t.kind)) + " · " + when_(t.at, h.now), style = T.Small, modifier = Modifier.weight(1f))
            if (t.night) Text(stringResource(R.string.night_mark), style = T.Small.copy(color = C.dew))
        }
        if (editing) {
            Field(text, { text = it }, "", Modifier.fillMaxWidth(), style = T.Thought, single = false, minLines = 2)
            Flow { for (k in Kind.entries) Chip(stringResource(kindName(k)), kind == k, { kind = k }, lead = { KindGlyph(k, 16.dp) }) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Btn(stringResource(R.string.cancel), { editing = false; text = t.text; kind = t.kind }, kind = BtnKind.QUIET)
                Btn(stringResource(R.string.save), { c.write { edit(t, text, kind) }; editing = false }, enabled = text.isNotBlank())
            }
        } else Text(t.text, style = T.Thought)

        if (!t.live) {
            Text(stringResource(if (t.cocoon != null) R.string.t_in_cocoon else R.string.t_in_fluff), style = T.Small)
            if (t.gone != null && t.cocoon == null) Btn(stringResource(R.string.restore), { c.write { restore(t) }; nav.close() }, Modifier.fillMaxWidth())
            return@Sheet
        }

        h.daysLeft(t)?.let { left ->
            Text(
                if (h.trembles(t)) pluralStringResource(R.plurals.t_wind_soon, left, left) else pluralStringResource(R.plurals.t_wind, left, left),
                style = T.Small.copy(color = if (h.trembles(t)) C.warn else C.muted),
            )
        }
        if (t.id == h.lure?.id) Text(stringResource(R.string.t_is_lure), style = T.Small.copy(color = C.teal))

        val threads = h.threads(t.id)
        Section(if (threads.isEmpty()) stringResource(R.string.t_no_threads) else pluralStringResource(R.plurals.threads, threads.size, threads.size))
        for (o in threads) ThoughtRow(h, o, onClick = { nav.thought(o.id) }) {
            Btn("✕", { c.write { untie(t.id, o.id) } }, kind = BtnKind.QUIET)
        }
        Btn(stringResource(R.string.tie), { nav.tie(t.id) }, Modifier.fillMaxWidth(), kind = BtnKind.PLAIN)

        val cluster = h.cluster(t.id)
        if (cluster.size >= Web.COCOON_MIN) Btn(stringResource(R.string.spin_from), { nav.spin(t.id) }, Modifier.fillMaxWidth())
        else if (cluster.size >= 2) Text(pluralStringResource(R.plurals.t_spin_need, Web.COCOON_MIN - cluster.size, Web.COCOON_MIN - cluster.size), style = T.Small)

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (!editing) Btn(stringResource(R.string.edit), { editing = true }, kind = BtnKind.QUIET)
            if (t.kind == Kind.QUESTION || t.id == h.lure?.id) Btn(
                stringResource(if (t.id == h.lure?.id) R.string.lure_off else R.string.lure_on),
                { c.write { lure(if (t.id == h.lure?.id) null else t.id) } }, kind = BtnKind.QUIET,
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Btn(stringResource(R.string.blow), { asking = "blow" }, kind = BtnKind.QUIET)
            Btn(stringResource(R.string.delete), { asking = "delete" }, kind = BtnKind.QUIET)
        }
    }
    when (asking) {
        "blow" -> Confirm(stringResource(R.string.blow_q), stringResource(R.string.blow_b), stringResource(R.string.blow), { c.write { blow(t) }; nav.close() }, { asking = null })
        "delete" -> Confirm(stringResource(R.string.delete_q), stringResource(R.string.delete_b), stringResource(R.string.delete), { c.write { forget(t) }; nav.close() }, { asking = null }, warn = true)
    }
}

/** Pick what to tie a thought to, for those who'd rather not drag silk across the screen. */
@Composable
fun TiePage(h: Home, t: Thought, nav: Nav) {
    val ctx = LocalContext.current
    val c = ctx.container
    var q by remember { mutableStateOf("") }
    Page(stringResource(R.string.tie_title), { nav.thought(t.id) }) {
        Text(t.text, style = T.Thought.copy(color = C.amber))
        Field(q, { q = it }, stringResource(R.string.search_hint), Modifier.fillMaxWidth())
        val tied = h.threads(t.id).map { it.id }.toSet()
        val words = Web.words(t.text)
        val pool = h.live.filter { it.id != t.id && it.id !in tied && (q.isBlank() || it.text.contains(q.trim(), ignoreCase = true)) }
            // Ones sharing words come first, then the newest.
            .sortedWith(compareByDescending<Thought> { Web.words(it.text).intersect(words).size }.thenByDescending { it.at })
        if (pool.isEmpty()) Text(stringResource(R.string.tie_none), style = T.Small)
        for (o in pool.take(60)) ThoughtRow(h, o, onClick = {
            c.write { if (tie(t.id, o.id)) note(ctx.getString(R.string.n_tied), ctx.getString(R.string.n_tied_b)) }
            nav.thought(t.id)
        })
    }
}

/** Spin a cluster into a cocoon under a name. */
@Composable
fun SpinSheet(h: Home, t: Thought, nav: Nav) {
    val ctx = LocalContext.current
    val c = ctx.container
    val cluster = remember(t.id) { h.cluster(t.id).toList() }
    // The nearest ones along the threads are picked to start with; tap to change.
    var picked by remember(t.id) { mutableStateOf(cluster.take(Web.COCOON_MAX).toSet()) }
    var name by rememberSaveable { mutableStateOf("") }
    Sheet({ nav.thought(t.id) }) {
        Text(stringResource(R.string.spin_title), style = T.Title)
        Text(stringResource(R.string.spin_b), style = T.Body.copy(color = C.silkD))
        Text(stringResource(R.string.spin_pick, picked.size, Web.COCOON_MIN, Web.COCOON_MAX), style = T.Small.copy(color = if (picked.size in Web.COCOON_MIN..Web.COCOON_MAX) C.muted else C.warn))
        for (id in cluster) h.byId[id]?.let { o ->
            val on = id in picked
            Row(
                Modifier.fillMaxWidth().clickable { picked = if (on) picked - id else picked + id }.padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(if (on) "●" else "○", style = T.Body.copy(color = if (on) C.amber else C.muted))
                KindGlyph(o.kind, 16.dp)
                Text(o.text, style = T.ThoughtSmall.copy(color = if (on) C.silk else C.muted), maxLines = 2)
            }
        }
        Field(name, { name = it }, stringResource(R.string.spin_hint), Modifier.fillMaxWidth())
        Btn(stringResource(R.string.spin_go), {
            val ids = picked
            c.write { spin(ids, name); note(ctx.getString(R.string.n_spun, name.trim()), ctx.getString(R.string.n_spun_b)) }
            nav.close()
        }, Modifier.fillMaxWidth(), enabled = name.isNotBlank() && picked.size in Web.COCOON_MIN..Web.COCOON_MAX)
    }
}

/** Evening: today's catches offered to the lure, one tap each. */
@Composable
fun LureSheet(h: Home, nav: Nav) {
    val ctx = LocalContext.current
    val c = ctx.container
    val lure = h.lure ?: return
    val offer = remember { h.lureOffer }
    var tied by remember { mutableStateOf(setOf<Long>()) }
    fun done() { c.write { settings.update { it.copy(lureAsked = Web.today()) } }; nav.close() }
    Sheet(::done) {
        Text(stringResource(R.string.lure_title), style = T.Title)
        Text(lure.text, style = T.Thought.copy(color = C.teal))
        Text(stringResource(R.string.lure_b), style = T.Small)
        for (o in offer) ThoughtRow(h, o, onClick = {
            if (o.id !in tied) { tied = tied + o.id; c.write { tie(lure.id, o.id) } }
        }) { Text(if (o.id in tied) "✓" else "+", style = T.Heading.copy(color = if (o.id in tied) C.teal else C.silkD)) }
        Btn(stringResource(R.string.done), ::done, Modifier.fillMaxWidth())
    }
}

/** Morning: what got caught in the night. */
@Composable
fun NightSheet(h: Home, nav: Nav) {
    val ctx = LocalContext.current
    val c = ctx.container
    val list = remember { h.nightCatch }
    fun done() { c.write { settings.update { it.copy(nightSeen = Web.today()) } }; nav.close() }
    Sheet(::done) {
        Text(stringResource(R.string.night_title), style = T.Title)
        Text(stringResource(R.string.night_b), style = T.Small)
        for (o in list) ThoughtRow(h, o, onClick = { c.write { settings.update { it.copy(nightSeen = Web.today()) } }; nav.thought(o.id) })
        Btn(stringResource(R.string.done), ::done, Modifier.fillMaxWidth())
    }
}
