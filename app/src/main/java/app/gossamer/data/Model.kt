package app.gossamer.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

const val MINUTE = 60_000L
const val HOUR = 60 * MINUTE
const val DAY = 24 * HOUR

/** What kind of fluff a thought is; each is drawn as its own little thing caught in the web. */
enum class Kind {
    /** A firefly. */
    IDEA,
    /** A burr with a hook: it clings until answered. */
    QUESTION,
    /** Dandelion down: something seen or heard. */
    NOTICE,
    /** A wisp of someone else's silk: a quote, a phrase overheard. */
    WORDS,
}

/**
 * One caught thought.
 * [touched] moves whenever it is reread, tied or dewed; the wind looks at it, not at [at].
 * [gone] is set when the wind blew it out of the window into the Fluff; [cocoon] when it was spun into one.
 */
@Entity(tableName = "thoughts")
data class Thought(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val kind: Kind = Kind.IDEA,
    val at: Long,
    val touched: Long = at,
    /** Caught between midnight and five in the morning: it glows. */
    val night: Boolean = false,
    val gone: Long? = null,
    val cocoon: Long? = null,
    /** How many times something almost the same was caught again. */
    val echoes: Int = 0,
) {
    val live: Boolean get() = gone == null && cocoon == null
}

/** A thread you pulled between two thoughts; [a] < [b]. An [echo] thread was tied by the spider on a repeat. */
@Entity(tableName = "strands", indices = [Index(value = ["a", "b"], unique = true)])
data class Strand(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val a: Long,
    val b: Long,
    val at: Long,
    val echo: Boolean = false,
) {
    fun other(id: Long) = if (a == id) b else a
    fun has(id: Long) = a == id || b == id

    companion object {
        fun of(x: Long, y: Long, at: Long, echo: Boolean = false) = Strand(a = minOf(x, y), b = maxOf(x, y), at = at, echo = echo)
    }
}

/**
 * A cluster of tied thoughts spun into a cocoon under a name. It ripens by [ripe]; opened with one
 * sentence ([verdict]), it lets out a butterfly drawn from [seed] and the kinds inside.
 */
@Entity(tableName = "cocoons")
data class Cocoon(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val at: Long,
    val ripe: Long,
    val hatched: Long? = null,
    val verdict: String = "",
    val seed: Int,
)
