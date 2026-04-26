package com.vamsi.mlkitshowcase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vamsi.mlkitshowcase.domain.model.ScanHistoryStore
import com.vamsi.mlkitshowcase.presentation.barcode.BarcodeScannerScreen
import com.vamsi.mlkitshowcase.presentation.document.DocumentScannerScreen
import com.vamsi.mlkitshowcase.presentation.home.HomeScreen
import com.vamsi.mlkitshowcase.presentation.text.TextRecognitionScreen
import com.vamsi.mlkitshowcase.ui.theme.MLKitShowcaseTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Main Activity for Scanner Demo
 *
 * This activity demonstrates ML Kit integration with proper permission handling and navigation
 * between different scanning modes.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var historyStore: ScanHistoryStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MLKitShowcaseTheme {
                // Update status bar appearance based on theme
                val darkTheme = isSystemInDarkTheme()
                LaunchedEffect(darkTheme) {
                    WindowCompat.getInsetsController(window, window.decorView).apply {
                        isAppearanceLightStatusBars = !darkTheme
                    }
                }

                ScannerDemoApp(historyStore)
            }
        }
    }
}

@Composable
fun ScannerDemoApp(historyStore: ScanHistoryStore) {
    val navController = rememberNavController()
    val historyItems by historyStore.items.collectAsStateWithLifecycle()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                historyItems = historyItems,
                onNavigateToBarcode = { navController.navigate("barcode") },
                onNavigateToText = { navController.navigate("text") },
                onNavigateToDocument = { navController.navigate("document") }
            )
        }

        composable("barcode") {
            BarcodeScannerScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable("text") {
            TextRecognitionScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable("document") {
            DocumentScannerScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
