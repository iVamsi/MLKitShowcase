package com.vamsi.mlkitshowcase.presentation.barcode

import com.vamsi.mlkitshowcase.domain.model.BarcodeFormat
import com.vamsi.mlkitshowcase.domain.model.ScanResult
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class BarcodeScannerUiStateTest {

    @Test
    fun `Scanning state should be object`() {
        // Given & When
        val state1 = BarcodeScannerUiState.Scanning
        val state2 = BarcodeScannerUiState.Scanning

        // Then
        assertThat(state1).isSameInstanceAs(state2)
    }

    @Test
    fun `Success state should hold barcode result`() {
        // Given
        val barcodeResult = ScanResult.BarcodeResult(
            value = "123456789",
            format = BarcodeFormat.EAN_13
        )

        // When
        val state = BarcodeScannerUiState.Success(barcodeResult)

        // Then
        assertThat(state.result).isEqualTo(barcodeResult)
    }

    @Test
    fun `Success state equality should work correctly`() {
        // Given
        val barcodeResult1 = ScanResult.BarcodeResult("123", BarcodeFormat.QR_CODE)
        val barcodeResult2 = ScanResult.BarcodeResult("123", BarcodeFormat.QR_CODE)
        val barcodeResult3 = ScanResult.BarcodeResult("456", BarcodeFormat.QR_CODE)

        // When
        val state1 = BarcodeScannerUiState.Success(barcodeResult1)
        val state2 = BarcodeScannerUiState.Success(barcodeResult2)
        val state3 = BarcodeScannerUiState.Success(barcodeResult3)

        // Then
        assertEquals(state1, state2)
        assertNotEquals(state1, state3)
    }

    @Test
    fun `Error state should hold error message`() {
        // Given
        val errorMessage = "Scanning failed"

        // When
        val state = BarcodeScannerUiState.Error(errorMessage)

        // Then
        assertThat(state.message).isEqualTo(errorMessage)
    }

    @Test
    fun `Error state equality should work correctly`() {
        // Given
        val message1 = "Error 1"
        val message2 = "Error 1"
        val message3 = "Error 2"

        // When
        val state1 = BarcodeScannerUiState.Error(message1)
        val state2 = BarcodeScannerUiState.Error(message2)
        val state3 = BarcodeScannerUiState.Error(message3)

        // Then
        assertEquals(state1, state2)
        assertNotEquals(state1, state3)
    }

    @Test
    fun `different state types should not be equal`() {
        // Given
        val scanningState = BarcodeScannerUiState.Scanning
        val successState = BarcodeScannerUiState.Success(
            ScanResult.BarcodeResult("123", BarcodeFormat.QR_CODE)
        )
        val errorState = BarcodeScannerUiState.Error("Error")

        // Then
        assertThat(scanningState).isNotEqualTo(successState)
        assertThat(scanningState).isNotEqualTo(errorState)
        assertThat(successState).isNotEqualTo(errorState)
    }

    @Test
    fun `states should have correct toString representation`() {
        // Given
        val scanningState = BarcodeScannerUiState.Scanning
        val successState = BarcodeScannerUiState.Success(
            ScanResult.BarcodeResult("123", BarcodeFormat.QR_CODE)
        )
        val errorState = BarcodeScannerUiState.Error("Test error")

        // When & Then
        assertThat(scanningState.toString()).contains("Scanning")
        assertThat(successState.toString()).contains("Success")
        assertThat(errorState.toString()).contains("Error")
        assertThat(errorState.toString()).contains("Test error")
    }
}