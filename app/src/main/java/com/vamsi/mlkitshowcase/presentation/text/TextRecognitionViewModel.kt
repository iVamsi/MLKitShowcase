package com.vamsi.mlkitshowcase.presentation.text

import android.content.Context
import android.net.Uri
import androidx.camera.core.ImageAnalysis
import androidx.camera.view.LifecycleCameraController
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vamsi.mlkitshowcase.data.scanner.MLKitTextRecognizer
import com.vamsi.mlkitshowcase.domain.model.ScanHistoryEntry
import com.vamsi.mlkitshowcase.domain.model.ScanHistoryStore
import com.vamsi.mlkitshowcase.domain.model.ScanResult
import com.vamsi.mlkitshowcase.domain.model.ScannerMode
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
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
constructor(
    private val textRecognizer: MLKitTextRecognizer,
    private val historyStore: ScanHistoryStore = ScanHistoryStore(),
) : ViewModel() {

    private val _uiState = MutableStateFlow<TextRecognitionUiState>(TextRecognitionUiState.Scanning)
    val uiState: StateFlow<TextRecognitionUiState> = _uiState.asStateFlow()
    val detectedBounds = textRecognizer.detectedBounds

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
                            historyStore.add(
                                ScanHistoryEntry(
                                    mode = ScannerMode.TEXT,
                                    title = result.text.lineSequence().firstOrNull().orEmpty()
                                        .take(80),
                                    subtitle = "${result.scan.lineCount} lines, ${result.scan.blocks.size} blocks"
                                )
                            )
                            _uiState.value = TextRecognitionUiState.Success(result)
                        }
                        // Continue scanning for longer text
                    }

                    is ScanResult.Error -> {
                        // Show error dialog
                        _uiState.value = TextRecognitionUiState.Error(result.message)
                    }

                    is ScanResult.NoResult,
                    is ScanResult.BarcodeResult,
                    is ScanResult.DocumentResult -> {
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

    fun scanImage(context: Context, uri: Uri) {
        textRecognizer.stopScanning()
        _uiState.value = TextRecognitionUiState.Scanning
        textRecognizer.scanImage(context, uri)
    }

    /** Resume scanning after showing a result or error */
    fun resumeScanning() {
        startScanning()
    }

    /** Get the image analyzer for CameraX integration */
    suspend fun getImageAnalyzer(): ImageAnalysis.Analyzer {
        return textRecognizer.getImageAnalyzer(
            analysisExecutor = Executors.newSingleThreadExecutor()
        )
    }

    fun getImageAnalyzer(
        analysisExecutor: ExecutorService,
        @Suppress("UNUSED_PARAMETER") cameraController: LifecycleCameraController,
    ): ImageAnalysis.Analyzer {
        return textRecognizer.getImageAnalyzer(analysisExecutor = analysisExecutor)
    }

    override fun onCleared() {
        textRecognizer.close()
        super.onCleared()
    }
}

/** UI State for Text Recognition */
sealed class TextRecognitionUiState {
    data object Scanning : TextRecognitionUiState()
    data class Success(val result: ScanResult.TextResult) : TextRecognitionUiState()
    data class Error(val message: String) : TextRecognitionUiState()
}
