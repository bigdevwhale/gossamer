package app.gossamer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.gossamer.R
import app.gossamer.data.Kind

/** An attic window after dark: slate glass, silver silk, and the four warm colours of what gets caught. */
@Immutable
object C {
    val ink = Color(0xFF0D121A)
    val deep = Color(0xFF141B26)
    val panel = Color(0xFF1B2431)
    val panelL = Color(0xFF243042)
    val line = Color(0xFF34435A)
    val silk = Color(0xFFEDE8DC)
    val silkD = Color(0xFFB9B5AA)
    val muted = Color(0xFF8793A4)
    val frame = Color(0xFF2B2420)
    val frameL = Color(0xFF4A3C32)
    val amber = Color(0xFFF2B85B)
    val teal = Color(0xFF86C9C2)
    val rose = Color(0xFFEDB9AC)
    val violet = Color(0xFFBBA7E6)
    val dew = Color(0xFFCFE9FF)
    val warn = Color(0xFFE88A74)

    fun of(k: Kind) = when (k) {
        Kind.IDEA -> amber
        Kind.QUESTION -> teal
        Kind.NOTICE -> rose
        Kind.WORDS -> violet
    }
}

/** Cormorant (Christian Thalmann, SIL OFL) for titles; Lora (Cyreal, SIL OFL) for reading; Caveat (Impallari, SIL OFL) for the thoughts themselves. */
val Display = FontFamily(Font(R.font.cormorant_semibold, FontWeight.SemiBold), Font(R.font.cormorant_bold, FontWeight.Bold), Font(R.font.cormorant_italic, FontWeight.Normal, FontStyle.Italic))
val Serif = FontFamily(Font(R.font.lora_regular, FontWeight.Normal), Font(R.font.lora_italic, FontWeight.Normal, FontStyle.Italic), Font(R.font.lora_semibold, FontWeight.SemiBold))
val Hand = FontFamily(Font(R.font.caveat, FontWeight.Normal))

object T {
    val Display = TextStyle(fontFamily = app.gossamer.ui.theme.Display, fontWeight = FontWeight.SemiBold, fontSize = 34.sp, lineHeight = 36.sp, color = C.silk)
    val Title = TextStyle(fontFamily = app.gossamer.ui.theme.Display, fontWeight = FontWeight.SemiBold, fontSize = 27.sp, lineHeight = 30.sp, color = C.silk)
    val Heading = TextStyle(fontFamily = app.gossamer.ui.theme.Display, fontWeight = FontWeight.Bold, fontSize = 21.sp, lineHeight = 24.sp, color = C.silk)
    val Italic = TextStyle(fontFamily = app.gossamer.ui.theme.Display, fontStyle = FontStyle.Italic, fontSize = 20.sp, lineHeight = 24.sp, color = C.silkD)
    val Body = TextStyle(fontFamily = Serif, fontSize = 15.sp, lineHeight = 22.sp, color = C.silk)
    val Small = TextStyle(fontFamily = Serif, fontSize = 13.sp, lineHeight = 18.sp, color = C.muted)
    val Button = TextStyle(fontFamily = Serif, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 18.sp)
    val Thought = TextStyle(fontFamily = Hand, fontSize = 25.sp, lineHeight = 27.sp, color = C.silk)
    val ThoughtSmall = TextStyle(fontFamily = Hand, fontSize = 19.sp, lineHeight = 20.sp, color = C.silk)
}

@Composable
fun GossamerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = C.amber, onPrimary = C.ink, background = C.ink, surface = C.panel, onSurface = C.silk,
            onBackground = C.silk, surfaceContainer = C.panel, outline = C.line, error = C.warn,
        ),
        typography = MaterialTheme.typography.copy(bodyLarge = T.Body, bodyMedium = T.Body, bodySmall = T.Small, labelLarge = T.Button, titleLarge = T.Title),
        content = content,
    )
}
