package com.vamsi.mlkitshowcase.presentation.components

import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.vamsi.mlkitshowcase.R

/**
 * Camera preview component with ML Kit integration
 *
 * This composable demonstrates how to integrate CameraX with ML Kit for real-time scanning.
 *
 * Key features:
 * - CameraX Preview integration
 * - ImageAnalysis with ML Kit analyzer
 * - Proper lifecycle management
 * - Error handling and loading states
 * - Overlay UI for user guidance
 *
 * @param modifier Modifier for the composable
 * @param getImageAnalyzer Suspend function that returns the ML Kit analyzer
 * @param overlayContent Content to display as overlay (e.g., scan indicators)
 * @param onClose Callback when user closes the camera
 */
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    getImageAnalyzer: suspend () -> ImageAnalysis.Analyzer,
    overlayContent: @Composable BoxScope.() -> Unit = {},
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Create PreviewView - this will display the camera feed
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    // Initialize camera when the composable is first composed
    LaunchedEffect(Unit) {
        try {
            // Get CameraProvider instance
            val cameraProvider = ProcessCameraProvider.getInstance(context).get()

            // Get the image analyzer for ML Kit
            val analyzer = getImageAnalyzer()

            // Configure Preview use case
            val preview = Preview.Builder()
                .setTargetResolution(android.util.Size(1280, 720))
                .build()

            // Configure ImageAnalysis use case
            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(android.util.Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            // Set the analyzer on the ImageAnalysis use case
            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context), analyzer)

            // Connect the preview to the PreviewView
            preview.setSurfaceProvider(previewView.surfaceProvider)

            // Unbind all use cases before rebinding
            cameraProvider.unbindAll()

            // Bind use cases to camera lifecycle
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA, // Use back camera
                preview,
                imageAnalysis
            )

            isLoading = false
        } catch (e: Exception) {
            errorMessage = "Failed to start camera: ${e.message}"
            isLoading = false
        }
    }

    // Cleanup camera when composable is disposed
    DisposableEffect(lifecycleOwner) {
        onDispose {
            try {
                val cameraProvider = ProcessCameraProvider.getInstance(context).get()
                cameraProvider.unbindAll()
            } catch (e: Exception) {
                // Log error but don't crash
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            isLoading -> {
                // Loading state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "Starting camera...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            errorMessage != null -> {
                // Error state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Camera Error",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = errorMessage!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Button(
                            onClick = onClose,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(stringResource(R.string.close))
                        }
                    }
                }
            }

            else -> {
                // Camera preview
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Close button (always visible)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            FilledIconButton(
                onClick = onClose,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.7f)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.close),
                    tint = Color.White
                )
            }
        }

        // Overlay content (scan indicators, etc.)
        if (!isLoading && errorMessage == null) {
            overlayContent()
        }
    }
}