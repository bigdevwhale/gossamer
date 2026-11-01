package app.gossamer.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.gossamer.R
import app.gossamer.ui.theme.C
import app.gossamer.ui.theme.T

val Round = RoundedCornerShape(14.dp)
val Pill = RoundedCornerShape(50)

/** Swallows touches so nothing under an overlay reacts. */
fun Modifier.blockTouches() = clickable(interactionSource = MutableInteractionSource(), indication = null) {}

enum class Kind { MAIN, PLAIN, QUIET, WARN }

@Composable
fun Btn(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, kind: Kind = Kind.MAIN, enabled: Boolean = true) {
    val (bg, fg, edge) = when (kind) {
        Kind.MAIN -> Triple(C.amber, C.ink, C.amber)
        Kind.PLAIN -> Triple(Color.Transparent, C.silk, C.line)
        Kind.QUIET -> Triple(Color.Transparent, C.silkD, Color.Transparent)
        Kind.WARN -> Triple(Color.Transparent, C.warn, C.warn.copy(alpha = .5f))
    }
    Box(
        modifier.heightIn(min = 46.dp).clip(Pill).background(if (enabled) bg else bg.copy(alpha = .35f)).border(1.dp, edge.copy(alpha = if (enabled) edge.alpha else .3f), Pill)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick).padding(horizontal = 18.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) { Text(text, style = T.Button.copy(color = if (enabled) fg else fg.copy(alpha = .5f)), textAlign = TextAlign.Center) }
}

@Composable
fun Chip(text: String, on: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, lead: (@Composable () -> Unit)? = null) {
    Row(
        modifier.clip(Pill).background(if (on) C.panelL else Color.Transparent).border(1.dp, if (on) C.silkD else C.line, Pill)
            .clickable(role = Role.RadioButton, onClick = onClick).padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        lead?.invoke()
        Text(text, style = T.Small.copy(color = if (on) C.silk else C.silkD))
    }
}

@Composable
fun Card(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, pad: Dp = 16.dp, color: Color = C.panel, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier.clip(Round).background(color).border(1.dp, C.line.copy(alpha = .6f), Round).let { if (onClick != null) it.clickable(onClick = onClick) else it }.padding(pad),
        verticalArrangement = Arrangement.spacedBy(6.dp), content = content,
    )
}

@Composable
fun Section(text: String) {
    Text(text.uppercase(), style = T.Small.copy(color = C.silkD, letterSpacing = androidx.compose.ui.unit.TextUnit(1.5f, androidx.compose.ui.unit.TextUnitType.Sp)), modifier = Modifier.padding(top = 14.dp))
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Flow(modifier: Modifier = Modifier, content: @Composable FlowRowScope.() -> Unit) {
    FlowRow(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
}

@Composable
fun Field(
    value: String, onChange: (String) -> Unit, hint: String, modifier: Modifier = Modifier, style: TextStyle = T.Body,
    single: Boolean = true, focus: FocusRequester? = null, onDone: (() -> Unit)? = null, minLines: Int = 1,
) {
    Box(modifier.clip(Round).background(C.deep).border(1.dp, C.line, Round).padding(horizontal = 14.dp, vertical = 12.dp)) {
        if (value.isEmpty()) Text(hint, style = style.copy(color = C.muted))
        BasicTextField(
            value, onChange, Modifier.fillMaxWidth().let { m -> focus?.let { m.focusRequester(it) } ?: m },
            textStyle = style, singleLine = single, minLines = minLines, cursorBrush = SolidColor(C.amber),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = if (single) ImeAction.Done else ImeAction.Default),
            keyboardActions = KeyboardActions(onDone = { onDone?.invoke() }),
        )
    }
}

/** A full-screen page over the tabs, with a title and a way back. */
@Composable
fun Page(title: String, onClose: () -> Unit, scroll: Boolean = true, content: @Composable ColumnScope.() -> Unit) {
    BackHandler(onBack = onClose)
    Column(Modifier.fillMaxSize().background(C.ink).blockTouches().statusBarsPadding().imePadding()) {
        Row(Modifier.fillMaxWidth().padding(start = 4.dp, end = 16.dp, top = 6.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(Pill).clickable(onClick = onClose), contentAlignment = Alignment.Center) { Text("←", style = T.Title.copy(color = C.silkD)) }
            Text(title, style = T.Title, maxLines = 1)
        }
        Column(
            Modifier.fillMaxWidth().weight(1f).let { if (scroll) it.verticalScroll(rememberScrollState()) else it }.padding(horizontal = 20.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            content()
            Spacer(Modifier.height(24.dp))
        }
    }
}

/** A sheet rising from the bottom over a dim web; a tap on the dim closes it. */
@Composable
fun Sheet(onClose: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    BackHandler(onBack = onClose)
    Box(Modifier.fillMaxSize().background(C.ink.copy(alpha = .62f)).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClose).imePadding()) {
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().heightIn(max = 720.dp).clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)).background(C.panel)
                .blockTouches().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(Modifier.align(Alignment.CenterHorizontally).size(width = 40.dp, height = 4.dp).clip(Pill).background(C.line))
            content()
        }
    }
}

/** Two buttons under a question: a quiet no and a yes. */
@Composable
fun Confirm(title: String, body: String, yes: String, onYes: () -> Unit, onNo: () -> Unit, warn: Boolean = false) {
    BackHandler(onBack = onNo)
    Box(Modifier.fillMaxSize().background(C.ink.copy(alpha = .7f)).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onNo), contentAlignment = Alignment.Center) {
        Card(Modifier.padding(28.dp).blockTouches(), pad = 20.dp) {
            Text(title, style = T.Heading)
            Text(body, style = T.Body.copy(color = C.silkD))
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                Btn(stringResource(R.string.cancel), onNo, kind = Kind.QUIET)
                Btn(yes, onYes, kind = if (warn) Kind.WARN else Kind.MAIN)
            }
        }
    }
}

@Composable
fun Center(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) = Box(modifier, contentAlignment = Alignment.Center, content = content)
