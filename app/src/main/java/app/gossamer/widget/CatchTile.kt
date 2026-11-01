package app.gossamer.widget

import android.annotation.SuppressLint
import android.os.Build
import android.service.quicksettings.TileService
import app.gossamer.CatchActivity

/** A tile in the quick settings: pull down the shade, tap, write. */
class CatchTile : TileService() {
    @SuppressLint("StartActivityAndCollapseDeprecated")
    override fun onClick() {
        super.onClick()
        if (Build.VERSION.SDK_INT >= 34) startActivityAndCollapse(CatchWidget.pending(this, 3, voice = false))
        else @Suppress("DEPRECATION") startActivityAndCollapse(CatchActivity.intent(this, voice = false))
    }
}
