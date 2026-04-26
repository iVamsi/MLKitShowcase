package com.vamsi.mlkitshowcase.presentation.document

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.core.content.FileProvider
import java.io.File

internal const val DOCUMENT_FILE_PROVIDER_AUTHORITY = "com.vamsi.mlkitshowcase.fileprovider"

sealed class DocumentOpenTarget(
    private val uriString: String,
    private val mimeType: String,
) {
    data class Pdf(private val uri: String) : DocumentOpenTarget(uri, "application/pdf")
    data class PageImage(private val uri: String) : DocumentOpenTarget(uri, "image/jpeg")

    fun toViewIntent(context: Context): Intent {
        val uri = uriString.toUri().toShareableUri(context)
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            clipData = ClipData.newUri(context.contentResolver, "ML Kit scan", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun android.net.Uri.toShareableUri(context: Context): android.net.Uri {
        if (scheme != "file") return this

        val path = requireNotNull(path) { "File URI must include a path." }
        return FileProvider.getUriForFile(
            context,
            DOCUMENT_FILE_PROVIDER_AUTHORITY,
            File(path)
        )
    }
}
