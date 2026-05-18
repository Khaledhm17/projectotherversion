package com.example.projectotherversion.presentation.screens.rating

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectotherversion.domain.model.Rating
import com.example.projectotherversion.domain.usecase.rating.SubmitRatingUseCase
import com.example.projectotherversion.domain.usecase.user.GetCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class RatingViewModel @Inject constructor(
    private val submitRatingUseCase: SubmitRatingUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RatingState())
    val state = _state.asStateFlow()

    fun onRatingChanged(rating: Int) {
        _state.update { it.copy(rating = rating, error = null) }
    }

    fun submitRating(artisanId: String) {
        // التحقق من اختيار التقييم قبل البدء
        if (_state.value.rating == 0) {
            _state.update { it.copy(error = "يرجى اختيار عدد النجوم أولاً") }
            return
        }

        viewModelScope.launch {
            Log.d("RATING_VM", "Starting submitRating for artisan: $artisanId")
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                // نقل العمليات الثقيلة والشبكة إلى خيط الـ IO لتجنب Skipped frames
                val result = withContext(Dispatchers.IO) {
                    val currentUser = getCurrentUserUseCase()

                    if (currentUser == null) {
                        return@withContext Result.failure(Exception("يجب تسجيل الدخول للتقييم"))
                    }

                    val ratingObj = Rating(
                        artisanId = artisanId,
                        customerId = currentUser.id,
                        rating = _state.value.rating,
                        createdAt = System.currentTimeMillis()
                    )

                    Log.d("RATING_VM", "Sending rating to Supabase: ${ratingObj.rating}")
                    // هنا يتم إرسال التقييم فقط، والـ Trigger في Supabase سيتكفل بالباقي
                    submitRatingUseCase(ratingObj)
                }

                result.onSuccess {
                    Log.d("RATING_VM", "Rating submitted successfully")
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true,
                            error = null
                        )
                    }

                    // تأخير بسيط لضمان ملاحظة الواجهة لحالة النجاح قبل التصفير
                    delay(500)
                    resetSuccessState()

                }.onFailure { e ->
                    Log.e("RATING_VM", "Submission failed: ${e.message}")
                    _state.update { it.copy(isLoading = false, error = e.message ?: "فشل إرسال التقييم") }
                }

            } catch (e: Exception) {
                Log.e("RATING_VM", "Unexpected error: ${e.message}")
                _state.update { it.copy(isLoading = false, error = "حدث خطأ في الاتصال بالخادم") }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    private fun resetSuccessState() {
        _state.update { it.copy(isSuccess = false) }
    }
}

data class RatingState(
    val rating: Int = 0,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)