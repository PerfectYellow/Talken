package com.example.cyloop.crypto

import java.security.SecureRandom

actual fun generateSecureRandomBytes(size: Int): ByteArray {
    val bytes = ByteArray(size)
    SecureRandom().nextBytes(bytes)
    return bytes
}
