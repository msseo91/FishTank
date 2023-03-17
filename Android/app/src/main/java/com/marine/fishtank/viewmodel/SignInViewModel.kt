package com.marine.fishtank.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marine.fishtank.SettingsRepository
import com.marine.fishtank.api.TankDataSource
import com.marine.fishtank.api.TankResult
import com.orhanobut.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val tankDataSource: TankDataSource,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _signInResultFlow = MutableStateFlow<SignInResult>(SignInResult.Notyet())
    val signInResultFlow = _signInResultFlow.asSharedFlow()

    val userId = settingsRepository.userIdFlow
    val password = settingsRepository.userPasswordFlow

    private suspend fun saveUser(id: String, password: String) {
        settingsRepository.saveUserId(id)
        settingsRepository.saveUserPassword(password)
    }

    fun signIn(userId: String, password: String) {
        viewModelScope.launch {
            tankDataSource.signIn(userId, password)
                .collect {
                    when (val signInResult = it.toSignInResult()) {
                        is SignInResult.Success -> {
                            if (signInResult.result) saveUser(userId, password)
                            _signInResultFlow.emit(signInResult)
                        }
                        is SignInResult.Error -> {
                            Logger.e("Error: ${signInResult.message}\n${signInResult.exception}")
                            _signInResultFlow.emit(signInResult)
                        }
                        is SignInResult.Loading -> {
                            Logger.d("Loading.. Sign-In")
                            _signInResultFlow.emit(signInResult)
                        }
                        else -> _signInResultFlow.emit(signInResult)
                    }
                }
        }
    }
}

sealed class SignInResult {
    class Success(val result: Boolean = false): SignInResult()
    class Error(val exception: Throwable? = null, val message: String = ""): SignInResult()
    class Loading: SignInResult()
    class Notyet: SignInResult()
}

fun TankResult<String>.toSignInResult(): SignInResult {
    return when (this) {
        is TankResult.Success -> SignInResult.Success(this.data.isNotEmpty())
        is TankResult.Error -> SignInResult.Error(this.exception, this.message)
        is TankResult.Loading -> SignInResult.Loading()
    }
}