package com.example.cyloop.storage

actual object SecureStorage {
    actual suspend fun getPassword(): String? {
        // Implement iOS Keychain logic here
        return null
    }

    actual suspend fun setPassword(password: String) {
        // Implement iOS Keychain logic here
    }
}

actual fun authenticateWithBiometrics(
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    // Implement iOS LAContext logic here
    onFailure("Biometrics not implemented for iOS yet")
}
