package com.vamsi.mlkitshowcase.data.scanner

import com.google.common.truth.Truth.assertThat
import com.google.mlkit.vision.barcode.common.Barcode
import com.vamsi.mlkitshowcase.domain.model.BarcodeFormat
import com.vamsi.mlkitshowcase.domain.model.BarcodePayload
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class BarcodeResultMapperTest {

    @Test
    fun `maps url barcode to structured url payload`() {
        val url = mockk<Barcode.UrlBookmark>()
        every { url.title } returns "Example"
        every { url.url } returns "https://example.com"

        val barcode = baseBarcode(
            rawValue = "https://example.com",
            format = Barcode.FORMAT_QR_CODE,
            valueType = Barcode.TYPE_URL
        )
        every { barcode.url } returns url

        val result = BarcodeResultMapper.map(barcode)

        assertThat(result.value).isEqualTo("https://example.com")
        assertThat(result.format).isEqualTo(BarcodeFormat.QR_CODE)
        assertThat(result.payload).isEqualTo(
            BarcodePayload.Url(title = "Example", url = "https://example.com")
        )
    }

    @Test
    fun `maps wifi barcode without exposing password in summary`() {
        val wifi = mockk<Barcode.WiFi>()
        every { wifi.ssid } returns "Cafe Wi-Fi"
        every { wifi.password } returns "secret"
        every { wifi.encryptionType } returns Barcode.WiFi.TYPE_WPA

        val barcode = baseBarcode(
            rawValue = "WIFI:S:Cafe Wi-Fi;T:WPA;P:secret;;",
            valueType = Barcode.TYPE_WIFI
        )
        every { barcode.wifi } returns wifi

        val result = BarcodeResultMapper.map(barcode)

        assertThat(result.payload).isEqualTo(
            BarcodePayload.Wifi(
                ssid = "Cafe Wi-Fi",
                password = "secret",
                encryptionType = "WPA"
            )
        )
        assertThat(result.summary).isEqualTo("Wi-Fi: Cafe Wi-Fi")
    }

    @Test
    fun `falls back to plain text payload for unknown value type`() {
        val barcode = baseBarcode(
            rawValue = "plain payload",
            displayValue = null,
            format = Barcode.FORMAT_CODE_128,
            valueType = Barcode.TYPE_UNKNOWN
        )

        val result = BarcodeResultMapper.map(barcode)

        assertThat(result.value).isEqualTo("plain payload")
        assertThat(result.format).isEqualTo(BarcodeFormat.CODE_128)
        assertThat(result.payload).isEqualTo(BarcodePayload.PlainText("plain payload"))
        assertThat(result.summary).isEqualTo("plain payload")
    }

    @Test
    fun `barcode gate requires two matching decoded values before accepting`() {
        val first = BarcodeResultMapper.map(
            baseBarcode(rawValue = "same", format = Barcode.FORMAT_QR_CODE)
        )
        val second = first.copy()
        val different = BarcodeResultMapper.map(
            baseBarcode(rawValue = "different", format = Barcode.FORMAT_QR_CODE)
        )
        val gate = ConsecutiveBarcodeResultGate(requiredMatches = 2)

        assertThat(gate.accept(first)).isNull()
        assertThat(gate.accept(different)).isNull()
        assertThat(gate.accept(first)).isNull()
        assertThat(gate.accept(second)).isEqualTo(second)
    }

    private fun baseBarcode(
        rawValue: String,
        displayValue: String? = rawValue,
        format: Int = Barcode.FORMAT_QR_CODE,
        valueType: Int = Barcode.TYPE_TEXT,
    ): Barcode {
        val barcode = mockk<Barcode>(relaxed = true)
        every { barcode.rawValue } returns rawValue
        every { barcode.displayValue } returns displayValue
        every { barcode.format } returns format
        every { barcode.valueType } returns valueType
        every { barcode.rawBytes } returns rawValue.encodeToByteArray()
        every { barcode.boundingBox } returns null
        return barcode
    }
}
