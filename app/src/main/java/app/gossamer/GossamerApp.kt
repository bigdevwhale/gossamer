package app.gossamer

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import app.gossamer.data.Chime
import app.gossamer.data.Cocoon
import app.gossamer.data.DemoSeed
import app.gossamer.data.Kind
import app.gossamer.data.SettingsStore
import app.gossamer.data.Strand
import app.gossamer.data.Thought
import app.gossamer.data.WebDb
import app.gossamer.domain.Web
import app.gossamer.widget.CatchWidget
import app.gossamer.widget.Shortcuts
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

/** A line shown for a few seconds at the top of the screen. */
data class Note(val title: String, val body: String = "", val stamp: Long = System.nanoTime())

/** What a catch did: the new thought, and the old one it echoed, if any. */
data class Caught(val thought: Thought, val echo: Thought?)

class AppContainer(context: Context) {
    val app: Context = context.applicationContext
    val db = WebDb.create(app)
    val web = db.web()
    val settings = SettingsStore(app)
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val notes = mutableStateListOf<Note>()

    /** True while the demo build spins its web, so the first screen doesn't jump. */
    val seeding = MutableStateFlow(BuildConfig.DEMO)

    /** The last thought caught or tied, for the spider to run to. */
    val focus = MutableStateFlow<Long?>(null)

    fun write(block: suspend AppContainer.() -> Unit) {
        scope.launch { block() }
    }

    fun note(title: String, body: String = "") {
        scope.launch(Dispatchers.Main) { notes.add(Note(title, body)) }
    }

    private suspend fun chime(high: Boolean) {
        if (settings.now().sound) Chime.play(high)
    }

    /** Sticks a new thought to the web. Almost the same thing caught before? It goes right next to it, on a thicker thread. */
    suspend fun catch(text: String, kind: Kind, now: Long = System.currentTimeMillis()): Caught {
        val clean = text.trim()
        val live = web.thoughtList().filter { it.live }
        val echo = Web.echoOf(clean, live)
        val id = web.insert(Thought(text = clean, kind = kind, at = now, night = Web.isNight(now)))
        if (echo != null) {
            web.tie(Strand.of(id, echo.id, now, echo = true))
            web.update(echo.copy(echoes = echo.echoes + 1, touched = now))
        }
        focus.value = id
        chime(true)
        refreshOutside()
        return Caught(web.thought(id)!!, echo)
    }

    suspend fun edit(t: Thought, text: String, kind: Kind) {
        web.update(t.copy(text = text.trim(), kind = kind, touched = System.currentTimeMillis()))
    }

    /** Reread: the wind lets go of it, and dew on it counts as touched. */
    suspend fun touch(id: Long, now: Long = System.currentTimeMillis()) {
        val t = web.thought(id) ?: return
        web.update(t.copy(touched = now))
        val s = settings.now()
        if (id in s.dew && id !in s.dewDone) settings.update { it.copy(dewDone = it.dewDone + id) }
    }

    suspend fun tie(a: Long, b: Long, now: Long = System.currentTimeMillis()): Boolean {
        if (a == b) return false
        val ok = web.tie(Strand.of(a, b, now)) > 0
        if (ok) {
            touch(a, now); touch(b, now)
            focus.value = b
            chime(false)
        }
        return ok
    }

    suspend fun untie(a: Long, b: Long) = web.untie(minOf(a, b), maxOf(a, b))

    /** Let the wind have it now: it goes to the Fluff, threads and all. */
    suspend fun blow(t: Thought) {
        web.untieAll(t.id)
        web.update(t.copy(gone = System.currentTimeMillis()))
        if (settings.now().lure == t.id) settings.update { it.copy(lure = null) }
        refreshOutside()
    }

    suspend fun restore(t: Thought) {
        web.update(t.copy(gone = null, touched = System.currentTimeMillis()))
        focus.value = t.id
        refreshOutside()
    }

    suspend fun forget(t: Thought) {
        web.untieAll(t.id)
        web.delete(t)
        settings.update { s -> s.copy(lure = s.lure.takeIf { it != t.id }, yearPicks = s.yearPicks - t.id, dew = s.dew - t.id) }
        refreshOutside()
    }

    suspend fun lure(id: Long?) {
        settings.update { it.copy(lure = id) }
        id?.let { touch(it) }
    }

    /**
     * What happens while you were away: the wind takes what hung untied too long, and a new morning
     * lays new dew. Returns how many thoughts the wind took.
     */
    suspend fun sweep(now: Long = System.currentTimeMillis()): Int {
        val s = settings.now()
        val all = web.thoughtList()
        val deg = Web.degrees(web.strandList())
        val blown = all.filter { Web.blows(it, deg[it.id] ?: 0, s.lure, s.windDays, now) }
        if (blown.isNotEmpty()) web.updateAll(blown.map { it.copy(gone = now) })
        val today = Web.day(now)
        if (s.dewDay != today) {
            val live = all.filter { it.live && it !in blown }
            settings.update { it.copy(dewDay = today, dew = Web.dew(live, today, now), dewDone = emptySet()) }
        }
        if (blown.isNotEmpty()) refreshOutside()
        return blown.size
    }

    /** Spins a cluster into a cocoon: the thoughts leave the web and ripen inside for three days. */
    suspend fun spin(ids: Set<Long>, name: String, now: Long = System.currentTimeMillis()): Long {
        val id = web.spin(Cocoon(name = name.trim(), at = now, ripe = now + Web.RIPEN, seed = Random.nextInt()))
        val all = web.thoughtList().filter { it.id in ids }
        web.updateAll(all.map { it.copy(cocoon = id, touched = now) })
        settings.update { s -> s.copy(lure = s.lure.takeIf { it !in ids }) }
        chime(false)
        refreshOutside()
        return id
    }

    suspend fun hatch(c: Cocoon, verdict: String, now: Long = System.currentTimeMillis()) {
        web.update(c.copy(hatched = now, verdict = verdict.trim()))
        chime(true)
    }

    /** Unspins a cocoon that never hatched: its thoughts go back on the web. */
    suspend fun unspin(c: Cocoon) {
        val now = System.currentTimeMillis()
        web.updateAll(web.thoughtList().filter { it.cocoon == c.id }.map { it.copy(cocoon = null, touched = now) })
        web.delete(c)
        refreshOutside()
    }

    suspend fun refreshOutside() {
        CatchWidget.refresh(app, web.liveCount())
    }
}

class GossamerApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        Shortcuts.publish(this)
        if (BuildConfig.DEMO) container.scope.launch { try { DemoSeed.seedIfEmpty(this@GossamerApp, container) } finally { container.seeding.value = false } }
    }
}

val Context.container: AppContainer get() = (applicationContext as GossamerApp).container

val Context.ru: Boolean get() = resources.configuration.locales[0].language == "ru"
