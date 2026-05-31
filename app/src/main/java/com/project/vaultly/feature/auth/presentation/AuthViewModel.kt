package com.project.vaultly.feature.auth.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.vaultly.core.network.ApiResult
import com.project.vaultly.feature.auth.data.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf("")
        private set

    fun clearError() {
        errorMessage = ""
    }

    fun login(
        email: String,
        password: String,
        onSuccess: (String) -> Unit
    ) {
        isLoading = true
        errorMessage = ""
        viewModelScope.launch {
            when (val result = AuthRepository.login(email, password)) {
                is ApiResult.Success -> onSuccess(email)
                is ApiResult.Failure -> errorMessage = result.message
            }
            isLoading = false
        }
    }

    fun register(
        fullName: String,
        email: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        isLoading = true
        errorMessage = ""
        viewModelScope.launch {
            when (val result = AuthRepository.register(fullName, email, password)) {
                is ApiResult.Success -> onSuccess()
                is ApiResult.Failure -> errorMessage = result.message
            }
            isLoading = false
        }
    }
}
