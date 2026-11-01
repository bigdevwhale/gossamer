package app.gossamer.data

import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** The whole web as a file: JSON to move it to another phone, Markdown to read it anywhere. */
object Backup {
    data class Dump(val thoughts: List<Thought>, val strands: List<Strand>, val cocoons: List<Cocoon>)

    fun json(d: Dump): String {
        val o = JSONObject().put("app", "gossamer").put("version", 1)
        o.put("thoughts", JSONArray().apply {
            for (t in d.thoughts) put(JSONObject().put("id", t.id).put("text", t.text).put("kind", t.kind.name).put("at", t.at).put("touched", t.touched)
                .put("night", t.night).put("gone", t.gone ?: JSONObject.NULL).put("cocoon", t.cocoon ?: JSONObject.NULL).put("echoes", t.echoes))
        })
        o.put("strands", JSONArray().apply { for (s in d.strands) put(JSONObject().put("a", s.a).put("b", s.b).put("at", s.at).put("echo", s.echo)) })
        o.put("cocoons", JSONArray().apply {
            for (c in d.cocoons) put(JSONObject().put("id", c.id).put("name", c.name).put("at", c.at).put("ripe", c.ripe).put("hatched", c.hatched ?: JSONObject.NULL).put("verdict", c.verdict).put("seed", c.seed))
        })
        return o.toString(1)
    }

    private fun JSONObject.longOrNull(k: String): Long? = if (isNull(k) || !has(k)) null else getLong(k)

    fun parse(text: String): Dump {
        val o = JSONObject(text)
        require(o.optString("app") == "gossamer") { "not a gossamer backup" }
        val th = o.getJSONArray("thoughts")
        val thoughts = (0 until th.length()).map { i ->
            val j = th.getJSONObject(i)
            Thought(
                id = j.getLong("id"), text = j.getString("text"), kind = runCatching { Kind.valueOf(j.getString("kind")) }.getOrDefault(Kind.IDEA),
                at = j.getLong("at"), touched = j.optLong("touched", j.getLong("at")), night = j.optBoolean("night"),
                gone = j.longOrNull("gone"), cocoon = j.longOrNull("cocoon"), echoes = j.optInt("echoes"),
            )
        }
        val st = o.optJSONArray("strands") ?: JSONArray()
        val strands = (0 until st.length()).map { i -> val j = st.getJSONObject(i); Strand.of(j.getLong("a"), j.getLong("b"), j.getLong("at"), j.optBoolean("echo")) }
        val cc = o.optJSONArray("cocoons") ?: JSONArray()
        val cocoons = (0 until cc.length()).map { i ->
            val j = cc.getJSONObject(i)
            Cocoon(id = j.getLong("id"), name = j.getString("name"), at = j.getLong("at"), ripe = j.getLong("ripe"), hatched = j.longOrNull("hatched"), verdict = j.optString("verdict"), seed = j.optInt("seed"))
        }
        return Dump(thoughts, strands, cocoons)
    }

    /** Adds a backup to what is already here, giving everything new ids. Returns how many thoughts came in. */
    suspend fun restore(dao: WebDao, d: Dump): Int {
        val existing = dao.thoughtList().map { it.text to it.at }.toSet()
        val cocoonIds = HashMap<Long, Long>()
        for (c in d.cocoons) cocoonIds[c.id] = dao.spin(c.copy(id = 0))
        val ids = HashMap<Long, Long>()
        for (t in d.thoughts) {
            if ((t.text to t.at) in existing) continue
            ids[t.id] = dao.insert(t.copy(id = 0, cocoon = t.cocoon?.let { cocoonIds[it] }))
        }
        for (s in d.strands) {
            val a = ids[s.a] ?: continue
            val b = ids[s.b] ?: continue
            dao.tie(Strand.of(a, b, s.at, s.echo))
        }
        return ids.size
    }

    fun markdown(d: Dump, title: String, labels: Labels): String {
        val f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault())
        val byId = d.thoughts.associateBy { it.id }
        val sb = StringBuilder("# $title\n\n")
        fun line(t: Thought) {
            sb.append("- **").append(labels.kind(t.kind)).append("** · ").append(f.format(Instant.ofEpochMilli(t.at))).append("  \n  ").append(t.text.replace("\n", "  \n  ")).append('\n')
            val tied = d.strands.filter { it.has(t.id) }.mapNotNull { byId[it.other(t.id)] }
            if (tied.isNotEmpty()) sb.append("  ").append(labels.tied).append(": ").append(tied.joinToString("; ") { "«" + it.text.take(60) + "»" }).append('\n')
        }
        sb.append("## ").append(labels.web).append("\n\n")
        d.thoughts.filter { it.live }.sortedBy { it.at }.forEach(::line)
        for (c in d.cocoons) {
            sb.append("\n## ").append(c.name).append('\n')
            if (c.hatched != null) sb.append("\n> ").append(c.verdict).append("\n\n") else sb.append('\n')
            d.thoughts.filter { it.cocoon == c.id }.forEach(::line)
        }
        val fluff = d.thoughts.filter { it.gone != null && it.cocoon == null }
        if (fluff.isNotEmpty()) { sb.append("\n## ").append(labels.fluff).append("\n\n"); fluff.forEach(::line) }
        return sb.toString()
    }

    class Labels(val web: String, val fluff: String, val tied: String, val kind: (Kind) -> String)
}
