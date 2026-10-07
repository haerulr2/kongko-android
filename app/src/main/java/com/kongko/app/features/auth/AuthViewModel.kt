package com.kongko.app.features.auth

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.kongko.app.core.datastore.SessionDataStore
import com.kongko.app.core.network.KongkoApiService
import com.kongko.app.core.network.dto.VerifyTokenRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    data class CodeSent(val verificationId: String, val phoneNumber: String) : AuthUiState
    object Success : AuthUiState
    data class Error(val message: String) : AuthUiState
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val sessionDataStore: SessionDataStore,
    private val apiService: KongkoApiService
) : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    fun sendOtp(phoneNumber: String, activity: Activity) {
        _uiState.value = AuthUiState.Loading

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Verifikasi gagal")
                }

                override fun onCodeSent(
                    vId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    verificationId = vId
                    resendToken = token
                    _uiState.value = AuthUiState.CodeSent(vId, phoneNumber)
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyOtp(code: String) {
        val vId = verificationId ?: return
        _uiState.value = AuthUiState.Loading

        val credential = PhoneAuthProvider.getCredential(vId, code)
        signInWithCredential(credential)
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    firebaseUser?.getIdToken(true)?.addOnCompleteListener { tokenTask ->
                        if (tokenTask.isSuccessful) {
                            val idToken = tokenTask.result?.token
                            if (idToken != null) {
                                exchangeTokenWithBackend(idToken)
                            } else {
                                _uiState.value = AuthUiState.Error("Token Firebase kosong")
                            }
                        } else {
                            _uiState.value = AuthUiState.Error(tokenTask.exception?.message ?: "Gagal mengambil token")
                        }
                    }
                } else {
                    _uiState.value = AuthUiState.Error(task.exception?.message ?: "Kode verifikasi salah")
                }
            }
    }

    private fun exchangeTokenWithBackend(idToken: String) {
        viewModelScope.launch {
            try {
                val response = apiService.verifyToken(VerifyTokenRequest(idToken))
                if (response.isSuccessful && response.body()?.data != null) {
                    val authData = response.body()!!.data!!
                    sessionDataStore.saveAuthTokens(authData.accessToken, authData.refreshToken)
                    sessionDataStore.saveUserProfile(
                        userId = authData.user.id,
                        phone = authData.user.phoneNumber,
                        name = authData.user.username ?: "Pengguna",
                        avatarUrl = authData.user.avatarUrl
                    )
                    _uiState.value = AuthUiState.Success
                } else {
                    _uiState.value = AuthUiState.Error(response.body()?.error?.message ?: "Gagal autentikasi ke server")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Koneksi gagal: ${e.message}")
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
