package com.vamsi.mlkitshowcase.presentation.barcode

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.camera.core.ImageAnalysis
import com.vamsi.mlkitshowcase.data.scanner.MLKitBarcodeScanner
import com.vamsi.mlkitshowcase.domain.model.BarcodeFormat
import com.vamsi.mlkitshowcase.domain.model.ScanResult
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class BarcodeScannerViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var mockBarcodeScanner: MLKitBarcodeScanner
    private lateinit var viewModel: BarcodeScannerViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockBarcodeScanner = mockk(relaxed = true)

        // Setup default behavior for scanResults flow
        every { mockBarcodeScanner.scanResults } returns flowOf()

        viewModel = BarcodeScannerViewModel(mockBarcodeScanner)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `initial state should be Scanning`() = runTest {
        // Given & When - ViewModel is created

        // Then
        assertThat(viewModel.uiState.value).isEqualTo(BarcodeScannerUiState.Scanning)
    }

    @Test
    fun `startScanning should start scanner and set state to Scanning`() = runTest {
        // Given
        every { mockBarcodeScanner.startScanning() } just Runs

        // When
        viewModel.startScanning()

        // Then
        verify { mockBarcodeScanner.startScanning() }
        assertThat(viewModel.uiState.value).isEqualTo(BarcodeScannerUiState.Scanning)
    }

    @Test
    fun `stopScanning should stop scanner`() = runTest {
        // Given
        every { mockBarcodeScanner.stopScanning() } just Runs

        // When
        viewModel.stopScanning()

        // Then
        verify { mockBarcodeScanner.stopScanning() }
    }

    @Test
    fun `resumeScanning should call startScanning`() = runTest {
        // Given
        every { mockBarcodeScanner.startScanning() } just Runs

        // When
        viewModel.resumeScanning()

        // Then
        verify { mockBarcodeScanner.startScanning() }
        assertThat(viewModel.uiState.value).isEqualTo(BarcodeScannerUiState.Scanning)
    }

    @Test
    fun `getImageAnalyzer should return analyzer from scanner`() = runTest {
        // Given
        val mockAnalyzer = mockk<ImageAnalysis.Analyzer>()
        coEvery { mockBarcodeScanner.getImageAnalyzer() } returns mockAnalyzer

        // When
        val result = viewModel.getImageAnalyzer()

        // Then
        assertThat(result).isEqualTo(mockAnalyzer)
        coVerify { mockBarcodeScanner.getImageAnalyzer() }
    }

    @Test
    fun `barcode result should stop scanning and update state to Success`() = runTest {
        // Given
        val barcodeResult = ScanResult.BarcodeResult(
            value = "123456789",
            format = BarcodeFormat.EAN_13
        )
        val resultsChannel = Channel<ScanResult>(Channel.UNLIMITED)

        every { mockBarcodeScanner.scanResults } returns resultsChannel.receiveAsFlow()
        every { mockBarcodeScanner.stopScanning() } just Runs

        // Create new ViewModel to capture flow subscription
        viewModel = BarcodeScannerViewModel(mockBarcodeScanner)

        // When
        resultsChannel.trySend(barcodeResult)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify { mockBarcodeScanner.stopScanning() }
        assertThat(viewModel.uiState.value).isInstanceOf(BarcodeScannerUiState.Success::class.java)

        val successState = viewModel.uiState.value as BarcodeScannerUiState.Success
        assertThat(successState.result).isEqualTo(barcodeResult)
    }

    @Test
    fun `error result should update state to Error`() = runTest {
        // Given
        val errorMessage = "Scanning failed"
        val errorResult = ScanResult.Error(errorMessage)
        val resultsChannel = Channel<ScanResult>(Channel.UNLIMITED)

        every { mockBarcodeScanner.scanResults } returns resultsChannel.receiveAsFlow()

        // Create new ViewModel to capture flow subscription
        viewModel = BarcodeScannerViewModel(mockBarcodeScanner)

        // When
        resultsChannel.trySend(errorResult)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertThat(viewModel.uiState.value).isInstanceOf(BarcodeScannerUiState.Error::class.java)

        val errorState = viewModel.uiState.value as BarcodeScannerUiState.Error
        assertThat(errorState.message).isEqualTo(errorMessage)
    }

    @Test
    fun `text result should not change state - continue scanning`() = runTest {
        // Given
        val textResult = ScanResult.TextResult("Some text")
        val resultsChannel = Channel<ScanResult>(Channel.UNLIMITED)

        every { mockBarcodeScanner.scanResults } returns resultsChannel.receiveAsFlow()

        // Create new ViewModel to capture flow subscription
        viewModel = BarcodeScannerViewModel(mockBarcodeScanner)

        // When
        resultsChannel.trySend(textResult)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertThat(viewModel.uiState.value).isEqualTo(BarcodeScannerUiState.Scanning)
        verify(exactly = 0) { mockBarcodeScanner.stopScanning() }
    }

    @Test
    fun `no result should not change state - continue scanning`() = runTest {
        // Given
        val noResult = ScanResult.NoResult
        val resultsChannel = Channel<ScanResult>(Channel.UNLIMITED)

        every { mockBarcodeScanner.scanResults } returns resultsChannel.receiveAsFlow()

        // Create new ViewModel to capture flow subscription
        viewModel = BarcodeScannerViewModel(mockBarcodeScanner)

        // When
        resultsChannel.trySend(noResult)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertThat(viewModel.uiState.value).isEqualTo(BarcodeScannerUiState.Scanning)
        verify(exactly = 0) { mockBarcodeScanner.stopScanning() }
    }

    @Test
    fun `multiple results should be handled correctly`() = runTest {
        // Given
        val resultsChannel = Channel<ScanResult>(Channel.UNLIMITED)
        val barcodeResult = ScanResult.BarcodeResult("123", BarcodeFormat.QR_CODE)

        every { mockBarcodeScanner.scanResults } returns resultsChannel.receiveAsFlow()
        every { mockBarcodeScanner.stopScanning() } just Runs

        // Create new ViewModel to capture flow subscription
        viewModel = BarcodeScannerViewModel(mockBarcodeScanner)

        // When - Send multiple results
        resultsChannel.trySend(ScanResult.NoResult)
        resultsChannel.trySend(ScanResult.TextResult("text"))
        resultsChannel.trySend(barcodeResult) // This should trigger success
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify { mockBarcodeScanner.stopScanning() }
        assertThat(viewModel.uiState.value).isInstanceOf(BarcodeScannerUiState.Success::class.java)

        val successState = viewModel.uiState.value as BarcodeScannerUiState.Success
        assertThat(successState.result).isEqualTo(barcodeResult)
    }
}