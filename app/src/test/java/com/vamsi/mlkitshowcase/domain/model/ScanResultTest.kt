package com.vamsi.mlkitshowcase.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ScanResultTest {

    @Test
    fun `BarcodeResult should create instance with correct values`() {
        // Given
        val value = "123456789"
        val format = BarcodeFormat.EAN_13
        val rawBytes = byteArrayOf(1, 2, 3)
        
        // When
        val result = ScanResult.BarcodeResult(value, format, rawBytes)
        
        // Then
        assertThat(result.value).isEqualTo(value)
        assertThat(result.format).isEqualTo(format)
        assertThat(result.rawBytes).isEqualTo(rawBytes)
    }

    @Test
    fun `BarcodeResult should create instance without rawBytes`() {
        // Given
        val value = "123456789"
        val format = BarcodeFormat.QR_CODE
        
        // When
        val result = ScanResult.BarcodeResult(value, format)
        
        // Then
        assertThat(result.value).isEqualTo(value)
        assertThat(result.format).isEqualTo(format)
        assertThat(result.rawBytes).isNull()
    }

    @Test
    fun `BarcodeResult equality should work correctly with same values`() {
        // Given
        val value = "123456789"
        val format = BarcodeFormat.CODE_128
        val rawBytes = byteArrayOf(1, 2, 3)
        
        // When
        val result1 = ScanResult.BarcodeResult(value, format, rawBytes)
        val result2 = ScanResult.BarcodeResult(value, format, rawBytes)
        
        // Then
        assertEquals(result1, result2)
        assertEquals(result1.hashCode(), result2.hashCode())
    }

    @Test
    fun `BarcodeResult equality should work correctly without rawBytes`() {
        // Given
        val value = "123456789"
        val format = BarcodeFormat.QR_CODE
        
        // When
        val result1 = ScanResult.BarcodeResult(value, format, null)
        val result2 = ScanResult.BarcodeResult(value, format, null)
        
        // Then
        assertEquals(result1, result2)
        assertEquals(result1.hashCode(), result2.hashCode())
    }

    @Test
    fun `BarcodeResult equality should fail with different values`() {
        // Given
        val result1 = ScanResult.BarcodeResult("123", BarcodeFormat.QR_CODE)
        val result2 = ScanResult.BarcodeResult("456", BarcodeFormat.QR_CODE)
        
        // Then
        assertNotEquals(result1, result2)
    }

    @Test
    fun `BarcodeResult equality should fail with different formats`() {
        // Given
        val result1 = ScanResult.BarcodeResult("123", BarcodeFormat.QR_CODE)
        val result2 = ScanResult.BarcodeResult("123", BarcodeFormat.EAN_13)
        
        // Then
        assertNotEquals(result1, result2)
    }

    @Test
    fun `BarcodeResult equality should fail with different rawBytes`() {
        // Given
        val result1 = ScanResult.BarcodeResult("123", BarcodeFormat.QR_CODE, byteArrayOf(1, 2, 3))
        val result2 = ScanResult.BarcodeResult("123", BarcodeFormat.QR_CODE, byteArrayOf(4, 5, 6))
        
        // Then
        assertNotEquals(result1, result2)
    }

    @Test
    fun `BarcodeResult equality should fail when one has rawBytes and other doesn't`() {
        // Given
        val result1 = ScanResult.BarcodeResult("123", BarcodeFormat.QR_CODE, byteArrayOf(1, 2, 3))
        val result2 = ScanResult.BarcodeResult("123", BarcodeFormat.QR_CODE, null)
        
        // Then
        assertNotEquals(result1, result2)
    }

    @Test
    fun `BarcodeResult should handle same instance equality`() {
        // Given
        val result = ScanResult.BarcodeResult("123", BarcodeFormat.QR_CODE)
        
        // Then
        assertEquals(result, result)
    }

    @Test
    fun `BarcodeResult should handle null and different class equality`() {
        // Given
        val result = ScanResult.BarcodeResult("123", BarcodeFormat.QR_CODE)
        
        // Then
        assertThat(result).isNotEqualTo(null)
        assertThat(result).isNotEqualTo("some string")
    }

    @Test
    fun `TextResult should create instance with correct values`() {
        // Given
        val text = "Hello World"
        val confidence = 0.95f
        
        // When
        val result = ScanResult.TextResult(text, confidence)
        
        // Then
        assertThat(result.text).isEqualTo(text)
        assertThat(result.confidence).isEqualTo(confidence)
    }

    @Test
    fun `TextResult should create instance without confidence`() {
        // Given
        val text = "Hello World"
        
        // When
        val result = ScanResult.TextResult(text)
        
        // Then
        assertThat(result.text).isEqualTo(text)
        assertThat(result.confidence).isNull()
    }

    @Test
    fun `TextResult equality should work correctly`() {
        // Given
        val text = "Hello World"
        val confidence = 0.95f
        
        // When
        val result1 = ScanResult.TextResult(text, confidence)
        val result2 = ScanResult.TextResult(text, confidence)
        
        // Then
        assertEquals(result1, result2)
    }

    @Test
    fun `NoResult should be singleton`() {
        // Given & When
        val result1 = ScanResult.NoResult
        val result2 = ScanResult.NoResult
        
        // Then
        assertThat(result1).isSameInstanceAs(result2)
    }

    @Test
    fun `Error should create instance with message and throwable`() {
        // Given
        val message = "Scanning failed"
        val throwable = RuntimeException("Test exception")
        
        // When
        val result = ScanResult.Error(message, throwable)
        
        // Then
        assertThat(result.message).isEqualTo(message)
        assertThat(result.throwable).isEqualTo(throwable)
    }

    @Test
    fun `Error should create instance with message only`() {
        // Given
        val message = "Scanning failed"
        
        // When
        val result = ScanResult.Error(message)
        
        // Then
        assertThat(result.message).isEqualTo(message)
        assertThat(result.throwable).isNull()
    }

    @Test
    fun `Error equality should work correctly`() {
        // Given
        val message = "Scanning failed"
        val throwable = RuntimeException("Test exception")
        
        // When
        val result1 = ScanResult.Error(message, throwable)
        val result2 = ScanResult.Error(message, throwable)
        
        // Then
        assertEquals(result1, result2)
    }
}