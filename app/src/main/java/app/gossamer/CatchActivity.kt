package app.gossamer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import app.gossamer.data.Kind
import app.gossamer.ui.catching.CatchForm
import app.gossamer.ui.components.Sheet
import app.gossamer.ui.theme.GossamerTheme

/**
 * The quick way in: from the widget, the quick-settings tile, a shortcut, «Share» in another app or
 * selected text. Just the catch sheet over whatever was on the screen; it closes as soon as the thought sticks.
 */
class CatchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val t = android.graphics.Color.TRANSPARENT
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(t), navigationBarStyle = SystemBarStyle.dark(t))
        val shared = when (intent?.action) {
            Intent.ACTION_SEND -> listOfNotNull(intent.getStringExtra(Intent.EXTRA_SUBJECT), intent.getStringExtra(Intent.EXTRA_TEXT)).distinct().joinToString("\n")
            Intent.ACTION_PROCESS_TEXT -> intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString().orEmpty()
            else -> ""
        }.trim()
        val voice = intent?.getBooleanExtra(VOICE, false) == true
        setContent {
            GossamerTheme {
                Sheet(onClose = ::finish) {
                    CatchForm(initial = shared, initialKind = if (shared.isNotEmpty()) Kind.WORDS else Kind.IDEA, voice = voice) {
                        Toast.makeText(this@CatchActivity, if (it.echo != null) R.string.n_echo else R.string.n_caught, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        }
    }

    companion object {
        const val VOICE = "voice"
        const val ACTION = "app.gossamer.CATCH"

        fun intent(ctx: Context, voice: Boolean) = Intent(ctx, CatchActivity::class.java)
            .setAction(ACTION)
            .putExtra(VOICE, voice)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    }
}
