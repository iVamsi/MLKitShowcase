package com.vamsi.mlkitshowcase.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BarcodeFormatTest {

    @Test
    fun `BarcodeFormat should have all expected values`() {
        // Given & When
        val formats = BarcodeFormat.entries.toTypedArray()

        // Then
        assertThat(formats).hasLength(14)
        assertThat(formats)
                .asList()
                .containsExactly(
                        BarcodeFormat.UNKNOWN,
                        BarcodeFormat.CODE_128,
                        BarcodeFormat.CODE_39,
                        BarcodeFormat.CODE_93,
                        BarcodeFormat.CODABAR,
                        BarcodeFormat.DATA_MATRIX,
                        BarcodeFormat.EAN_13,
                        BarcodeFormat.EAN_8,
                        BarcodeFormat.ITF,
                        BarcodeFormat.QR_CODE,
                        BarcodeFormat.UPC_A,
                        BarcodeFormat.UPC_E,
                        BarcodeFormat.PDF417,
                        BarcodeFormat.AZTEC
                )
    }

    @Test
    fun `BarcodeFormat valueOf should work correctly`() {
        // Given & When & Then
        assertThat(BarcodeFormat.valueOf("QR_CODE")).isEqualTo(BarcodeFormat.QR_CODE)
        assertThat(BarcodeFormat.valueOf("EAN_13")).isEqualTo(BarcodeFormat.EAN_13)
        assertThat(BarcodeFormat.valueOf("CODE_128")).isEqualTo(BarcodeFormat.CODE_128)
        assertThat(BarcodeFormat.valueOf("UNKNOWN")).isEqualTo(BarcodeFormat.UNKNOWN)
    }

    @Test
    fun `BarcodeFormat ordinal values should be consistent`() {
        // Given & When & Then
        assertThat(BarcodeFormat.UNKNOWN.ordinal).isEqualTo(0)
        assertThat(BarcodeFormat.CODE_128.ordinal).isEqualTo(1)
        assertThat(BarcodeFormat.QR_CODE.ordinal).isEqualTo(9)
        assertThat(BarcodeFormat.AZTEC.ordinal).isEqualTo(13)
    }

    @Test
    fun `BarcodeFormat name property should work correctly`() {
        // Given & When & Then
        assertThat(BarcodeFormat.QR_CODE.name).isEqualTo("QR_CODE")
        assertThat(BarcodeFormat.EAN_13.name).isEqualTo("EAN_13")
        assertThat(BarcodeFormat.CODE_128.name).isEqualTo("CODE_128")
    }
}
