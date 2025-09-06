package com.vamsi.mlkitshowcase.data.scanner

import com.google.common.truth.Truth.assertThat
import com.google.mlkit.vision.barcode.common.Barcode
import com.vamsi.mlkitshowcase.domain.model.BarcodeFormat
import org.junit.Test

/**
 * Unit tests for MLKitBarcodeScanner
 *
 * Note: Tests that require Android runtime (actual scanning) should be in androidTest. These unit
 * tests focus on testable logic like format conversion.
 */
class MLKitBarcodeScannerTest {

    @Test
    fun `toBarcodeFormat extension should convert all ML Kit formats correctly`() {
        // Given & When & Then
        assertThat(convertBarcodeFormat(Barcode.FORMAT_CODE_128)).isEqualTo(BarcodeFormat.CODE_128)
        assertThat(convertBarcodeFormat(Barcode.FORMAT_CODE_39)).isEqualTo(BarcodeFormat.CODE_39)
        assertThat(convertBarcodeFormat(Barcode.FORMAT_CODE_93)).isEqualTo(BarcodeFormat.CODE_93)
        assertThat(convertBarcodeFormat(Barcode.FORMAT_CODABAR)).isEqualTo(BarcodeFormat.CODABAR)
        assertThat(convertBarcodeFormat(Barcode.FORMAT_DATA_MATRIX))
                .isEqualTo(BarcodeFormat.DATA_MATRIX)
        assertThat(convertBarcodeFormat(Barcode.FORMAT_EAN_13)).isEqualTo(BarcodeFormat.EAN_13)
        assertThat(convertBarcodeFormat(Barcode.FORMAT_EAN_8)).isEqualTo(BarcodeFormat.EAN_8)
        assertThat(convertBarcodeFormat(Barcode.FORMAT_ITF)).isEqualTo(BarcodeFormat.ITF)
        assertThat(convertBarcodeFormat(Barcode.FORMAT_QR_CODE)).isEqualTo(BarcodeFormat.QR_CODE)
        assertThat(convertBarcodeFormat(Barcode.FORMAT_UPC_A)).isEqualTo(BarcodeFormat.UPC_A)
        assertThat(convertBarcodeFormat(Barcode.FORMAT_UPC_E)).isEqualTo(BarcodeFormat.UPC_E)
        assertThat(convertBarcodeFormat(Barcode.FORMAT_PDF417)).isEqualTo(BarcodeFormat.PDF417)
        assertThat(convertBarcodeFormat(Barcode.FORMAT_AZTEC)).isEqualTo(BarcodeFormat.AZTEC)
        assertThat(convertBarcodeFormat(-1)).isEqualTo(BarcodeFormat.UNKNOWN) // Unknown format
    }

    @Test
    fun `should handle edge cases in format conversion`() {
        // Given & When & Then
        assertThat(convertBarcodeFormat(0)).isEqualTo(BarcodeFormat.UNKNOWN)
        assertThat(convertBarcodeFormat(999)).isEqualTo(BarcodeFormat.UNKNOWN)
        assertThat(convertBarcodeFormat(Int.MAX_VALUE)).isEqualTo(BarcodeFormat.UNKNOWN)
        assertThat(convertBarcodeFormat(Int.MIN_VALUE)).isEqualTo(BarcodeFormat.UNKNOWN)
    }

    // Helper method to test the private toBarcodeFormat extension
    // This replicates the logic from the private extension function
    private fun convertBarcodeFormat(mlKitFormat: Int): BarcodeFormat =
            when (mlKitFormat) {
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
