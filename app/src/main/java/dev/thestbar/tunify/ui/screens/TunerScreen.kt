package dev.thestbar.tunify.ui.screens

import android.graphics.Paint as NativePaint
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import dev.thestbar.tunify.R
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.thestbar.tunify.data.viewmodels.TunerUiState
import dev.thestbar.tunify.ui.util.KeepScreenOn
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.cos

private fun inTune(cents: Float) = abs(cents) <= 4f

private fun polar(cx: Float, cy: Float, r: Float, thetaDeg: Float): Offset {
    val a = (thetaDeg * PI / 180).toFloat()
    return Offset(cx + r * sin(a), cy - r * cos(a))
}

private fun parseNote(note: String): Pair<String, String> {
    if (note.isEmpty()) return "—" to ""
    val match = Regex("""^([A-Ga-g][#b]?)(\d)$""").find(note.trim())
    return if (match != null) match.groupValues[1] to match.groupValues[2] else note to ""
}

@Composable
fun TunerScreen(
    state: TunerUiState,
    onToggleTuning: () -> Unit,
    modifier: Modifier = Modifier
) {
    KeepScreenOn(enabled = state.isTuning)

    var selectedStringIndex by remember { mutableStateOf(0) }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TunerHeader(isTuning = state.isTuning, onToggle = onToggleTuning)

        Column(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val noteStr = state.detectedNote.ifEmpty {
                state.currentTuningStrings.getOrElse(selectedStringIndex) { "E2" }
            }
            val (noteName, noteOctave) = parseNote(noteStr)
            ArcMeter(cents = state.centsOffset, noteName = noteName, noteOctave = noteOctave)
        }

        if (state.currentTuningStrings.size == 6) {
            Headstock(
                strings = state.currentTuningStrings,
                selectedIndex = selectedStringIndex,
                onSelect = { selectedStringIndex = it }
            )
        }
    }
}

@Composable
private fun TunerHeader(isTuning: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Tunify",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(end = 8.dp)
        ) {
            Text(
                text = if (isTuning) "Tuning" else "Muted",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Switch(checked = isTuning, onCheckedChange = { onToggle() })
        }
    }
}

@Composable
private fun NoteReadout(noteName: String, noteOctave: String, cents: Float) {
    val ok = inTune(cents)
    val noteColor by animateColorAsState(
        targetValue = if (ok) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(200),
        label = "noteColor"
    )
    val statusColor by animateColorAsState(
        targetValue = if (ok) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(200),
        label = "statusColor"
    )
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontSize = 45.sp, fontWeight = FontWeight.Medium, color = noteColor)) {
                    append(noteName)
                }
                if (noteOctave.isNotEmpty()) {
                    withStyle(
                        SpanStyle(
                            fontSize = 20.sp,
                            color = onSurfaceVariant,
                            baselineShift = BaselineShift.Superscript
                        )
                    ) {
                        append(noteOctave)
                    }
                }
            },
            lineHeight = 56.sp
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = if (ok) "In tune" else "${if (cents > 0) "+" else ""}${cents.roundToInt()}",
                style = MaterialTheme.typography.titleMedium,
                color = statusColor
            )
            if (!ok && noteName != "—") {
                Text(
                    text = "cents",
                    style = MaterialTheme.typography.bodyMedium,
                    color = onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ArcMeter(cents: Float, noteName: String, noteOctave: String) {
    val ok = inTune(cents)
    val accentColor by animateColorAsState(
        targetValue = if (ok) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
        animationSpec = tween(200),
        label = "accent"
    )
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceColor = MaterialTheme.colorScheme.surface
    val animatedCents by animateFloatAsState(
        targetValue = cents.coerceIn(-50f, 50f),
        animationSpec = tween(120),
        label = "cents"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        NoteReadout(noteName = noteName, noteOctave = noteOctave, cents = cents)

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(300f / 172f)
                .padding(horizontal = 8.dp)
        ) {
            val scale = size.width / 300f
            val cx = 150f * scale
            val cy = 150f * scale
            val R = 118f * scale
            val Rtick = 104f * scale
            val maxA = 80f
            val theta = ((animatedCents / 50f) * maxA).coerceIn(-maxA, maxA)

            // Background arc track
            drawArc(
                color = outlineVariant.copy(alpha = 0.5f),
                topLeft = Offset(cx - Rtick, cy - Rtick),
                size = Size(Rtick * 2, Rtick * 2),
                startAngle = -maxA - 90f,
                sweepAngle = maxA * 2,
                useCenter = false,
                style = Stroke(width = 2f * scale, cap = StrokeCap.Round)
            )

            // Tick marks
            for (t in -50..50 step 10) {
                val a = (t / 50f) * maxA
                val isCenter = t == 0
                val outerPt = polar(cx, cy, Rtick, a)
                val innerPt = polar(cx, cy, Rtick - (if (isCenter) 16f else 10f) * scale, a)
                drawLine(
                    color = if (isCenter) accentColor else outlineVariant,
                    start = outerPt,
                    end = innerPt,
                    strokeWidth = (if (isCenter) 3f else 2f) * scale,
                    cap = StrokeCap.Round
                )
            }

            // Tick labels
            val labelRadius = Rtick - 24f * scale
            val labelColor = onSurfaceVariant.toArgb()
            drawIntoCanvas { canvas ->
                val paint = NativePaint().apply {
                    textSize = 11f * scale
                    color = labelColor
                    textAlign = NativePaint.Align.CENTER
                    isAntiAlias = true
                }
                for (t in -50..50 step 10) {
                    val a = (t / 50f) * maxA
                    val pt = polar(cx, cy, labelRadius, a)
                    val label = if (t > 0) "+$t" else "$t"
                    canvas.nativeCanvas.drawText(label, pt.x, pt.y + paint.textSize / 3f, paint)
                }
            }

            // Value arc from 0 to theta
            if (abs(theta) > 0.5f) {
                drawArc(
                    color = accentColor,
                    topLeft = Offset(cx - R, cy - R),
                    size = Size(R * 2, R * 2),
                    startAngle = -90f,
                    sweepAngle = theta,
                    useCenter = false,
                    style = Stroke(width = 5f * scale, cap = StrokeCap.Round)
                )
            }

            // Needle line from center to arc
            val needlePt = polar(cx, cy, R - 6f * scale, theta)
            drawLine(
                color = accentColor,
                start = Offset(cx, cy),
                end = needlePt,
                strokeWidth = 3.5f * scale,
                cap = StrokeCap.Round
            )

            // Center pivot circle
            drawCircle(color = surfaceColor, radius = 9f * scale, center = Offset(cx, cy))
            drawCircle(
                color = accentColor,
                radius = 9f * scale,
                center = Offset(cx, cy),
                style = Stroke(width = 3.5f * scale)
            )
        }
    }
}

@Composable
private fun StringChip(note: String, isActive: Boolean, onClick: () -> Unit) {
    val brand = MaterialTheme.colorScheme.primary
    val onBrand = MaterialTheme.colorScheme.onPrimary
    val outline = MaterialTheme.colorScheme.outline
    val onSurface = MaterialTheme.colorScheme.onSurface
    val bgColor by animateColorAsState(
        targetValue = if (isActive) brand else Color.Transparent,
        animationSpec = tween(150),
        label = "chipBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isActive) onBrand else onSurface,
        animationSpec = tween(150),
        label = "chipText"
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .defaultMinSize(minWidth = 56.dp, minHeight = 40.dp)
            .background(color = bgColor, shape = RoundedCornerShape(20.dp))
            .then(
                if (!isActive) Modifier.border(1.dp, outline, RoundedCornerShape(20.dp)) else Modifier
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp)
    ) {
        Text(text = note, style = MaterialTheme.typography.titleMedium, color = textColor)
    }
}

@Composable
private fun Headstock(strings: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
        ) {
            listOf(0, 1, 2).forEach { i ->
                StringChip(
                    note = strings.getOrElse(i) { "E2" },
                    isActive = selectedIndex == i,
                    onClick = { onSelect(i) }
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        Image(
            painter = painterResource(id = R.drawable.guitar_img),
            contentDescription = null,
            modifier = Modifier.size(240.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(Modifier.width(8.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
        ) {
            listOf(3, 4, 5).forEach { i ->
                StringChip(
                    note = strings.getOrElse(i) { "E4" },
                    isActive = selectedIndex == i,
                    onClick = { onSelect(i) }
                )
            }
        }
    }
}
