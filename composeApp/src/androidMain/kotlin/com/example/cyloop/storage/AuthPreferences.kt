package com.example.cyloop.storage

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "auth_prefs")

object AuthPreferences {
    private val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")

    fun isBiometricEnabled(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[BIOMETRIC_ENABLED] ?: false
        }
    }

    suspend fun setBiometricEnabled(context: Context, enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[BIOMETRIC_ENABLED] = enabled
        }
    }

    suspend fun getPassword(context: android.content.Context): String? {
        // Implement password retrieval logic here
        // For SharedPreferences:
        val prefs = context.getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE)
        return prefs.getString("user_password", null)

        // Or if using EncryptedSharedPreferences, retrieve accordingly
    }
}
