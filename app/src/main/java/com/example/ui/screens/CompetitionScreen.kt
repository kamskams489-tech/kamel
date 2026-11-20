package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.LeaderboardEntryDoc
import com.example.ui.CompetitionFilter
import com.example.ui.TasbihUiState
import com.example.ui.components.GoldCard
import com.example.ui.components.formatCount
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.CreamText
import com.example.ui.theme.CrownGoldBottom
import com.example.ui.theme.CrownGoldTop
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.EmeraldBorder
import com.example.ui.theme.EmeraldDarkBg
import com.example.ui.theme.EmeraldDeepBg
import com.example.ui.theme.GoldGlow
import com.example.ui.theme.GoldMain
import com.example.ui.theme.MutedGold
import com.example.ui.theme.OliveBg
import com.example.ui.theme.OliveLift

@Composable
fun CompetitionScreen(
    uiState: TasbihUiState,
    currentUserId: String,
    onSelectFilter: (CompetitionFilter) -> Unit
) {
    val entries = uiState.leaderboard

    val istighfarChampion = remember(entries) {
        entries.filter { it.todayIstighfar > 0L }.maxByOrNull { it.todayIstighfar }
    }
    val tasbihChampion = remember(entries) {
        entries.filter { it.todayTotal > 0L }.maxByOrNull { it.todayTotal }
    }

    val rankedEntries = remember(entries, uiState.competitionFilter) {
        when (uiState.competitionFilter) {
            CompetitionFilter.TODAY -> entries.sortedByDescending { it.todayTotal }
            CompetitionFilter.ISTIGHFAR -> entries.sortedByDescending { it.todayIstighfar }
            CompetitionFilter.ALL_TIME -> entries.sortedByDescending { it.allTimeTotal }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Quranic Header
            item(key = "comp_quran_header") {
                GoldCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = stringResource(R.string.comp_quran_verse),
                        fontFamily = AmiriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = GoldGlow,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.comp_subtitle),
                        style = MaterialTheme.typography.bodySmall.copy(color = MutedGold),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Two Prominent Crown Cards
            item(key = "comp_crown_cards") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CrownChampionCard(
                        title = stringResource(R.string.comp_crown_istighfar),
                        championName = istighfarChampion?.displayName
                            ?: stringResource(R.string.comp_waiting_leader),
                        score = istighfarChampion?.todayIstighfar ?: 0L,
                        icon = Icons.Filled.EmojiEvents,
                        gradientColors = listOf(CrownGoldTop, CrownGoldBottom),
                        borderColor = GoldMain.copy(alpha = 0.80f),
                        accentColor = GoldGlow,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("crown_istighfar_card")
                    )

                    CrownChampionCard(
                        title = stringResource(R.string.comp_crown_tasbih),
                        championName = tasbihChampion?.displayName
                            ?: stringResource(R.string.comp_waiting_leader),
                        score = tasbihChampion?.todayTotal ?: 0L,
                        icon = Icons.Filled.MilitaryTech,
                        gradientColors = listOf(EmeraldDeepBg, EmeraldDarkBg),
                        borderColor = EmeraldBorder.copy(alpha = 0.65f),
                        accentColor = EmeraldAccent,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("crown_tasbih_card")
                    )
                }
            }

            // 3 Filter Tabs
            item(key = "comp_filter_tabs") {
                GoldCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CompetitionFilterTab(
                            text = stringResource(R.string.comp_filter_today),
                            selected = uiState.competitionFilter == CompetitionFilter.TODAY,
                            onClick = { onSelectFilter(CompetitionFilter.TODAY) },
                            tag = "comp_filter_today",
                            modifier = Modifier.weight(1f)
                        )
                        CompetitionFilterTab(
                            text = stringResource(R.string.comp_filter_istighfar),
                            selected = uiState.competitionFilter == CompetitionFilter.ISTIGHFAR,
                            onClick = { onSelectFilter(CompetitionFilter.ISTIGHFAR) },
                            tag = "comp_filter_istighfar",
                            modifier = Modifier.weight(1f)
                        )
                        CompetitionFilterTab(
                            text = stringResource(R.string.comp_filter_alltime),
                            selected = uiState.competitionFilter == CompetitionFilter.ALL_TIME,
                            onClick = { onSelectFilter(CompetitionFilter.ALL_TIME) },
                            tag = "comp_filter_alltime",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (rankedEntries.isEmpty()) {
                item(key = "comp_empty") {
                    GoldCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.comp_no_competitors),
                            style = MaterialTheme.typography.bodyMedium.copy(color = MutedGold),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        )
                    }
                }
            } else {
                itemsIndexed(rankedEntries, key = { _, item -> item.userId }) { index, entry ->
                    val score = when (uiState.competitionFilter) {
                        CompetitionFilter.TODAY -> entry.todayTotal
                        CompetitionFilter.ISTIGHFAR -> entry.todayIstighfar
                        CompetitionFilter.ALL_TIME -> entry.allTimeTotal
                    }
                    LeaderboardRowCard(
                        rank = index + 1,
                        entry = entry,
                        score = score,
                        isCurrentUser = entry.userId == currentUserId
                    )
                }
            }
        }
    }
}

@Composable
private fun CrownChampionCard(
    title: String,
    championName: String,
    score: Long,
    icon: ImageVector,
    gradientColors: List<Color>,
    borderColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.heightIn(min = 132.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        border = BorderStroke(1.8.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .background(Brush.verticalGradient(gradientColors))
                .padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MutedGold,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Text(
                text = championName,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = CreamText,
                    fontWeight = FontWeight.ExtraBold
                ),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatCount(score),
                style = MaterialTheme.typography.titleLarge.copy(
                    color = accentColor,
                    fontWeight = FontWeight.Black,
                    fontFeatureSettings = "tnum"
                )
            )
        }
    }
}

@Composable
private fun CompetitionFilterTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    tag: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .heightIn(min = 42.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(if (selected) GoldMain else Color.Transparent)
            .clickable(onClick = onClick)
            .testTag(tag)
            .padding(horizontal = 6.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Bold,
                color = if (selected) OliveBg else MutedGold
            ),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun LeaderboardRowCard(
    rank: Int,
    entry: LeaderboardEntryDoc,
    score: Long,
    isCurrentUser: Boolean
) {
    val rankBadgeColor = when (rank) {
        1 -> GoldGlow
        2 -> Color(0xFFE2E8F0)
        3 -> Color(0xFFFB923C)
        else -> MutedGold
    }

    GoldCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        borderColor = if (isCurrentUser) GoldMain.copy(alpha = 0.72f) else GoldMain.copy(alpha = 0.22f),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Rank Circle
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            if (rank <= 3) rankBadgeColor.copy(alpha = 0.18f) else OliveLift
                        )
                        .border(
                            1.dp,
                            rankBadgeColor.copy(alpha = if (rank <= 3) 0.75f else 0.25f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (rank) {
                            1 -> "🥇"
                            2 -> "🥈"
                            3 -> "🥉"
                            else -> rank.toString()
                        },
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = rankBadgeColor,
                            fontWeight = FontWeight.Black
                        )
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = entry.displayName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = CreamText,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isCurrentUser) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(GoldMain.copy(alpha = 0.22f))
                                    .border(1.dp, GoldMain.copy(alpha = 0.6f), RoundedCornerShape(50))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.comp_you_badge),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = GoldGlow,
                                        fontWeight = FontWeight.Black
                                    )
                                )
                            }
                        }
                    }
                    Text(
                        text = "الإجمالي: ${formatCount(entry.allTimeTotal)}",
                        style = MaterialTheme.typography.labelSmall.copy(color = MutedGold)
                    )
                }
            }

            Text(
                text = formatCount(score),
                style = MaterialTheme.typography.titleLarge.copy(
                    color = GoldGlow,
                    fontWeight = FontWeight.Black,
                    fontFeatureSettings = "tnum"
                )
            )
        }
    }
}
