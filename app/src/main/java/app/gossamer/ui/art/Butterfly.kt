package app.gossamer.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import app.gossamer.data.Kind
import app.gossamer.ui.theme.C
import kotlin.math.sin
import kotlin.random.Random

/**
 * A butterfly let out of a cocoon. Its shape comes from the cocoon's seed; its colours from what was
 * inside: the commonest kind of thought paints the wings, the next one the bands, and there is one
 * eye-spot for every thought, up to six a side.
 */
fun DrawScope.butterfly(center: Offset, span: Float, seed: Int, kinds: Map<Kind, Int>, t: Float = 0f, flap: Boolean = false) {
    val r = Random(seed)
    val order = kinds.entries.sortedByDescending { it.value }.map { it.key }
    // Each butterfly leans towards a wing colour of its own; what was inside tints it.
    val wild = listOf(Color(0xFFC8732E), Color(0xFF5E8FC0), Color(0xFF7FA05A), Color(0xFFE9DFC6), Color(0xFF8E5CA8), Color(0xFFB8463A), Color(0xFFD9B44A))
    val main = mix(C.of(order.firstOrNull() ?: Kind.IDEA), wild[r.nextInt(wild.size)], .45f + r.nextFloat() * .25f)
    val band = C.of(order.getOrNull(1) ?: order.firstOrNull() ?: Kind.NOTICE)
    val dark = mix(main, Color(0xFF1A1410), .72f)
    val total = kinds.values.sum().coerceAtLeast(1)
    val spots = total.coerceAtMost(6)
    val fore = .8f + r.nextFloat() * .4f
    val tipRound = r.nextFloat()
    val hindLen = .5f + r.nextFloat() * .4f
    val tails = r.nextInt(2) == 0
    val bandAt = .45f + r.nextFloat() * .2f
    val s = span / 2
    val open = if (flap) .75f + .25f * sin(t * 6f) else 1f

    for (side in listOf(-1f, 1f)) {
        scale(side * open, 1f, center) {
            val c = center
            // Forewing.
            val f = Path().apply {
                moveTo(c.x + s * .04f, c.y - s * .1f)
                cubicTo(c.x + s * .3f, c.y - s * .9f * fore, c.x + s * (.85f + tipRound * .1f), c.y - s * 1.0f * fore, c.x + s * fore, c.y - s * (.7f - tipRound * .2f))
                cubicTo(c.x + s * (1.0f - tipRound * .1f), c.y - s * .25f, c.x + s * .6f, c.y + s * .05f, c.x + s * .06f, c.y + s * .04f)
                close()
            }
            // Hindwing.
            val h = Path().apply {
                moveTo(c.x + s * .05f, c.y + s * .02f)
                cubicTo(c.x + s * .65f, c.y + s * .02f, c.x + s * .8f, c.y + s * hindLen * .6f, c.x + s * .55f, c.y + s * hindLen)
                if (tails) { lineTo(c.x + s * .5f, c.y + s * (hindLen + .35f)); lineTo(c.x + s * .38f, c.y + s * hindLen * .95f) }
                cubicTo(c.x + s * .3f, c.y + s * hindLen * 1.05f, c.x + s * .12f, c.y + s * .5f, c.x + s * .04f, c.y + s * .15f)
                close()
            }
            drawPath(h, Brush.radialGradient(listOf(main, dark), c, s * 1.1f))
            drawPath(f, Brush.radialGradient(listOf(main, dark), c, s * 1.2f))
            // A band across the forewing.
            drawLine(band.copy(alpha = .85f), Offset(c.x + s * (bandAt - .1f), c.y - s * .75f * fore), Offset(c.x + s * (bandAt + .3f), c.y - s * .15f), s * .09f, StrokeCap.Round)
            // Veins.
            for (i in 0 until 4) {
                val a = Offset(c.x + s * (.3f + i * .17f) * fore, c.y - s * (.75f - i * .14f) * fore)
                drawLine(dark.copy(alpha = .6f), Offset(c.x + s * .06f, c.y), a, s * .012f)
            }
            drawPath(f, dark, style = Stroke(s * .025f))
            drawPath(h, dark, style = Stroke(s * .025f))
            // Eye-spots, one per thought.
            for (i in 0 until spots) {
                val onHind = i % 2 == 1
                val k = i / 2
                val p = if (onHind) Offset(c.x + s * (.3f + k * .12f), c.y + s * (.25f + k * .1f) * hindLen * 1.4f)
                else Offset(c.x + s * (.55f + k * .12f) * fore, c.y - s * (.55f - k * .12f) * fore)
                val rr = s * (.075f - k * .01f)
                drawCircle(Color(0xFF1A1410), rr * 1.35f, p)
                drawCircle(band, rr, p)
                drawCircle(Color.White.copy(alpha = .85f), rr * .35f, Offset(p.x - rr * .25f, p.y - rr * .25f))
            }
        }
    }
    // Body and feelers.
    drawLine(Color(0xFF1E1712), Offset(center.x, center.y - s * .28f), Offset(center.x, center.y + s * .45f), s * .09f, StrokeCap.Round)
    for (side in listOf(-1f, 1f)) {
        val tip = Offset(center.x + side * s * .32f, center.y - s * .72f)
        drawLine(Color(0xFF1E1712), Offset(center.x, center.y - s * .26f), tip, s * .02f)
        drawCircle(Color(0xFF1E1712), s * .035f, tip)
    }
}
