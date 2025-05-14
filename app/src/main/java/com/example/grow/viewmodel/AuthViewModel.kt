package com.example.grow.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grow.data.Resource
import com.example.grow.data.model.AuthResponse
import com.example.grow.data.model.ForgotPasswordRequest
import com.example.grow.data.model.LoginRequest
import com.example.grow.data.repository.AuthRepository
import com.example.grow.util.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<Resource<AuthResponse>>(Resource.Loading())
    val loginState: StateFlow<Resource<AuthResponse>> = _loginState

    private val _userIdState = MutableStateFlow<Int?>(null)
    val userIdState: StateFlow<Int?> = _userIdState

    private val _forgotPasswordState = MutableStateFlow<Resource<String>>(Resource.Loading())
    val forgotPasswordState: StateFlow<Resource<String>> = _forgotPasswordState

    fun login(email: String, password: String, context: Context) {
        viewModelScope.launch {
            try {
                _loginState.value = Resource.Loading()
                val response = repository.login(LoginRequest(email, password))
                _loginState.value = Resource.Success(response)
                _userIdState.value = response.user.id
                SessionManager.saveLoginSession(context, response.user.id)
            } catch (e: Exception) {
                _loginState.value = Resource.Error(e.localizedMessage ?: "Error login")
            }
        }
    }

    fun logout(context: Context) {
        viewModelScope.launch {
            SessionManager.clearSession(context)
            _loginState.value = Resource.Loading() // Reset login state
            _userIdState.value = null
            Log.d("AuthViewModel", "Logged out, loginState reset")
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            try {
                _forgotPasswordState.value = Resource.Loading()
                val response = repository.forgotPassword(ForgotPasswordRequest(email))
                _forgotPasswordState.value = Resource.Success(response.message)
                Log.d("AuthViewModel", "Forgot password success: ${response.message}")
            } catch (e: Exception) {
                _forgotPasswordState.value = Resource.Error(e.localizedMessage ?: "Gagal mengirim permintaan reset password")
                Log.e("AuthViewModel", "Forgot password error: ${e.message}")
            }
        }
    }

    fun isLoggedIn(context: Context): Boolean {
        return SessionManager.isLoggedIn(context)
    }

    fun getUserId(context: Context): Int {
        return SessionManager.getUserId(context)
    }
}