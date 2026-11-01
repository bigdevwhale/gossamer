package app.gossamer

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.test.core.app.ApplicationProvider
import app.gossamer.data.DemoSeed
import app.gossamer.ui.GossamerRoot
import app.gossamer.ui.Overlay
import app.gossamer.ui.Tab
import app.gossamer.ui.art.Art
import app.gossamer.ui.onboarding.Onboarding
import app.gossamer.ui.theme.GossamerTheme
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.time.LocalDate
import java.time.ZoneId

/** Every main screen over the demo web, drawn to PNGs (run with `-Prender`), in both languages. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
abstract class ScreensBase {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    abstract val lang: String
    private val out by lazy { File(System.getProperty("render.out") ?: "build/art", "screens-$lang").apply { mkdirs() } }

    private fun at(hour: Int) = LocalDate.now().atTime(hour, 10).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    @Before fun still() { Art.still = true }

    private fun seed(): app.gossamer.data.Settings = runBlocking {
        val app = ApplicationProvider.getApplicationContext<GossamerApp>()
        DemoSeed.seedIfEmpty(app, app.container)
        app.container.settings.now()
    }

    private fun shot(name: String, content: @Composable () -> Unit) {
        rule.mainClock.autoAdvance = false
        rule.setContent { GossamerTheme { content() } }
        val title = ApplicationProvider.getApplicationContext<GossamerApp>().getString(R.string.app_title)
        rule.waitUntil(15_000) { rule.mainClock.advanceTimeBy(100); rule.onAllNodesWithText(title, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty() }
        rule.mainClock.advanceTimeBy(2_000)
        rule.waitForIdle()
        rule.mainClock.advanceTimeBy(500)
        rule.runOnUiThread {
            val view = rule.activity.window.decorView
            val image = android.graphics.Bitmap.createBitmap(view.width, view.height, android.graphics.Bitmap.Config.ARGB_8888)
            view.draw(android.graphics.Canvas(image))
            File(out, "$name.png").outputStream().use { image.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
        }
    }

    private fun screen(name: String, tab: Tab, hour: Int = 19, overlay: Overlay? = null) {
        val s = seed()
        shot(name) { GossamerRoot(s, at(hour), tab, overlay) }
    }

    @Test fun welcome() = shot("0-welcome") { Onboarding() }
    @Test fun web() = screen("1-web", Tab.WEB, 20)
    @Test fun night() = screen("2-web-night", Tab.WEB, 23)
    @Test fun thought() = screen("3-thought", Tab.WEB, 20, Overlay.Thought(31))
    @Test fun thoughts() = screen("4-thoughts", Tab.THOUGHTS)
    @Test fun butterflies() = screen("5-butterflies", Tab.COLLECTION)
    @Test fun butterfly() = screen("6-butterfly", Tab.COLLECTION, overlay = Overlay.Cocoon(1))
    @Test fun hatch() = screen("7-hatch", Tab.COLLECTION, overlay = Overlay.Hatch(3))
    @Test fun catching() = screen("8-catch", Tab.WEB, 10, Overlay.Catch(false))
}

@Config(sdk = [34], qualifiers = "en-w411dp-h891dp-xxhdpi")
class RenderScreensEn : ScreensBase() { override val lang = "en" }

@Config(sdk = [34], qualifiers = "ru-w411dp-h891dp-xxhdpi")
class RenderScreensRu : ScreensBase() { override val lang = "ru" }
