package com.example.cyloop.storage

import android.content.Context
import com.example.cyloop.CyLoopApp

actual object SecureStorage {
    private val prefs by lazy {
        CyLoopApp.instance.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
    }

    actual suspend fun getPassword(): String? {
        return prefs.getString("user_password", null)
    }

    actual suspend fun setPassword(password: String) {
        prefs.edit().putString("user_password", password).apply()
    }
}

actual fun authenticateWithBiometrics(
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    // This needs an Activity. We might need a better way to handle this.
    // For now, we'll just call onFailure or implement a way to get the current activity.
    onFailure("Biometrics not implemented yet for Android in commonMain")
}
