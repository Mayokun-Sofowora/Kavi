package com.mayor.kavi.ui.viewmodel

import android.graphics.Bitmap
import com.mayor.kavi.data.models.detection.Detection
import com.mayor.kavi.data.repository.RoboflowRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import androidx.arch.core.executor.testing.InstantTaskExecutorRule

@OptIn(ExperimentalCoroutinesApi::class)
class DetectionViewModelTest {

    @get:Rule
    val rule: TestRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: DetectionViewModel
    
    @MockK
    private lateinit var repository: RoboflowRepository
    
    @MockK
    private lateinit var mockBitmap: Bitmap

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = DetectionViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `detectDice should update state to Processing then Success when detections found`() = runTest {
        // Given
        val detections = listOf(mockk<Detection>())
        coEvery { repository.detectDice(any()) } returns detections

        // When
        viewModel.detectDice(mockBitmap)
        
        // Then - Initial state should be Processing
        assert(viewModel.detectionState.value is DetectionState.Processing)
        
        // Advance time to allow coroutine to complete
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then - Final state should be Success with detections
        assert(viewModel.detectionState.value is DetectionState.Success)
        assert((viewModel.detectionState.value as DetectionState.Success).detections == detections)
    }

    @Test
    fun `detectDice should update state to Processing then NoDetections when no detections found`() = runTest {
        // Given
        coEvery { repository.detectDice(any()) } returns emptyList()

        // When
        viewModel.detectDice(mockBitmap)
        
        // Then - Initial state should be Processing
        assert(viewModel.detectionState.value is DetectionState.Processing)
        
        // Advance time to allow coroutine to complete
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then - Final state should be NoDetections
        assert(viewModel.detectionState.value is DetectionState.NoDetections)
    }

    @Test
    fun `detectDice should update state to Processing then Error when exception occurs`() = runTest {
        // Given
        val errorMessage = "Test error"
        coEvery { repository.detectDice(any()) } throws Exception(errorMessage)

        // When
        viewModel.detectDice(mockBitmap)
        
        // Then - Initial state should be Processing
        assert(viewModel.detectionState.value is DetectionState.Processing)
        
        // Advance time to allow coroutine to complete
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then - Final state should be Error
        assert(viewModel.detectionState.value is DetectionState.Error)
        assert((viewModel.detectionState.value as DetectionState.Error).message == errorMessage)
    }

    @Test
    fun `clearDetections should set state to Idle`() = runTest {
        // Given - Set some initial state
        coEvery { repository.detectDice(any()) } returns listOf(mockk())
        viewModel.detectDice(mockBitmap)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.clearDetections()

        // Then
        assert(viewModel.detectionState.value is DetectionState.Idle)
    }
} 