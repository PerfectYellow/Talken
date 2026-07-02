package com.example.cyloop.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class SavedContact(
    val id: String,
    val name: String,
    val nftAddress: String,
    val ownerAddress: String,
    val imageUrl: String?
)

private val Context.contactDataStore by preferencesDataStore(name = "contact_prefs")

class ContactPreferences(private val context: Context) {
    private val CONTACTS_KEY = stringPreferencesKey("saved_contacts")
    private val json = Json { ignoreUnknownKeys = true }

    val savedContacts: Flow<List<SavedContact>> = context.contactDataStore.data.map { preferences ->
        val jsonString = preferences[CONTACTS_KEY] ?: "[]"
        try {
            json.decodeFromString<List<SavedContact>>(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveContact(contact: SavedContact) {
        context.contactDataStore.edit { preferences ->
            val currentJson = preferences[CONTACTS_KEY] ?: "[]"
            val currentList = try {
                json.decodeFromString<List<SavedContact>>(currentJson).toMutableList()
            } catch (e: Exception) {
                mutableListOf()
            }
            
            if (currentList.none { it.nftAddress == contact.nftAddress }) {
                currentList.add(contact)
                preferences[CONTACTS_KEY] = json.encodeToString(currentList)
            }
        }
    }

    suspend fun deleteContact(nftAddress: String) {
        context.contactDataStore.edit { preferences ->
            val currentJson = preferences[CONTACTS_KEY] ?: "[]"
            val currentList = try {
                json.decodeFromString<List<SavedContact>>(currentJson).toMutableList()
            } catch (e: Exception) {
                mutableListOf()
            }

            if (currentList.removeIf { it.nftAddress == nftAddress }) {
                preferences[CONTACTS_KEY] = json.encodeToString(currentList)
            }
        }
    }
}
