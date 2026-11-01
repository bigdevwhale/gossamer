package app.gossamer.ui.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.gossamer.R
import app.gossamer.container
import app.gossamer.data.Kind
import app.gossamer.domain.Web
import app.gossamer.ui.art.Geo
import app.gossamer.ui.art.backdrop
import app.gossamer.ui.art.butterfly
import app.gossamer.ui.art.cocoon
import app.gossamer.ui.art.dew
import app.gossamer.ui.art.glyph
import app.gossamer.ui.art.rememberTime
import app.gossamer.ui.art.spider
import app.gossamer.ui.art.strand
import app.gossamer.ui.art.webSilk
import app.gossamer.ui.components.Btn
import app.gossamer.ui.components.Kind as BtnKind
import app.gossamer.ui.theme.C
import app.gossamer.ui.theme.T

/** Three pages: catch, tie, let it fly. */
@Composable
fun Onboarding() {
    val c = LocalContext.current.container
    var page by rememberSaveable { mutableIntStateOf(0) }
    val time by rememberTime()
    val u = with(LocalDensity.current) { 1.dp.toPx() }
    Column(Modifier.fillMaxSize().background(C.ink)) {
        Box(Modifier.fillMaxWidth().weight(1f)) {
            Canvas(Modifier.fillMaxSize()) {
                backdrop(Web.Light.DUSK, Web.Season.AUTUMN, time, u)
                val g = Geo(size, time, u)
                webSilk(g, Web.Light.DUSK, Web.Season.SUMMER, u)
                val a = g.at(2, .55f); val b = g.at(6, .7f); val d = g.at(10, .5f); val e = g.at(12, .8f)
                when (page) {
                    0 -> {
                        glyph(Kind.IDEA, a, u * 1.4f, time, 1); glyph(Kind.QUESTION, b, u * 1.4f, time, 2)
                        glyph(Kind.NOTICE, d, u * 1.4f, time, 3, night = true); glyph(Kind.WORDS, e, u * 1.4f, time, 4)
                        spider(g.hub, u * 1.3f, time)
                    }
                    1 -> {
                        strand(g, a, b, u, false, true); strand(g, b, d, u, false, false); strand(g, a, d, u, true, false)
                        glyph(Kind.IDEA, a, u * 1.4f, time, 1); glyph(Kind.QUESTION, b, u * 1.4f, time, 2, big = true)
                        glyph(Kind.NOTICE, d, u * 1.4f, time, 3); glyph(Kind.WORDS, e, u * 1.4f, time, 4, dim = true)
                        dew(d, u * 1.3f, time)
                        spider(b, u * 1.3f, time)
                    }
                    else -> {
                        cocoon(Offset(size.width * .3f, size.height * .3f), u * 1.6f, time, ripe = true)
                        butterfly(Offset(size.width * .6f, size.height * .5f + kotlin.math.sin(time) * 6 * u), size.width * .45f, 7, mapOf(Kind.IDEA to 3, Kind.QUESTION to 2, Kind.NOTICE to 1), time, flap = true)
                    }
                }
            }
            Text(stringResource(R.string.app_title), style = T.Display, modifier = Modifier.statusBarsPadding().padding(start = 36.dp, top = 30.dp))
        }
        Column(Modifier.fillMaxWidth().background(C.ink).navigationBarsPadding().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val (t, b) = listOf(R.string.ob1_t to R.string.ob1_b, R.string.ob2_t to R.string.ob2_b, R.string.ob3_t to R.string.ob3_b)[page]
            Text(stringResource(t), style = T.Title)
            Text(stringResource(b), style = T.Body.copy(color = C.silkD), modifier = Modifier.height(132.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (i in 0..2) Box(Modifier.size(if (i == page) 10.dp else 7.dp).clip(CircleShape).background(if (i == page) C.amber else C.line))
                Box(Modifier.weight(1f))
                if (page > 0) Btn(stringResource(R.string.back), { page-- }, kind = BtnKind.QUIET)
                Btn(stringResource(if (page < 2) R.string.next else R.string.start), {
                    if (page < 2) page++ else c.write { settings.update { it.copy(onboarded = true, started = System.currentTimeMillis()) } }
                })
            }
        }
    }
}
