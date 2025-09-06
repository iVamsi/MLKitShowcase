package com.vamsi.mlkitshowcase.presentation.text

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.camera.core.ImageAnalysis
import com.vamsi.mlkitshowcase.data.scanner.MLKitTextRecognizer
import com.vamsi.mlkitshowcase.domain.model.BarcodeFormat
import com.vamsi.mlkitshowcase.domain.model.ScanResult
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class TextRecognitionViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var mockTextRecognizer: MLKitTextRecognizer
    private lateinit var viewModel: TextRecognitionViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockTextRecognizer = mockk(relaxed = true)

        // Setup default behavior for scanResults flow
        every { mockTextRecognizer.scanResults } returns flowOf()

        viewModel = TextRecognitionViewModel(mockTextRecognizer)
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
        assertThat(viewModel.uiState.value).isEqualTo(TextRecognitionUiState.Scanning)
    }

    @Test
    fun `startScanning should start recognizer and set state to Scanning`() = runTest {
        // Given
        every { mockTextRecognizer.startScanning() } just Runs

        // When
        viewModel.startScanning()

        // Then
        verify { mockTextRecognizer.startScanning() }
        assertThat(viewModel.uiState.value).isEqualTo(TextRecognitionUiState.Scanning)
    }

    @Test
    fun `stopScanning should stop recognizer`() = runTest {
        // Given
        every { mockTextRecognizer.stopScanning() } just Runs

        // When
        viewModel.stopScanning()

        // Then
        verify { mockTextRecognizer.stopScanning() }
    }

    @Test
    fun `resumeScanning should call startScanning`() = runTest {
        // Given
        every { mockTextRecognizer.startScanning() } just Runs

        // When
        viewModel.resumeScanning()

        // Then
        verify { mockTextRecognizer.startScanning() }
        assertThat(viewModel.uiState.value).isEqualTo(TextRecognitionUiState.Scanning)
    }

    @Test
    fun `getImageAnalyzer should return analyzer from recognizer`() = runTest {
        // Given
        val mockAnalyzer = mockk<ImageAnalysis.Analyzer>()
        coEvery { mockTextRecognizer.getImageAnalyzer() } returns mockAnalyzer

        // When
        val result = viewModel.getImageAnalyzer()

        // Then
        assertThat(result).isEqualTo(mockAnalyzer)
        coVerify { mockTextRecognizer.getImageAnalyzer() }
    }

    @Test
    fun `text result with meaningful text should stop scanning and update state to Success`() =
        runTest {
            // Given
            val meaningfulText = "This is meaningful text with more than 3 characters"
            val textResult = ScanResult.TextResult(meaningfulText, 0.95f)
            val resultsChannel = Channel<ScanResult>(Channel.UNLIMITED)

            every { mockTextRecognizer.scanResults } returns resultsChannel.receiveAsFlow()
            every { mockTextRecognizer.stopScanning() } just Runs

            // Create new ViewModel to capture flow subscription
            viewModel = TextRecognitionViewModel(mockTextRecognizer)

            // When
            resultsChannel.trySend(textResult)
            testDispatcher.scheduler.advanceUntilIdle()

            // Then
            verify { mockTextRecognizer.stopScanning() }
            assertThat(viewModel.uiState.value).isInstanceOf(TextRecognitionUiState.Success::class.java)

            val successState = viewModel.uiState.value as TextRecognitionUiState.Success
            assertThat(successState.result).isEqualTo(textResult)
        }

    @Test
    fun `text result with short text should continue scanning`() = runTest {
        // Given
        val shortText = "Hi" // Less than 3 characters after trim
        val textResult = ScanResult.TextResult(shortText)
        val resultsChannel = Channel<ScanResult>(Channel.UNLIMITED)

        every { mockTextRecognizer.scanResults } returns resultsChannel.receiveAsFlow()

        // Create new ViewModel to capture flow subscription
        viewModel = TextRecognitionViewModel(mockTextRecognizer)

        // When
        resultsChannel.trySend(textResult)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify(exactly = 0) { mockTextRecognizer.stopScanning() }
        assertThat(viewModel.uiState.value).isEqualTo(TextRecognitionUiState.Scanning)
    }

    @Test
    fun `text result with whitespace-only text should continue scanning`() = runTest {
        // Given
        val whitespaceText = "   \n\t   " // Only whitespace
        val textResult = ScanResult.TextResult(whitespaceText)
        val resultsChannel = Channel<ScanResult>(Channel.UNLIMITED)

        every { mockTextRecognizer.scanResults } returns resultsChannel.receiveAsFlow()

        // Create new ViewModel to capture flow subscription
        viewModel = TextRecognitionViewModel(mockTextRecognizer)

        // When
        resultsChannel.trySend(textResult)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify(exactly = 0) { mockTextRecognizer.stopScanning() }
        assertThat(viewModel.uiState.value).isEqualTo(TextRecognitionUiState.Scanning)
    }

    @Test
    fun `text result with exactly 4 characters should trigger success`() = runTest {
        // Given
        val fourCharText = "Test" // Exactly 4 characters (> 3)
        val textResult = ScanResult.TextResult(fourCharText)
        val resultsChannel = Channel<ScanResult>(Channel.UNLIMITED)

        every { mockTextRecognizer.scanResults } returns resultsChannel.receiveAsFlow()
        every { mockTextRecognizer.stopScanning() } just Runs

        // Create new ViewModel to capture flow subscription
        viewModel = TextRecognitionViewModel(mockTextRecognizer)

        // When
        resultsChannel.trySend(textResult)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify { mockTextRecognizer.stopScanning() }
        assertThat(viewModel.uiState.value).isInstanceOf(TextRecognitionUiState.Success::class.java)
    }

    @Test
    fun `error result should update state to Error`() = runTest {
        // Given
        val errorMessage = "Text recognition failed"
        val errorResult = ScanResult.Error(errorMessage)
        val resultsChannel = Channel<ScanResult>(Channel.UNLIMITED)

        every { mockTextRecognizer.scanResults } returns resultsChannel.receiveAsFlow()

        // Create new ViewModel to capture flow subscription
        viewModel = TextRecognitionViewModel(mockTextRecognizer)

        // When
        resultsChannel.trySend(errorResult)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertThat(viewModel.uiState.value).isInstanceOf(TextRecognitionUiState.Error::class.java)

        val errorState = viewModel.uiState.value as TextRecognitionUiState.Error
        assertThat(errorState.message).isEqualTo(errorMessage)
    }

    @Test
    fun `barcode result should not change state - continue scanning`() = runTest {
        // Given
        val barcodeResult = ScanResult.BarcodeResult("123456", BarcodeFormat.QR_CODE)
        val resultsChannel = Channel<ScanResult>(Channel.UNLIMITED)

        every { mockTextRecognizer.scanResults } returns resultsChannel.receiveAsFlow()

        // Create new ViewModel to capture flow subscription
        viewModel = TextRecognitionViewModel(mockTextRecognizer)

        // When
        resultsChannel.trySend(barcodeResult)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertThat(viewModel.uiState.value).isEqualTo(TextRecognitionUiState.Scanning)
        verify(exactly = 0) { mockTextRecognizer.stopScanning() }
    }

    @Test
    fun `no result should not change state - continue scanning`() = runTest {
        // Given
        val noResult = ScanResult.NoResult
        val resultsChannel = Channel<ScanResult>(Channel.UNLIMITED)

        every { mockTextRecognizer.scanResults } returns resultsChannel.receiveAsFlow()

        // Create new ViewModel to capture flow subscription
        viewModel = TextRecognitionViewModel(mockTextRecognizer)

        // When
        resultsChannel.trySend(noResult)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertThat(viewModel.uiState.value).isEqualTo(TextRecognitionUiState.Scanning)
        verify(exactly = 0) { mockTextRecognizer.stopScanning() }
    }

    @Test
    fun `multiple text results should handle first meaningful one`() = runTest {
        // Given
        val resultsChannel = Channel<ScanResult>(Channel.UNLIMITED)
        val meaningfulText = "First meaningful text"
        val textResult = ScanResult.TextResult(meaningfulText)

        every { mockTextRecognizer.scanResults } returns resultsChannel.receiveAsFlow()
        every { mockTextRecognizer.stopScanning() } just Runs

        // Create new ViewModel to capture flow subscription
        viewModel = TextRecognitionViewModel(mockTextRecognizer)

        // When - Send multiple results
        resultsChannel.trySend(ScanResult.NoResult)
        resultsChannel.trySend(ScanResult.TextResult("Hi")) // Too short
        resultsChannel.trySend(textResult) // This should trigger success
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify { mockTextRecognizer.stopScanning() }
        assertThat(viewModel.uiState.value).isInstanceOf(TextRecognitionUiState.Success::class.java)

        val successState = viewModel.uiState.value as TextRecognitionUiState.Success
        assertThat(successState.result).isEqualTo(textResult)
    }
}