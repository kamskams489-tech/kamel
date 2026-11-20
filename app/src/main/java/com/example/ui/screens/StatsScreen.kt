package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.DhikrItem
import com.example.ui.StatsPeriod
import com.example.ui.StatsScope
import com.example.ui.TasbihUiState
import com.example.ui.components.GoldCard
import com.example.ui.components.formatCount
import com.example.ui.components.parseHexColor
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.CreamText
import com.example.ui.theme.GoldBronze
import com.example.ui.theme.GoldGlow
import com.example.ui.theme.GoldMain
import com.example.ui.theme.MutedGold
import com.example.ui.theme.OliveBg
import com.example.ui.theme.OliveCard
import com.example.ui.theme.OliveLift

private data class DhikrStatRow(
    val dhikr: DhikrItem,
    val count: Long,
    val ratio: Float
)

@Composable
fun StatsScreen(
    uiState: TasbihUiState,
    onSelectScope: (StatsScope) -> Unit,
    onSelectPeriod: (StatsPeriod) -> Unit
) {
    val (domeTotal, dhikrCountsMap) = remember(
        uiState.local,
        uiState.leaderboard,
        uiState.statsScope,
        uiState.statsPeriod
    ) {
        when (uiState.statsScope) {
            StatsScope.MINE -> {
                if (uiState.statsPeriod == StatsPeriod.TOTAL) {
                    uiState.local.allTimeTotal to uiState.local.dhikrTotals
                } else {
                    uiState.local.todayTotal to uiState.local.dhikrToday
                }
            }
            StatsScope.ALL -> {
                val aggregated = mutableMapOf<String, Long>()
                var sumTotal = 0L
                uiState.leaderboard.forEach { entry ->
                    if (uiState.statsPeriod == StatsPeriod.TOTAL) {
                        sumTotal += entry.allTimeTotal
                        entry.dhikrTotals.forEach { (k, v) ->
                            aggregated[k] = (aggregated[k] ?: 0L) + v
                        }
                    } else {
                        sumTotal += entry.todayTotal
                        entry.dhikrToday.forEach { (k, v) ->
                            aggregated[k] = (aggregated[k] ?: 0L) + v
                        }
                    }
                }
                sumTotal to aggregated
            }
        }
    }

    val rows = remember(uiState.allAdhkar, dhikrCountsMap, domeTotal) {
        val denom = domeTotal.coerceAtLeast(1L).toFloat()
        uiState.allAdhkar.map { dhikr ->
            val count = dhikrCountsMap[dhikr.id] ?: 0L
            DhikrStatRow(
                dhikr = dhikr,
                count = count,
                ratio = (count / denom).coerceIn(0f, 1f)
            )
        }.sortedByDescending { it.count }
    }

    val domeLabel = if (uiState.statsScope == StatsScope.MINE) {
        stringResource(R.string.stats_scope_mine)
    } else {
        stringResource(R.string.stats_scope_all)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item(key = "stats_header_dome") {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Glowing Golden Semi-Circular Dome
                    Box(
                        modifier = Modifier
                            .width(268.dp)
                            .height(130.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Subtle golden halo behind dome
                            drawOval(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        GoldMain.copy(alpha = 0.28f),
                                        Color.Transparent
                                    ),
                                    center = Offset(size.width / 2f, size.height * 0.2f),
                                    radius = size.width * 0.56f
                                ),
                                topLeft = Offset(0f, -size.height * 0.4f),
                                size = Size(size.width, size.height * 1.4f)
                            )
                            // Inverted Semi-Circular Golden Dome (matches HTML SVG path)
                            val arcHeight = size.height * 1.85f
                            drawArc(
                                brush = Brush.linearGradient(
                                    colors = listOf(GoldGlow, GoldMain, GoldBronze),
                                    start = Offset(0f, 0f),
                                    end = Offset(size.width, size.height)
                                ),
                                startAngle = 0f,
                                sweepAngle = 180f,
                                useCenter = true,
                                topLeft = Offset(12.dp.toPx(), -arcHeight / 2f),
                                size = Size(size.width - 24.dp.toPx(), arcHeight)
                            )
                        }

                        Column(
                            modifier = Modifier
                                .padding(top = 18.dp, start = 24.dp, end = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = formatCount(domeTotal),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.Black,
                                    color = OliveBg,
                                    fontFeatureSettings = "tnum"
                                ),
                                modifier = Modifier.testTag("stats_dome_total")
                            )
                            Text(
                                text = domeLabel,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = OliveBg.copy(alpha = 0.85f)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Personal vs Global Community Toggle
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = OliveCard,
                        border = BorderStroke(1.dp, GoldMain.copy(alpha = 0.26f))
                    ) {
                        Row(
                            modifier = Modifier.padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            ScopePillButton(
                                text = stringResource(R.string.stats_scope_mine),
                                selected = uiState.statsScope == StatsScope.MINE,
                                onClick = { onSelectScope(StatsScope.MINE) },
                                tag = "stats_scope_mine"
                            )
                            ScopePillButton(
                                text = stringResource(R.string.stats_scope_all),
                                selected = uiState.statsScope == StatsScope.ALL,
                                onClick = { onSelectScope(StatsScope.ALL) },
                                tag = "stats_scope_all"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Period Filter: الإجمالي الكلي / اليوم فقط
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PeriodChipButton(
                            text = stringResource(R.string.stats_period_total),
                            selected = uiState.statsPeriod == StatsPeriod.TOTAL,
                            onClick = { onSelectPeriod(StatsPeriod.TOTAL) },
                            tag = "stats_period_total"
                        )
                        PeriodChipButton(
                            text = stringResource(R.string.stats_period_today),
                            selected = uiState.statsPeriod == StatsPeriod.TODAY,
                            onClick = { onSelectPeriod(StatsPeriod.TODAY) },
                            tag = "stats_period_today"
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            if (domeTotal == 0L) {
                item(key = "stats_empty_tip") {
                    GoldCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.stats_empty_hint),
                            style = MaterialTheme.typography.bodyMedium.copy(color = MutedGold),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                    }
                }
            }

            items(rows, key = { it.dhikr.id }) { row ->
                DhikrStatItemCard(row = row)
            }
        }
    }
}

@Composable
private fun ScopePillButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) GoldMain else Color.Transparent)
            .clickable(onClick = onClick)
            .testTag(tag)
            .padding(horizontal = 18.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Bold,
                color = if (selected) OliveBg else MutedGold
            )
        )
    }
}

@Composable
private fun PeriodChipButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) OliveLift else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (selected) GoldMain.copy(alpha = 0.48f) else GoldMain.copy(alpha = 0.16f),
                shape = RoundedCornerShape(50)
            )
            .clickable(onClick = onClick)
            .testTag(tag)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(
                color = if (selected) GoldGlow else MutedGold,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold
            )
        )
    }
}

@Composable
private fun DhikrStatItemCard(row: DhikrStatRow) {
    val accentColor = remember(row.dhikr.colorHex) { parseHexColor(row.dhikr.colorHex) }
    val animatedProgress by animateFloatAsState(
        targetValue = row.ratio,
        animationSpec = tween(durationMillis = 360),
        label = "dhikrStatBar"
    )
    val percentText = "${(row.ratio * 100f).toInt()}%"

    GoldCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
                Text(
                    text = row.dhikr.title,
                    fontFamily = AmiriFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = CreamText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = percentText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MutedGold,
                        fontFeatureSettings = "tnum"
                    )
                )
                Text(
                    text = formatCount(row.count),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = GoldGlow,
                        fontWeight = FontWeight.Black,
                        fontFeatureSettings = "tnum"
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50))
                .background(OliveBg)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress.coerceAtLeast(if (row.count > 0) 0.03f else 0f))
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(accentColor.copy(alpha = 0.75f), accentColor)
                        )
                    )
            )
        }
    }
}
