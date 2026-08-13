package com.example.cyloop.storage

import platform.Foundation.*
import platform.Security.*
import kotlinx.cinterop.*

actual class SecureStorage actual constructor() {
    actual fun saveString(key: String, value: String) {
        // Mocking for now to avoid complex Keychain interop errors
    }

    actual fun getString(key: String): String? {
        return null
    }

    actual fun delete(key: String) {
    }

    actual fun clear() {
    }

    actual fun savePassword(password: String) {
    }

    actual fun getPassword(): String? {
        return null
    }
}
