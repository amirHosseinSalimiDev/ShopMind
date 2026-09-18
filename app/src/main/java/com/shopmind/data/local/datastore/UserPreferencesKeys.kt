package com.shopmind.data.local.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object UserPreferencesKeys {
    val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    val USER_ID = stringPreferencesKey("user_id")
}
