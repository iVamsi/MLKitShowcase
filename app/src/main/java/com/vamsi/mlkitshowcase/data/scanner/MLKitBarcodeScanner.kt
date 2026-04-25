package com.vamsi.mlkitshowcase.data.scanner

import android.content.Context
import android.graphics.Rect
import android.net.Uri
import androidx.camera.core.ImageAnalysis
import androidx.camera.mlkit.vision.MlKitAnalyzer
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.ZoomSuggestionOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
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

class MLKitBarcodeScanner @Inject constructor() {

    private val _scanResults = MutableSharedFlow<ScanResult>(
        extraBufferCapacity = 4,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val scanResults: Flow<ScanResult> = _scanResults.asSharedFlow()

    private val _detectedBounds = MutableStateFlow<List<Rect>>(emptyList())
    val detectedBounds: StateFlow<List<Rect>> = _detectedBounds.asStateFlow()

    private var activeScanner: BarcodeScanner? = null
    private var isScanning = false
    private val resultGate = ConsecutiveBarcodeResultGate(requiredMatches = 2)

    fun startScanning() {
        resultGate.reset()
        _detectedBounds.value = emptyList()
        isScanning = true
    }

    fun stopScanning() {
        isScanning = false
        _detectedBounds.value = emptyList()
    }

    fun close() {
        stopScanning()
        activeScanner?.close()
        activeScanner = null
    }

    fun getImageAnalyzer(
        analysisExecutor: Executor = Executors.newSingleThreadExecutor(),
        maxSupportedZoomRatio: Float = 1f,
        onZoomSuggestion: (Float) -> Boolean = { false },
    ): ImageAnalysis.Analyzer {
        activeScanner?.close()
        val scanner = BarcodeScanning.getClient(
            scannerOptions(
                maxSupportedZoomRatio = maxSupportedZoomRatio,
                onZoomSuggestion = onZoomSuggestion
            )
        )
        activeScanner = scanner

        return MlKitAnalyzer(
            listOf(scanner),
            ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED,
            analysisExecutor
        ) { result ->
            if (!isScanning) return@MlKitAnalyzer

            val throwable = result.getThrowable(scanner)
            if (throwable != null) {
                _scanResults.tryEmit(ScanResult.Error(throwable.toScanMessage(), throwable))
                return@MlKitAnalyzer
            }

            val barcodeResults = result.getValue(scanner)
                .orEmpty()
                .map(BarcodeResultMapper::map)
            _detectedBounds.value = barcodeResults.mapNotNull { it.boundingBox }

            val barcodeResult = barcodeResults.firstOrNull { it.value.isNotBlank() }

            if (barcodeResult == null) {
                _scanResults.tryEmit(ScanResult.NoResult)
                return@MlKitAnalyzer
            }

            resultGate.accept(barcodeResult)?.let {
                _scanResults.tryEmit(ScanResult.BarcodeResult(it))
            }
        }
    }

    fun scanImage(context: Context, uri: Uri) {
        val scanner = BarcodeScanning.getClient(scannerOptions())
        val image = InputImage.fromFilePath(context, uri)
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                val barcodeResults = barcodes.map(BarcodeResultMapper::map)
                _detectedBounds.value = barcodeResults.mapNotNull { it.boundingBox }
                val barcode = barcodeResults
                    .firstOrNull { it.value.isNotBlank() }
                _scanResults.tryEmit(
                    barcode?.let { ScanResult.BarcodeResult(it) } ?: ScanResult.NoResult
                )
            }
            .addOnFailureListener { throwable ->
                _scanResults.tryEmit(ScanResult.Error(throwable.toScanMessage(), throwable))
            }
            .addOnCompleteListener {
                scanner.close()
            }
    }

    private fun scannerOptions(
        maxSupportedZoomRatio: Float = 1f,
        onZoomSuggestion: (Float) -> Boolean = { false },
    ): BarcodeScannerOptions {
        val builder = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_CODE_128,
                Barcode.FORMAT_CODE_39,
                Barcode.FORMAT_CODE_93,
                Barcode.FORMAT_CODABAR,
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_ITF,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_UPC_E,
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_DATA_MATRIX,
                Barcode.FORMAT_PDF417,
                Barcode.FORMAT_AZTEC
            )
            .enableAllPotentialBarcodes()

        if (maxSupportedZoomRatio > 1f) {
            builder.setZoomSuggestionOptions(
                ZoomSuggestionOptions.Builder(onZoomSuggestion)
                    .setMaxSupportedZoomRatio(maxSupportedZoomRatio)
                    .build()
            )
        }

        return builder.build()
    }

    private fun Throwable.toScanMessage(): String = when (this) {
        is MlKitException -> when (errorCode) {
            MlKitException.UNAVAILABLE -> "Barcode scanner is not available yet."
            MlKitException.NOT_ENOUGH_SPACE -> "Not enough storage for ML Kit barcode models."
            else -> "Barcode scanning failed: ${message ?: "unknown error"}"
        }

        else -> "Barcode scanning failed: ${message ?: "unknown error"}"
    }
}
