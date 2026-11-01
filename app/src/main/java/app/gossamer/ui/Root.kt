package app.gossamer.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.gossamer.R
import app.gossamer.container
import app.gossamer.data.Settings
import app.gossamer.ui.catching.CatchForm
import app.gossamer.ui.collection.CocoonPage
import app.gossamer.ui.collection.CollectionScreen
import app.gossamer.ui.collection.HatchPage
import app.gossamer.ui.collection.YearPage
import app.gossamer.ui.components.Sheet
import app.gossamer.ui.list.FluffPage
import app.gossamer.ui.list.ThoughtsScreen
import app.gossamer.ui.onboarding.Onboarding
import app.gossamer.ui.settings.SettingsPage
import app.gossamer.ui.theme.C
import app.gossamer.ui.theme.T
import app.gossamer.ui.thought.LureSheet
import app.gossamer.ui.thought.NightSheet
import app.gossamer.ui.thought.SpinSheet
import app.gossamer.ui.thought.ThoughtSheet
import app.gossamer.ui.thought.TiePage
import app.gossamer.ui.web.WebScreen
import kotlinx.coroutines.delay

enum class Tab { WEB, THOUGHTS, COLLECTION }

/** What is open over the tabs. */
sealed class Overlay {
    data class Catch(val voice: Boolean) : Overlay()
    data class Thought(val id: Long) : Overlay()
    data class Tie(val id: Long) : Overlay()
    data class Spin(val id: Long) : Overlay()
    data class Hatch(val id: Long) : Overlay()
    data class Cocoon(val id: Long) : Overlay()
    data object Fluff : Overlay()
    data object Lure : Overlay()
    data object Night : Overlay()
    data object Year : Overlay()
    data object Settings : Overlay()
}

class Nav(val go: (Overlay?) -> Unit, val tab: (Tab) -> Unit) {
    fun catch(voice: Boolean) = go(Overlay.Catch(voice))
    fun thought(id: Long) = go(Overlay.Thought(id))
    fun tie(id: Long) = go(Overlay.Tie(id))
    fun spin(id: Long) = go(Overlay.Spin(id))
    fun hatch(id: Long) = go(Overlay.Hatch(id))
    fun cocoon(id: Long) = go(Overlay.Cocoon(id))
    fun fluff() = go(Overlay.Fluff)
    fun lure() = go(Overlay.Lure)
    fun night() = go(Overlay.Night)
    fun year() = go(Overlay.Year)
    fun close() = go(null)
}

/** [clock], [tab0] and [overlay0] let renders open a fixed moment on a given screen; the app passes none. */
@Composable
fun GossamerRoot(s: Settings, clock: Long? = null, tab0: Tab = Tab.WEB, overlay0: Overlay? = null) {
    val ctx = LocalContext.current
    val c = ctx.container
    if (!s.onboarded) { Onboarding(); return }
    val thoughtsFlow = remember { c.web.thoughts() }
    val strandsFlow = remember { c.web.strands() }
    val cocoonsFlow = remember { c.web.cocoons() }
    val thoughts by thoughtsFlow.collectAsStateWithLifecycle(initialValue = null)
    val strands by strandsFlow.collectAsStateWithLifecycle(initialValue = null)
    val cocoons by cocoonsFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    var now by remember { mutableLongStateOf(clock ?: System.currentTimeMillis()) }
    LaunchedEffect(Unit) { if (clock == null) while (true) { delay(60_000); now = System.currentTimeMillis() } }
    val list = thoughts ?: return
    val st = strands ?: return
    val h = remember(list, st, cocoons, s, now) { Home(list, st, cocoons, s, if (clock == null) maxOf(now, System.currentTimeMillis()) else now) }
    var tab by rememberSaveable { mutableStateOf(tab0) }
    var overlay by remember { mutableStateOf(overlay0) }
    val nav = remember { Nav({ overlay = it }, { tab = it; overlay = null }) }

    Box(Modifier.fillMaxSize().background(C.ink)) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f)) {
                when (tab) {
                    Tab.WEB -> WebScreen(h, nav)
                    Tab.THOUGHTS -> ThoughtsScreen(h, nav)
                    Tab.COLLECTION -> CollectionScreen(h, nav)
                }
                Header(h, tab) { overlay = Overlay.Settings }
            }
            TabBar(h, tab) { tab = it }
        }
        when (val o = overlay) {
            is Overlay.Catch -> Sheet(nav::close) { CatchForm(voice = o.voice) { nav.tab(Tab.WEB) } }
            is Overlay.Thought -> h.byId[o.id]?.let { ThoughtSheet(h, it, nav) }
            is Overlay.Tie -> h.byId[o.id]?.let { TiePage(h, it, nav) }
            is Overlay.Spin -> h.byId[o.id]?.let { SpinSheet(h, it, nav) }
            is Overlay.Hatch -> h.cocoon(o.id)?.let { HatchPage(h, it, nav) }
            is Overlay.Cocoon -> h.cocoon(o.id)?.let { CocoonPage(h, it, nav) }
            Overlay.Fluff -> FluffPage(h, nav)
            Overlay.Lure -> LureSheet(h, nav)
            Overlay.Night -> NightSheet(h, nav)
            Overlay.Year -> YearPage(h, nav)
            Overlay.Settings -> SettingsPage(s, nav)
            null -> Unit
        }
        Notes(Modifier.align(Alignment.TopCenter))
    }
}

@Composable
private fun Header(h: Home, tab: Tab, onSettings: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(if (tab == Tab.WEB) androidx.compose.ui.graphics.Color.Transparent else C.ink).statusBarsPadding().padding(start = 34.dp, end = 10.dp, top = 6.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.app_title), style = T.Title)
            if (tab == Tab.WEB) Text(pluralStringResource(R.plurals.on_web, h.live.size, h.live.size), style = T.Small.copy(color = C.silkD))
        }
        val label = stringResource(R.string.settings)
        Box(Modifier.size(44.dp).clip(CircleShape).background(C.ink.copy(alpha = .5f)).clickable(onClick = onSettings).semantics { contentDescription = label }, contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(20.dp)) {
                val w = size.width
                // A tiny orb web for the settings.
                for (i in 0 until 6) {
                    val a = i * Math.PI.toFloat() / 3
                    drawLine(C.silkD, center, Offset(center.x + kotlin.math.cos(a) * w / 2, center.y + kotlin.math.sin(a) * w / 2), w * .06f)
                }
                drawCircle(C.silkD, w * .22f, style = Stroke(w * .06f))
                drawCircle(C.silkD, w * .42f, style = Stroke(w * .06f))
            }
        }
    }
}

@Composable
private fun TabBar(h: Home, tab: Tab, onTab: (Tab) -> Unit) {
    Row(Modifier.fillMaxWidth().background(C.deep).navigationBarsPadding().padding(vertical = 4.dp)) {
        for ((t, l) in listOf(Tab.WEB to R.string.tab_web, Tab.THOUGHTS to R.string.tab_thoughts, Tab.COLLECTION to R.string.tab_collection)) {
            val on = t == tab
            val dot = t == Tab.COLLECTION && h.ripe.isNotEmpty()
            Column(Modifier.weight(1f).clickable { onTab(t) }.padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(l) + if (dot) " •" else "", style = T.Button.copy(color = if (on) C.silk else C.muted), textAlign = TextAlign.Center, maxLines = 1)
                Box(Modifier.padding(top = 4.dp).size(width = 18.dp, height = 2.dp).background(if (on) C.amber else androidx.compose.ui.graphics.Color.Transparent))
            }
        }
    }
}

/** Notes come one after another at the top, each for a few seconds; a tap hides one. */
@Composable
private fun Notes(modifier: Modifier) {
    val c = LocalContext.current.container
    val n = c.notes.firstOrNull() ?: return
    LaunchedEffect(n) { delay(3400); c.notes.remove(n) }
    Column(
        modifier.statusBarsPadding().padding(top = 60.dp, start = 16.dp, end = 16.dp).fillMaxWidth().clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
            .background(C.panelL).clickable { c.notes.remove(n) }.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(n.title, style = T.Heading)
        if (n.body.isNotBlank()) Text(n.body, style = T.Small.copy(color = C.silkD))
    }
}
