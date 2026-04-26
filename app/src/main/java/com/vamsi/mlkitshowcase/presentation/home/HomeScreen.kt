package com.vamsi.mlkitshowcase.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
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
import com.vamsi.mlkitshowcase.domain.model.ScanHistoryEntry
import com.vamsi.mlkitshowcase.domain.model.ScannerMode

/**
 * Home Screen
 *
 * Main screen that allows users to choose between barcode scanning and text recognition.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    historyItems: List<ScanHistoryEntry> = emptyList(),
    onNavigateToBarcode: () -> Unit,
    onNavigateToText: () -> Unit,
    onNavigateToDocument: () -> Unit = {},
) {
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "ML Kit on-device scanning",
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

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FeatureCard(
                    title = stringResource(R.string.barcode_scanner),
                    description = "Scan QR codes, product codes, Wi-Fi cards, and URLs",
                    icon = { FeatureIcon(Icons.Default.CameraAlt) },
                    testTag = "barcode_scanner_card",
                    onClick = onNavigateToBarcode
                )
                FeatureCard(
                    title = stringResource(R.string.text_recognizer),
                    description = "Recognize Latin-script text from the camera or an image",
                    icon = { FeatureIcon(Icons.Default.TextFields) },
                    testTag = "text_recognition_card",
                    onClick = onNavigateToText
                )
                FeatureCard(
                    title = stringResource(R.string.document_scanner),
                    description = "Use ML Kit's guided document scanner for JPEG or PDF output",
                    icon = { FeatureIcon(Icons.Default.Description) },
                    testTag = "document_scanner_card",
                    onClick = onNavigateToDocument
                )
            }

            if (historyItems.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.recent_scans),
                        style = MaterialTheme.typography.titleMedium
                    )
                    historyItems.forEach { item ->
                        ListItem(
                            headlineContent = { Text(item.title) },
                            supportingContent = { Text(item.subtitle) },
                            leadingContent = {
                                Text(
                                    text = item.mode.label(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureCard(
    title: String,
    description: String,
    icon: @Composable () -> Unit,
    testTag: String,
    onClick: () -> Unit,
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().testTag(testTag)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FeatureIcon(imageVector: androidx.compose.ui.graphics.vector.ImageVector) {
    Icon(
        imageVector = imageVector,
        contentDescription = null,
        modifier = Modifier.size(40.dp),
        tint = MaterialTheme.colorScheme.primary
    )
}

private fun ScannerMode.label(): String = when (this) {
    ScannerMode.BARCODE -> "Code"
    ScannerMode.TEXT -> "Text"
    ScannerMode.DOCUMENT -> "Doc"
}
