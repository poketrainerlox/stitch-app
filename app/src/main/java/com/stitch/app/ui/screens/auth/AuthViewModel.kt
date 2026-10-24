package com.stitch.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stitch.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun signIn(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter both email and password")
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val res = authRepository.signInWithEmail(email.trim(), pass)
            if (res.isSuccess) {
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = res.exceptionOrNull()?.localizedMessage ?: "Sign in failed"
                )
            }
        }
    }

    fun signUp(name: String, email: String, pass: String) {
        if (name.isBlank() || email.isBlank() || pass.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please fill in all fields")
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val res = authRepository.signUpWithEmail(name.trim(), email.trim(), pass)
            if (res.isSuccess) {
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = res.exceptionOrNull()?.localizedMessage ?: "Sign up failed"
                )
            }
        }
    }

    fun continueAsGuest() {
        authRepository.continueAsGuest()
        _uiState.value = _uiState.value.copy(isSuccess = true)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
