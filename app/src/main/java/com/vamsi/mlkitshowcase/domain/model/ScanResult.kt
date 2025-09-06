package com.vamsi.mlkitshowcase.domain.model

/**
 * Represents the result of a scanning operation
 */
sealed class ScanResult {
    data class BarcodeResult(
        val value: String,
        val format: BarcodeFormat,
        val rawBytes: ByteArray? = null,
    ) : ScanResult() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as BarcodeResult

            if (value != other.value) return false
            if (format != other.format) return false
            if (rawBytes != null) {
                if (other.rawBytes == null) return false
                if (!rawBytes.contentEquals(other.rawBytes)) return false
            } else if (other.rawBytes != null) return false

            return true
        }

        override fun hashCode(): Int {
            var result = value.hashCode()
            result = 31 * result + format.hashCode()
            result = 31 * result + (rawBytes?.contentHashCode() ?: 0)
            return result
        }
    }

    data class TextResult(
        val text: String,
        val confidence: Float? = null,
    ) : ScanResult()

    data object NoResult : ScanResult()
    data class Error(val message: String, val throwable: Throwable? = null) : ScanResult()
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