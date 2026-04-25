package com.vamsi.mlkitshowcase.presentation.document

import androidx.lifecycle.ViewModel
import com.vamsi.mlkitshowcase.domain.model.DocumentScanResult
import com.vamsi.mlkitshowcase.domain.model.ScanHistoryEntry
import com.vamsi.mlkitshowcase.domain.model.ScanHistoryStore
import com.vamsi.mlkitshowcase.domain.model.ScannerMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class DocumentScannerViewModel @Inject constructor(
    private val historyStore: ScanHistoryStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DocumentScannerUiState>(DocumentScannerUiState.Idle)
    val uiState: StateFlow<DocumentScannerUiState> = _uiState.asStateFlow()

    fun launching() {
        _uiState.value = DocumentScannerUiState.Launching
    }

    fun idle() {
        _uiState.value = DocumentScannerUiState.Idle
    }

    fun showError(message: String) {
        _uiState.value = DocumentScannerUiState.Error(message)
    }

    fun showResult(result: DocumentScanResult) {
        historyStore.add(
            ScanHistoryEntry(
                mode = ScannerMode.DOCUMENT,
                title = "${result.pageCount} page document",
                subtitle = if (result.pdfUri != null) "PDF and JPEG output" else "JPEG output"
            )
        )
        _uiState.value = DocumentScannerUiState.Success(result)
    }
}

sealed class DocumentScannerUiState {
    data object Idle : DocumentScannerUiState()
    data object Launching : DocumentScannerUiState()
    data class Success(val result: DocumentScanResult) : DocumentScannerUiState()
    data class Error(val message: String) : DocumentScannerUiState()
}
