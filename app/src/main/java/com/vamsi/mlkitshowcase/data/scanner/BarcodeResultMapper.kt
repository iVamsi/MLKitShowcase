package com.vamsi.mlkitshowcase.data.scanner

import com.google.mlkit.vision.barcode.common.Barcode
import com.vamsi.mlkitshowcase.domain.model.BarcodeFormat
import com.vamsi.mlkitshowcase.domain.model.BarcodePayload
import com.vamsi.mlkitshowcase.domain.model.BarcodeScanResult

object BarcodeResultMapper {

    fun map(barcode: Barcode): BarcodeScanResult {
        val value = barcode.displayValue ?: barcode.rawValue ?: ""
        return BarcodeScanResult(
            value = value,
            format = barcode.format.toBarcodeFormat(),
            rawBytes = barcode.rawBytes,
            payload = barcode.toPayload(value),
            boundingBox = barcode.boundingBox
        )
    }

    private fun Barcode.toPayload(value: String): BarcodePayload = when (valueType) {
        Barcode.TYPE_URL -> url?.let {
            BarcodePayload.Url(
                title = it.title,
                url = it.url ?: value
            )
        } ?: BarcodePayload.PlainText(value)

        Barcode.TYPE_WIFI -> wifi?.let {
            BarcodePayload.Wifi(
                ssid = it.ssid,
                password = it.password,
                encryptionType = it.encryptionType.toWifiEncryptionLabel()
            )
        } ?: BarcodePayload.PlainText(value)

        Barcode.TYPE_CONTACT_INFO -> contactInfo?.let {
            BarcodePayload.Contact(
                name = it.name?.formattedName,
                organization = it.organization
            )
        } ?: BarcodePayload.PlainText(value)

        Barcode.TYPE_PHONE -> phone?.let {
            BarcodePayload.Phone(
                number = it.number,
                type = it.type.toPhoneTypeLabel()
            )
        } ?: BarcodePayload.PlainText(value)

        Barcode.TYPE_SMS -> sms?.let {
            BarcodePayload.Sms(
                phoneNumber = it.phoneNumber,
                message = it.message
            )
        } ?: BarcodePayload.PlainText(value)

        Barcode.TYPE_EMAIL -> email?.let {
            BarcodePayload.Email(
                address = it.address,
                subject = it.subject
            )
        } ?: BarcodePayload.PlainText(value)

        Barcode.TYPE_CALENDAR_EVENT -> calendarEvent?.let {
            BarcodePayload.CalendarEvent(
                summaryText = it.summary,
                location = it.location
            )
        } ?: BarcodePayload.PlainText(value)

        Barcode.TYPE_GEO -> geoPoint?.let {
            BarcodePayload.GeoPoint(
                latitude = it.lat,
                longitude = it.lng
            )
        } ?: BarcodePayload.PlainText(value)

        Barcode.TYPE_ISBN -> BarcodePayload.Isbn(value)

        else -> BarcodePayload.PlainText(value)
    }

    private fun Int.toBarcodeFormat(): BarcodeFormat = when (this) {
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

    private fun Int.toWifiEncryptionLabel(): String = when (this) {
        Barcode.WiFi.TYPE_OPEN -> "Open"
        Barcode.WiFi.TYPE_WEP -> "WEP"
        Barcode.WiFi.TYPE_WPA -> "WPA"
        else -> "Unknown"
    }

    private fun Int.toPhoneTypeLabel(): String = when (this) {
        Barcode.Phone.TYPE_HOME -> "Home"
        Barcode.Phone.TYPE_MOBILE -> "Mobile"
        Barcode.Phone.TYPE_WORK -> "Work"
        Barcode.Phone.TYPE_FAX -> "Fax"
        else -> "Unknown"
    }
}

class ConsecutiveBarcodeResultGate(
    private val requiredMatches: Int = 2,
) {
    private var previousKey: String? = null
    private var matchCount: Int = 0

    fun accept(result: BarcodeScanResult): BarcodeScanResult? {
        val key = "${result.format}:${result.value}"
        matchCount = if (key == previousKey) matchCount + 1 else 1
        previousKey = key
        return if (matchCount >= requiredMatches) result else null
    }

    fun reset() {
        previousKey = null
        matchCount = 0
    }
}
