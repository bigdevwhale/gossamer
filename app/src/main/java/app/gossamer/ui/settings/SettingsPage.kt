package app.gossamer.ui.settings

import android.app.StatusBarManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import app.gossamer.BuildConfig
import app.gossamer.R
import app.gossamer.container
import app.gossamer.data.Backup
import app.gossamer.data.Settings
import app.gossamer.ui.Nav
import app.gossamer.ui.catching.kindName
import app.gossamer.ui.components.Btn
import app.gossamer.ui.components.Card
import app.gossamer.ui.components.Chip
import app.gossamer.ui.components.Flow
import app.gossamer.ui.components.Kind
import app.gossamer.ui.components.Page
import app.gossamer.ui.components.Section
import app.gossamer.ui.theme.C
import app.gossamer.ui.theme.T
import app.gossamer.widget.CatchTile
import app.gossamer.widget.CatchWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** The StillCraft bot: deep links file a bug or an idea for this app, or follow its releases. */
object Community {
    private const val BOT = "https://t.me/ArcaneWhisperBot?start="
    const val SLUG = "gossamer"
    const val BUG = "${BOT}bug_$SLUG"
    const val IDEA = "${BOT}wish_$SLUG"
    const val FOLLOW = "${BOT}f_$SLUG"
    const val CHAT = "https://t.me/+Q4gFEDk8O842OGVi"
}

@Composable
fun SettingsPage(s: Settings, nav: Nav) {
    val ctx = LocalContext.current
    val c = ctx.container
    fun open(url: String) { runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } }
    suspend fun dump() = Backup.Dump(c.web.thoughtList(), c.web.strandList(), c.web.cocoonList())
    val saveJson = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        c.write {
            val ok = runCatching { withContext(Dispatchers.IO) { app.contentResolver.openOutputStream(uri)?.use { it.write(Backup.json(dump()).toByteArray()) } } }.isSuccess
            note(ctx.getString(if (ok) R.string.n_saved else R.string.n_failed))
        }
    }
    val saveMd = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/markdown")) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        c.write {
            val labels = Backup.Labels(ctx.getString(R.string.tab_web), ctx.getString(R.string.fluff), ctx.getString(R.string.md_tied)) { ctx.getString(kindName(it)) }
            val ok = runCatching { withContext(Dispatchers.IO) { app.contentResolver.openOutputStream(uri)?.use { it.write(Backup.markdown(dump(), ctx.getString(R.string.app_title), labels).toByteArray()) } } }.isSuccess
            note(ctx.getString(if (ok) R.string.n_saved else R.string.n_failed))
        }
    }
    val load = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        c.write {
            val n = runCatching {
                val text = withContext(Dispatchers.IO) { app.contentResolver.openInputStream(uri)?.use { it.readBytes().decodeToString() } } ?: error("empty")
                Backup.restore(web, Backup.parse(text))
            }.getOrNull()
            if (n == null) note(ctx.getString(R.string.n_bad_backup)) else note(ctx.resources.getQuantityString(R.plurals.n_restored, n, n))
            refreshOutside()
        }
    }

    Page(stringResource(R.string.settings), nav::close) {
        Section(stringResource(R.string.set_wind))
        Text(stringResource(R.string.set_wind_b), style = T.Small)
        Flow {
            for (d in listOf(14, 30, 60, 90)) Chip(stringResource(R.string.days_n, d), s.windDays == d, { c.write { settings.update { it.copy(windDays = d) } } })
            Chip(stringResource(R.string.wind_off), s.windDays == 0, { c.write { settings.update { it.copy(windDays = 0) } } })
        }

        Section(stringResource(R.string.set_quick))
        Text(stringResource(R.string.set_quick_b), style = T.Small)
        val wm = ctx.getSystemService(AppWidgetManager::class.java)
        if (wm?.isRequestPinAppWidgetSupported == true) Btn(stringResource(R.string.add_widget), {
            wm.requestPinAppWidget(ComponentName(ctx, CatchWidget::class.java), null, null)
            c.note(ctx.getString(R.string.widget_asked), ctx.getString(R.string.widget_asked_b))
        }, Modifier.fillMaxWidth(), kind = Kind.PLAIN)
        if (Build.VERSION.SDK_INT >= 33) Btn(stringResource(R.string.add_tile), {
            val sb = ctx.getSystemService(StatusBarManager::class.java)
            sb?.requestAddTileService(ComponentName(ctx, CatchTile::class.java), ctx.getString(R.string.tile_label), Icon.createWithResource(ctx, R.drawable.ic_tile), ctx.mainExecutor) {}
        }, Modifier.fillMaxWidth(), kind = Kind.PLAIN)

        Section(stringResource(R.string.set_sound))
        Flow {
            Chip(stringResource(R.string.on), s.sound, { c.write { settings.update { it.copy(sound = true) } } })
            Chip(stringResource(R.string.off), !s.sound, { c.write { settings.update { it.copy(sound = false) } } })
        }

        Section(stringResource(R.string.set_lang))
        val cur = AppCompatDelegate.getApplicationLocales().toLanguageTags().substringBefore('-').substringBefore(',')
        Flow {
            for ((tag, l) in listOf("" to R.string.lang_system, "ru" to R.string.lang_ru, "en" to R.string.lang_en))
                Chip(stringResource(l), cur == tag, { AppCompatDelegate.setApplicationLocales(if (tag.isEmpty()) LocaleListCompat.getEmptyLocaleList() else LocaleListCompat.forLanguageTags(tag)) })
        }

        Section(stringResource(R.string.set_files))
        Text(stringResource(R.string.set_files_b), style = T.Small)
        Btn(stringResource(R.string.export_md), { saveMd.launch("gossamer.md") }, Modifier.fillMaxWidth(), kind = Kind.PLAIN)
        Btn(stringResource(R.string.export_json), { saveJson.launch("gossamer-backup.json") }, Modifier.fillMaxWidth(), kind = Kind.PLAIN)
        Btn(stringResource(R.string.import_json), { load.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) }, Modifier.fillMaxWidth(), kind = Kind.QUIET)

        Section(stringResource(R.string.community))
        Text(stringResource(R.string.community_text), style = T.Small)
        for ((label, sub, url) in listOf(
            Triple(R.string.community_bug, R.string.community_bug_sub, Community.BUG),
            Triple(R.string.community_idea, R.string.community_idea_sub, Community.IDEA),
            Triple(R.string.community_follow, R.string.community_follow_sub, Community.FOLLOW),
            Triple(R.string.community_chat, R.string.community_chat_sub, Community.CHAT),
        )) Card(Modifier.fillMaxWidth(), onClick = { open(url) }, pad = 14.dp) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) { Text(stringResource(label), style = T.Body); Text(stringResource(sub), style = T.Small) }
                Text("›", style = T.Heading.copy(color = C.amber))
            }
        }

        Section(stringResource(R.string.about))
        Text(stringResource(R.string.about_text, BuildConfig.VERSION_NAME), style = T.Small)
        Btn(stringResource(R.string.source), { open("https://github.com/bigdevwhale/gossamer") }, kind = Kind.QUIET)
    }
}
