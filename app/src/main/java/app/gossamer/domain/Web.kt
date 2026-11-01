package app.gossamer.domain

import app.gossamer.data.DAY
import app.gossamer.data.Strand
import app.gossamer.data.Thought
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/** The web's rules, kept apart from the screens so they can be tested. */
object Web {
    const val SPOKES = 14
    const val DEW = 3
    const val DEW_MIN_AGE = 3 * DAY
    const val COCOON_MIN = 5
    const val RIPEN = 3 * DAY
    const val HUB = 3
    const val YEAR_PICKS = 12
    /** At most this many thoughts go into one cocoon. */
    const val COCOON_MAX = 9

    fun day(at: Long, zone: ZoneId = ZoneId.systemDefault()): Long = Instant.ofEpochMilli(at).atZone(zone).toLocalDate().toEpochDay()
    fun hour(at: Long, zone: ZoneId = ZoneId.systemDefault()): Int = Instant.ofEpochMilli(at).atZone(zone).hour
    fun month(at: Long, zone: ZoneId = ZoneId.systemDefault()): Int = Instant.ofEpochMilli(at).atZone(zone).monthValue
    fun year(at: Long, zone: ZoneId = ZoneId.systemDefault()): Int = Instant.ofEpochMilli(at).atZone(zone).year

    /** Caught between midnight and five: a night thought. */
    fun isNight(at: Long, zone: ZoneId = ZoneId.systemDefault()) = hour(at, zone) < 5

    fun degrees(strands: List<Strand>): Map<Long, Int> {
        val m = HashMap<Long, Int>()
        for (s in strands) { m[s.a] = (m[s.a] ?: 0) + 1; m[s.b] = (m[s.b] ?: 0) + 1 }
        return m
    }

    /** Everything reachable from [id] along threads, among [among]. */
    fun cluster(id: Long, among: Set<Long>, strands: List<Strand>): Set<Long> {
        if (id !in among) return emptySet()
        val next = HashMap<Long, MutableList<Long>>()
        for (s in strands) if (s.a in among && s.b in among) {
            next.getOrPut(s.a) { mutableListOf() }.add(s.b)
            next.getOrPut(s.b) { mutableListOf() }.add(s.a)
        }
        val seen = linkedSetOf(id)
        val queue = ArrayDeque(listOf(id))
        while (queue.isNotEmpty()) for (n in next[queue.removeFirst()].orEmpty()) if (seen.add(n)) queue.add(n)
        return seen
    }

    /** When the wind starts to tug at an untied thought: at two thirds of its time. */
    fun trembles(t: Thought, degree: Int, lure: Long?, windDays: Int, now: Long): Boolean =
        windDays > 0 && t.live && degree == 0 && t.id != lure && now - t.touched > windDays * DAY * 2 / 3

    fun blows(t: Thought, degree: Int, lure: Long?, windDays: Int, now: Long): Boolean =
        windDays > 0 && t.live && degree == 0 && t.id != lure && now - t.touched > windDays * DAY

    /** Days left before the wind takes it, or null if it is tied or there is no wind. */
    fun daysLeft(t: Thought, degree: Int, lure: Long?, windDays: Int, now: Long): Int? {
        if (windDays <= 0 || degree > 0 || t.id == lure || !t.live) return null
        return max(0, ((t.touched + windDays * DAY - now + DAY - 1) / DAY).toInt())
    }

    /**
     * The morning's dew: [DEW] live thoughts at least three days old, drawn from the ones least recently
     * touched, the same all day long ([day] seeds the draw).
     */
    fun dew(live: List<Thought>, day: Long, now: Long, n: Int = DEW): List<Long> {
        val old = live.filter { now - it.at >= DEW_MIN_AGE }.sortedBy { it.touched }
        val pool = old.take(max(n * 4, 12))
        return pool.shuffled(Random(day * 31 + 7)).take(n).map { it.id }
    }

    // --- echoes -----------------------------------------------------------------------------------

    private val STOP = setOf(
        "this", "that", "with", "from", "have", "what", "when", "they", "them", "there", "their", "about", "would", "could",
        "should", "which", "into", "just", "like", "more", "some", "than", "then", "will", "your", "been", "were", "also",
        "если", "когда", "чтобы", "этот", "этого", "этом", "очень", "можно", "нужно", "тоже", "только", "потом", "почему",
        "который", "которые", "какой", "какая", "просто", "будет", "было", "была", "были", "есть", "себя", "свой", "своё",
        "сейчас", "всегда", "здесь", "тогда", "между", "через", "после", "перед", "надо", "хочу", "может",
    )

    /** The words that carry a thought: four letters or more, cut to a stem of five, minus the glue words. */
    fun words(text: String): Set<String> =
        text.lowercase().replace('ё', 'е').split(Regex("[^\\p{L}\\p{N}]+"))
            .filter { it.length >= 4 && it !in STOP }
            .map { it.take(5) }
            .toSet()

    fun similar(a: String, b: String): Boolean {
        val na = a.lowercase().replace(Regex("[^\\p{L}\\p{N}]+"), " ").trim()
        val nb = b.lowercase().replace(Regex("[^\\p{L}\\p{N}]+"), " ").trim()
        if (na.isNotEmpty() && na == nb) return true
        val wa = words(a)
        val wb = words(b)
        val shared = wa.intersect(wb).size
        if (shared < 2) return false
        return shared.toDouble() / wa.union(wb).size >= .5
    }

    /** The live thought this new text echoes, the latest one if several do. */
    fun echoOf(text: String, live: List<Thought>): Thought? = live.lastOrNull { similar(text, it.text) }

    // --- the picture --------------------------------------------------------------------------------

    /** Where a thought hangs: on which spoke and how far from the hub (0 the hub, 1 the rim). */
    data class Spot(val spoke: Int, val r: Float, val wobble: Float)

    /**
     * Thoughts go round the spokes in order of arrival, five spokes apart, winding outwards like the
     * capture spiral: the oldest near the hub, the newest at the rim. A thought keeps its spoke for
     * good; one with [HUB] threads or more is pulled in along it, close to the hub, so tying never
     * reshuffles the rest of the web.
     */
    fun layout(live: List<Thought>, strands: List<Strand>): Map<Long, Spot> {
        val deg = degrees(strands)
        val sorted = live.sortedBy { it.at }
        val n = sorted.size
        val out = LinkedHashMap<Long, Spot>()
        sorted.forEachIndexed { i, t ->
            val f = if (n == 1) .5f else i.toFloat() / (n - 1)
            val r = .3f + .63f * Math.pow(f.toDouble(), .85).toFloat()
            val hub = (deg[t.id] ?: 0) >= HUB
            out[t.id] = Spot((i * 5 + 2) % SPOKES, if (hub) .14f + r * .2f else r, wob(t.id))
        }
        return out
    }

    private fun wob(id: Long): Float = ((id * 2654435761L) ushr 8 and 0xFF).toFloat() / 255f - .5f

    // --- time of day and season -------------------------------------------------------------------

    enum class Light { DAWN, DAY, DUSK, NIGHT }

    fun light(at: Long, zone: ZoneId = ZoneId.systemDefault()): Light = when (hour(at, zone)) {
        in 5..7 -> Light.DAWN
        in 8..17 -> Light.DAY
        in 18..21 -> Light.DUSK
        else -> Light.NIGHT
    }

    enum class Season { WINTER, SPRING, SUMMER, AUTUMN, INDIAN }

    /** September is Indian summer: gossamer flies, and the year's twelve thoughts can be picked. */
    fun season(at: Long, zone: ZoneId = ZoneId.systemDefault()): Season = when (month(at, zone)) {
        12, 1, 2 -> Season.WINTER
        3, 4, 5 -> Season.SPRING
        6, 7, 8 -> Season.SUMMER
        9 -> Season.INDIAN
        else -> Season.AUTUMN
    }

    fun today(zone: ZoneId = ZoneId.systemDefault()): Long = LocalDate.now(zone).toEpochDay()
}
