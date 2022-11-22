package com.marine.fishtank.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.marine.fishtank.BuildConfig
import com.marine.fishtank.SettingsRepository
import com.marine.fishtank.api.TankApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class SignInResult(
    val result: Boolean,
    val reason: String
)

class LogInViewModel(application: Application) : AndroidViewModel(application) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val settingsRepository = SettingsRepository.getInstance(context = application)

    val signInResult = MutableLiveData<SignInResult>()
    val userIdData = MutableLiveData<String>()
    val userPasswordData = MutableLiveData<String>()

    private val tankApi = TankApi.getInstance(BuildConfig.SERVER_URL)

    private suspend fun saveUser(id: String, password: String) {
        settingsRepository.saveUserId(id)
        settingsRepository.saveUserPassword(password)
    }

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
            val result = tankApi.signIn(userId, password)
            if(result) {
                saveUser(userId, password)
            }
            signInResult.postValue(
                SignInResult(result, "Success")
            )
        }
    }
}