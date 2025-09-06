package com.vamsi.mlkitshowcase.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vamsi.mlkitshowcase.R

/**
 * Home Screen
 *
 * Main screen that allows users to choose between barcode scanning and text recognition.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onNavigateToBarcode: () -> Unit, onNavigateToText: () -> Unit) {
    Scaffold(
            topBar = {
                TopAppBar(
                        title = {
                            Text(
                                    text = stringResource(R.string.ml_kit_scanner_demo),
                                    style = MaterialTheme.typography.headlineSmall
                            )
                        }
                )
            }
    ) { paddingValues ->
        Column(
                modifier =
                        Modifier.fillMaxSize()
                                .padding(paddingValues)
                                .padding(24.dp)
                                .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Header
            Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                        text = "ML Kit On-Device Scanning",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center
                )
                Text(
                        text = stringResource(R.string.choose_scan_type),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action buttons
            Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Barcode Scanner Button
                ElevatedCard(
                        onClick = { onNavigateToBarcode() },
                        modifier = Modifier.fillMaxWidth().testTag("barcode_scanner_card")
                ) {
                    Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                                text = stringResource(R.string.barcode_scanner),
                                style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                                text = "Scan QR codes, UPC, EAN, and other barcodes",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Text Recognition Button
                ElevatedCard(
                        onClick = { onNavigateToText() },
                        modifier = Modifier.fillMaxWidth().testTag("text_recognition_card")
                ) {
                    Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                                imageVector = Icons.Default.TextFields,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                                text = stringResource(R.string.text_recognizer),
                                style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                                text = "Extract text from images and documents",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
