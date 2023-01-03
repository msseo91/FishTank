package com.marine.fishtank.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marine.fishtank.SettingsRepository
import com.marine.fishtank.api.TankDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignInResult(
    val result: Boolean,
    val reason: String
)

@HiltViewModel
class LogInViewModel @Inject constructor(
    private val tankDataSource: TankDataSource,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val signInResult = MutableLiveData<SignInResult>()
    val userIdData = MutableLiveData<String>()
    val userPasswordData = MutableLiveData<String>()

    private suspend fun saveUser(id: String, password: String) {
        settingsRepository.saveUserId(id)
        settingsRepository.saveUserPassword(password)
    }

    fun isAlreadySignIn() = tankDataSource.isAlreadySignIn()

    fun fetchSavedUser() {
        viewModelScope.launch {
            settingsRepository.userIdFlow.collect { id ->
                userIdData.postValue(id)
            }
        }

        viewModelScope.launch {
            settingsRepository.userPasswordFlow.collect { password ->
                userPasswordData.postValue(password)
            }
        }
    }

    fun signIn(userId: String, password: String) {
        viewModelScope.launch {
            tankDataSource.signIn(userId, password).collect { result ->
                if(result) {
                    saveUser(userId, password)
                }

                signInResult.postValue(
                    SignInResult(result, "Success")
                )
            }
        }
    }
}