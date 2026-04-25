package com.polyscores.kenya.presentation.viewModel

import android.app.Application
import com.polyscores.kenya.data.preferences.PreferencesManager
import com.polyscores.kenya.data.preferences.UserPreferences
import com.polyscores.kenya.data.repository.PresenceRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PresenceViewModelTest {

    private lateinit var viewModel: PresenceViewModel
    private val application = mockk<Application>(relaxed = true)
    private val presenceRepository = mockk<PresenceRepository>(relaxed = true)
    private val preferencesManager = mockk<PreferencesManager>(relaxed = true)
    
    private val testDispatcher = StandardTestDispatcher()
    private val activeDeviceFlow = MutableStateFlow(0)
    private val userPrefsFlow = MutableStateFlow(UserPreferences(deviceId = ""))

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        every { presenceRepository.activeDeviceCount } returns activeDeviceFlow
        every { preferencesManager.userPreferencesFlow } returns userPrefsFlow
        coEvery { preferencesManager.saveDeviceIdIfNeeded(any()) } just runs
        every { presenceRepository.startPresenceTracking(any()) } just runs
        
        viewModel = PresenceViewModel(application, presenceRepository, preferencesManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `activeDeviceCount maps from repository flow`() = runTest(testDispatcher) {
        assertEquals(0, viewModel.activeDeviceCount.value)
        
        activeDeviceFlow.value = 42
        
        assertEquals(42, viewModel.activeDeviceCount.value)
    }

    @Test
    fun `viewModel init starts tracking if deviceId is not blank`() = runTest(testDispatcher) {
        // Arrange
        val validPrefs = UserPreferences(deviceId = "test_device_123")
        
        // Act - push valid preferences
        userPrefsFlow.value = validPrefs
        advanceUntilIdle() // let coroutines execute
        
        // Assert
        coVerify { preferencesManager.saveDeviceIdIfNeeded(validPrefs) }
        verify { presenceRepository.startPresenceTracking("test_device_123") }
    }

    @Test
    fun `viewModel init does not start tracking if deviceId is blank`() = runTest(testDispatcher) {
        // Arrange
        val blankPrefs = UserPreferences(deviceId = "   ")
        
        // Act
        userPrefsFlow.value = blankPrefs
        advanceUntilIdle()
        
        // Assert
        coVerify(exactly = 0) { preferencesManager.saveDeviceIdIfNeeded(any()) }
        verify(exactly = 0) { presenceRepository.startPresenceTracking(any()) }
    }
}
