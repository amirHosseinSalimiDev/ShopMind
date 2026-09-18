package com.shopmind.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

class UserDataStore(private val context: Context) {

    val isLoggedIn: Flow<Boolean> = context.userDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[UserPreferencesKeys.IS_LOGGED_IN] ?: false
        }

    val userId: Flow<Long?> = context.userDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[UserPreferencesKeys.USER_ID]
        }

    suspend fun saveSession(userId: Long) {
        context.userDataStore.edit { preferences ->
            preferences[UserPreferencesKeys.IS_LOGGED_IN] = true
            preferences[UserPreferencesKeys.USER_ID] = userId
        }
    }

    suspend fun clearSession() {
        context.userDataStore.edit { preferences ->
            preferences[UserPreferencesKeys.IS_LOGGED_IN] = false
            preferences[UserPreferencesKeys.USER_ID] = -1L
        }
    }
}
