package com.example.cyloop.storage

import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.cyloop.CyLoopApp

actual class SecureStorage actual constructor() {
    private val masterKey = MasterKey.Builder(CyLoopApp.instance)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences = EncryptedSharedPreferences.create(
        CyLoopApp.instance,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    actual fun saveString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    actual fun getString(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    actual fun delete(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }

    actual fun clear() {
        sharedPreferences.edit().clear().apply()
    }

    actual fun savePassword(password: String) {
        saveString("app_password", password)
    }

    actual fun getPassword(): String? {
        return getString("app_password")
    }
}
