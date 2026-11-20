package com.example.ui

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CardAspectRatio
import com.example.data.CardImageSize
import com.example.data.DhikrCardAiRepository
import com.example.data.ImageQualityModel
import com.google.firebase.ai.type.QuotaExceededException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

sealed interface ImageGenUiState {
    data object Idle : ImageGenUiState
    data object Loading : ImageGenUiState
    data class Success(
        val bitmap: Bitmap,
        val model: ImageQualityModel,
        val imageSize: CardImageSize,
        val aspectRatio: CardAspectRatio
    ) : ImageGenUiState
    data class Error(val message: String) : ImageGenUiState
}

data class DhikrCardStudioState(
    val prompt: String = "بطاقة ذكر إسلامية فاخرة بنقوش هندسية ذهبية مضيئة وفانوس رمضاني هادئ على خلفية ليلية داكنة",
    val selectedModel: ImageQualityModel = ImageQualityModel.STUDIO_PRO,
    val selectedImageSize: CardImageSize = CardImageSize.SIZE_1K,
    val selectedAspectRatio: CardAspectRatio = CardAspectRatio.RATIO_1_1,
    val generationState: ImageGenUiState = ImageGenUiState.Idle
)

class DhikrCardViewModel(
    private val aiRepository: DhikrCardAiRepository = DhikrCardAiRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(DhikrCardStudioState())
    val state: StateFlow<DhikrCardStudioState> = _state.asStateFlow()

    fun updatePrompt(newPrompt: String) {
        _state.update { it.copy(prompt = newPrompt) }
    }

    fun selectModel(model: ImageQualityModel) {
        _state.update { it.copy(selectedModel = model) }
    }

    fun selectImageSize(size: CardImageSize) {
        _state.update { it.copy(selectedImageSize = size) }
    }

    fun selectAspectRatio(ratio: CardAspectRatio) {
        _state.update { it.copy(selectedAspectRatio = ratio) }
    }

    fun generateCard() {
        val current = _state.value
        val trimmedPrompt = current.prompt.trim()
        if (trimmedPrompt.isEmpty()) {
            _state.update {
                it.copy(generationState = ImageGenUiState.Error("يرجى كتابة وصف للبطاقة قبل التوليد."))
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(generationState = ImageGenUiState.Loading) }
            try {
                // Realistic 60s timeout window for high-resolution image synthesis
                val bitmap = withTimeout(60_000L) {
                    aiRepository.generateDhikrCard(
                        prompt = trimmedPrompt,
                        model = current.selectedModel,
                        imageSize = current.selectedImageSize,
                        aspectRatio = current.selectedAspectRatio
                    )
                }
                if (bitmap != null) {
                    _state.update {
                        it.copy(
                            generationState = ImageGenUiState.Success(
                                bitmap = bitmap,
                                model = current.selectedModel,
                                imageSize = current.selectedImageSize,
                                aspectRatio = current.selectedAspectRatio
                            )
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            generationState = ImageGenUiState.Error(
                                "لم يُرجع النموذج صورة. يرجى تعديل الوصف والمحاولة مرة أخرى."
                            )
                        )
                    }
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Image generation timed out", e)
                _state.update {
                    it.copy(
                        generationState = ImageGenUiState.Error(
                            "انتهت مهلة توليد الصورة (60 ثانية). جرّب دقة 1K أو أعد المحاولة."
                        )
                    )
                }
            } catch (e: QuotaExceededException) {
                Log.e(TAG, "Image generation quota exceeded", e)
                _state.update {
                    it.copy(
                        generationState = ImageGenUiState.Error(
                            "تم تجاوز حد الاستخدام حالياً. يرجى المحاولة لاحقاً أو مراجعة خطة الفوترة."
                        )
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Image generation failed", e)
                _state.update {
                    it.copy(
                        generationState = ImageGenUiState.Error(
                            e.localizedMessage ?: "فشل توليد البطاقة. تأكد من الاتصال بالإنترنت."
                        )
                    )
                }
            }
        }
    }

    companion object {
        private const val TAG = "DhikrCardVM"
    }
}
