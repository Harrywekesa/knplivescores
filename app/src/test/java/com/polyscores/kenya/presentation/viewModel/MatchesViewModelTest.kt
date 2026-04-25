package com.polyscores.kenya.presentation.viewModel

import android.app.Application
import com.polyscores.kenya.data.model.Match
import com.polyscores.kenya.data.model.MatchStatus
import com.polyscores.kenya.data.preferences.PreferencesManager
import com.polyscores.kenya.data.preferences.UserPreferences
import com.polyscores.kenya.data.repository.MatchesRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MatchesViewModelTest {

    private lateinit var viewModel: MatchesViewModel
    private val application = mockk<Application>(relaxed = true)
    private val matchesRepository = mockk<MatchesRepository>(relaxed = true)
    private val preferencesManager = mockk<PreferencesManager>(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()
    private val matchesFlow = MutableStateFlow<List<Match>>(emptyList())
    private val userPrefsFlow = MutableStateFlow(UserPreferences())

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        every { matchesRepository.getAllMatches() } returns matchesFlow
        every { preferencesManager.userPreferencesFlow } returns userPrefsFlow

        viewModel = MatchesViewModel(application, matchesRepository, preferencesManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `updateMatchStatus from SCHEDULED to LIVE sets start time`() = runTest(testDispatcher) {
        // Arrange
        val mockMatch = Match(id = "1", matchStatus = MatchStatus.SCHEDULED)
        matchesFlow.value = listOf(mockMatch)
        advanceUntilIdle()

        // Act
        viewModel.updateMatchStatus("1", MatchStatus.LIVE)
        advanceUntilIdle()

        // Assert
        coVerify { matchesRepository.updateMatchStatus("1", MatchStatus.LIVE, setStartTime = true, setSecondHalfStartTime = false) }
    }

    @Test
    fun `updateMatchStatus from HALFTIME to SECOND_HALF sets second half start time`() = runTest(testDispatcher) {
        // Arrange
        val mockMatch = Match(id = "2", matchStatus = MatchStatus.HALFTIME)
        matchesFlow.value = listOf(mockMatch)
        advanceUntilIdle()

        // Act
        viewModel.updateMatchStatus("2", MatchStatus.SECOND_HALF)
        advanceUntilIdle()

        // Assert
        coVerify { matchesRepository.updateMatchStatus("2", MatchStatus.SECOND_HALF, setStartTime = false, setSecondHalfStartTime = true) }
    }

    @Test
    fun `deleteMatch calls repository deleteMatch`() = runTest(testDispatcher) {
        coEvery { matchesRepository.deleteMatch("1") } returns true

        var successCalled = false
        viewModel.deleteMatch("1", onSuccess = { successCalled = true })
        advanceUntilIdle()

        coVerify { matchesRepository.deleteMatch("1") }
        assert(successCalled)
    }
}
