package com.vamsi.mlkitshowcase.presentation.barcode

import androidx.camera.core.ImageAnalysis
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vamsi.mlkitshowcase.data.scanner.MLKitBarcodeScanner
import com.vamsi.mlkitshowcase.domain.model.ScanResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for Barcode Scanner Screen
 *
 * Manages the state of barcode scanning and coordinates between the UI and the ML Kit scanner.
 */
@HiltViewModel
class BarcodeScannerViewModel @Inject constructor(private val barcodeScanner: MLKitBarcodeScanner) :
    ViewModel() {

    private val _uiState = MutableStateFlow<BarcodeScannerUiState>(BarcodeScannerUiState.Scanning)
    val uiState: StateFlow<BarcodeScannerUiState> = _uiState.asStateFlow()

    init {
        // Collect scan results from the scanner
        viewModelScope.launch {
            barcodeScanner.scanResults.collect { result ->
                when (result) {
                    is ScanResult.BarcodeResult -> {
                        // Stop scanning and show result
                        barcodeScanner.stopScanning()
                        _uiState.value = BarcodeScannerUiState.Success(result)
                    }

                    is ScanResult.Error -> {
                        // Show error dialog
                        _uiState.value = BarcodeScannerUiState.Error(result.message)
                    }

                    is ScanResult.NoResult, is ScanResult.TextResult -> {
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

    /** Resume scanning after showing a result or error */
    fun resumeScanning() {
        startScanning()
    }

    /** Get the image analyzer for CameraX integration */
    suspend fun getImageAnalyzer(): ImageAnalysis.Analyzer {
        return barcodeScanner.getImageAnalyzer()
    }
}

/** UI State for Barcode Scanner */
sealed class BarcodeScannerUiState {
    data object Scanning : BarcodeScannerUiState()
    data class Success(val result: ScanResult.BarcodeResult) : BarcodeScannerUiState()
    data class Error(val message: String) : BarcodeScannerUiState()
}
