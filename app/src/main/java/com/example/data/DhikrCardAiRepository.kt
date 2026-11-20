package com.example.data

import android.graphics.Bitmap
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.AspectRatio
import com.google.firebase.ai.type.ImageConfig
import com.google.firebase.ai.type.ImagePart
import com.google.firebase.ai.type.ImageSize
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.generationConfig

enum class ImageQualityModel(
    val modelId: String,
    val labelAr: String,
    val badge: String
) {
    STUDIO_PRO(
        modelId = "gemini-3-pro-image-preview",
        labelAr = "جودة استوديو",
        badge = "Pro"
    ),
    FAST_STANDARD(
        modelId = "gemini-nano-banana-2.1",
        labelAr = "توليد سريع",
        badge = "Fast"
    )
}

enum class CardImageSize(
    val apiValue: String,
    val label: String,
    val sdkSize: ImageSize
) {
    SIZE_1K("1K", "1K", ImageSize.SIZE_1K),
    SIZE_2K("2K", "2K", ImageSize.SIZE_2K),
    SIZE_4K("4K", "4K", ImageSize.SIZE_4K)
}

enum class CardAspectRatio(
    val apiValue: String,
    val label: String,
    val widthUnits: Float,
    val heightUnits: Float,
    val sdkRatio: AspectRatio
) {
    RATIO_1_1("1:1", "1:1", 1f, 1f, AspectRatio.SQUARE_1x1),
    RATIO_2_3("2:3", "2:3", 2f, 3f, AspectRatio.PORTRAIT_2x3),
    RATIO_3_2("3:2", "3:2", 3f, 2f, AspectRatio.LANDSCAPE_3x2),
    RATIO_3_4("3:4", "3:4", 3f, 4f, AspectRatio.PORTRAIT_3x4),
    RATIO_4_3("4:3", "4:3", 4f, 3f, AspectRatio.LANDSCAPE_4x3),
    RATIO_9_16("9:16", "9:16", 9f, 16f, AspectRatio.PORTRAIT_9x16),
    RATIO_16_9("16:9", "16:9", 16f, 9f, AspectRatio.LANDSCAPE_16x9),
    RATIO_21_9("21:9", "21:9", 21f, 9f, AspectRatio.LANDSCAPE_21x9)
}

class DhikrCardAiRepository {
    suspend fun generateDhikrCard(
        prompt: String,
        model: ImageQualityModel,
        imageSize: CardImageSize,
        aspectRatio: CardAspectRatio
    ): Bitmap? {
        val config = generationConfig {
            responseModalities = listOf(ResponseModality.IMAGE)
            imageConfig = ImageConfig.builder()
                .setAspectRatio(aspectRatio.sdkRatio)
                .setImageSize(imageSize.sdkSize)
                .build()
        }

        val generativeModel = Firebase.ai.generativeModel(
            modelName = model.modelId,
            generationConfig = config
        )

        val enrichedPrompt = buildString {
            append("Create a refined, studio-quality Islamic spiritual artwork and dhikr card. ")
            append("Aesthetic: nocturnal deep dark olive-black (#0E0B02) atmosphere with luminous brushed metallic gold (#D4AF37 and #F3D26A) geometric arabesque ornamentation and serene spiritual lighting. ")
            append("User request: ")
            append(prompt.trim())
        }

        val response = generativeModel.generateContent(enrichedPrompt)
        val imagePart = response.candidates.firstOrNull()
            ?.content?.parts?.filterIsInstance<ImagePart>()
            ?.firstOrNull()

        return imagePart?.image
    }
}
