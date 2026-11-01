package app.gossamer

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.gossamer.data.Settings
import app.gossamer.ui.GossamerRoot
import app.gossamer.ui.theme.GossamerTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val t = android.graphics.Color.TRANSPARENT
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(t), navigationBarStyle = SystemBarStyle.dark(t))
        setContent {
            val settings: Settings? by container.settings.settings.collectAsStateWithLifecycle(initialValue = null)
            val seeding by container.seeding.collectAsStateWithLifecycle()
            // Wait for settings (and the demo web) so the welcome doesn't flash for someone already here.
            if (!seeding) settings?.let { s -> GossamerTheme { GossamerRoot(s) } }
        }
    }

    override fun onStart() {
        super.onStart()
        container.write {
            val n = sweep()
            if (n > 0) note(resources.getQuantityString(R.plurals.n_wind, n, n), getString(R.string.n_wind_b))
        }
    }
}
