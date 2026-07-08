package com.example.cyloop.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

expect fun createDataStore(context: Any?, name: String): DataStore<Preferences>

internal const val AUTH_PREFS = "auth_prefs"
internal const val CONTACT_PREFS = "contact_prefs"
