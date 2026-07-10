package com.example.cyloop.storage

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

@Serializable
data class SavedContact(
    val id: String,
    val name: String,
    val nftAddress: String,
    val ownerAddress: String,
    val imageUrl: String?
)

object ContactPreferences {
    private val CONTACTS_KEY = stringPreferencesKey("saved_contacts")
    private val json = Json { ignoreUnknownKeys = true }
    private lateinit var dataStore: DataStore<Preferences>

    fun init(dataStore: DataStore<Preferences>) {
        this.dataStore = dataStore
    }

    val savedContacts: Flow<List<SavedContact>> get() = if (::dataStore.isInitialized) {
        dataStore.data.map { preferences ->
            val jsonString = preferences[CONTACTS_KEY] ?: "[]"
            try {
                json.decodeFromString<List<SavedContact>>(jsonString)
            } catch (_: Exception) {
                emptyList()
            }
        }
    } else {
        emptyFlow()
    }

    suspend fun saveContact(contact: SavedContact) {
        if (!::dataStore.isInitialized) return
        dataStore.edit { preferences ->
            val currentJson = preferences[CONTACTS_KEY] ?: "[]"
            val currentList = try {
                json.decodeFromString<List<SavedContact>>(currentJson).toMutableList()
            } catch (_: Exception) {
                mutableListOf()
            }
            
            if (currentList.none { it.nftAddress == contact.nftAddress }) {
                currentList.add(contact)
                preferences[CONTACTS_KEY] = json.encodeToString(currentList)
            }
        }
    }

    suspend fun deleteContact(nftAddress: String) {
        if (!::dataStore.isInitialized) return
        dataStore.edit { preferences ->
            val currentJson = preferences[CONTACTS_KEY] ?: "[]"
            val currentList = try {
                json.decodeFromString<List<SavedContact>>(currentJson).toMutableList()
            } catch (_: Exception) {
                mutableListOf()
            }

            if (currentList.removeAll { it.nftAddress == nftAddress }) {
                preferences[CONTACTS_KEY] = json.encodeToString(currentList)
            }
        }
    }
}
