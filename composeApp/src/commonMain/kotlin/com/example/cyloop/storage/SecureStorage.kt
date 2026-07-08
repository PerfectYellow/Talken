package com.example.cyloop.storage

expect object SecureStorage {
    suspend fun getPassword(): String?
    suspend fun setPassword(password: String)
}

expect fun authenticateWithBiometrics(
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
)
