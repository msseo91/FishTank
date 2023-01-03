package com.marine.fishtank

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DEFAULT_SERVER_URL = "fish.marineseo.xyz"
private const val DEFAULT_SERVER_PORT = 53265

private const val DEFAULT_RTSP_URL = "rtsp://msseo:1116@$DEFAULT_SERVER_URL:8888/fishtank"

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
val DEFAULT_CONNECTION_SETTING = ConnectionSetting(
    serverUrl = DEFAULT_SERVER_URL,
    serverPort = DEFAULT_SERVER_PORT,
    rtspUrl = DEFAULT_RTSP_URL
)

data class ConnectionSetting(
    var serverUrl: String,
    var serverPort: Int,
    var rtspUrl: String
) {
    override fun equals(other: Any?): Boolean {
        if(other !is ConnectionSetting) return false
        return other.serverUrl == this.serverUrl
                && other.serverPort == this.serverPort
                && other.rtspUrl == this.rtspUrl
    }

    override fun hashCode(): Int {
        return serverUrl.hashCode() + serverPort + rtspUrl.hashCode()
    }
}


class SettingsRepository(private val context: Context) {
    private val keyUserId = stringPreferencesKey("user_id")
    private val keyUserPassword = stringPreferencesKey("user_password")

    val userIdFlow = context.dataStore.data.map { pref -> pref[keyUserId] ?: "" }
    val userPasswordFlow = context.dataStore.data.map { pref -> pref[keyUserPassword] ?: "" }

    suspend fun saveUserId(id: String) {
        context.dataStore.edit {
            it[keyUserId] = id
        }
    }

    suspend fun saveUserPassword(password: String) {
        context.dataStore.edit {
            it[keyUserPassword] = password
        }
    }
}