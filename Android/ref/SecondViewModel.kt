package com.ms.seo.composefirst

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.*
import kotlinx.coroutines.launch

@Stable
data class UiState(
    val outWaterValveState: Boolean,
    val inWaterValveState: Boolean,
    val lightState: Boolean,
    val pumpState: Boolean,

    val resultText: String,
)

sealed class UiEvent(val value: Boolean) {
    class OutWaterEvent(enable: Boolean) : UiEvent(enable)
    class InWaterEvent(enable: Boolean) : UiEvent(enable)
    class LightEvent(enable: Boolean) : UiEvent(enable)
    class PumpEvent(enable: Boolean) : UiEvent(enable)
}

class SecondViewModel : ViewModel() {
    private val _uiState = MutableLiveData<UiState>()
    val uiState: LiveData<UiState>
        get() = _uiState

    fun fetchState() {
        viewModelScope.launch {
            // Some Fetches occur...
            _uiState.value = UiState(
                outWaterValveState = true,
                inWaterValveState = false,
                lightState = true,
                pumpState = true,
                "Fetch complete!"
            )
        }
    }

    fun uiEvent(uiEvent: UiEvent) {
        when (uiEvent) {
            is UiEvent.OutWaterEvent -> {
                _uiState.postValue(
                    UiState(
                        outWaterValveState = uiEvent.value,
                        inWaterValveState = !uiEvent.value,
                        lightState = true,
                        pumpState = true,
                        resultText = "${if (uiEvent.value) "Open" else "Close"} Out-Water valve!"
                    )
                )
            }
            is UiEvent.InWaterEvent -> {
                _uiState.postValue(
                    UiState(
                        outWaterValveState = !uiEvent.value,
                        inWaterValveState = uiEvent.value,
                        lightState = true,
                        pumpState = true,
                        resultText = "${if (uiEvent.value) "Open" else "Close"} In-Water valve!"
                    )
                )
            }
        }
    }
}

class SecondViewModelFactory() : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SecondViewModel::class.java)) {
            return SecondViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}