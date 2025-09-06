package com.vamsi.mlkitshowcase.data.scanner

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.vamsi.mlkitshowcase.domain.model.BarcodeFormat
import com.vamsi.mlkitshowcase.domain.model.ScanResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ML Kit Barcode Scanner implementation
 *
 * This class demonstrates how to use ML Kit's on-device barcode scanning capabilities.
 *
 * Key features:
 * - On-device processing (no internet required after model download)
 * - Supports multiple barcode formats
 * - Real-time scanning with CameraX integration
 * - Automatic model download on first use
 *
 * Usage:
 * 1. Call startScanning() to begin
 * 2. Use getImageAnalyzer() with CameraX
 * 3. Collect results from scanResults flow
 * 4. Call stopScanning() when done
 */
@Singleton
class MLKitBarcodeScanner @Inject constructor() {

    // Configure scanner for common barcode formats
    private val scannerOptions = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            // 1D Barcodes
            Barcode.FORMAT_CODE_128,
            Barcode.FORMAT_CODE_39,
            Barcode.FORMAT_CODE_93,
            Barcode.FORMAT_CODABAR,
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_EAN_8,
            Barcode.FORMAT_ITF,
            Barcode.FORMAT_UPC_A,
            Barcode.FORMAT_UPC_E,
            // 2D Barcodes
            Barcode.FORMAT_QR_CODE,
            Barcode.FORMAT_DATA_MATRIX,
            Barcode.FORMAT_PDF417,
            Barcode.FORMAT_AZTEC
        )
        .build()

    private val scanner: BarcodeScanner by lazy {
        BarcodeScanning.getClient(scannerOptions)
    }

    private val _scanResults = Channel<ScanResult>(Channel.UNLIMITED)
    val scanResults: Flow<ScanResult> = _scanResults.receiveAsFlow()

    private var isScanning = false

    /**
     * Start the scanning process
     */
    fun startScanning() {
        isScanning = true
    }

    /**
     * Stop the scanning process
     */
    fun stopScanning() {
        isScanning = false
    }

    /**
     * Get ImageAnalysis.Analyzer for CameraX integration
     */
    fun getImageAnalyzer(): ImageAnalysis.Analyzer {
        return BarcodeAnalyzer(scanner) { result ->
            if (isScanning) {
                _scanResults.trySend(result)
            }
        }
    }

    /**
     * Convert ML Kit barcode format to our domain format
     */
    private fun Int.toBarcodeFormat(): BarcodeFormat = when (this) {
        Barcode.FORMAT_CODE_128 -> BarcodeFormat.CODE_128
        Barcode.FORMAT_CODE_39 -> BarcodeFormat.CODE_39
        Barcode.FORMAT_CODE_93 -> BarcodeFormat.CODE_93
        Barcode.FORMAT_CODABAR -> BarcodeFormat.CODABAR
        Barcode.FORMAT_DATA_MATRIX -> BarcodeFormat.DATA_MATRIX
        Barcode.FORMAT_EAN_13 -> BarcodeFormat.EAN_13
        Barcode.FORMAT_EAN_8 -> BarcodeFormat.EAN_8
        Barcode.FORMAT_ITF -> BarcodeFormat.ITF
        Barcode.FORMAT_QR_CODE -> BarcodeFormat.QR_CODE
        Barcode.FORMAT_UPC_A -> BarcodeFormat.UPC_A
        Barcode.FORMAT_UPC_E -> BarcodeFormat.UPC_E
        Barcode.FORMAT_PDF417 -> BarcodeFormat.PDF417
        Barcode.FORMAT_AZTEC -> BarcodeFormat.AZTEC
        else -> BarcodeFormat.UNKNOWN
    }

    /**
     * ImageAnalysis.Analyzer implementation for barcode scanning
     */
    private class BarcodeAnalyzer(
        private val scanner: BarcodeScanner,
        private val onResult: (ScanResult) -> Unit,
    ) : ImageAnalysis.Analyzer {

        private var lastAnalyzedTimestamp = 0L
        private val analyzeEveryMs = 100L // Process every 100ms for battery optimization

        @androidx.camera.core.ExperimentalGetImage
        override fun analyze(imageProxy: ImageProxy) {
            val currentTimestamp = System.currentTimeMillis()
            if (currentTimestamp - lastAnalyzedTimestamp < analyzeEveryMs) {
                imageProxy.close()
                return
            }
            lastAnalyzedTimestamp = currentTimestamp
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(
                    mediaImage,
                    imageProxy.imageInfo.rotationDegrees
                )

                scanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        if (barcodes.isNotEmpty()) {
                            val barcode = barcodes[0] // Take first barcode found
                            val result = ScanResult.BarcodeResult(
                                value = barcode.displayValue ?: barcode.rawValue ?: "",
                                format = barcode.format.toBarcodeFormat(),
                                rawBytes = barcode.rawBytes
                            )
                            onResult(result)
                        } else {
                            onResult(ScanResult.NoResult)
                        }
                    }
                    .addOnFailureListener { exception ->
                        val errorMessage = when (exception) {
                            is MlKitException -> when (exception.errorCode) {
                                MlKitException.UNAVAILABLE -> "Downloading scanner model, please wait..."
                                MlKitException.NOT_ENOUGH_SPACE -> "Not enough storage for ML models"
                                else -> "Scanning failed: ${exception.message}"
                            }

                            else -> "Scanning failed: ${exception.message}"
                        }
                        onResult(ScanResult.Error(errorMessage, exception))
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            } else {
                imageProxy.close()
            }
        }

        private fun Int.toBarcodeFormat(): BarcodeFormat = when (this) {
            Barcode.FORMAT_CODE_128 -> BarcodeFormat.CODE_128
            Barcode.FORMAT_CODE_39 -> BarcodeFormat.CODE_39
            Barcode.FORMAT_CODE_93 -> BarcodeFormat.CODE_93
            Barcode.FORMAT_CODABAR -> BarcodeFormat.CODABAR
            Barcode.FORMAT_DATA_MATRIX -> BarcodeFormat.DATA_MATRIX
            Barcode.FORMAT_EAN_13 -> BarcodeFormat.EAN_13
            Barcode.FORMAT_EAN_8 -> BarcodeFormat.EAN_8
            Barcode.FORMAT_ITF -> BarcodeFormat.ITF
            Barcode.FORMAT_QR_CODE -> BarcodeFormat.QR_CODE
            Barcode.FORMAT_UPC_A -> BarcodeFormat.UPC_A
            Barcode.FORMAT_UPC_E -> BarcodeFormat.UPC_E
            Barcode.FORMAT_PDF417 -> BarcodeFormat.PDF417
            Barcode.FORMAT_AZTEC -> BarcodeFormat.AZTEC
            else -> BarcodeFormat.UNKNOWN
        }
    }
}