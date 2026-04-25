package com.vamsi.mlkitshowcase.data.scanner

import android.content.Context
import android.graphics.Rect
import android.net.Uri
import androidx.camera.core.ImageAnalysis
import androidx.camera.mlkit.vision.MlKitAnalyzer
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.vamsi.mlkitshowcase.domain.model.ScanResult
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class MLKitTextRecognizer @Inject constructor() {

    private val _scanResults = MutableSharedFlow<ScanResult>(
        extraBufferCapacity = 4,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val scanResults: Flow<ScanResult> = _scanResults.asSharedFlow()

    private val _detectedBounds = MutableStateFlow<List<Rect>>(emptyList())
    val detectedBounds: StateFlow<List<Rect>> = _detectedBounds.asStateFlow()

    private var activeRecognizer: TextRecognizer? = null
    private var isScanning = false

    fun startScanning() {
        _detectedBounds.value = emptyList()
        isScanning = true
    }

    fun stopScanning() {
        isScanning = false
        _detectedBounds.value = emptyList()
    }

    fun close() {
        stopScanning()
        activeRecognizer?.close()
        activeRecognizer = null
    }

    fun getImageAnalyzer(
        analysisExecutor: Executor = Executors.newSingleThreadExecutor(),
    ): ImageAnalysis.Analyzer {
        activeRecognizer?.close()
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        activeRecognizer = recognizer

        return MlKitAnalyzer(
            listOf(recognizer),
            ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED,
            analysisExecutor
        ) { result ->
            if (!isScanning) return@MlKitAnalyzer

            val throwable = result.getThrowable(recognizer)
            if (throwable != null) {
                _scanResults.tryEmit(ScanResult.Error(throwable.toRecognitionMessage(), throwable))
                return@MlKitAnalyzer
            }

            val text = result.getValue(recognizer)
            if (text == null || text.text.isBlank()) {
                _detectedBounds.value = emptyList()
                _scanResults.tryEmit(ScanResult.NoResult)
                return@MlKitAnalyzer
            }

            _detectedBounds.value = text.textBlocks.mapNotNull { it.boundingBox }
            _scanResults.tryEmit(ScanResult.TextResult(TextResultMapper.map(text)))
        }
    }

    fun scanImage(context: Context, uri: Uri) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val image = InputImage.fromFilePath(context, uri)
        recognizer.process(image)
            .addOnSuccessListener { text ->
                _detectedBounds.value = text.textBlocks.mapNotNull { it.boundingBox }
                _scanResults.tryEmit(
                    if (text.text.isBlank()) {
                        ScanResult.NoResult
                    } else {
                        ScanResult.TextResult(TextResultMapper.map(text))
                    }
                )
            }
            .addOnFailureListener { throwable ->
                _scanResults.tryEmit(ScanResult.Error(throwable.toRecognitionMessage(), throwable))
            }
            .addOnCompleteListener {
                recognizer.close()
            }
    }

    private fun Throwable.toRecognitionMessage(): String = when (this) {
        is MlKitException -> when (errorCode) {
            MlKitException.UNAVAILABLE -> "Text recognition is not available yet."
            MlKitException.NOT_ENOUGH_SPACE -> "Not enough storage for ML Kit text models."
            else -> "Text recognition failed: ${message ?: "unknown error"}"
        }

        else -> "Text recognition failed: ${message ?: "unknown error"}"
    }
}
