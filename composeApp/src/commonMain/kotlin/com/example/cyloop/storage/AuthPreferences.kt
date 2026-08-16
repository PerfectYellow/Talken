package com.example.cyloop.storage

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

object AuthPreferences {
    private val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
    private val BALANCE_VISIBLE = booleanPreferencesKey("balance_visible")
    private val WALLET_ONBOARDED = booleanPreferencesKey("wallet_onboarded")
    private val LAST_KNOWN_BALANCE = stringPreferencesKey("last_known_balance")
    private val SOLANA_NETWORK = stringPreferencesKey("solana_network")

    // We will need a way to provide the DataStore instance
    // For now, let's assume we can get it from a provider
    private lateinit var dataStore: DataStore<Preferences>

    fun init(dataStore: DataStore<Preferences>) {
        this.dataStore = dataStore
    }

    fun getLastKnownBalance(): Flow<String> {
        return if (::dataStore.isInitialized) {
            dataStore.data.map { preferences ->
                preferences[LAST_KNOWN_BALANCE] ?: "0.00"
            }
        } else {
            emptyFlow()
        }
    }

    suspend fun setLastKnownBalance(balance: String) {
        if (!::dataStore.isInitialized) return
        dataStore.edit { preferences ->
            preferences[LAST_KNOWN_BALANCE] = balance
        }
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

    fun isWalletOnboarded(): Flow<Boolean> {
        return if (::dataStore.isInitialized) {
            dataStore.data.map { preferences ->
                preferences[WALLET_ONBOARDED] ?: false
            }
        } else {
            emptyFlow()
        }
    }

    suspend fun setWalletOnboarded(onboarded: Boolean) {
        if (!::dataStore.isInitialized) return
        dataStore.edit { preferences ->
            preferences[WALLET_ONBOARDED] = onboarded
        }
    }

    fun getSolanaNetwork(): Flow<String> {
        return if (::dataStore.isInitialized) {
            dataStore.data.map { preferences ->
                preferences[SOLANA_NETWORK] ?: "DEVNET"
            }
        } else {
            emptyFlow()
        }
    }

    suspend fun setSolanaNetwork(network: String) {
        if (!::dataStore.isInitialized) return
        dataStore.edit { preferences ->
            preferences[SOLANA_NETWORK] = network
        }
    }
    
    // Legacy support or platform specific
    // This will be moved to an expect/actual or handled differently
}

fun authenticateWithBiometrics(
    onSuccess: () -> Unit,
    onFailure: () -> Unit
) {
    authenticateBiometrics(onSuccess, onFailure)
}

expect fun authenticateBiometrics(
    onSuccess: () -> Unit,
    onFailure: () -> Unit
)

expect suspend fun getLegacyPassword(context: Any?): String?
