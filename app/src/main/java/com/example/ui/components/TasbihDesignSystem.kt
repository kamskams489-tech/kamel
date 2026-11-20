package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.GoldDeep
import com.example.ui.theme.GoldGlow
import com.example.ui.theme.GoldMain
import com.example.ui.theme.OliveBannerMid
import com.example.ui.theme.OliveBg
import com.example.ui.theme.OliveCard
import com.example.ui.theme.OliveCardDeep
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

fun Modifier.islamicSurface(): Modifier = this
    .background(OliveBg)
    .drawBehind {
        val spacing = 28.dp.toPx()
        val halfSpacing = spacing / 2f
        val primaryDotColor = GoldMain.copy(alpha = 0.085f)
        val secondaryDotColor = GoldGlow.copy(alpha = 0.04f)
        val radius = 1.15.dp.toPx()

        var y = 0f
        while (y < size.height + spacing) {
            var x = 0f
            while (x < size.width + spacing) {
                drawCircle(
                    color = primaryDotColor,
                    radius = radius,
                    center = Offset(x, y)
                )
                drawCircle(
                    color = secondaryDotColor,
                    radius = radius * 0.85f,
                    center = Offset(x + halfSpacing, y + halfSpacing)
                )
                x += spacing
            }
            y += spacing
        }
    }

@Composable
fun GoldCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    borderColor: Color = GoldMain.copy(alpha = 0.24f),
    contentPadding: PaddingValues = PaddingValues(14.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = Color.Transparent,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            OliveCard.copy(alpha = 0.96f),
                            OliveCardDeep.copy(alpha = 0.98f)
                        )
                    )
                )
                .padding(contentPadding),
            content = content
        )
    }
}

@Composable
fun GoldActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingContent: (@Composable () -> Unit)? = null
) {
    val shape = RoundedCornerShape(16.dp)
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = OliveBg,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = OliveBg.copy(alpha = 0.6f)
        ),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier.heightIn(min = 48.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clip(shape)
                .background(
                    Brush.linearGradient(
                        colors = if (enabled) {
                            listOf(GoldGlow, GoldMain, GoldDeep)
                        } else {
                            listOf(
                                GoldGlow.copy(alpha = 0.45f),
                                GoldMain.copy(alpha = 0.45f),
                                GoldDeep.copy(alpha = 0.45f)
                            )
                        }
                    )
                )
                .padding(horizontal = 18.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (leadingContent != null) {
                    leadingContent()
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = OliveBg
                    )
                )
            }
        }
    }
}

@Composable
fun DedicationBanner(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cdText = stringResource(R.string.cd_about_dedication)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(OliveCard, OliveBannerMid, OliveCard)
                )
            )
            .border(
                width = 0.75.dp,
                color = GoldMain.copy(alpha = 0.40f)
            )
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = cdText }
            .testTag("dedication_banner")
            .padding(horizontal = 12.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.dedication_banner_text),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = GoldGlow,
                lineHeight = 17.sp
            ),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun TasbihEmblemIcon(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val emblemCd = stringResource(R.string.cd_app_emblem)
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(14.dp))
            .background(OliveCard)
            .border(1.dp, GoldMain.copy(alpha = 0.42f), RoundedCornerShape(14.dp))
            .semantics { contentDescription = emblemCd },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.82f)) {
            val cx = this.size.width / 2f
            val cy = this.size.height / 2f
            val outerR = this.size.minDimension * 0.46f
            val innerR = this.size.minDimension * 0.34f
            val points = 16
            val path = Path()
            for (i in 0 until points) {
                val r = if (i % 2 == 0) outerR else innerR
                val angle = -PI / 2.0 + (2.0 * PI * i / points)
                val x = cx + (r * cos(angle)).toFloat()
                val y = cy + (r * sin(angle)).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(
                path = path,
                color = GoldMain.copy(alpha = 0.85f),
                style = Stroke(width = 1.6.dp.toPx())
            )
        }
        Text(
            text = "تَسْبِيح",
            fontFamily = AmiriFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.27f).sp,
            color = GoldGlow
        )
    }
}

fun parseHexColor(hex: String, fallback: Color = GoldMain): Color {
    return try {
        val cleaned = hex.removePrefix("#")
        val value = cleaned.toLong(16)
        when (cleaned.length) {
            6 -> Color(0xFF000000 or value)
            8 -> Color(value)
            else -> fallback
        }
    } catch (_: Exception) {
        fallback
    }
}

fun formatCount(value: Long): String {
    return NumberFormat.getNumberInstance(Locale.US).format(value)
}

fun formatCount(value: Int): String = formatCount(value.toLong())
