package com.kongko.app.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "kongko_session")

@Singleton
class SessionDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        private val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_PHONE_NUMBER = stringPreferencesKey("phone_number")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_AVATAR_URL = stringPreferencesKey("avatar_url")
        private val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    }

    val isLoggedInFlow: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_IS_LOGGED_IN] == true && !prefs[KEY_ACCESS_TOKEN].isNullOrBlank()
    }

    val accessTokenFlow: Flow<String?> = dataStore.data.map { it[KEY_ACCESS_TOKEN] }
    val userIdFlow: Flow<String?> = dataStore.data.map { it[KEY_USER_ID] }
    val userNameFlow: Flow<String?> = dataStore.data.map { it[KEY_USER_NAME] }

    suspend fun getAccessToken(): String? = dataStore.data.first()[KEY_ACCESS_TOKEN]
    suspend fun getRefreshToken(): String? = dataStore.data.first()[KEY_REFRESH_TOKEN]
    suspend fun getUserId(): String? = dataStore.data.first()[KEY_USER_ID]

    suspend fun saveAuthTokens(accessToken: String, refreshToken: String) {
        dataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = accessToken
            prefs[KEY_REFRESH_TOKEN] = refreshToken
            prefs[KEY_IS_LOGGED_IN] = true
        }
    }

    suspend fun saveUserProfile(userId: String, phone: String, name: String, avatarUrl: String?) {
        dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = userId
            prefs[KEY_PHONE_NUMBER] = phone
            prefs[KEY_USER_NAME] = name
            if (avatarUrl != null) prefs[KEY_AVATAR_URL] = avatarUrl
        }
    }

    suspend fun clearSession() {
        dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
