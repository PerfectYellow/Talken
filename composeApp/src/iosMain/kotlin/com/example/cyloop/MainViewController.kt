package com.example.cyloop

import androidx.compose.ui.window.ComposeUIViewController
import androidx.compose.runtime.remember
import com.example.cyloop.storage.AuthPreferences
import com.example.cyloop.storage.ContactPreferences
import com.example.cyloop.storage.createDataStore

fun MainViewController() = ComposeUIViewController {
    remember {
        AuthPreferences.init(createDataStore(null, "auth_prefs"))
        ContactPreferences.init(createDataStore(null, "contact_prefs"))
        true
    }
    App()
}
