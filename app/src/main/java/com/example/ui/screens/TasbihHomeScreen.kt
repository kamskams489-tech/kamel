package com.example.ui.screens

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.DhikrItem
import com.example.ui.TasbihUiState
import com.example.ui.components.GoldCard
import com.example.ui.components.formatCount
import com.example.ui.components.parseHexColor
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.CreamText
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.GoldGlow
import com.example.ui.theme.GoldMain
import com.example.ui.theme.MutedGold
import com.example.ui.theme.OliveBg
import com.example.ui.theme.OliveCard
import com.example.ui.theme.OliveLift
import kotlin.math.roundToInt

private data class TapVisualEffect(
    val id: Long,
    val offset: Offset
)

@Composable
fun TasbihHomeScreen(
    uiState: TasbihUiState,
    onTapTasbih: () -> Unit,
    onSelectDhikr: (DhikrItem) -> Unit,
    onCycleTarget: () -> Unit,
    onToggleSound: () -> Unit,
    onToggleVibration: () -> Unit,
    onResetSession: () -> Unit,
    onOpenAddDhikr: () -> Unit,
    onDeleteCustomDhikr: (String) -> Unit,
    onTargetCompletedHandled: () -> Unit
) {
    val context = LocalContext.current
    val local = uiState.local
    val activeDhikr = uiState.activeDhikr
    val dhikrColor = remember(activeDhikr.colorHex) { parseHexColor(activeDhikr.colorHex) }

    val tapEffects = remember { mutableStateListOf<TapVisualEffect>() }
    var effectIdCounter by remember { mutableLongStateOf(0L) }

    val counterScale = remember { Animatable(1f) }
    LaunchedEffect(local.sessionCount) {
        if (local.sessionCount > 0) {
            counterScale.snapTo(1.10f)
            counterScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessHigh
                )
            )
        }
    }

    LaunchedEffect(local.targetJustCompleted) {
        if (local.targetJustCompleted) {
            if (local.vibrateEnabled) {
                triggerTasbihVibration(context, strong = true)
            }
            onTargetCompletedHandled()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Horizontal Scrollable Dhikr Pills + Add Custom Dhikr
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dhikr_pills_row"),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(uiState.allAdhkar, key = { it.id }) { dhikr ->
                val isSelected = dhikr.id == activeDhikr.id
                val pillAccent = parseHexColor(dhikr.colorHex)
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isSelected) GoldMain else OliveCard,
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) GoldGlow else GoldMain.copy(alpha = 0.28f)
                    ),
                    modifier = Modifier
                        .heightIn(min = 40.dp)
                        .clickable { onSelectDhikr(dhikr) }
                        .testTag("dhikr_pill_${dhikr.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) OliveBg else pillAccent)
                        )
                        Text(
                            text = dhikr.title,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                color = if (isSelected) OliveBg else CreamText
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (dhikr.custom) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.action_delete),
                                tint = if (isSelected) OliveBg else MutedGold,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { onDeleteCustomDhikr(dhikr.id) }
                            )
                        }
                    }
                }
            }

            item(key = "add_custom_dhikr_pill") {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = OliveLift,
                    border = BorderStroke(1.dp, GoldMain.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .heightIn(min = 40.dp)
                        .clickable(onClick = onOpenAddDhikr)
                        .testTag("add_dhikr_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            tint = GoldGlow,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(R.string.action_add_dhikr),
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = GoldGlow,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    }
                }
            }
        }

        // LARGE CENTRAL CIRCULAR INTERACTIVE TAP AREA (#tap-zone)
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            val circleSize = minOf(maxWidth, maxHeight, 345.dp).coerceAtLeast(220.dp)
            val progressTarget = if (local.targetCount > 0) {
                ((local.sessionCount % local.targetCount).let { mod ->
                    if (mod == 0 && local.sessionCount > 0) local.targetCount else mod
                }.toFloat() / local.targetCount.toFloat()).coerceIn(0f, 1f)
            } else {
                ((local.sessionCount % 100).toFloat() / 100f)
            }
            val animatedSweep by animateFloatAsState(
                targetValue = progressTarget * 360f,
                animationSpec = tween(durationMillis = 160, easing = FastOutSlowInEasing),
                label = "tasbihProgressSweep"
            )

            val tapZoneCd = stringResource(R.string.tap_to_tasbih)
            val dhikrTodayCount = local.dhikrToday[activeDhikr.id] ?: 0L

            Box(
                modifier = Modifier
                    .size(circleSize)
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(OliveCard, Color(0xFF151104), OliveBg)
                        )
                    )
                    .border(
                        width = 2.dp,
                        color = GoldMain.copy(alpha = 0.48f),
                        shape = CircleShape
                    )
                    .pointerInput(local.soundEnabled, local.vibrateEnabled) {
                        detectTapGestures { offset ->
                            effectIdCounter += 1
                            tapEffects.add(TapVisualEffect(effectIdCounter, offset))
                            if (tapEffects.size > 10) {
                                tapEffects.removeAt(0)
                            }
                            if (local.vibrateEnabled) {
                                triggerTasbihVibration(context, strong = false)
                            }
                            if (local.soundEnabled) {
                                playTasbihClickSound(context)
                            }
                            onTapTasbih()
                        }
                    }
                    .semantics {
                        contentDescription = tapZoneCd
                        role = Role.Button
                    }
                    .testTag("tap_zone"),
                contentAlignment = Alignment.Center
            ) {
                // Progress Ring & Subtle Dashed Halo
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    val outerStroke = 1.5.dp.toPx()
                    val ringStroke = 9.dp.toPx()
                    val insetRing = 14.dp.toPx()

                    // Soft radial inner glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                GoldMain.copy(alpha = 0.11f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = size.minDimension * 0.48f
                        )
                    )

                    // Outer dashed halo
                    drawCircle(
                        color = GoldMain.copy(alpha = 0.22f),
                        radius = size.minDimension / 2f - outerStroke,
                        style = Stroke(
                            width = outerStroke,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 14f))
                        )
                    )

                    // Track circle
                    val arcSize = Size(
                        width = size.width - insetRing * 2,
                        height = size.height - insetRing * 2
                    )
                    val topLeft = Offset(insetRing, insetRing)
                    drawArc(
                        color = GoldMain.copy(alpha = 0.16f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = ringStroke)
                    )

                    // Glowing progress arc
                    if (animatedSweep > 0.5f) {
                        drawArc(
                            color = GoldGlow.copy(alpha = 0.28f),
                            startAngle = -90f,
                            sweepAngle = animatedSweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = ringStroke + 5.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(GoldMain, GoldGlow, GoldMain)
                            ),
                            startAngle = -90f,
                            sweepAngle = animatedSweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = ringStroke, cap = StrokeCap.Round)
                        )
                    }
                }

                // Active Touch Ripples & Floating +1 Feedback
                tapEffects.forEach { effect ->
                    TapEffectOverlay(
                        effect = effect,
                        onFinished = { tapEffects.remove(effect) }
                    )
                }

                // Dhikr Content & Tabular Counter
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Target Pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(OliveBg.copy(alpha = 0.85f))
                            .border(1.dp, GoldMain.copy(alpha = 0.32f), RoundedCornerShape(50))
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(dhikrColor)
                        )
                        val targetLabel = if (local.targetCount > 0) {
                            "${stringResource(R.string.target_prefix)} ${formatCount(local.targetCount)}"
                        } else {
                            "${stringResource(R.string.target_prefix)} ${stringResource(R.string.target_open)}"
                        }
                        Text(
                            text = targetLabel,
                            style = MaterialTheme.typography.labelMedium.copy(color = MutedGold)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Calligraphic Dhikr Title
                    Text(
                        text = activeDhikr.title,
                        fontFamily = AmiriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (activeDhikr.title.length > 22) 22.sp else 28.sp,
                        lineHeight = 34.sp,
                        color = CreamText,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("active_dhikr_title")
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Large Session Counter
                    Text(
                        text = formatCount(local.sessionCount),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 62.sp,
                            color = GoldGlow,
                            fontFeatureSettings = "tnum"
                        ),
                        modifier = Modifier
                            .scale(counterScale.value)
                            .testTag("session_counter_value")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Today for this Dhikr Badge
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(OliveCard.copy(alpha = 0.92f))
                            .border(1.dp, GoldMain.copy(alpha = 0.32f), RoundedCornerShape(50))
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.today_for_dhikr),
                            style = MaterialTheme.typography.labelSmall.copy(color = CreamText.copy(alpha = 0.88f))
                        )
                        Text(
                            text = formatCount(dhikrTodayCount),
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = GoldGlow,
                                fontWeight = FontWeight.Black,
                                fontFeatureSettings = "tnum"
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.tap_to_tasbih),
                        style = MaterialTheme.typography.labelSmall.copy(color = MutedGold)
                    )
                }
            }
        }

        // UTILITY CONTROLS BAR STRICTLY OUTSIDE TAP ZONE
        Column(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Compact Daily & All-Time Metrics Strip
            GoldCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MetricStripCell(
                        label = stringResource(R.string.metric_today_tasbih),
                        value = formatCount(local.todayTotal),
                        valueColor = EmeraldAccent,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(28.dp)
                            .background(GoldMain.copy(alpha = 0.22f))
                    )
                    MetricStripCell(
                        label = stringResource(R.string.metric_today_istighfar),
                        value = formatCount(local.todayIstighfar),
                        valueColor = GoldGlow,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(28.dp)
                            .background(GoldMain.copy(alpha = 0.22f))
                    )
                    MetricStripCell(
                        label = stringResource(R.string.metric_all_time),
                        value = formatCount(local.allTimeTotal),
                        valueColor = CreamText,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Target / Sound / Vibration / Reset Controls
            GoldCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Cycle Target Button
                    val targetShort = if (local.targetCount > 0) {
                        formatCount(local.targetCount)
                    } else {
                        stringResource(R.string.target_open)
                    }
                    Surface(
                        onClick = onCycleTarget,
                        shape = RoundedCornerShape(12.dp),
                        color = OliveBg,
                        border = BorderStroke(1.dp, GoldMain.copy(alpha = 0.36f)),
                        modifier = Modifier
                            .weight(1.1f)
                            .heightIn(min = 48.dp)
                            .testTag("cycle_target_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "${stringResource(R.string.target_prefix)} $targetShort",
                                style = MaterialTheme.typography.labelLarge.copy(color = GoldGlow),
                                maxLines = 1
                            )
                        }
                    }

                    // Sound Toggle
                    IconButton(
                        onClick = onToggleSound,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(OliveBg)
                            .border(
                                1.dp,
                                if (local.soundEnabled) GoldMain.copy(alpha = 0.42f) else GoldMain.copy(alpha = 0.18f),
                                RoundedCornerShape(12.dp)
                            )
                            .testTag("toggle_sound_button")
                    ) {
                        Icon(
                            imageVector = if (local.soundEnabled) {
                                Icons.AutoMirrored.Filled.VolumeUp
                            } else {
                                Icons.AutoMirrored.Filled.VolumeOff
                            },
                            contentDescription = stringResource(R.string.cd_toggle_sound),
                            tint = if (local.soundEnabled) GoldMain else MutedGold
                        )
                    }

                    // Vibration Toggle
                    IconButton(
                        onClick = onToggleVibration,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(OliveBg)
                            .border(
                                1.dp,
                                if (local.vibrateEnabled) GoldMain.copy(alpha = 0.42f) else GoldMain.copy(alpha = 0.18f),
                                RoundedCornerShape(12.dp)
                            )
                            .testTag("toggle_vibration_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Vibration,
                            contentDescription = stringResource(R.string.cd_toggle_vibration),
                            tint = if (local.vibrateEnabled) GoldMain else MutedGold.copy(alpha = 0.6f)
                        )
                    }

                    // Reset Session Counter
                    Surface(
                        onClick = onResetSession,
                        shape = RoundedCornerShape(12.dp),
                        color = OliveBg,
                        border = BorderStroke(1.dp, GoldMain.copy(alpha = 0.28f)),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("reset_session_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = stringResource(R.string.cd_reset_session),
                                tint = MutedGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.action_reset),
                                style = MaterialTheme.typography.labelLarge.copy(color = MutedGold),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricStripCell(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(color = MutedGold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                color = valueColor,
                fontWeight = FontWeight.Black,
                fontFeatureSettings = "tnum"
            ),
            maxLines = 1
        )
    }
}

@Composable
private fun TapEffectOverlay(
    effect: TapVisualEffect,
    onFinished: () -> Unit
) {
    val progress = remember(effect.id) { Animatable(0f) }
    LaunchedEffect(effect.id) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 560, easing = FastOutSlowInEasing)
        )
        onFinished()
    }

    val p = progress.value
    val density = LocalDensity.current
    val risePx = with(density) { 64.dp.toPx() * p }

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = (18.dp.toPx() + 92.dp.toPx() * p)
            val alpha = (1f - p).coerceIn(0f, 1f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        GoldGlow.copy(alpha = 0.42f * alpha),
                        Color.Transparent
                    ),
                    center = effect.offset,
                    radius = radius
                ),
                radius = radius,
                center = effect.offset
            )
            drawCircle(
                color = GoldGlow.copy(alpha = 0.72f * alpha),
                radius = radius * 0.75f,
                center = effect.offset,
                style = Stroke(width = 1.5.dp.toPx())
            )
        }

        Text(
            text = "+1",
            style = MaterialTheme.typography.titleLarge.copy(
                color = GoldGlow,
                fontWeight = FontWeight.Black
            ),
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = (effect.offset.x - 18f).roundToInt(),
                        y = (effect.offset.y - 24f - risePx).roundToInt()
                    )
                }
                .alpha((1f - p).coerceIn(0f, 1f))
                .scale(0.88f + 0.3f * p)
        )
    }
}

private fun playTasbihClickSound(context: Context) {
    try {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        audioManager?.playSoundEffect(AudioManager.FX_KEY_CLICK, 0.55f)
    } catch (_: Exception) {
        // Ignore on devices without sound output
    }
}

@Suppress("DEPRECATION")
private fun triggerTasbihVibration(context: Context, strong: Boolean) {
    try {
        val durationMs = if (strong) 75L else 18L
        val amplitude = if (strong) 180 else 70
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator?.vibrate(
                VibrationEffect.createOneShot(durationMs, amplitude)
            )
        } else {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, amplitude))
            } else {
                vibrator?.vibrate(durationMs)
            }
        }
    } catch (_: Exception) {
        // Ignore on devices without vibrator hardware
    }
}
