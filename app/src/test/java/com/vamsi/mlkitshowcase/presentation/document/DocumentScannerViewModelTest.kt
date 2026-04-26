package com.vamsi.mlkitshowcase.presentation.document

import com.google.common.truth.Truth.assertThat
import com.vamsi.mlkitshowcase.domain.model.DocumentScanResult
import com.vamsi.mlkitshowcase.domain.model.ScanHistoryStore
import com.vamsi.mlkitshowcase.domain.model.ScannerMode
import org.junit.Test

class DocumentScannerViewModelTest {

    @Test
    fun `showResult displays document result and records history`() {
        val historyStore = ScanHistoryStore()
        val viewModel = DocumentScannerViewModel(historyStore)
        val result = DocumentScanResult(
            pageCount = 2,
            imageUris = listOf("content://scan/page-1", "content://scan/page-2"),
            pdfUri = "content://scan/result.pdf"
        )

        viewModel.showResult(result)

        assertThat(viewModel.uiState.value).isEqualTo(DocumentScannerUiState.Success(result))
        assertThat(historyStore.items.value).hasSize(1)
        assertThat(historyStore.items.value.first().mode).isEqualTo(ScannerMode.DOCUMENT)
        assertThat(historyStore.items.value.first().title).isEqualTo("2 page document")
        assertThat(historyStore.items.value.first().subtitle).isEqualTo("PDF and JPEG output")
    }

    @Test
    fun `showError displays an error state without recording history`() {
        val historyStore = ScanHistoryStore()
        val viewModel = DocumentScannerViewModel(historyStore)

        viewModel.showError("Document scanner is unavailable")

        assertThat(viewModel.uiState.value)
            .isEqualTo(DocumentScannerUiState.Error("Document scanner is unavailable"))
        assertThat(historyStore.items.value).isEmpty()
    }
}
