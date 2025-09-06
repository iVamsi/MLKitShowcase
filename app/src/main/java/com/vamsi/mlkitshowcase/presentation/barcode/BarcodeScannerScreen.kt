package com.vamsi.mlkitshowcase.presentation.barcode

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vamsi.mlkitshowcase.R
import com.vamsi.mlkitshowcase.domain.model.ScanResult
import com.vamsi.mlkitshowcase.presentation.components.CameraPreview

/**
 * Barcode Scanner Screen
 *
 * This screen demonstrates ML Kit barcode scanning with:
 * - Real-time camera preview
 * - Multiple barcode format support
 * - Scan result display
 * - Clean UI with overlay indicators
 */
@Composable
fun BarcodeScannerScreen(
    onNavigateBack: () -> Unit,
    viewModel: BarcodeScannerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.startScanning()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopScanning()
        }
    }

    when (uiState) {
        is BarcodeScannerUiState.Scanning -> {
            CameraPreview(
                getImageAnalyzer = { viewModel.getImageAnalyzer() },
                overlayContent = {
                    // Scan area indicator
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier.size(250.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Transparent
                            ),
                            border = BorderStroke(2.dp, Color.White),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Text(
                                    text = stringResource(R.string.point_camera_at_barcode),
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }

                    // Scanning status
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(100.dp))

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Black.copy(alpha = 0.7f)
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.scanning),
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                },
                onClose = {
                    viewModel.stopScanning()
                    onNavigateBack()
                }
            )
        }

        is BarcodeScannerUiState.Success -> {
            ScanResultDialog(
                result = (uiState as BarcodeScannerUiState.Success).result,
                onDismiss = viewModel::resumeScanning,
                onClose = {
                    viewModel.stopScanning()
                    onNavigateBack()
                }
            )
        }

        is BarcodeScannerUiState.Error -> {
            ErrorDialog(
                message = (uiState as BarcodeScannerUiState.Error).message,
                onRetry = viewModel::resumeScanning,
                onClose = {
                    viewModel.stopScanning()
                    onNavigateBack()
                }
            )
        }
    }
}

@Composable
private fun ScanResultDialog(
    result: ScanResult.BarcodeResult,
    onDismiss: () -> Unit,
    onClose: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.scan_result),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Format: ${result.format}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Value:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = result.value,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.try_again))
            }
        },
        dismissButton = {
            TextButton(onClick = onClose) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

@Composable
private fun ErrorDialog(
    message: String,
    onRetry: () -> Unit,
    onClose: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onRetry,
        title = {
            Text(
                text = "Scanning Error",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onRetry) {
                Text(stringResource(R.string.try_again))
            }
        },
        dismissButton = {
            TextButton(onClick = onClose) {
                Text(stringResource(R.string.close))
            }
        }
    )
}