package app.gossamer.ui

import app.gossamer.data.Cocoon
import app.gossamer.data.Kind
import app.gossamer.data.Settings
import app.gossamer.data.Strand
import app.gossamer.data.Thought
import app.gossamer.domain.Web

/** Everything the screens read, worked out once per change. */
class Home(val thoughts: List<Thought>, val strands: List<Strand>, val cocoons: List<Cocoon>, val s: Settings, val now: Long) {
    val byId = thoughts.associateBy { it.id }
    val live = thoughts.filter { it.live }
    val liveIds = live.map { it.id }.toSet()
    val fluff = thoughts.filter { it.gone != null && it.cocoon == null }.sortedByDescending { it.gone }
    val liveStrands = strands.filter { it.a in liveIds && it.b in liveIds }
    val deg = Web.degrees(liveStrands)
    val spots = Web.layout(live, liveStrands)
    val today = Web.day(now)
    val hour = Web.hour(now)
    val light = Web.light(now)
    val season = Web.season(now)

    fun degree(id: Long) = deg[id] ?: 0
    fun threads(id: Long): List<Thought> = liveStrands.filter { it.has(id) }.mapNotNull { byId[it.other(id)] }
    fun strand(a: Long, b: Long) = liveStrands.firstOrNull { it.has(a) && it.has(b) }
    fun cluster(id: Long) = Web.cluster(id, liveIds, liveStrands)
    fun canSpin(id: Long) = cluster(id).size >= Web.COCOON_MIN
    fun trembles(t: Thought) = Web.trembles(t, degree(t.id), s.lure, s.windDays, now)
    fun daysLeft(t: Thought) = Web.daysLeft(t, degree(t.id), s.lure, s.windDays, now)

    /** Today's dew still on the web, not yet touched. */
    val dew = if (s.dewDay == today) s.dew.filter { it in liveIds && it !in s.dewDone } else emptyList()

    val lure = s.lure?.let { byId[it] }?.takeIf { it.live }

    /** In the evening, what was caught today and could hang off the lure. */
    val lureOffer: List<Thought> = if (lure == null || hour < 18 || s.lureAsked == today) emptyList()
    else live.filter { Web.day(it.at) == today && it.id != lure.id && strand(it.id, lure.id) == null }

    /** In the morning, what was caught in the night. */
    val nightCatch: List<Thought> = if (hour !in 5..13 || s.nightSeen == today) emptyList()
    else live.filter { it.night && Web.day(it.at) == today }

    val ripe = cocoons.filter { it.hatched == null && it.ripe <= now }
    val ripening = cocoons.filter { it.hatched == null && it.ripe > now }
    val butterflies = cocoons.filter { it.hatched != null }.sortedByDescending { it.hatched }
    fun inside(c: Cocoon) = thoughts.filter { it.cocoon == c.id }.sortedBy { it.at }
    fun kinds(c: Cocoon): Map<Kind, Int> = inside(c).groupingBy { it.kind }.eachCount()
    fun cocoon(id: Long) = cocoons.firstOrNull { it.id == id }

    /** September: the year's twelve can be picked. */
    val indian = season == Web.Season.INDIAN
    val yearNow = Web.year(now)
    val yearPicks = if (s.year == yearNow) s.yearPicks.mapNotNull { byId[it] } else emptyList()
    val yearPool = thoughts.filter { now - it.at < 365L * app.gossamer.data.DAY }.sortedByDescending { it.at }

    val hubs = live.filter { degree(it.id) >= Web.HUB }
}
