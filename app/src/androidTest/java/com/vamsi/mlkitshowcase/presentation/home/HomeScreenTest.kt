package com.vamsi.mlkitshowcase.presentation.home

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
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

        // When
        composeTestRule.setContent {
            MLKitShowcaseTheme {
                HomeScreen(
                        onNavigateToBarcode = { barcodeNavigationCalled = true },
                        onNavigateToText = { textNavigationCalled = true }
                )
            }
        }

        // Then - Check if all main elements are displayed
        composeTestRule.onNodeWithText("ML Kit Showcase").assertIsDisplayed()

        composeTestRule.onNodeWithText("ML Kit On-Device Scanning").assertIsDisplayed()

        composeTestRule.onNodeWithText("Choose scanning type:").assertIsDisplayed()

        composeTestRule.onNodeWithText("Barcode Scanner").assertIsDisplayed()

        composeTestRule.onNodeWithText("Text Recognition").assertIsDisplayed()
    }

    @Test
    fun homeScreen_barcodeCardClick_triggersNavigation() {
        // Given
        var barcodeNavigationCalled = false
        var textNavigationCalled = false

        composeTestRule.setContent {
            MLKitShowcaseTheme {
                HomeScreen(
                        onNavigateToBarcode = { barcodeNavigationCalled = true },
                        onNavigateToText = { textNavigationCalled = true }
                )
            }
        }

        // When - Use test tag for more reliable testing
        composeTestRule.onNodeWithTag("barcode_scanner_card").performClick()

        // Then
        assert(barcodeNavigationCalled) { "Barcode navigation should be called" }
        assert(!textNavigationCalled) { "Text navigation should not be called" }
    }

    @Test
    fun homeScreen_textRecognitionCardClick_triggersNavigation() {
        // Given
        var barcodeNavigationCalled = false
        var textNavigationCalled = false

        composeTestRule.setContent {
            MLKitShowcaseTheme {
                HomeScreen(
                        onNavigateToBarcode = { barcodeNavigationCalled = true },
                        onNavigateToText = { textNavigationCalled = true }
                )
            }
        }

        // When - Use test tag for more reliable testing
        composeTestRule.onNodeWithTag("text_recognition_card").performClick()

        // Then
        assert(!barcodeNavigationCalled) { "Barcode navigation should not be called" }
        assert(textNavigationCalled) { "Text navigation should be called" }
    }

    @Test
    fun homeScreen_displaysCorrectDescriptions() {
        // Given
        composeTestRule.setContent {
            MLKitShowcaseTheme { HomeScreen(onNavigateToBarcode = {}, onNavigateToText = {}) }
        }

        // Then - Check descriptive texts
        composeTestRule
                .onNodeWithText("Scan QR codes, UPC, EAN, and other barcodes")
                .assertIsDisplayed()

        composeTestRule.onNodeWithText("Extract text from images and documents").assertIsDisplayed()
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
    }
}
