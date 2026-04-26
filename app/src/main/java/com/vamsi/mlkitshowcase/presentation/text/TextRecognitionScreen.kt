package com.vamsi.mlkitshowcase.presentation.text

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.vamsi.mlkitshowcase.domain.model.ScanResult
import com.vamsi.mlkitshowcase.presentation.components.CameraPreview
import com.vamsi.mlkitshowcase.presentation.components.DetectedBoundsOverlay
import com.vamsi.mlkitshowcase.presentation.components.ScannerActionPrompt
import com.vamsi.mlkitshowcase.presentation.components.ScannerScreenScaffold

@Composable
fun TextRecognitionScreen(
    onNavigateBack: () -> Unit,
    viewModel: TextRecognitionViewModel = hiltViewModel(),
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
        title = stringResource(R.string.text_recognizer),
        onNavigateBack = {
            viewModel.stopScanning()
            onNavigateBack()
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is TextRecognitionUiState.Scanning -> {
                if (hasCameraPermission) {
                    LiveTextScanner(
                        modifier = Modifier.padding(paddingValues),
                        viewModel = viewModel,
                        onPickImage = { imageLauncher.launch("image/*") },
                    )
                } else {
                    ScannerActionPrompt(
                        modifier = Modifier.padding(paddingValues),
                        title = "Recognize text",
                        description = "Use the camera for live Latin-script text recognition or scan an image.",
                        onStartCamera = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        onPickImage = { imageLauncher.launch("image/*") },
                    )
                }
            }

            is TextRecognitionUiState.Success -> {
                TextResultDialog(
                    result = state.result,
                    onDismiss = viewModel::resumeScanning,
                    onClose = {
                        viewModel.stopScanning()
                        onNavigateBack()
                    }
                )
            }

            is TextRecognitionUiState.Error -> {
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
private fun LiveTextScanner(
    modifier: Modifier = Modifier,
    viewModel: TextRecognitionViewModel,
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
                    modifier = Modifier.size(300.dp, 200.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
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
                        text = "Scanning for text",
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
private fun TextResultDialog(
    result: ScanResult.TextResult,
    onDismiss: () -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Recognized text",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.height(400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${result.scan.lineCount} lines, ${result.scan.blocks.size} blocks",
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { context.copyText(result.text) }) {
                    Text("Copy")
                }
                TextButton(onClick = { context.shareText(result.text) }) {
                    Text("Share")
                }
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.try_again))
                }
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
                text = "Text recognition error",
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

private fun Context.copyText(text: String) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Recognized text", text))
}

private fun Context.shareText(text: String) {
    val intent = Intent(Intent.ACTION_SEND)
        .setType("text/plain")
        .putExtra(Intent.EXTRA_TEXT, text)
    startActivity(Intent.createChooser(intent, "Share recognized text"))
}
