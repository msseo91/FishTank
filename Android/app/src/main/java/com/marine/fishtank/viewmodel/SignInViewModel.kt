package com.marine.fishtank.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marine.fishtank.SettingsRepository
import com.marine.fishtank.api.TankDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignInResult(
    val result: Boolean,
    val reason: String
)

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val tankDataSource: TankDataSource,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _signInResultFlow = MutableStateFlow(SignInResult(false, "Not yet"))
    val signInResultFlow: StateFlow<SignInResult> = _signInResultFlow

    val userId = settingsRepository.userIdFlow
    val password = settingsRepository.userPasswordFlow

    private suspend fun saveUser(id: String, password: String) {
        settingsRepository.saveUserId(id)
        settingsRepository.saveUserPassword(password)
    }

    fun isAlreadySignIn() = tankDataSource.isAlreadySignIn()

    fun signIn(userId: String, password: String) {
        viewModelScope.launch {
            tankDataSource.signIn(userId, password).collect { result ->
                if(result) {
                    saveUser(userId, password)
                }

                _signInResultFlow.value = SignInResult(result, "Success")
            }
        }
    }
}