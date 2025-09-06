package com.vamsi.mlkitshowcase.presentation.text

import com.vamsi.mlkitshowcase.domain.model.ScanResult
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class TextRecognitionUiStateTest {

    @Test
    fun `Scanning state should be object`() {
        // Given & When
        val state1 = TextRecognitionUiState.Scanning
        val state2 = TextRecognitionUiState.Scanning

        // Then
        assertThat(state1).isSameInstanceAs(state2)
    }

    @Test
    fun `Success state should hold text result`() {
        // Given
        val textResult = ScanResult.TextResult(
            text = "Hello World",
            confidence = 0.95f
        )

        // When
        val state = TextRecognitionUiState.Success(textResult)

        // Then
        assertThat(state.result).isEqualTo(textResult)
    }

    @Test
    fun `Success state equality should work correctly`() {
        // Given
        val textResult1 = ScanResult.TextResult("Hello", 0.9f)
        val textResult2 = ScanResult.TextResult("Hello", 0.9f)
        val textResult3 = ScanResult.TextResult("World", 0.9f)

        // When
        val state1 = TextRecognitionUiState.Success(textResult1)
        val state2 = TextRecognitionUiState.Success(textResult2)
        val state3 = TextRecognitionUiState.Success(textResult3)

        // Then
        assertEquals(state1, state2)
        assertNotEquals(state1, state3)
    }

    @Test
    fun `Error state should hold error message`() {
        // Given
        val errorMessage = "Text recognition failed"

        // When
        val state = TextRecognitionUiState.Error(errorMessage)

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
        val state1 = TextRecognitionUiState.Error(message1)
        val state2 = TextRecognitionUiState.Error(message2)
        val state3 = TextRecognitionUiState.Error(message3)

        // Then
        assertEquals(state1, state2)
        assertNotEquals(state1, state3)
    }

    @Test
    fun `different state types should not be equal`() {
        // Given
        val scanningState = TextRecognitionUiState.Scanning
        val successState = TextRecognitionUiState.Success(
            ScanResult.TextResult("Hello World")
        )
        val errorState = TextRecognitionUiState.Error("Error")

        // Then
        assertThat(scanningState).isNotEqualTo(successState)
        assertThat(scanningState).isNotEqualTo(errorState)
        assertThat(successState).isNotEqualTo(errorState)
    }

    @Test
    fun `states should have correct toString representation`() {
        // Given
        val scanningState = TextRecognitionUiState.Scanning
        val successState = TextRecognitionUiState.Success(
            ScanResult.TextResult("Test text")
        )
        val errorState = TextRecognitionUiState.Error("Test error")

        // When & Then
        assertThat(scanningState.toString()).contains("Scanning")
        assertThat(successState.toString()).contains("Success")
        assertThat(errorState.toString()).contains("Error")
        assertThat(errorState.toString()).contains("Test error")
    }

    @Test
    fun `Success state should handle text result without confidence`() {
        // Given
        val textResult = ScanResult.TextResult("Hello World") // No confidence

        // When
        val state = TextRecognitionUiState.Success(textResult)

        // Then
        assertThat(state.result.text).isEqualTo("Hello World")
        assertThat(state.result.confidence).isNull()
    }

    @Test
    fun `Success state should handle text result with confidence`() {
        // Given
        val textResult = ScanResult.TextResult("Hello World", 0.85f)

        // When
        val state = TextRecognitionUiState.Success(textResult)

        // Then
        assertThat(state.result.text).isEqualTo("Hello World")
        assertThat(state.result.confidence).isEqualTo(0.85f)
    }
}