package com.marine.fishtank.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.marine.fishtank.SettingsRepository
import com.marine.fishtank.api.TankDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
    private val scope = CoroutineScope(Dispatchers.IO)

    val signInResult = MutableLiveData<SignInResult>()
    val userIdData = MutableLiveData<String>()
    val userPasswordData = MutableLiveData<String>()

    private suspend fun saveUser(id: String, password: String) {
        settingsRepository.saveUserId(id)
        settingsRepository.saveUserPassword(password)
    }

    fun isAlreadySignIn() = tankDataSource.isAlreadySignIn()

    fun fetchSavedUser() {
        scope.launch {
            settingsRepository.userIdFlow.collect { id ->
                userIdData.postValue(id)
            }
        }

        scope.launch {
            settingsRepository.userPasswordFlow.collect { password ->
                userPasswordData.postValue(password)
            }
        }
    }

    fun signIn(userId: String, password: String) {
        scope.launch {
            val result = tankDataSource.signIn(userId, password)
            if(result) {
                saveUser(userId, password)
            }
            signInResult.postValue(
                SignInResult(result, "Success")
            )
        }
    }
}