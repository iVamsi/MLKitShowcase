package com.vamsi.mlkitshowcase.presentation.barcode

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vamsi.mlkitshowcase.R
import com.vamsi.mlkitshowcase.domain.model.BarcodePayload
import com.vamsi.mlkitshowcase.domain.model.ScanResult
import com.vamsi.mlkitshowcase.presentation.components.CameraPreview
import com.vamsi.mlkitshowcase.presentation.components.DetectedBoundsOverlay
import com.vamsi.mlkitshowcase.presentation.components.ScannerActionPrompt
import com.vamsi.mlkitshowcase.presentation.components.ScannerScreenScaffold

@Composable
fun BarcodeScannerScreen(
    onNavigateBack: () -> Unit,
    viewModel: BarcodeScannerViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var hasCameraPermission by remember {
        mutableStateOf(context.hasCameraPermission())
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }
    val imageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.scanImage(context, it) }
    }

    LaunchedEffect(hasCameraPermission) {
        if (hasCameraPermission) {
            viewModel.startScanning()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopScanning()
        }
    }

    ScannerScreenScaffold(
        title = stringResource(R.string.barcode_scanner),
        onNavigateBack = {
            viewModel.stopScanning()
            onNavigateBack()
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is BarcodeScannerUiState.Scanning -> {
                if (hasCameraPermission) {
                    LiveBarcodeScanner(
                        modifier = Modifier.padding(paddingValues),
                        viewModel = viewModel,
                        onPickImage = { imageLauncher.launch("image/*") },
                    )
                } else {
                    ScannerActionPrompt(
                        modifier = Modifier.padding(paddingValues),
                        title = "Scan a barcode",
                        description = "Use the camera for live scanning or scan a barcode from an image.",
                        onStartCamera = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        onPickImage = { imageLauncher.launch("image/*") },
                    )
                }
            }

            is BarcodeScannerUiState.Success -> {
                ScanResultDialog(
                    result = state.result,
                    onDismiss = viewModel::resumeScanning,
                    onClose = {
                        viewModel.stopScanning()
                        onNavigateBack()
                    }
                )
            }

            is BarcodeScannerUiState.Error -> {
                ErrorDialog(
                    message = state.message,
                    onRetry = viewModel::resumeScanning,
                    onClose = {
                        viewModel.stopScanning()
                        onNavigateBack()
                    }
                )
            }
        }
    }
}

@Composable
private fun LiveBarcodeScanner(
    modifier: Modifier = Modifier,
    viewModel: BarcodeScannerViewModel,
    onPickImage: () -> Unit,
) {
    val detectedBounds by viewModel.detectedBounds.collectAsStateWithLifecycle()
    CameraPreview(
        modifier = modifier,
        getImageAnalyzer = viewModel::getImageAnalyzer,
        overlayContent = {
            DetectedBoundsOverlay(bounds = detectedBounds)
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.size(250.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
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
                OutlinedButton(
                    onClick = onPickImage,
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text("Scan image")
                }
            }
        },
        onClose = viewModel::stopScanning
    )
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
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = result.scan.summary,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
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
                BarcodePayloadDetails(result.scan.payload)
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
private fun BarcodePayloadDetails(payload: BarcodePayload) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            when (payload) {
                is BarcodePayload.PlainText -> DetailRow("Type", "Plain text")
                is BarcodePayload.Url -> {
                    DetailRow("Type", "URL")
                    DetailRow("Title", payload.title)
                    DetailRow("URL", payload.url)
                }

                is BarcodePayload.Wifi -> {
                    DetailRow("Type", "Wi-Fi")
                    DetailRow("SSID", payload.ssid)
                    DetailRow("Security", payload.encryptionType)
                    DetailRow("Password", payload.password)
                }

                is BarcodePayload.Contact -> {
                    DetailRow("Type", "Contact")
                    DetailRow("Name", payload.name)
                    DetailRow("Organization", payload.organization)
                }

                is BarcodePayload.Phone -> {
                    DetailRow("Type", "Phone")
                    DetailRow("Number", payload.number)
                    DetailRow("Kind", payload.type)
                }

                is BarcodePayload.Sms -> {
                    DetailRow("Type", "SMS")
                    DetailRow("Phone", payload.phoneNumber)
                    DetailRow("Message", payload.message)
                }

                is BarcodePayload.Email -> {
                    DetailRow("Type", "Email")
                    DetailRow("Address", payload.address)
                    DetailRow("Subject", payload.subject)
                }

                is BarcodePayload.CalendarEvent -> {
                    DetailRow("Type", "Calendar")
                    DetailRow("Summary", payload.summaryText)
                    DetailRow("Location", payload.location)
                }

                is BarcodePayload.GeoPoint -> {
                    DetailRow("Type", "Location")
                    DetailRow("Latitude", payload.latitude.toString())
                    DetailRow("Longitude", payload.longitude.toString())
                }

                is BarcodePayload.Isbn -> {
                    DetailRow("Type", "ISBN")
                    DetailRow("Value", payload.value)
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String?) {
    if (!value.isNullOrBlank()) {
        Text(
            text = "$label: $value",
            style = MaterialTheme.typography.bodySmall
        )
    }
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
                text = "Scanning error",
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

private fun Context.hasCameraPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED
