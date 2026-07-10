package com.example.cyloop.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map

object IpfsPreferences {
    private val BASE_URL = stringPreferencesKey("ipfs_base_url")
    private val JWT = stringPreferencesKey("ipfs_jwt")
    private val API_KEY = stringPreferencesKey("ipfs_api_key")
    private val API_SECRET = stringPreferencesKey("ipfs_api_secret")

    private lateinit var dataStore: DataStore<Preferences>

    fun init(dataStore: DataStore<Preferences>) {
        this.dataStore = dataStore
    }

    data class IpfsConfig(
        val baseUrl: String,
        val jwt: String,
        val apiKey: String,
        val apiSecret: String
    )

    fun getConfig(): Flow<IpfsConfig> {
        return if (::dataStore.isInitialized) {
            dataStore.data.map { preferences ->
                IpfsConfig(
                    baseUrl = preferences[BASE_URL] ?: "https://api.pinata.cloud",
                    jwt = preferences[JWT] ?: "",
                    apiKey = preferences[API_KEY] ?: "",
                    apiSecret = preferences[API_SECRET] ?: ""
                )
            }
        } else {
            emptyFlow()
        }
    }

    suspend fun setConfig(config: IpfsConfig) {
        if (!::dataStore.isInitialized) return
        dataStore.edit { preferences ->
            preferences[BASE_URL] = config.baseUrl
            preferences[JWT] = config.jwt
            preferences[API_KEY] = config.apiKey
            preferences[API_SECRET] = config.apiSecret
        }
    }
}
