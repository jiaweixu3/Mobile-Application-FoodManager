package com.example.foodmanager.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val supabase: SupabaseClient) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    fun login(emailInput: String, passwordInput: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _authError.value = null

                supabase.auth.signInWith(Email) {
                    email = emailInput
                    password = passwordInput
                }

                onSuccess()
            } catch (e: Exception) {
                _authError.value = "Login failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(emailInput: String, passwordInput: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _authError.value = null

                val authResult = supabase.auth.signUpWith(Email) {
                    email = emailInput
                    password = passwordInput
                }

                // Catch duplicate email via empty identities list
                if (authResult?.identities?.isEmpty() == true) {
                    _authError.value = "An account with this email already exists."
                    return@launch
                }

                onSuccess()
            } catch (e: Exception) {
                val msg = e.message?.lowercase() ?: ""
                if (msg.contains("already registered")) {
                    _authError.value = "An account with this email already exists."
                } else {
                    _authError.value = "Registration failed: ${e.message}"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _authError.value = null
    }
}