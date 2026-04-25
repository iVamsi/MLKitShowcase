package com.vamsi.mlkitshowcase.domain.model

import android.graphics.Rect
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Represents the result of a scanning operation
 */
sealed class ScanResult {
    data class BarcodeResult(
        val scan: BarcodeScanResult,
    ) : ScanResult() {
        constructor(
            value: String,
            format: BarcodeFormat,
            rawBytes: ByteArray? = null,
        ) : this(
            BarcodeScanResult(
                value = value,
                format = format,
                rawBytes = rawBytes,
                payload = BarcodePayload.PlainText(value)
            )
        )

        val value: String
            get() = scan.value

        val format: BarcodeFormat
            get() = scan.format

        val rawBytes: ByteArray?
            get() = scan.rawBytes

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as BarcodeResult

            if (scan != other.scan) return false

            return true
        }

        override fun hashCode(): Int {
            return scan.hashCode()
        }
    }

    data class TextResult(
        val scan: TextScanResult,
        val confidence: Float? = null,
    ) : ScanResult() {
        constructor(
            text: String,
            confidence: Float? = null,
        ) : this(TextScanResult(text = text.trim()), confidence)

        val text: String
            get() = scan.text
    }

    data class DocumentResult(val scan: DocumentScanResult) : ScanResult()
    data object NoResult : ScanResult()
    data class Error(val message: String, val throwable: Throwable? = null) : ScanResult()
}

enum class ScannerMode {
    BARCODE,
    TEXT,
    DOCUMENT
}

data class BarcodeScanResult(
    val value: String,
    val format: BarcodeFormat,
    val rawBytes: ByteArray? = null,
    val payload: BarcodePayload,
    val boundingBox: Rect? = null,
) {
    val summary: String
        get() = payload.summary.ifBlank { value }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BarcodeScanResult

        if (value != other.value) return false
        if (format != other.format) return false
        if (rawBytes != null) {
            if (other.rawBytes == null) return false
            if (!rawBytes.contentEquals(other.rawBytes)) return false
        } else if (other.rawBytes != null) return false
        if (payload != other.payload) return false
        if (boundingBox != other.boundingBox) return false

        return true
    }

    override fun hashCode(): Int {
        var result = value.hashCode()
        result = 31 * result + format.hashCode()
        result = 31 * result + (rawBytes?.contentHashCode() ?: 0)
        result = 31 * result + payload.hashCode()
        result = 31 * result + (boundingBox?.hashCode() ?: 0)
        return result
    }
}

sealed class BarcodePayload {
    abstract val summary: String

    data class PlainText(val text: String) : BarcodePayload() {
        override val summary: String = text
    }

    data class Url(val title: String?, val url: String) : BarcodePayload() {
        override val summary: String = title?.takeIf { it.isNotBlank() } ?: url
    }

    data class Wifi(
        val ssid: String?,
        val password: String?,
        val encryptionType: String,
    ) : BarcodePayload() {
        override val summary: String = "Wi-Fi: ${ssid?.takeIf { it.isNotBlank() } ?: "hidden network"}"
    }

    data class Contact(val name: String?, val organization: String?) : BarcodePayload() {
        override val summary: String = listOfNotNull(name, organization)
            .firstOrNull { it.isNotBlank() }
            ?: "Contact"
    }

    data class Phone(val number: String?, val type: String) : BarcodePayload() {
        override val summary: String = number?.let { "Phone: $it" } ?: "Phone"
    }

    data class Sms(val phoneNumber: String?, val message: String?) : BarcodePayload() {
        override val summary: String = phoneNumber?.let { "SMS: $it" } ?: "SMS"
    }

    data class Email(val address: String?, val subject: String?) : BarcodePayload() {
        override val summary: String = address?.let { "Email: $it" } ?: "Email"
    }

    data class CalendarEvent(val summaryText: String?, val location: String?) : BarcodePayload() {
        override val summary: String = summaryText?.takeIf { it.isNotBlank() } ?: "Calendar event"
    }

    data class GeoPoint(val latitude: Double, val longitude: Double) : BarcodePayload() {
        override val summary: String = "Location: $latitude, $longitude"
    }

    data class Isbn(val value: String) : BarcodePayload() {
        override val summary: String = "ISBN: $value"
    }
}

data class TextBlockResult(
    val text: String,
    val boundingBox: Rect? = null,
    val lineCount: Int = 0,
)

data class TextScanResult(
    val text: String,
    val blocks: List<TextBlockResult> = emptyList(),
    val lineCount: Int = 0,
    val elementCount: Int = 0,
    val symbolCount: Int = 0,
)

data class DocumentScanResult(
    val pageCount: Int,
    val imageUris: List<String>,
    val pdfUri: String?,
)

data class ScanHistoryEntry(
    val mode: ScannerMode,
    val title: String,
    val subtitle: String,
)

@Singleton
class ScanHistoryStore(
    private val capacity: Int = 5,
) {
    @Inject
    constructor() : this(capacity = 5)

    private val _items = MutableStateFlow<List<ScanHistoryEntry>>(emptyList())
    val items: StateFlow<List<ScanHistoryEntry>> = _items.asStateFlow()

    fun add(entry: ScanHistoryEntry) {
        _items.value = (listOf(entry) + _items.value).take(capacity)
    }
}

/**
 * Supported barcode formats
 */
enum class BarcodeFormat {
    UNKNOWN,
    CODE_128,
    CODE_39,
    CODE_93,
    CODABAR,
    DATA_MATRIX,
    EAN_13,
    EAN_8,
    ITF,
    QR_CODE,
    UPC_A,
    UPC_E,
    PDF417,
    AZTEC
}
