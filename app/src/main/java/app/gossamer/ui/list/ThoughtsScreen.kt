package app.gossamer.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import app.gossamer.ui.Home
import app.gossamer.ui.Nav
import app.gossamer.ui.catching.KindGlyph
import app.gossamer.ui.catching.kindName
import app.gossamer.ui.components.Btn
import app.gossamer.ui.components.Card
import app.gossamer.ui.components.Chip
import app.gossamer.ui.components.Field
import app.gossamer.ui.components.Flow
import app.gossamer.ui.components.Kind as BtnKind
import app.gossamer.ui.components.Page
import app.gossamer.ui.components.when_
import app.gossamer.ui.theme.C
import app.gossamer.ui.theme.T
import app.gossamer.ui.thought.ThoughtRow

/** Every thought on the web as a list, with a search: the spider runs to the one you tap. */
@Composable
fun ThoughtsScreen(h: Home, nav: Nav) {
    var q by rememberSaveable { mutableStateOf("") }
    var kind by rememberSaveable { mutableStateOf<Kind?>(null) }
    var order by rememberSaveable { mutableStateOf(0) }
    val c = LocalContext.current.container
    val list = h.live.filter { (kind == null || it.kind == kind) && (q.isBlank() || it.text.contains(q.trim(), ignoreCase = true)) }
        .let { l -> when (order) { 1 -> l.sortedByDescending { h.degree(it.id) }; 2 -> l.sortedBy { it.touched }; else -> l.sortedByDescending { it.at } } }
    LazyColumn(
        Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 64.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Field(q, { q = it }, stringResource(R.string.search_hint), Modifier.fillMaxWidth())
                Flow {
                    Chip(stringResource(R.string.all), kind == null, { kind = null })
                    for (k in Kind.entries) Chip(stringResource(kindName(k)), kind == k, { kind = if (kind == k) null else k }, lead = { KindGlyph(k, 16.dp) })
                }
                Flow {
                    for ((i, l) in listOf(R.string.o_new, R.string.o_tied, R.string.o_old).withIndex()) Chip(stringResource(l), order == i, { order = i })
                }
            }
        }
        if (list.isEmpty()) item { Text(stringResource(if (h.live.isEmpty()) R.string.list_empty else R.string.list_none), style = T.Body.copy(color = C.silkD), modifier = Modifier.padding(vertical = 20.dp)) }
        items(list, key = { it.id }) { t ->
            ThoughtRow(h, t, onClick = { c.focus.value = t.id; nav.tab(app.gossamer.ui.Tab.WEB); nav.thought(t.id) })
        }
        item {
            Spacer(Modifier.height(8.dp))
            Card(Modifier.fillMaxWidth(), onClick = nav::fluff) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.fluff), style = T.Heading)
                        Text(if (h.fluff.isEmpty()) stringResource(R.string.fluff_empty_short) else pluralStringResource(R.plurals.fluff_n, h.fluff.size, h.fluff.size), style = T.Small)
                    }
                    Text("›", style = T.Heading.copy(color = C.silkD))
                }
            }
        }
    }
}

/** The Fluff: what the wind took. Nothing is lost; anything can be put back on the web. */
@Composable
fun FluffPage(h: Home, nav: Nav) {
    val c = LocalContext.current.container
    var q by rememberSaveable { mutableStateOf("") }
    Page(stringResource(R.string.fluff), nav::close) {
        Text(stringResource(R.string.fluff_b), style = T.Body.copy(color = C.silkD))
        if (h.fluff.size > 6) Field(q, { q = it }, stringResource(R.string.search_hint), Modifier.fillMaxWidth())
        if (h.fluff.isEmpty()) Text(stringResource(R.string.fluff_empty), style = T.Italic)
        for (t in h.fluff.filter { q.isBlank() || it.text.contains(q.trim(), ignoreCase = true) }) Card(Modifier.fillMaxWidth(), pad = 12.dp) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KindGlyph(t.kind, 20.dp)
                Column(Modifier.weight(1f)) {
                    Text(t.text, style = T.ThoughtSmall.copy(color = C.silkD), maxLines = 4)
                    Text(stringResource(R.string.fluff_when, when_(t.at, h.now), when_(t.gone ?: t.at, h.now)), style = T.Small)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Btn(stringResource(R.string.restore), { c.write { restore(t) } }, kind = BtnKind.PLAIN)
                Btn(stringResource(R.string.let_go), { c.write { forget(t) } }, kind = BtnKind.QUIET)
            }
        }
    }
}
