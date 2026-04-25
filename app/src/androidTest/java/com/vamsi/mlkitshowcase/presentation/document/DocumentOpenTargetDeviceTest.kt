package com.vamsi.mlkitshowcase.presentation.document

import android.content.Intent
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DocumentOpenTargetDeviceTest {

    @Test
    fun filePdfTargetUsesFileProviderContentUri() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val pdfFile = File(context.cacheDir, "scan.pdf").apply {
            writeText("fake pdf")
        }

        val intent = DocumentOpenTarget.Pdf(Uri.fromFile(pdfFile).toString()).toViewIntent(context)

        assertEquals("content", intent.data?.scheme)
        assertEquals(DOCUMENT_FILE_PROVIDER_AUTHORITY, intent.data?.authority)
        assertEquals("application/pdf", intent.type)
        assertEquals(1, intent.clipData?.itemCount)
        assertEquals(intent.data, intent.clipData?.getItemAt(0)?.uri)
        assertNotEquals(0, intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION)
        assertNotNull(context.contentResolver.openInputStream(requireNotNull(intent.data)))
    }
}
