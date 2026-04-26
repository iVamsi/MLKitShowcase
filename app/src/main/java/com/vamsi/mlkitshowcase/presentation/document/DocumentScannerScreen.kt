package com.vamsi.mlkitshowcase.presentation.document

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.os.FileUriExposedException
import android.widget.ImageView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.vamsi.mlkitshowcase.domain.model.DocumentScanResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentScannerScreen(
    onNavigateBack: () -> Unit,
    viewModel: DocumentScannerViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var openErrorMessage by remember { mutableStateOf<String?>(null) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { activityResult ->
        if (activityResult.resultCode != Activity.RESULT_OK) {
            viewModel.idle()
            return@rememberLauncherForActivityResult
        }

        val scanResult = GmsDocumentScanningResult.fromActivityResultIntent(activityResult.data)
        if (scanResult == null) {
            viewModel.showError("Document scanner returned no result.")
        } else {
            viewModel.showResult(scanResult.toDocumentScanResult())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Document scanner") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val state = uiState) {
                DocumentScannerUiState.Idle -> {
                    Text(
                        text = "Scan a document",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "ML Kit opens a guided scanner and returns page images and a PDF when available.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = {
                            context.findActivity()?.let { activity ->
                                startDocumentScanner(
                                    activity = activity,
                                    onLaunching = viewModel::launching,
                                    onIntent = { request -> launcher.launch(request) },
                                    onError = viewModel::showError
                                )
                            } ?: viewModel.showError("Unable to start scanner from this context.")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Start document scanner")
                    }
                }

                DocumentScannerUiState.Launching -> {
                    CircularProgressIndicator()
                    Text("Preparing document scanner")
                }

                is DocumentScannerUiState.Success -> {
                    DocumentResultCard(
                        result = state.result,
                        onOpenTarget = { target ->
                            context.openDocumentTarget(
                                target = target,
                                onError = { message -> openErrorMessage = message }
                            )
                        }
                    )
                    openErrorMessage?.let { message ->
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            openErrorMessage = null
                            viewModel.idle()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Scan another document")
                    }
                }

                is DocumentScannerUiState.Error -> {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    OutlinedButton(
                        onClick = viewModel::idle,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Try again")
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentResultCard(
    result: DocumentScanResult,
    onOpenTarget: (DocumentOpenTarget) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = result.pageLabel,
                style = MaterialTheme.typography.titleLarge
            )
            Text("JPEG pages: ${result.imageUris.size}")
            Text("PDF: ${if (result.pdfUri != null) "available" else "not returned"}")
            result.imageUris.firstOrNull()?.let { pageUri ->
                DocumentPagePreview(uriString = pageUri)
                OutlinedButton(
                    onClick = { onOpenTarget(DocumentOpenTarget.PageImage(pageUri)) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open page image")
                }
            }
            result.pdfUri?.let { pdfUri ->
                Button(
                    onClick = { onOpenTarget(DocumentOpenTarget.Pdf(pdfUri)) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open PDF")
                }
            }
        }
    }
}

@Composable
private fun DocumentPagePreview(uriString: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        AndroidView(
            factory = { context ->
                ImageView(context).apply {
                    adjustViewBounds = true
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    contentDescription = "Preview of scanned page 1"
                }
            },
            update = { imageView ->
                runCatching {
                    imageView.setImageURI(uriString.toUri())
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 180.dp, max = 360.dp)
                .semantics {
                    contentDescription = "Preview of scanned page 1"
                }
        )
    }
}

private fun startDocumentScanner(
    activity: Activity,
    onLaunching: () -> Unit,
    onIntent: (IntentSenderRequest) -> Unit,
    onError: (String) -> Unit,
) {
    val options = GmsDocumentScannerOptions.Builder()
        .setGalleryImportAllowed(true)
        .setPageLimit(5)
        .setResultFormats(RESULT_FORMAT_JPEG, RESULT_FORMAT_PDF)
        .setScannerMode(SCANNER_MODE_FULL)
        .build()
    val scanner = GmsDocumentScanning.getClient(options)

    onLaunching()
    scanner.getStartScanIntent(activity)
        .addOnSuccessListener { intentSender ->
            onIntent(IntentSenderRequest.Builder(intentSender).build())
        }
        .addOnFailureListener { throwable ->
            onError(throwable.toDocumentScannerMessage())
        }
}

private fun GmsDocumentScanningResult.toDocumentScanResult(): DocumentScanResult {
    val imageUris = pages?.map { it.imageUri.toString() }.orEmpty()
    val pdfUri = pdf?.uri?.toString()
    val pageCount = pdf?.pageCount ?: imageUris.size
    return DocumentScanResult(
        pageCount = pageCount,
        imageUris = imageUris,
        pdfUri = pdfUri
    )
}

private fun Throwable.toDocumentScannerMessage(): String = when (this) {
    is MlKitException -> when (errorCode) {
        MlKitException.UNSUPPORTED -> "Document scanner is not supported on this device."
        MlKitException.UNAVAILABLE -> "Document scanner is not available yet. Try again after Google Play services finishes setup."
        else -> "Document scanner failed: ${message ?: "unknown error"}"
    }

    else -> "Document scanner failed: ${message ?: "unknown error"}"
}

private val DocumentScanResult.pageLabel: String
    get() = "$pageCount ${if (pageCount == 1) "page" else "pages"}"

private fun Context.openDocumentTarget(
    target: DocumentOpenTarget,
    onError: (String) -> Unit,
) {
    try {
        startActivity(target.toViewIntent(this))
    } catch (exception: ActivityNotFoundException) {
        onError("No app is available to open this scan.")
    } catch (exception: FileUriExposedException) {
        onError("Unable to share this scan file.")
    } catch (exception: IllegalArgumentException) {
        onError("Unable to prepare this scan file.")
    } catch (exception: SecurityException) {
        onError("The scanned file is no longer available. Scan the document again.")
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
