package com.polyscores.kenya.presentation.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    application: Application,
    private val auth: FirebaseAuth
) : AndroidViewModel(application) {

    constructor(application: Application) : this(application, FirebaseAuth.getInstance())

    private val _isAdminSession = MutableStateFlow(auth.currentUser != null)
    val isAdminSession: StateFlow<Boolean> = _isAdminSession.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _isAdminSession.value = firebaseAuth.currentUser != null
        }
    }

    fun login(email: String, pin: String, onSuccess: () -> Unit) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || pin.isBlank()) {
            _authError.value = "Email and PIN cannot be empty."
            return
        }

        _isLoading.value = true
        _authError.value = null

        // In this implementation, we use Firebase Auth Email/Password
        // The "pin" is treated as the password for the admin account.
        auth.signInWithEmailAndPassword(trimmedEmail, pin)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    _authError.value = task.exception?.message ?: "Login failed"
                }
            }
    }

    fun logout() {
        auth.signOut()
    }

    fun clearError() {
        _authError.value = null
    }
}
