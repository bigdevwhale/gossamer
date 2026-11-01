package app.gossamer.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import app.gossamer.CatchActivity
import app.gossamer.R
import app.gossamer.container
import kotlinx.coroutines.launch

/** A strip on the home screen: tap to write a thought, the microphone to say it. */
class CatchWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        val c = context.container
        val pending = goAsync()
        c.scope.launch {
            try { draw(context, manager, ids, c.web.liveCount()) } finally { pending.finish() }
        }
    }

    companion object {
        fun refresh(context: Context, count: Int) {
            val m = AppWidgetManager.getInstance(context)
            val ids = m.getAppWidgetIds(ComponentName(context, CatchWidget::class.java))
            if (ids.isNotEmpty()) draw(context, m, ids, count)
        }

        private fun draw(context: Context, m: AppWidgetManager, ids: IntArray, count: Int) {
            val v = RemoteViews(context.packageName, R.layout.widget_catch)
            v.setTextViewText(R.id.w_count, context.resources.getQuantityString(R.plurals.on_web, count, count))
            v.setOnClickPendingIntent(R.id.w_root, pending(context, 1, voice = false))
            v.setOnClickPendingIntent(R.id.w_voice, pending(context, 2, voice = true))
            m.updateAppWidget(ids, v)
        }

        fun pending(context: Context, code: Int, voice: Boolean): PendingIntent =
            PendingIntent.getActivity(context, code, CatchActivity.intent(context, voice), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
}
