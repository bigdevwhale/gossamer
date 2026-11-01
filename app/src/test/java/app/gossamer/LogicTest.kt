package app.gossamer

import app.gossamer.data.Backup
import app.gossamer.data.Cocoon
import app.gossamer.data.DAY
import app.gossamer.data.Kind
import app.gossamer.data.Strand
import app.gossamer.data.Thought
import app.gossamer.domain.Web
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.ZoneId
import java.time.ZonedDateTime

@RunWith(RobolectricTestRunner::class)
class LogicTest {
    private val now = 1_800_000_000_000L
    private fun t(id: Long, daysAgo: Int, touchedDaysAgo: Int = daysAgo, text: String = "thought $id") =
        Thought(id = id, text = text, at = now - daysAgo * DAY, touched = now - touchedDaysAgo * DAY)

    @Test fun windTakesOnlyUntiedAndUntouched() {
        val old = t(1, 40)
        assertTrue(Web.blows(old, 0, null, 30, now))
        assertFalse(Web.blows(old, 1, null, 30, now))
        assertFalse(Web.blows(old, 0, 1L, 30, now))
        assertFalse(Web.blows(old, 0, null, 0, now))
        assertFalse(Web.blows(t(2, 40, touchedDaysAgo = 5), 0, null, 30, now))
        assertTrue(Web.trembles(t(3, 25), 0, null, 30, now))
        assertFalse(Web.trembles(t(4, 10), 0, null, 30, now))
        assertEquals(5, Web.daysLeft(t(5, 25), 0, null, 30, now))
        assertNull(Web.daysLeft(t(6, 25), 2, null, 30, now))
    }

    @Test fun dewIsStableForTheDayAndSkipsFreshThoughts() {
        val live = (1L..20L).map { t(it, it.toInt()) }
        val a = Web.dew(live, 100, now)
        assertEquals(3, a.size)
        assertEquals(a, Web.dew(live, 100, now))
        assertTrue(a.none { it < 3 })
        assertTrue(Web.dew(live.take(2), 100, now).isEmpty())
    }

    @Test fun echoesCatchRepeatsInBothLanguages() {
        assertTrue(Web.similar("Tours at dusk, when the windows light up", "Tours at dusk, when windows light up one by one"))
        assertTrue(Web.similar("Экскурсии в сумерках, когда загораются окна", "Экскурсии в сумерках, когда окна загораются одно за другим"))
        assertTrue(Web.similar("buy milk", "Buy milk!"))
        assertFalse(Web.similar("A bakery that sells old bread", "Tours at dusk, when the windows light up"))
        assertFalse(Web.similar("Почему я устаю?", "Когда я отдыхаю?"))
    }

    @Test fun clustersFollowThreads() {
        val s = listOf(Strand.of(1, 2, 0), Strand.of(2, 3, 0), Strand.of(4, 5, 0))
        assertEquals(setOf(1L, 2L, 3L), Web.cluster(1, setOf(1, 2, 3, 4, 5), s))
        assertEquals(setOf(1L, 2L), Web.cluster(1, setOf(1, 2, 4, 5), s))
        assertEquals(mapOf(1L to 1, 2L to 2, 3L to 1, 4L to 1, 5L to 1), Web.degrees(s))
    }

    @Test fun layoutKeepsEveryThoughtOnTheWebAndHubsInside() {
        val live = (1L..60L).map { t(it, 60 - it.toInt()) }
        val s = (2L..6L).map { Strand.of(1, it, 0) }
        val spots = Web.layout(live, s)
        assertEquals(60, spots.size)
        assertTrue(spots.values.all { it.r in 0f..1f && it.spoke in 0 until Web.SPOKES })
        val hub = spots.getValue(1)
        assertTrue(hub.r < spots.filterKeys { it != 1L }.values.minOf { it.r })
    }

    @Test fun nightIsAfterMidnight() {
        val z = ZoneId.of("Europe/Moscow")
        assertTrue(Web.isNight(ZonedDateTime.of(2026, 11, 1, 2, 30, 0, 0, z).toInstant().toEpochMilli(), z))
        assertFalse(Web.isNight(ZonedDateTime.of(2026, 11, 1, 23, 30, 0, 0, z).toInstant().toEpochMilli(), z))
        assertEquals(Web.Season.INDIAN, Web.season(ZonedDateTime.of(2026, 9, 10, 12, 0, 0, 0, z).toInstant().toEpochMilli(), z))
    }

    @Test fun backupRoundTrips() {
        val d = Backup.Dump(
            listOf(Thought(1, "one\nline", Kind.QUESTION, 10, 20, true, null, 7, 2), Thought(2, "«two»", Kind.WORDS, 11, gone = 30)),
            listOf(Strand.of(2, 1, 12, echo = true)),
            listOf(Cocoon(7, "Name", 5, 6, 8, "So", 42)),
        )
        val back = Backup.parse(Backup.json(d))
        assertEquals(d.thoughts, back.thoughts)
        assertEquals(d.strands, back.strands)
        assertEquals(d.cocoons, back.cocoons)
        val md = Backup.markdown(d, "Gossamer", Backup.Labels("Web", "Fluff", "tied to") { it.name })
        assertTrue(md.contains("## Name") && md.contains("> So") && md.contains("## Fluff"))
    }
}
