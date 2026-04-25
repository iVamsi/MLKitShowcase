package com.vamsi.mlkitshowcase.presentation.document

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.core.content.FileProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vamsi.mlkitshowcase.domain.model.DocumentScanResult
import com.vamsi.mlkitshowcase.domain.model.ScanHistoryStore
import com.vamsi.mlkitshowcase.ui.theme.MLKitShowcaseTheme
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DocumentScannerScreenTest {

    @get:Rule val composeRule = createComposeRule()

    @Test
    fun successfulScanShowsPreviewAndOpenActions() {
        val viewModel = DocumentScannerViewModel(ScanHistoryStore())
        viewModel.showResult(
            DocumentScanResult(
                pageCount = 1,
                imageUris = listOf(createPreviewImageUri()),
                pdfUri = "content://scan/result.pdf"
            )
        )

        composeRule.setContent {
            MLKitShowcaseTheme {
                DocumentScannerScreen(
                    onNavigateBack = {},
                    viewModel = viewModel
                )
            }
        }

        composeRule.onNodeWithText("1 page").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Preview of scanned page 1").assertIsDisplayed()
        composeRule.onNodeWithText("Open PDF").assertIsDisplayed()
        composeRule.onNodeWithText("Open page image").assertIsDisplayed()
    }

    private fun createPreviewImageUri(): String {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val previewFile = File(context.cacheDir, "document-preview-test.png")
        val bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.WHITE)
        }

        previewFile.outputStream().use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        }
        bitmap.recycle()

        return FileProvider
            .getUriForFile(context, DOCUMENT_FILE_PROVIDER_AUTHORITY, previewFile)
            .toString()
    }
}
