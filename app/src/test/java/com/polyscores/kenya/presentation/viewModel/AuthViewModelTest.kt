package com.polyscores.kenya.presentation.viewModel

import android.app.Application
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private lateinit var viewModel: AuthViewModel
    private val application = mockk<Application>(relaxed = true)
    private val auth = mockk<FirebaseAuth>(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Mock current user to be null initially
        every { auth.currentUser } returns null
        
        viewModel = AuthViewModel(application, auth)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `login with empty email sets error state`() = runTest {
        viewModel.login(email = "   ", pin = "1234", onSuccess = {})
        
        assertEquals("Email and PIN cannot be empty.", viewModel.authError.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `login with empty pin sets error state`() = runTest {
        viewModel.login(email = "test@admin.com", pin = "   ", onSuccess = {})
        
        assertEquals("Email and PIN cannot be empty.", viewModel.authError.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `login with valid credentials calls signInWithEmailAndPassword`() = runTest {
        // Arrange
        val mockTask = mockk<Task<AuthResult>>(relaxed = true)
        every { auth.signInWithEmailAndPassword(any(), any()) } returns mockTask
        
        // Act
        viewModel.login(email = " test@admin.com ", pin = "1234", onSuccess = {})
        
        // Assert
        verify { auth.signInWithEmailAndPassword("test@admin.com", "1234") }
        assertTrue(viewModel.isLoading.value)
    }

    @Test
    fun `clearError resets authError to null`() = runTest {
        // Setup error state
        viewModel.login(email = "", pin = "", onSuccess = {})
        assertNotNull(viewModel.authError.value)
        
        // Clear error
        viewModel.clearError()
        assertNull(viewModel.authError.value)
    }

    @Test
    fun `logout calls auth signOut`() {
        viewModel.logout()
        verify { auth.signOut() }
    }
}
