package com.vamsi.mlkitshowcase.presentation.text

import androidx.camera.core.ImageAnalysis
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vamsi.mlkitshowcase.data.scanner.MLKitTextRecognizer
import com.vamsi.mlkitshowcase.domain.model.ScanResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for Text Recognition Screen
 *
 * Manages the state of text recognition and coordinates between the UI and the ML Kit text
 * recognizer.
 */
@HiltViewModel
class TextRecognitionViewModel
@Inject
constructor(private val textRecognizer: MLKitTextRecognizer) : ViewModel() {

    private val _uiState = MutableStateFlow<TextRecognitionUiState>(TextRecognitionUiState.Scanning)
    val uiState: StateFlow<TextRecognitionUiState> = _uiState.asStateFlow()

    init {
        // Collect scan results from the text recognizer
        viewModelScope.launch {
            textRecognizer.scanResults.collect { result ->
                when (result) {
                    is ScanResult.TextResult -> {
                        // Only show results with meaningful text (more than just whitespace)
                        if (result.text.trim().length > 3) {
                            // Stop scanning and show result
                            textRecognizer.stopScanning()
                            _uiState.value = TextRecognitionUiState.Success(result)
                        }
                        // Continue scanning for longer text
                    }

                    is ScanResult.Error -> {
                        // Show error dialog
                        _uiState.value = TextRecognitionUiState.Error(result.message)
                    }

                    is ScanResult.NoResult, is ScanResult.BarcodeResult -> {
                        // Continue scanning for text results
                    }
                }
            }
        }
    }

    /** Start scanning for text */
    fun startScanning() {
        textRecognizer.startScanning()
        _uiState.value = TextRecognitionUiState.Scanning
    }

    /** Stop scanning */
    fun stopScanning() {
        textRecognizer.stopScanning()
    }

    /** Resume scanning after showing a result or error */
    fun resumeScanning() {
        startScanning()
    }

    /** Get the image analyzer for CameraX integration */
    suspend fun getImageAnalyzer(): ImageAnalysis.Analyzer {
        return textRecognizer.getImageAnalyzer()
    }
}

/** UI State for Text Recognition */
sealed class TextRecognitionUiState {
    data object Scanning : TextRecognitionUiState()
    data class Success(val result: ScanResult.TextResult) : TextRecognitionUiState()
    data class Error(val message: String) : TextRecognitionUiState()
}
