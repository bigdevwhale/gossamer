package app.gossamer.ui.art

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import app.gossamer.data.Kind
import app.gossamer.domain.Web
import app.gossamer.ui.theme.C
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

object Art {
    /** Renders and screenshots want still silk. */
    @Volatile var still = false
}

/** Seconds since the screen opened, looping every ten minutes; frozen at 7 s for still renders. */
@Composable
fun rememberTime(): State<Float> {
    if (Art.still) return remember { mutableFloatStateOf(7f) }
    val t = rememberInfiniteTransition(label = "time")
    return t.animateFloat(0f, 600f, infiniteRepeatable(tween(600_000, easing = LinearEasing), RepeatMode.Restart), label = "t")
}

fun mix(a: Color, b: Color, f: Float) = Color(a.red + (b.red - a.red) * f, a.green + (b.green - a.green) * f, a.blue + (b.blue - a.blue) * f, a.alpha + (b.alpha - a.alpha) * f)

/** The web's frame of reference on a canvas: the hub, the radius, where each spoke points. */
class Geo(val size: Size, val t: Float, val unit: Float) {
    val hub = Offset(size.width * .54f, size.height * .44f)
    val radius = min(size.width * .47f, size.height * .4f)

    fun angle(spoke: Int, wobble: Float = 0f) = (spoke * 2 * PI / Web.SPOKES - PI / 2 + .2).toFloat() + wobble * .2f

    /** The rim isn't a circle: each spoke is tied off at its own length. */
    fun rim(spoke: Int) = 1.03f + .07f * sin(spoke * 1.9f + .5f)

    /** A point on the web, swaying a little in the draught: the further out, the more. */
    fun at(spoke: Int, r: Float, wobble: Float = 0f): Offset {
        val a = angle(spoke, wobble)
        val rr = r * radius * (if (r > .98f) 1f else 1f)
        val sway = sin(t * .7f + r * 3f + spoke * .3f) * r * unit * 2.2f
        return Offset(hub.x + cos(a) * rr + sway, hub.y + sin(a) * rr + sway * .4f)
    }
}

// --- the window and the sky ---------------------------------------------------------------------------

fun DrawScope.backdrop(light: Web.Light, season: Web.Season, t: Float, u: Float) {
    val (top, bottom, glow) = when (light) {
        Web.Light.NIGHT -> Triple(Color(0xFF0A111D), Color(0xFF17233A), Color(0xFF2B3D5E))
        Web.Light.DUSK -> Triple(Color(0xFF1E2139), Color(0xFF6B4A4A), Color(0xFFE09A62))
        Web.Light.DAWN -> Triple(Color(0xFF26324C), Color(0xFFB38877), Color(0xFFF4C59A))
        Web.Light.DAY -> Triple(Color(0xFF41566A), Color(0xFF8D9A9C), Color(0xFFD9DCD2))
    }
    drawRect(Brush.verticalGradient(listOf(top, bottom)))
    // A soft light low on the far side of the glass: the moon at night, the sun's last glow at dusk.
    drawCircle(Brush.radialGradient(listOf(glow.copy(alpha = .45f), Color.Transparent), Offset(size.width * .85f, size.height * .82f), size.width * .8f), size.width * .8f, Offset(size.width * .85f, size.height * .82f))
    if (light == Web.Light.NIGHT) {
        val r = Random(11)
        repeat(46) {
            val p = Offset(r.nextFloat() * size.width, r.nextFloat() * size.height * .8f)
            val tw = .35f + .35f * sin(t * (.6f + r.nextFloat()) + it)
            drawCircle(Color.White.copy(alpha = tw * .7f), u * (.5f + r.nextFloat() * .7f), p)
        }
        drawCircle(Color(0xFFF3EEDC).copy(alpha = .9f), u * 13, Offset(size.width * .8f, size.height * .2f))
        val moon = Offset(size.width * .8f, size.height * .2f)
        drawCircle(Brush.radialGradient(listOf(Color(0xFFF3EEDC).copy(alpha = .22f), Color.Transparent), moon, u * 46), u * 46, moon)
    }
    if (season == Web.Season.WINTER) frost(u)
    // The corner of the window frame the spider built in.
    val bar = u * 20
    drawRect(Brush.verticalGradient(listOf(C.frame, C.frameL), 0f, bar), Offset.Zero, Size(size.width, bar))
    drawRect(Brush.horizontalGradient(listOf(C.frame, C.frameL), 0f, bar), Offset.Zero, Size(bar, size.height))
    drawLine(Color.Black.copy(alpha = .35f), Offset(bar, bar), Offset(size.width, bar), u * 1.5f)
    drawLine(Color.Black.copy(alpha = .35f), Offset(bar, bar), Offset(bar, size.height), u * 1.5f)
}

private fun DrawScope.frost(u: Float) {
    val r = Random(5)
    val c = Color.White.copy(alpha = .22f)
    fun branch(p: Offset, a: Float, len: Float, depth: Int) {
        if (depth == 0 || len < u * 2) return
        val q = Offset(p.x + cos(a) * len, p.y + sin(a) * len)
        drawLine(c, p, q, u * .7f)
        branch(q, a + .5f + r.nextFloat() * .3f, len * .6f, depth - 1)
        branch(q, a - .5f - r.nextFloat() * .3f, len * .6f, depth - 1)
        branch(q, a + (r.nextFloat() - .5f) * .2f, len * .75f, depth - 1)
    }
    for (i in 0 until 9) branch(Offset(u * 20, u * 20 + i * u * 26), (r.nextFloat() - .3f) * 1.2f, u * 22, 4)
    for (i in 0 until 7) branch(Offset(size.width - i * u * 30, size.height), -PI.toFloat() / 2 + (r.nextFloat() - .5f), u * 20, 4)
}

// --- the web --------------------------------------------------------------------------------------------

fun DrawScope.webSilk(g: Geo, light: Web.Light, season: Web.Season, u: Float) {
    val silk = (if (light == Web.Light.DAY) Color(0xFFF7F4EC) else C.silk).copy(alpha = .38f)
    val n = Web.SPOKES
    // Anchor threads to the frame and off past the edges.
    val bar = u * 20
    val anchors = listOf(2 to Offset(size.width * .7f, bar), 9 to Offset(bar, size.height * .78f), 12 to Offset(bar, bar), 0 to Offset(size.width * .2f, bar), 5 to Offset(size.width, size.height * .62f), 7 to Offset(size.width * .55f, size.height))
    for ((s, p) in anchors) drawLine(silk.copy(alpha = .3f), g.at(s, g.rim(s)), p, u * .8f)
    // Spokes.
    for (s in 0 until n) drawLine(silk, g.hub, g.at(s, g.rim(s)), u * .8f)
    // The frame thread round the rim.
    val rimPath = Path()
    for (s in 0..n) { val p = g.at(s % n, g.rim(s % n)); if (s == 0) rimPath.moveTo(p.x, p.y) else rimPath.lineTo(p.x, p.y) }
    drawPath(rimPath, silk, style = Stroke(u * .9f))
    // The capture spiral, sagging a little between spokes.
    var k = .14f
    var ring = 0
    while (k < 1f) {
        val path = Path()
        for (s in 0..n) {
            val ss = s % n
            val rr = k * g.rim(ss) + (s.toFloat() / n) * .03f
            val p = g.at(ss, rr)
            if (s == 0) path.moveTo(p.x, p.y) else {
                val prev = g.at((s - 1) % n, k * g.rim((s - 1) % n) + ((s - 1).toFloat() / n) * .03f)
                val mid = Offset((prev.x + p.x) / 2, (prev.y + p.y) / 2)
                val c = Offset(mid.x + (g.hub.x - mid.x) * .05f, mid.y + (g.hub.y - mid.y) * .05f)
                path.quadraticTo(c.x, c.y, p.x, p.y)
            }
        }
        drawPath(path, silk.copy(alpha = silk.alpha * (.6f + .4f * k)), style = Stroke(u * .6f))
        if (season == Web.Season.WINTER || season == Web.Season.SPRING) for (s in 0 until n step 2) {
            val p = g.at((s + ring) % n, k * g.rim((s + ring) % n))
            if (season == Web.Season.WINTER) drawCircle(Color.White.copy(alpha = .55f), u * .9f, p)
            else drawCircle(C.dew.copy(alpha = .5f), u * 1.2f, Offset(p.x, p.y + u))
        }
        k += .058f
        ring++
    }
    // The hub: a small tangle of silk.
    drawCircle(silk.copy(alpha = .5f), g.radius * .06f, g.hub, style = Stroke(u * .7f))
    drawCircle(silk.copy(alpha = .35f), g.radius * .03f, g.hub, style = Stroke(u * .7f))
    when (season) {
        Web.Season.AUTUMN -> leaf(g.at(3, .93f), u, g.t)
        Web.Season.INDIAN -> flying(g, u)
        Web.Season.SUMMER -> motes(g, u)
        else -> Unit
    }
}

private fun DrawScope.leaf(p: Offset, u: Float, t: Float) {
    rotate(25f + sin(t * .9f) * 6f, p) {
        val path = Path().apply {
            moveTo(p.x, p.y); quadraticTo(p.x + u * 9, p.y + u * 2, p.x + u * 14, p.y + u * 12)
            quadraticTo(p.x + u * 2, p.y + u * 11, p.x, p.y); close()
        }
        drawPath(path, Color(0xFFC8743A).copy(alpha = .9f))
        drawLine(Color(0xFF7A3E1C), p, Offset(p.x + u * 13, p.y + u * 11), u * .7f)
    }
}

/** Indian summer: long threads drifting across the glass. */
private fun DrawScope.flying(g: Geo, u: Float) {
    for (i in 0 until 4) {
        val y0 = size.height * (.15f + i * .2f)
        val x0 = ((g.t * (14 + i * 5) * u / 6 + i * 300) % (size.width + 400)) - 200
        val path = Path()
        for (j in 0..20) {
            val x = x0 + j * u * 9
            val y = y0 + sin(j * .5f + g.t * .8f + i) * u * 6 - j * u * 1.5f
            if (j == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, Color.White.copy(alpha = .28f), style = Stroke(u * .6f))
    }
}

private fun DrawScope.motes(g: Geo, u: Float) {
    val r = Random(3)
    repeat(18) {
        val x = (r.nextFloat() * size.width + sin(g.t * .2f + it) * u * 20)
        val y = (r.nextFloat() * size.height + g.t * u * (1 + it % 3)) % size.height
        drawCircle(Color(0xFFFFF4D6).copy(alpha = .25f), u * .9f, Offset(x, y))
    }
}

/** A thread you pulled: a little bowed towards the hub, gold where it catches the light. */
fun DrawScope.strand(g: Geo, a: Offset, b: Offset, u: Float, echo: Boolean, lit: Boolean) {
    val mid = Offset((a.x + b.x) / 2, (a.y + b.y) / 2)
    val c = Offset(mid.x + (g.hub.x - mid.x) * .22f, mid.y + (g.hub.y - mid.y) * .22f)
    val path = Path().apply { moveTo(a.x, a.y); quadraticTo(c.x, c.y, b.x, b.y) }
    val col = if (lit) Color(0xFFFFE2A6) else Color(0xFFF1E6CC)
    if (lit) drawPath(path, col.copy(alpha = .18f), style = Stroke(u * 5, cap = StrokeCap.Round))
    drawPath(path, col.copy(alpha = if (lit) .95f else .7f), style = Stroke(u * (if (echo) 2.8f else 1.3f), cap = StrokeCap.Round))
}

// --- what gets caught --------------------------------------------------------------------------------

/** A thought as the thing it is: firefly, burr, dandelion down or a wisp of someone else's silk. */
fun DrawScope.glyph(kind: Kind, p: Offset, u: Float, t: Float, seed: Long, night: Boolean = false, big: Boolean = false, dim: Boolean = false) {
    val s = u * (if (big) 8.5f else 6f)
    val col = C.of(kind).copy(alpha = if (dim) .45f else 1f)
    val phase = (seed % 97).toFloat()
    if (night) drawCircle(Brush.radialGradient(listOf(Color(0xFF9FC4FF).copy(alpha = .35f), Color.Transparent), p, s * 3.2f), s * 3.2f, p)
    when (kind) {
        Kind.IDEA -> {
            val pulse = .55f + .45f * sin(t * 2.1f + phase)
            drawCircle(Brush.radialGradient(listOf(col.copy(alpha = .55f * pulse), Color.Transparent), p, s * 2.6f), s * 2.6f, p)
            drawOval(Color(0xFF2A2117), Offset(p.x - s * .35f, p.y - s * .8f), Size(s * .7f, s * 1.3f))
            drawOval(col.copy(alpha = .6f + .4f * pulse), Offset(p.x - s * .38f, p.y + s * .1f), Size(s * .76f, s * .75f))
            drawOval(Color.White.copy(alpha = .35f), Offset(p.x - s * .9f, p.y - s * .75f), Size(s * .7f, s * .45f))
            drawOval(Color.White.copy(alpha = .35f), Offset(p.x + s * .2f, p.y - s * .75f), Size(s * .7f, s * .45f))
        }
        Kind.QUESTION -> {
            drawCircle(col.copy(alpha = col.alpha * .25f), s * 1.4f, p)
            for (i in 0 until 9) {
                val a = i * 2 * PI.toFloat() / 9 + phase
                val q = Offset(p.x + cos(a) * s * 1.15f, p.y + sin(a) * s * 1.15f)
                drawLine(col, p, q, u * .8f)
                val h = Offset(q.x + cos(a + 1.9f) * s * .3f, q.y + sin(a + 1.9f) * s * .3f)
                drawLine(col, q, h, u * .8f)
            }
            drawCircle(mix(col, Color.Black, .35f), s * .55f, p)
        }
        Kind.NOTICE -> {
            val sway = sin(t * 1.3f + phase) * .15f
            for (i in 0 until 12) {
                val a = i * 2 * PI.toFloat() / 12 + sway + phase
                val q = Offset(p.x + cos(a) * s * 1.3f, p.y + sin(a) * s * 1.3f)
                drawLine(col.copy(alpha = col.alpha * .7f), p, q, u * .5f)
                drawCircle(col.copy(alpha = col.alpha * .6f), u * .7f, q)
            }
            drawCircle(mix(col, Color(0xFF6B4A3A), .5f), s * .32f, p)
        }
        Kind.WORDS -> {
            drawOval(col.copy(alpha = col.alpha * .85f), Offset(p.x - s * .5f, p.y - s), Size(s, s * 2))
            for (i in -2..2) {
                val y = p.y + i * s * .38f
                drawLine(Color.White.copy(alpha = .5f), Offset(p.x - s * .5f, y - s * .2f), Offset(p.x + s * .5f, y + s * .2f), u * .6f)
            }
            drawLine(col.copy(alpha = .6f), Offset(p.x, p.y - s), Offset(p.x + s * .2f, p.y - s * 2f), u * .6f)
        }
    }
}

/** Dew on a thought: three drops catching the morning light. */
fun DrawScope.dew(p: Offset, u: Float, t: Float) {
    for (i in 0 until 3) {
        val a = i * 2.1f + .4f
        val q = Offset(p.x + cos(a) * u * 11, p.y + sin(a) * u * 11)
        val tw = .6f + .4f * sin(t * 3 + i * 2)
        drawCircle(C.dew.copy(alpha = .25f * tw), u * 3.5f, q)
        drawCircle(C.dew.copy(alpha = .9f), u * 1.5f, q)
        drawCircle(Color.White.copy(alpha = tw), u * .6f, Offset(q.x - u * .5f, q.y - u * .5f))
    }
}

/** The spider: you. Eight legs, a round back with a pale cross, sitting still or running along a thread. */
fun DrawScope.spider(p: Offset, u: Float, t: Float, facing: Float = 0f) {
    val bob = sin(t * 1.6f) * u * .6f
    val c = Offset(p.x, p.y + bob)
    rotate(facing, c) {
        val leg = Color(0xFF1B1511)
        for (side in listOf(-1f, 1f)) for (i in 0 until 4) {
            val a = (-60f + i * 38f) * PI.toFloat() / 180
            val knee = Offset(c.x + side * cos(a) * u * 9, c.y + sin(a) * u * 7 - u * 4)
            val foot = Offset(c.x + side * cos(a) * u * 15, c.y + sin(a) * u * 13 + u * 2)
            drawLine(leg, Offset(c.x + side * u * 1.5f, c.y - u * 2), knee, u * 1.3f, StrokeCap.Round)
            drawLine(leg, knee, foot, u * 1.1f, StrokeCap.Round)
        }
        drawOval(Color(0xFF2A1F17), Offset(c.x - u * 5.5f, c.y - u * 1f), Size(u * 11, u * 12))
        drawOval(Color(0xFF3A2B1F), Offset(c.x - u * 3.5f, c.y - u * 6.5f), Size(u * 7, u * 6.5f))
        drawLine(Color(0xFFD9C9A8).copy(alpha = .8f), Offset(c.x, c.y + u * 1.5f), Offset(c.x, c.y + u * 8.5f), u)
        drawLine(Color(0xFFD9C9A8).copy(alpha = .8f), Offset(c.x - u * 2.5f, c.y + u * 4.5f), Offset(c.x + u * 2.5f, c.y + u * 4.5f), u)
        drawCircle(Color(0xFFE8D9B8).copy(alpha = .6f), u * .7f, Offset(c.x - u * 1.3f, c.y - u * 5.5f))
        drawCircle(Color(0xFFE8D9B8).copy(alpha = .6f), u * .7f, Offset(c.x + u * 1.3f, c.y - u * 5.5f))
    }
}

/** A cocoon: a spindle of wound silk; a ripe one glows from inside. */
fun DrawScope.cocoon(p: Offset, u: Float, t: Float, ripe: Boolean, scale: Float = 1f) {
    val w = u * 11 * scale
    val h = u * 24 * scale
    drawLine(C.silk.copy(alpha = .5f), Offset(p.x, p.y - h * .5f), Offset(p.x, p.y - h * 1.2f), u * .7f)
    if (ripe) drawCircle(Brush.radialGradient(listOf(C.amber.copy(alpha = .4f + .15f * sin(t * 2)), Color.Transparent), p, h), h, p)
    drawOval(Color(0xFFE9E1CF), Offset(p.x - w / 2, p.y - h / 2), Size(w, h))
    for (i in 0 until 9) {
        val y = p.y - h / 2 + h * (i + .5f) / 9
        val half = w / 2 * kotlin.math.sqrt(1 - ((y - p.y) / (h / 2)).let { it * it }.coerceAtMost(1f))
        drawLine(Color(0xFFB8AE98), Offset(p.x - half, y - u * 1.5f), Offset(p.x + half, y + u * 1.5f), u * .6f)
    }
    if (ripe) drawLine(C.amber, Offset(p.x - w * .1f, p.y - h * .3f), Offset(p.x + w * .12f, p.y + h * .05f), u * 1.1f)
}

fun dist(a: Offset, b: Offset) = hypot(a.x - b.x, a.y - b.y)

inline fun DrawScope.at(p: Offset, block: DrawScope.() -> Unit) = translate(p.x, p.y, block)
