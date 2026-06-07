package com.youtubescript.app.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youtubescript.app.data.TranscriptRepository
import com.youtubescript.app.data.TranscriptResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UiState(
    val url: String = "",
    val isLoading: Boolean = false,
    val result: TranscriptResult? = null,
    val error: String? = null,
    val selectedTab: Int = 0 // 0 = YouTube, 1 = Local Video
)

class MainViewModel : ViewModel() {

    private val repository = TranscriptRepository()
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    fun onUrlChanged(url: String) {
        _state.value = _state.value.copy(url = url, error = null, result = null)
    }

    fun onTabSelected(tab: Int) {
        _state.value = _state.value.copy(selectedTab = tab, error = null, result = null, url = "")
    }

    fun fetchTranscript() {
        val url = _state.value.url.trim()
        if (url.isBlank()) {
            _state.value = _state.value.copy(error = "الرجاء إدخال رابط فيديو يوتيوب")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, result = null)
            val result = repository.getTranscript(url)
            _state.value = _state.value.copy(
                isLoading = false,
                result = result,
                error = result.error
            )
        }
    }

    fun fetchLocalVideoTranscript(uri: Uri) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, result = null)
            val result = repository.getLocalVideoTranscript(uri)
            _state.value = _state.value.copy(
                isLoading = false,
                result = result,
                error = result.error
            )
        }
    }

    fun clearResult() {
        _state.value = _state.value.copy(result = null, error = null)
    }
}
