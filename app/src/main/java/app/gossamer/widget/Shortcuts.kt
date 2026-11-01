package app.gossamer.widget

import android.content.Context
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import app.gossamer.CatchActivity
import app.gossamer.R

/** Long-press the icon: «Catch a thought» and «Say it». Dynamic, so they point at whichever build is installed. */
object Shortcuts {
    fun publish(context: Context) {
        runCatching {
            val write = ShortcutInfoCompat.Builder(context, "catch")
                .setShortLabel(context.getString(R.string.sc_catch))
                .setIcon(IconCompat.createWithResource(context, R.drawable.ic_sc_write))
                .setIntent(CatchActivity.intent(context, voice = false))
                .build()
            val say = ShortcutInfoCompat.Builder(context, "say")
                .setShortLabel(context.getString(R.string.sc_say))
                .setIcon(IconCompat.createWithResource(context, R.drawable.ic_sc_voice))
                .setIntent(CatchActivity.intent(context, voice = true))
                .build()
            ShortcutManagerCompat.setDynamicShortcuts(context, listOf(write, say))
        }
    }
}
