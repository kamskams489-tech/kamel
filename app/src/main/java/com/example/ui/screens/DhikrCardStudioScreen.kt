package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.CardAspectRatio
import com.example.data.CardImageSize
import com.example.data.CardMediaHelper
import com.example.data.ImageQualityModel
import com.example.ui.DhikrCardViewModel
import com.example.ui.ImageGenUiState
import com.example.ui.components.GoldActionButton
import com.example.ui.components.GoldCard
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.CreamText
import com.example.ui.theme.ErrorContainerDark
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.GoldGlow
import com.example.ui.theme.GoldMain
import com.example.ui.theme.MutedGold
import com.example.ui.theme.OliveBg
import com.example.ui.theme.OliveCard
import com.example.ui.theme.OliveLift
import kotlinx.coroutines.launch

private data class StudioPreset(
    val id: String,
    val label: String,
    val prompt: String
)

@Composable
fun DhikrCardStudioScreen(
    activeDhikrTitle: String,
    viewModel: DhikrCardViewModel,
    onShowMessage: (String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val savedSuccessText = stringResource(R.string.studio_saved_success)

    val presets = remember(activeDhikrTitle) {
        listOf(
            StudioPreset(
                id = "active_dhikr",
                label = "الذكر الحالي",
                prompt = "بطاقة ذكر إسلامية فاخرة تحمل خطاً عربياً مزخرفاً بالذهب لذكر «$activeDhikrTitle» وسط إطار هندسي أندلسي مضيء على خلفية ليلية داكنة"
            ),
            StudioPreset(
                id = "sadaqah_card",
                label = "بطاقة صدقة جارية",
                prompt = "تصميم بطاقة إسلامية راقية للصدقة الجارية مع زخارف ذهبية وهلال مضيء وفوانيس دافئة وأجواء روحانية هادئة"
            ),
            StudioPreset(
                id = "mosque_night",
                label = "مسجد ليلي ذهبي",
                prompt = "منظر معماري روحاني لصحن مسجد تاريخي ليلاً تحت سماء مرصعة بالنجوم وهلال مضيء مع قناديل ذهبية متوهجة"
            ),
            StudioPreset(
                id = "friday_blessing",
                label = "جمعة مباركة",
                prompt = "بطاقة تهنئة إسلامية فاخرة ليوم الجمعة المبارك بزخارف نباتية وهندسية مذهبة ونور فجري دافئ"
            ),
            StudioPreset(
                id = "tasbih_wallpaper",
                label = "خلفية سبحة ذهبية",
                prompt = "تصوير فني استوديوي لسبحة كهرمان وذهب فاخرة فوق سجادة صلاة مخملية داكنة مع إضاءة سينمائية دافئة"
            )
        )
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 580.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Atmospheric Hero Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(126.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .border(1.dp, GoldMain.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_studio_banner_1795168460275),
                    contentDescription = stringResource(R.string.cd_studio_banner),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    OliveBg.copy(alpha = 0.35f),
                                    OliveBg.copy(alpha = 0.88f)
                                )
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp)
                ) {
                    Text(
                        text = stringResource(R.string.studio_title),
                        fontFamily = AmiriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = GoldGlow
                    )
                    Text(
                        text = stringResource(R.string.studio_subtitle),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = CreamText.copy(alpha = 0.88f)
                        )
                    )
                }
            }

            // Quick Islamic Prompt Presets
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(presets, key = { it.id }) { preset ->
                    Surface(
                        onClick = { viewModel.updatePrompt(preset.prompt) },
                        shape = RoundedCornerShape(50),
                        color = OliveCard,
                        border = BorderStroke(1.dp, GoldMain.copy(alpha = 0.32f)),
                        modifier = Modifier
                            .heightIn(min = 38.dp)
                            .testTag("studio_preset_${preset.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = GoldGlow,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = preset.label,
                                style = MaterialTheme.typography.labelMedium.copy(color = CreamText)
                            )
                        }
                    }
                }
            }

            // Prompt & Generation Controls Card
            GoldCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp)
            ) {
                OutlinedTextField(
                    value = state.prompt,
                    onValueChange = viewModel::updatePrompt,
                    label = { Text(stringResource(R.string.studio_prompt_label)) },
                    placeholder = {
                        Text(
                            stringResource(R.string.studio_prompt_hint),
                            color = MutedGold.copy(alpha = 0.65f)
                        )
                    },
                    minLines = 2,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldMain,
                        unfocusedBorderColor = GoldMain.copy(alpha = 0.34f),
                        focusedTextColor = CreamText,
                        unfocusedTextColor = CreamText,
                        focusedLabelColor = GoldGlow,
                        unfocusedLabelColor = MutedGold,
                        cursorColor = GoldGlow
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_prompt_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Model Quality Selector (Studio Pro = gemini-3-pro-image-preview | Fast = gemini-nano-banana-2.1)
                Text(
                    text = stringResource(R.string.studio_quality_label),
                    style = MaterialTheme.typography.labelMedium.copy(color = MutedGold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("model_selector"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ImageQualityModel.entries.forEach { model ->
                        val selected = state.selectedModel == model
                        Surface(
                            onClick = { viewModel.selectModel(model) },
                            shape = RoundedCornerShape(14.dp),
                            color = if (selected) GoldMain else OliveBg,
                            border = BorderStroke(
                                1.dp,
                                if (selected) GoldGlow else GoldMain.copy(alpha = 0.28f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("model_${model.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "${model.labelAr} (${model.badge})",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = if (selected) OliveBg else CreamText,
                                        fontWeight = if (selected) FontWeight.Black else FontWeight.Bold
                                    ),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Image Size Selector: 1K, 2K, 4K (REQUIRED AFFORDANCE)
                Text(
                    text = stringResource(R.string.studio_size_label),
                    style = MaterialTheme.typography.labelMedium.copy(color = MutedGold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("size_selector"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CardImageSize.entries.forEach { sizeOption ->
                        val selected = state.selectedImageSize == sizeOption
                        Surface(
                            onClick = { viewModel.selectImageSize(sizeOption) },
                            shape = RoundedCornerShape(14.dp),
                            color = if (selected) GoldMain else OliveBg,
                            border = BorderStroke(
                                1.dp,
                                if (selected) GoldGlow else GoldMain.copy(alpha = 0.28f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("size_${sizeOption.label.lowercase()}")
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = sizeOption.label,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = if (selected) OliveBg else GoldGlow,
                                        fontWeight = FontWeight.Black
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Aspect Ratio Selector: 1:1, 2:3, 3:2, 3:4, 4:3, 9:16, 16:9, 21:9 (REQUIRED AFFORDANCE)
                Text(
                    text = stringResource(R.string.studio_aspect_label),
                    style = MaterialTheme.typography.labelMedium.copy(color = MutedGold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("aspect_ratio_selector"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(CardAspectRatio.entries, key = { it.apiValue }) { ratio ->
                        val selected = state.selectedAspectRatio == ratio
                        Surface(
                            onClick = { viewModel.selectAspectRatio(ratio) },
                            shape = RoundedCornerShape(14.dp),
                            color = if (selected) GoldMain else OliveBg,
                            border = BorderStroke(
                                1.dp,
                                if (selected) GoldGlow else GoldMain.copy(alpha = 0.28f)
                            ),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("aspect_${ratio.apiValue.replace(":", "_")}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Proportional visual aspect ratio frame icon
                                val maxSide = 16f
                                val scale = maxSide / maxOf(ratio.widthUnits, ratio.heightUnits)
                                Box(
                                    modifier = Modifier
                                        .size(
                                            width = (ratio.widthUnits * scale).coerceAtLeast(6f).dp,
                                            height = (ratio.heightUnits * scale).coerceAtLeast(6f).dp
                                        )
                                        .border(
                                            width = 1.5.dp,
                                            color = if (selected) OliveBg else GoldGlow,
                                            shape = RoundedCornerShape(2.dp)
                                        )
                                )
                                Text(
                                    text = ratio.label,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = if (selected) OliveBg else CreamText,
                                        fontWeight = if (selected) FontWeight.Black else FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val isGenerating = state.generationState is ImageGenUiState.Loading
                GoldActionButton(
                    text = stringResource(R.string.studio_generate_btn),
                    enabled = !isGenerating,
                    onClick = { viewModel.generateCard() },
                    leadingContent = {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = OliveBg,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = OliveBg,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("generate_card_button")
                )
            }

            // Generation Result Area
            when (val genState = state.generationState) {
                is ImageGenUiState.Idle -> {
                    GoldCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 22.dp, horizontal = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = GoldMain.copy(alpha = 0.7f),
                                modifier = Modifier.size(34.dp)
                            )
                            Text(
                                text = stringResource(R.string.studio_idle_hint),
                                style = MaterialTheme.typography.bodyMedium.copy(color = MutedGold),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                is ImageGenUiState.Loading -> {
                    StudioShimmerLoadingCard(aspectRatio = state.selectedAspectRatio)
                }

                is ImageGenUiState.Error -> {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("studio_error_card"),
                        shape = RoundedCornerShape(20.dp),
                        color = ErrorContainerDark,
                        border = BorderStroke(1.dp, ErrorCrimson.copy(alpha = 0.55f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ErrorOutline,
                                contentDescription = null,
                                tint = ErrorCrimson,
                                modifier = Modifier.size(30.dp)
                            )
                            Text(
                                text = genState.message,
                                style = MaterialTheme.typography.bodyMedium.copy(color = CreamText),
                                textAlign = TextAlign.Center
                            )
                            OutlinedButton(
                                onClick = { viewModel.generateCard() },
                                border = BorderStroke(1.dp, GoldMain),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("studio_retry_button")
                            ) {
                                Text(
                                    text = stringResource(R.string.studio_retry_btn),
                                    color = GoldGlow,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                is ImageGenUiState.Success -> {
                    GoldCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("studio_result_card"),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${genState.model.labelAr} • ${genState.imageSize.label} • ${genState.aspectRatio.label}",
                                style = MaterialTheme.typography.labelMedium.copy(color = GoldGlow)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        val imageBitmap = remember(genState.bitmap) {
                            genState.bitmap.asImageBitmap()
                        }
                        val previewRatio = (genState.aspectRatio.widthUnits / genState.aspectRatio.heightUnits)
                            .coerceIn(0.55f, 2.4f)
                        Image(
                            bitmap = imageBitmap,
                            contentDescription = stringResource(R.string.cd_generated_card),
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(previewRatio)
                                .clip(RoundedCornerShape(16.dp))
                                .background(OliveBg)
                                .border(1.dp, GoldMain.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            GoldActionButton(
                                text = stringResource(R.string.studio_share_btn),
                                onClick = {
                                    scope.launch {
                                        CardMediaHelper.shareBitmap(
                                            context = context,
                                            bitmap = genState.bitmap,
                                            title = "بطاقة ذكر من تطبيق تسبيح — صدقة جارية"
                                        ).onFailure { err ->
                                            onShowMessage(err.localizedMessage ?: "تعذرت المشاركة")
                                        }
                                    }
                                },
                                leadingContent = {
                                    Icon(
                                        imageVector = Icons.Filled.Share,
                                        contentDescription = null,
                                        tint = OliveBg,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("studio_share_button")
                            )

                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        CardMediaHelper.saveBitmapToGallery(
                                            context = context,
                                            bitmap = genState.bitmap
                                        ).onSuccess {
                                            onShowMessage(savedSuccessText)
                                        }.onFailure { err ->
                                            onShowMessage(err.localizedMessage ?: "تعذر حفظ الصورة")
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, GoldMain.copy(alpha = 0.65f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("studio_save_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Download,
                                    contentDescription = null,
                                    tint = GoldGlow,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.studio_save_btn),
                                    color = GoldGlow,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun StudioShimmerLoadingCard(aspectRatio: CardAspectRatio) {
    val transition = rememberInfiniteTransition(label = "studioShimmer")
    val shimmerOffset by transition.animateFloat(
        initialValue = -300f,
        targetValue = 900f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerX"
    )
    val previewRatio = (aspectRatio.widthUnits / aspectRatio.heightUnits).coerceIn(0.75f, 2.2f)

    GoldCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("studio_loading_card"),
        shape = RoundedCornerShape(22.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(previewRatio)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            OliveLift,
                            GoldMain.copy(alpha = 0.22f),
                            OliveLift
                        ),
                        start = Offset(shimmerOffset, 0f),
                        end = Offset(shimmerOffset + 320f, 320f)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(20.dp)
            ) {
                CircularProgressIndicator(
                    color = GoldGlow,
                    trackColor = OliveBg,
                    strokeWidth = 3.dp
                )
                Text(
                    text = stringResource(R.string.studio_generating_msg),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = CreamText,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
