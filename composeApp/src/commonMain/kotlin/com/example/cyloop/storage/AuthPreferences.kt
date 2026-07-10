package com.example.cyloop.storage

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

object AuthPreferences {
    private val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
    private val BALANCE_VISIBLE = booleanPreferencesKey("balance_visible")

    // We will need a way to provide the DataStore instance
    // For now, let's assume we can get it from a provider
    private lateinit var dataStore: DataStore<Preferences>

    fun init(dataStore: DataStore<Preferences>) {
        this.dataStore = dataStore
    }

    fun isBiometricEnabled(): Flow<Boolean> {
        return if (::dataStore.isInitialized) {
            dataStore.data.map { preferences ->
                preferences[BIOMETRIC_ENABLED] ?: false
            }
        } else {
            emptyFlow()
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        if (!::dataStore.isInitialized) return
        dataStore.edit { preferences ->
            preferences[BIOMETRIC_ENABLED] = enabled
        }
    }

    fun isBalanceVisible(): Flow<Boolean> {
        return if (::dataStore.isInitialized) {
            dataStore.data.map { preferences ->
                preferences[BALANCE_VISIBLE] ?: true
            }
        } else {
            emptyFlow()
        }
    }

    suspend fun setBalanceVisible(visible: Boolean) {
        if (!::dataStore.isInitialized) return
        dataStore.edit { preferences ->
            preferences[BALANCE_VISIBLE] = visible
        }
    }
    
    // Legacy support or platform specific
    // This will be moved to an expect/actual or handled differently
}

expect suspend fun getLegacyPassword(context: Any?): String?
