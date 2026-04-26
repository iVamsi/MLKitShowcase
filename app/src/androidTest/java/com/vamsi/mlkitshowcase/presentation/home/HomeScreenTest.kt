package com.vamsi.mlkitshowcase.presentation.home

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vamsi.mlkitshowcase.domain.model.ScanHistoryEntry
import com.vamsi.mlkitshowcase.domain.model.ScannerMode
import com.vamsi.mlkitshowcase.ui.theme.MLKitShowcaseTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun homeScreen_displaysAllRequiredElements() {
        // Given
        var barcodeNavigationCalled = false
        var textNavigationCalled = false
        var documentNavigationCalled = false

        // When
        composeTestRule.setContent {
            MLKitShowcaseTheme {
                HomeScreen(
                        onNavigateToBarcode = { barcodeNavigationCalled = true },
                        onNavigateToText = { textNavigationCalled = true },
                        onNavigateToDocument = { documentNavigationCalled = true }
                )
            }
        }

        // Then - Check if all main elements are displayed
        composeTestRule.onNodeWithText("ML Kit Showcase").assertIsDisplayed()

        composeTestRule.onNodeWithText("ML Kit on-device scanning").assertIsDisplayed()

        composeTestRule.onNodeWithText("Choose scanning type:").assertIsDisplayed()

        composeTestRule.onNodeWithText("Barcode Scanner").assertIsDisplayed()

        composeTestRule.onNodeWithText("Text Recognition").assertIsDisplayed()

        composeTestRule.onNodeWithText("Document Scanner").assertIsDisplayed()
    }

    @Test
    fun homeScreen_barcodeCardClick_triggersNavigation() {
        // Given
        var barcodeNavigationCalled = false
        var textNavigationCalled = false
        var documentNavigationCalled = false

        composeTestRule.setContent {
            MLKitShowcaseTheme {
                HomeScreen(
                        onNavigateToBarcode = { barcodeNavigationCalled = true },
                        onNavigateToText = { textNavigationCalled = true },
                        onNavigateToDocument = { documentNavigationCalled = true }
                )
            }
        }

        // When - Use test tag for more reliable testing
        composeTestRule.onNodeWithTag("barcode_scanner_card").performClick()

        // Then
        assert(barcodeNavigationCalled) { "Barcode navigation should be called" }
        assert(!textNavigationCalled) { "Text navigation should not be called" }
        assert(!documentNavigationCalled) { "Document navigation should not be called" }
    }

    @Test
    fun homeScreen_textRecognitionCardClick_triggersNavigation() {
        // Given
        var barcodeNavigationCalled = false
        var textNavigationCalled = false
        var documentNavigationCalled = false

        composeTestRule.setContent {
            MLKitShowcaseTheme {
                HomeScreen(
                        onNavigateToBarcode = { barcodeNavigationCalled = true },
                        onNavigateToText = { textNavigationCalled = true },
                        onNavigateToDocument = { documentNavigationCalled = true }
                )
            }
        }

        // When - Use test tag for more reliable testing
        composeTestRule.onNodeWithTag("text_recognition_card").performClick()

        // Then
        assert(!barcodeNavigationCalled) { "Barcode navigation should not be called" }
        assert(textNavigationCalled) { "Text navigation should be called" }
        assert(!documentNavigationCalled) { "Document navigation should not be called" }
    }

    @Test
    fun homeScreen_documentScannerCardClick_triggersNavigation() {
        // Given
        var documentNavigationCalled = false

        composeTestRule.setContent {
            MLKitShowcaseTheme {
                HomeScreen(
                        onNavigateToBarcode = {},
                        onNavigateToText = {},
                        onNavigateToDocument = { documentNavigationCalled = true }
                )
            }
        }

        // When
        composeTestRule.onNodeWithTag("document_scanner_card").performClick()

        // Then
        assert(documentNavigationCalled) { "Document navigation should be called" }
    }

    @Test
    fun homeScreen_displaysCorrectDescriptions() {
        // Given
        composeTestRule.setContent {
            MLKitShowcaseTheme { HomeScreen(onNavigateToBarcode = {}, onNavigateToText = {}) }
        }

        // Then - Check descriptive texts
        composeTestRule
                .onNodeWithText("Scan QR codes, product codes, Wi-Fi cards, and URLs")
                .assertIsDisplayed()

        composeTestRule
                .onNodeWithText("Recognize Latin-script text from the camera or an image")
                .assertIsDisplayed()

        composeTestRule
                .onNodeWithText("Use ML Kit's guided document scanner for JPEG or PDF output")
                .assertIsDisplayed()
    }

    @Test
    fun homeScreen_hasCorrectSemantics() {
        // Given
        composeTestRule.setContent {
            MLKitShowcaseTheme { HomeScreen(onNavigateToBarcode = {}, onNavigateToText = {}) }
        }

        // Then - Check that clickable cards are properly accessible
        composeTestRule.onNode(hasText("Barcode Scanner") and hasClickAction()).assertIsDisplayed()

        composeTestRule.onNode(hasText("Text Recognition") and hasClickAction()).assertIsDisplayed()

        composeTestRule.onNode(hasText("Document Scanner") and hasClickAction()).assertIsDisplayed()
    }

    @Test
    fun homeScreen_cardLayout_isCorrect() {
        // Given
        composeTestRule.setContent {
            MLKitShowcaseTheme { HomeScreen(onNavigateToBarcode = {}, onNavigateToText = {}) }
        }

        // Then - Verify both cards exist and are properly laid out using test tags
        composeTestRule
                .onNodeWithTag("barcode_scanner_card")
                .assertIsDisplayed()
                .assertHasClickAction()

        composeTestRule
                .onNodeWithTag("text_recognition_card")
                .assertIsDisplayed()
                .assertHasClickAction()

        composeTestRule
                .onNodeWithTag("document_scanner_card")
                .assertIsDisplayed()
                .assertHasClickAction()
    }

    @Test
    fun homeScreen_displaysRecentHistory() {
        // Given
        val history = listOf(
                ScanHistoryEntry(
                        mode = ScannerMode.BARCODE,
                        title = "https://example.com",
                        subtitle = "QR_CODE"
                )
        )

        composeTestRule.setContent {
            MLKitShowcaseTheme {
                HomeScreen(
                        historyItems = history,
                        onNavigateToBarcode = {},
                        onNavigateToText = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Recent scans").assertIsDisplayed()
        composeTestRule.onNodeWithText("https://example.com").assertIsDisplayed()
        composeTestRule.onNodeWithText("QR_CODE").assertIsDisplayed()
    }
}
