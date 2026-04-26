package com.vamsi.mlkitshowcase.presentation.scanner

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vamsi.mlkitshowcase.data.scanner.MLKitBarcodeScanner
import com.vamsi.mlkitshowcase.data.scanner.MLKitTextRecognizer
import com.vamsi.mlkitshowcase.domain.model.ScanHistoryStore
import com.vamsi.mlkitshowcase.presentation.barcode.BarcodeScannerScreen
import com.vamsi.mlkitshowcase.presentation.barcode.BarcodeScannerViewModel
import com.vamsi.mlkitshowcase.presentation.text.TextRecognitionScreen
import com.vamsi.mlkitshowcase.presentation.text.TextRecognitionViewModel
import com.vamsi.mlkitshowcase.ui.theme.MLKitShowcaseTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScannerTopBarTest {

    @get:Rule val composeRule = createComposeRule()

    @Test
    fun barcodeScanner_usesTopAppBarNavigationInsteadOfStandaloneBackButton() {
        var backCalled = false
        val viewModel = BarcodeScannerViewModel(
            barcodeScanner = MLKitBarcodeScanner(),
            historyStore = ScanHistoryStore()
        )

        composeRule.setContent {
            MLKitShowcaseTheme {
                BarcodeScannerScreen(
                    onNavigateBack = { backCalled = true },
                    viewModel = viewModel
                )
            }
        }

        composeRule.onNodeWithText("Barcode Scanner").assertIsDisplayed()
        composeRule.onAllNodesWithText("Back").assertCountEquals(0)
        composeRule.onNodeWithContentDescription("Back").assertIsDisplayed().performClick()

        assert(backCalled) { "Top app bar navigation should trigger back navigation" }
    }

    @Test
    fun textRecognition_usesTopAppBarNavigationInsteadOfStandaloneBackButton() {
        var backCalled = false
        val viewModel = TextRecognitionViewModel(
            textRecognizer = MLKitTextRecognizer(),
            historyStore = ScanHistoryStore()
        )

        composeRule.setContent {
            MLKitShowcaseTheme {
                TextRecognitionScreen(
                    onNavigateBack = { backCalled = true },
                    viewModel = viewModel
                )
            }
        }

        composeRule.onNodeWithText("Text Recognition").assertIsDisplayed()
        composeRule.onAllNodesWithText("Back").assertCountEquals(0)
        composeRule.onNodeWithContentDescription("Back").assertIsDisplayed().performClick()

        assert(backCalled) { "Top app bar navigation should trigger back navigation" }
    }
}
