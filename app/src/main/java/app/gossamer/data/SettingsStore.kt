package app.gossamer.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("settings")

data class Settings(
    val onboarded: Boolean = false,
    val started: Long = 0,
    /** Days an untied, untouched thought hangs before the wind takes it; 0 = no wind. */
    val windDays: Int = 30,
    /** The morning the dew was last laid ([java.time.LocalDate.toEpochDay]), on which thoughts, and which were touched. */
    val dewDay: Long = -1,
    val dew: List<Long> = emptyList(),
    val dewDone: Set<Long> = emptySet(),
    /** The open question hung as a lure; evening catches are offered to it. */
    val lure: Long? = null,
    val lureAsked: Long = -1,
    /** The morning the night catch was shown. */
    val nightSeen: Long = -1,
    /** The year's flying gossamer: up to twelve thoughts picked in September, per year. */
    val year: Int = 0,
    val yearPicks: List<Long> = emptyList(),
    val sound: Boolean = true,
)

class SettingsStore(context: Context) {
    private val store = context.applicationContext.dataStore

    val settings: Flow<Settings> = store.data.map { it.read() }

    private fun ids(s: String?) = s?.split(',')?.mapNotNull { it.toLongOrNull() }.orEmpty()

    private fun Preferences.read() = Settings(
        onboarded = this[ONBOARDED] ?: false,
        started = this[STARTED] ?: 0L,
        windDays = this[WIND] ?: 30,
        dewDay = this[DEW_DAY] ?: -1L,
        dew = ids(this[DEW]),
        dewDone = ids(this[DEW_DONE]).toSet(),
        lure = this[LURE]?.takeIf { it > 0 },
        lureAsked = this[LURE_ASKED] ?: -1L,
        nightSeen = this[NIGHT_SEEN] ?: -1L,
        year = this[YEAR] ?: 0,
        yearPicks = ids(this[YEAR_PICKS]),
        sound = this[SOUND] ?: true,
    )

    suspend fun now(): Settings = settings.first()

    suspend fun update(change: (Settings) -> Settings) {
        store.edit {
            val s = change(it.read())
            it[ONBOARDED] = s.onboarded
            it[STARTED] = s.started
            it[WIND] = s.windDays
            it[DEW_DAY] = s.dewDay
            it[DEW] = s.dew.joinToString(",")
            it[DEW_DONE] = s.dewDone.joinToString(",")
            it[LURE] = s.lure ?: 0L
            it[LURE_ASKED] = s.lureAsked
            it[NIGHT_SEEN] = s.nightSeen
            it[YEAR] = s.year
            it[YEAR_PICKS] = s.yearPicks.joinToString(",")
            it[SOUND] = s.sound
        }
    }

    private companion object {
        val ONBOARDED = booleanPreferencesKey("onboarded")
        val STARTED = longPreferencesKey("started")
        val WIND = intPreferencesKey("wind")
        val DEW_DAY = longPreferencesKey("dew_day")
        val DEW = stringPreferencesKey("dew")
        val DEW_DONE = stringPreferencesKey("dew_done")
        val LURE = longPreferencesKey("lure")
        val LURE_ASKED = longPreferencesKey("lure_asked")
        val NIGHT_SEEN = longPreferencesKey("night_seen")
        val YEAR = intPreferencesKey("year")
        val YEAR_PICKS = stringPreferencesKey("year_picks")
        val SOUND = booleanPreferencesKey("sound")
    }
}
