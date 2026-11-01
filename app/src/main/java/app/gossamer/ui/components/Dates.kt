package app.gossamer.ui.components

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import app.gossamer.R
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** «today, 14:20», «yesterday, 9:05», «3 Nov», «3 Nov 2025». */
fun whenText(ctx: Context, at: Long, now: Long): String {
    val zone = ZoneId.systemDefault()
    val d = Instant.ofEpochMilli(at).atZone(zone)
    val n = Instant.ofEpochMilli(now).atZone(zone)
    val loc: Locale = ctx.resources.configuration.locales[0]
    val time = d.format(DateTimeFormatter.ofPattern("H:mm", loc))
    val days = n.toLocalDate().toEpochDay() - d.toLocalDate().toEpochDay()
    return when {
        days == 0L -> ctx.getString(R.string.d_today, time)
        days == 1L -> ctx.getString(R.string.d_yesterday, time)
        d.year == n.year -> d.format(DateTimeFormatter.ofPattern("d MMM", loc)).trimEnd('.')
        else -> d.format(DateTimeFormatter.ofPattern("d MMM yyyy", loc))
    }
}

@Composable
fun when_(at: Long, now: Long): String = whenText(LocalContext.current, at, now)
