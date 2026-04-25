package com.vamsi.mlkitshowcase.presentation.barcode

import android.content.Context
import android.net.Uri
import androidx.camera.core.ImageAnalysis
import androidx.camera.view.LifecycleCameraController
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vamsi.mlkitshowcase.data.scanner.MLKitBarcodeScanner
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
 * ViewModel for Barcode Scanner Screen
 *
 * Manages the state of barcode scanning and coordinates between the UI and the ML Kit scanner.
 */
@HiltViewModel
class BarcodeScannerViewModel @Inject constructor(
    private val barcodeScanner: MLKitBarcodeScanner,
    private val historyStore: ScanHistoryStore = ScanHistoryStore(),
) :
    ViewModel() {

    private val _uiState = MutableStateFlow<BarcodeScannerUiState>(BarcodeScannerUiState.Scanning)
    val uiState: StateFlow<BarcodeScannerUiState> = _uiState.asStateFlow()
    val detectedBounds = barcodeScanner.detectedBounds

    init {
        // Collect scan results from the scanner
        viewModelScope.launch {
            barcodeScanner.scanResults.collect { result ->
                when (result) {
                    is ScanResult.BarcodeResult -> {
                        // Stop scanning and show result
                        barcodeScanner.stopScanning()
                        historyStore.add(
                            ScanHistoryEntry(
                                mode = ScannerMode.BARCODE,
                                title = result.scan.summary,
                                subtitle = result.format.name
                            )
                        )
                        _uiState.value = BarcodeScannerUiState.Success(result)
                    }

                    is ScanResult.Error -> {
                        // Show error dialog
                        _uiState.value = BarcodeScannerUiState.Error(result.message)
                    }

                    is ScanResult.NoResult,
                    is ScanResult.TextResult,
                    is ScanResult.DocumentResult -> {
                        // Continue scanning for barcode results
                    }
                }
            }
        }
    }

    /** Start scanning for barcodes */
    fun startScanning() {
        barcodeScanner.startScanning()
        _uiState.value = BarcodeScannerUiState.Scanning
    }

    /** Stop scanning */
    fun stopScanning() {
        barcodeScanner.stopScanning()
    }

    fun scanImage(context: Context, uri: Uri) {
        barcodeScanner.stopScanning()
        _uiState.value = BarcodeScannerUiState.Scanning
        barcodeScanner.scanImage(context, uri)
    }

    /** Resume scanning after showing a result or error */
    fun resumeScanning() {
        startScanning()
    }

    /** Get the image analyzer for CameraX integration */
    suspend fun getImageAnalyzer(): ImageAnalysis.Analyzer {
        return barcodeScanner.getImageAnalyzer(
            analysisExecutor = Executors.newSingleThreadExecutor()
        )
    }

    fun getImageAnalyzer(
        analysisExecutor: ExecutorService,
        cameraController: LifecycleCameraController,
    ): ImageAnalysis.Analyzer {
        val maxZoomRatio = cameraController.cameraInfo?.zoomState?.value?.maxZoomRatio ?: 1f
        return barcodeScanner.getImageAnalyzer(
            analysisExecutor = analysisExecutor,
            maxSupportedZoomRatio = maxZoomRatio,
            onZoomSuggestion = { zoomRatio ->
                runCatching {
                    cameraController.setZoomRatio(zoomRatio)
                }.isSuccess
            }
        )
    }

    override fun onCleared() {
        barcodeScanner.close()
        super.onCleared()
    }
}

/** UI State for Barcode Scanner */
sealed class BarcodeScannerUiState {
    data object Scanning : BarcodeScannerUiState()
    data class Success(val result: ScanResult.BarcodeResult) : BarcodeScannerUiState()
    data class Error(val message: String) : BarcodeScannerUiState()
}
