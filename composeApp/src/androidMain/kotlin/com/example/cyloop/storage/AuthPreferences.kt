package com.example.cyloop.storage

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "auth_settings")

object AuthPreferences {

    private val BIOMETRIC_KEY =
        booleanPreferencesKey("biometric_enabled")

    suspend fun setBiometricEnabled(
        context: Context,
        enabled: Boolean
    ) {
        context.dataStore.edit { prefs ->
            prefs[BIOMETRIC_KEY] = enabled
        }
    }

    fun isBiometricEnabled(
        context: Context
    ): Flow<Boolean> {

        return context.dataStore.data.map { prefs ->
            prefs[BIOMETRIC_KEY] ?: false
        }
    }
}