package com.vamsi.mlkitshowcase.presentation.text

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
 * Text Recognition Screen
 *
 * This screen demonstrates ML Kit text recognition with:
 * - Real-time camera preview
 * - Latin script text recognition
 * - Extracted text display
 * - Confidence scoring (when available)
 */
@Composable
fun TextRecognitionScreen(
    onNavigateBack: () -> Unit,
    viewModel: TextRecognitionViewModel = hiltViewModel(),
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
        is TextRecognitionUiState.Scanning -> {
            CameraPreview(
                getImageAnalyzer = { viewModel.getImageAnalyzer() },
                overlayContent = {
                    // Text recognition area indicator
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier.size(300.dp, 200.dp),
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
                                    text = stringResource(R.string.point_camera_at_text),
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
                        Spacer(modifier = Modifier.height(80.dp))

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Black.copy(alpha = 0.7f)
                            )
                        ) {
                            Text(
                                text = "Scanning for text...",
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

        is TextRecognitionUiState.Success -> {
            TextResultDialog(
                result = (uiState as TextRecognitionUiState.Success).result,
                onDismiss = viewModel::resumeScanning,
                onClose = {
                    viewModel.stopScanning()
                    onNavigateBack()
                }
            )
        }

        is TextRecognitionUiState.Error -> {
            ErrorDialog(
                message = (uiState as TextRecognitionUiState.Error).message,
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
private fun TextResultDialog(
    result: ScanResult.TextResult,
    onDismiss: () -> Unit,
    onClose: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Recognized Text",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.height(400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Show confidence if available
                result.confidence?.let { confidence ->
                    Text(
                        text = "Confidence: ${(confidence * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            confidence > 0.8f -> MaterialTheme.colorScheme.primary
                            confidence > 0.6f -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.error
                        }
                    )
                }

                Text(
                    text = "Extracted Text:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Text(
                        text = result.text,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState())
                    )
                }

                Text(
                    text = "Character count: ${result.text.length}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                text = "Text Recognition Error",
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