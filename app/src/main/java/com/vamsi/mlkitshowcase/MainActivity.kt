package com.vamsi.mlkitshowcase

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.vamsi.mlkitshowcase.presentation.barcode.BarcodeScannerScreen
import com.vamsi.mlkitshowcase.presentation.home.HomeScreen
import com.vamsi.mlkitshowcase.presentation.text.TextRecognitionScreen
import com.vamsi.mlkitshowcase.ui.theme.MLKitShowcaseTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity for Scanner Demo
 *
 * This activity demonstrates ML Kit integration with proper permission handling and navigation
 * between different scanning modes.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
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

                ScannerDemoApp()
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScannerDemoApp() {
    val navController = rememberNavController()
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    when (cameraPermissionState.status) {
        is PermissionStatus.Denied -> {
            // Show permission request UI
            PermissionRequestScreen(
                onRequestPermission = { cameraPermissionState.launchPermissionRequest() }
            )
        }

        PermissionStatus.Granted -> {
            // Camera permission granted, show main navigation
            NavHost(navController = navController, startDestination = "home") {
                composable("home") {
                    HomeScreen(
                        onNavigateToBarcode = { navController.navigate("barcode") },
                        onNavigateToText = { navController.navigate("text") }
                    )
                }

                composable("barcode") {
                    BarcodeScannerScreen(onNavigateBack = { navController.popBackStack() })
                }

                composable("text") {
                    TextRecognitionScreen(onNavigateBack = { navController.popBackStack() })
                }
            }
        }
    }
}

@Composable
private fun PermissionRequestScreen(
    onRequestPermission: () -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Camera Permission Required",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.camera_permission_required),
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(onClick = onRequestPermission, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.grant_permission))
            }
        }
    }
}
