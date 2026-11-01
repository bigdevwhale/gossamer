package app.gossamer.ui.web

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.gossamer.R
import app.gossamer.container
import app.gossamer.domain.Web
import app.gossamer.ui.Home
import app.gossamer.ui.Nav
import app.gossamer.ui.art.Geo
import app.gossamer.ui.art.backdrop
import app.gossamer.ui.art.cocoon
import app.gossamer.ui.art.dew
import app.gossamer.ui.art.dist
import app.gossamer.ui.art.glyph
import app.gossamer.ui.art.rememberTime
import app.gossamer.ui.art.spider
import app.gossamer.ui.art.strand
import app.gossamer.ui.art.webSilk
import app.gossamer.ui.theme.C
import app.gossamer.ui.theme.T
import kotlin.math.atan2
import kotlin.math.sin

@Composable
fun WebScreen(h: Home, nav: Nav) {
    val ctx = LocalContext.current
    val c = ctx.container
    val haptic = LocalHapticFeedback.current
    val time by rememberTime()
    val u = with(LocalDensity.current) { 1.dp.toPx() }
    val measurer = rememberTextMeasurer()
    val focus by c.focus.collectAsStateWithLifecycle()
    val pos = remember { HashMap<Long, Offset>() }
    val cocoonPos = remember { ArrayList<Pair<Long, Offset>>() }
    var hub by remember { mutableStateOf(Offset.Zero) }
    var dragFrom by remember { mutableStateOf<Long?>(null) }
    var dragAt by remember { mutableStateOf<Offset?>(null) }
    val spiderAt = remember { Animatable(Offset.Unspecified, Offset.VectorConverter) }
    var facing by remember { mutableStateOf(0f) }

    // The spider runs along the silk to whatever was just caught or tied, then back to the hub after a while.
    LaunchedEffect(focus, hub) {
        if (hub == Offset.Zero) return@LaunchedEffect
        val target = focus?.let { pos[it] } ?: hub
        if (spiderAt.value == Offset.Unspecified) { spiderAt.snapTo(hub); return@LaunchedEffect }
        val from = spiderAt.value
        facing = Math.toDegrees(atan2((target.y - from.y).toDouble(), (target.x - from.x).toDouble())).toFloat() + 90f
        spiderAt.animateTo(target, tween((dist(from, target) / u * 9).toInt().coerceIn(250, 1400)))
        if (focus != null) {
            kotlinx.coroutines.delay(6000)
            c.focus.value = null
        }
    }

    fun nearest(p: Offset, max: Float = 30 * u): Long? = pos.entries.minByOrNull { dist(it.value, p) }?.takeIf { dist(it.value, p) < max }?.key

    Box(Modifier.fillMaxSize()) {
        val label = stringResource(R.string.web_a11y, h.live.size)
        Canvas(
            Modifier.fillMaxSize().semantics { contentDescription = label }
                .pointerInput(h) {
                    detectTapGestures { p ->
                        cocoonPos.firstOrNull { dist(it.second, p) < 26 * u }?.let { (id, _) ->
                            if (h.ripe.any { it.id == id }) nav.hatch(id) else nav.cocoon(id)
                            return@detectTapGestures
                        }
                        nearest(p)?.let { nav.thought(it) }
                    }
                }
                .pointerInput(h) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = { p -> nearest(p)?.let { dragFrom = it; dragAt = p; haptic.performHapticFeedback(HapticFeedbackType.LongPress) } },
                        onDrag = { change, d -> if (dragFrom != null) { change.consume(); dragAt = (dragAt ?: change.position) + d } },
                        onDragEnd = {
                            val a = dragFrom
                            val b = dragAt?.let { nearest(it) }
                            dragFrom = null; dragAt = null
                            if (a != null && b != null && a != b) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                c.write {
                                    if (tie(a, b)) note(ctx.getString(R.string.n_tied), ctx.getString(R.string.n_tied_b))
                                    else note(ctx.getString(R.string.n_tied_already))
                                }
                            }
                        },
                        onDragCancel = { dragFrom = null; dragAt = null },
                    )
                },
        ) {
            backdrop(h.light, h.season, time, u)
            val g = Geo(size, time, u)
            hub = g.hub
            webSilk(g, h.light, h.season, u)
            pos.clear()
            for (t in h.live) h.spots[t.id]?.let { s ->
                val tremble = if (h.trembles(t)) sin(time * 23f + t.id) * u * 1.8f else 0f
                pos[t.id] = g.at(s.spoke, s.r, s.wobble) + Offset(tremble, 0f)
            }
            val lit = focus ?: dragFrom
            for (s in h.liveStrands) {
                val a = pos[s.a] ?: continue
                val b = pos[s.b] ?: continue
                strand(g, a, b, u, s.echo, lit != null && s.has(lit))
            }
            // The thread being pulled right now.
            val from = dragFrom?.let { pos[it] }
            val to = dragAt
            if (from != null && to != null) {
                val target = nearest(to)?.takeIf { it != dragFrom }?.let { pos[it] }
                strand(g, from, target ?: to, u, echo = false, lit = true)
                if (target != null) drawCircle(C.amber, 16 * u, target, style = Stroke(1.5f * u))
            }
            for (t in h.live) {
                val p = pos[t.id] ?: continue
                val hubby = h.degree(t.id) >= Web.HUB
                glyph(t.kind, p, u, time, t.id, night = t.night, big = hubby || t.id == h.lure?.id, dim = h.trembles(t))
                if (t.id in h.dew) dew(p, u, time)
                if (t.id == h.lure?.id) drawCircle(C.teal.copy(alpha = .7f), 15 * u, p, style = Stroke(1.2f * u))
            }
            // Cocoons hang from the top of the frame.
            val hanging = (h.ripe + h.ripening).take(4)
            cocoonPos.clear()
            cocoonPos += hanging.mapIndexed { i, cc -> cc.id to Offset(size.width * (.7f + i * .08f), 20 * u + 96 * u + (i % 2) * 22 * u) }
            for ((id, p) in cocoonPos) {
                drawLine(C.silk.copy(alpha = .45f), Offset(p.x, 20 * u), p, .7f * u)
                cocoon(p, u, time, ripe = h.ripe.any { it.id == id })
            }
            val sp = spiderAt.value.takeIf { it != Offset.Unspecified } ?: g.hub
            spider(sp, u, time, facing)
            // Labels for the hubs, the lure and whatever the spider is visiting.
            val labelled = (h.hubs.map { it.id } + listOfNotNull(h.lure?.id, focus)).distinct()
            val placed = ArrayList<androidx.compose.ui.geometry.Rect>()
            for (id in labelled) {
                val t = h.byId[id] ?: continue
                val p = pos[id] ?: continue
                label(measurer, t.text, p, u, if (id == focus) C.amber else C.silk, placed)
            }
        }

        // Whatever is waiting today, two at most, just above the catch button.
        val banners = buildList<@Composable () -> Unit> {
            if (h.ripe.isNotEmpty()) add { Banner(stringResource(R.string.b_ripe, h.ripe.first().name), C.amber) { nav.hatch(h.ripe.first().id) } }
            if (h.nightCatch.isNotEmpty()) add { Banner(pluralStringResource(R.plurals.b_night, h.nightCatch.size, h.nightCatch.size), Color(0xFF9FC4FF)) { nav.night() } }
            if (h.lureOffer.isNotEmpty()) add { Banner(stringResource(R.string.b_lure, h.lure?.text.orEmpty().let { if (it.length > 42) it.take(40).trimEnd() + "…" else it }), C.teal) { nav.lure() } }
            if (h.dew.isNotEmpty()) add { Banner(pluralStringResource(R.plurals.b_dew, h.dew.size, h.dew.size), C.dew) { nav.thought(h.dew.first()) } }
        }
        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(start = 14.dp, end = 14.dp, bottom = 84.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (b in banners.take(2)) b()
        }

        if (h.live.isEmpty()) Column(Modifier.align(Alignment.Center).padding(32.dp).widthIn(max = 320.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.web_empty), style = T.Title, textAlign = TextAlign.Center)
            Text(stringResource(R.string.web_empty_b), style = T.Body.copy(color = C.silkD), textAlign = TextAlign.Center)
        } else if (h.liveStrands.isEmpty() && h.live.size >= 2) Text(
            stringResource(R.string.web_hint), style = T.Small.copy(color = C.silkD), textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.BottomCenter).padding(start = 32.dp, end = 32.dp, bottom = if (banners.isEmpty()) 92.dp else 200.dp),
        )

        Row(Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            val catchLabel = stringResource(R.string.catch_a)
            Box(Modifier.clip(CircleShape).background(C.amber).clickable { nav.catch(false) }.padding(horizontal = 26.dp, vertical = 15.dp).semantics { contentDescription = catchLabel }) {
                Text(stringResource(R.string.catch_btn), style = T.Button.copy(color = C.ink))
            }
            val sayLabel = stringResource(R.string.sc_say)
            Box(Modifier.size(52.dp).clip(CircleShape).background(C.panel).border(1.dp, C.line, CircleShape).clickable { nav.catch(true) }.semantics { contentDescription = sayLabel }, contentAlignment = Alignment.Center) {
                Mic(Modifier.size(22.dp), C.silk)
            }
        }
    }
}

/** A thought's words under it in handwriting; moved above, or further down, if another label is already there. */
private fun DrawScope.label(m: TextMeasurer, text: String, p: Offset, u: Float, color: Color, placed: MutableList<androidx.compose.ui.geometry.Rect>) {
    val short = text.replace("\n", " ").let { if (it.length > 34) it.take(32).trimEnd() + "…" else it }
    val layout = m.measure(short, T.ThoughtSmall.copy(color = color), maxLines = 1, overflow = TextOverflow.Ellipsis, constraints = Constraints(maxWidth = (size.width * .55f).toInt()))
    val w = layout.size.width.toFloat()
    val hh = layout.size.height.toFloat()
    val x = (p.x - w / 2).coerceIn(24 * u, size.width - w - 8 * u)
    val tries = listOf(p.y + 12 * u, p.y - 12 * u - hh, p.y + 14 * u + hh, p.y - 14 * u - 2 * hh, p.y + 16 * u + 2 * hh)
    val y = tries.firstOrNull { ty -> placed.none { it.overlaps(androidx.compose.ui.geometry.Rect(x - 6 * u, ty, x + w + 6 * u, ty + hh)) } } ?: tries.first()
    val box = androidx.compose.ui.geometry.Rect(x - 6 * u, y, x + w + 6 * u, y + hh)
    placed += box
    drawRoundRect(C.ink.copy(alpha = .66f), Offset(box.left, box.top - u), Size(box.width, box.height + 2 * u), androidx.compose.ui.geometry.CornerRadius(8 * u))
    drawText(layout, topLeft = Offset(x, y))
}

@Composable
private fun Banner(text: String, accent: Color, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(androidx.compose.foundation.shape.RoundedCornerShape(14.dp)).background(C.ink.copy(alpha = .78f))
            .border(1.dp, accent.copy(alpha = .5f), androidx.compose.foundation.shape.RoundedCornerShape(14.dp)).clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(accent))
        Text(text, style = T.Small.copy(color = C.silk), modifier = Modifier.weight(1f))
        Text("›", style = T.Heading.copy(color = accent))
    }
}

/** A microphone drawn in two strokes. */
@Composable
fun Mic(modifier: Modifier, color: Color) {
    Canvas(modifier) {
        val w = size.width
        drawRoundRect(color, Offset(w * .35f, w * .08f), Size(w * .3f, w * .5f), androidx.compose.ui.geometry.CornerRadius(w * .15f))
        drawArc(color, 0f, 180f, false, Offset(w * .22f, w * .28f), Size(w * .56f, w * .48f), style = Stroke(w * .08f))
        drawLine(color, Offset(w * .5f, w * .76f), Offset(w * .5f, w * .92f), w * .08f)
        drawLine(color, Offset(w * .36f, w * .92f), Offset(w * .64f, w * .92f), w * .08f)
    }
}
