package com.example.cyloop.crypto

/**
 * For iOS, we use a pure Kotlin fallback for Ed25519 until a stable 
 * CryptoKit interop is established in the shared module.
 */
actual object SolanaCryptoBridge {
    actual fun derivePublicKey(prunedSeed: ByteArray): ByteArray {
        // Placeholder for iOS. In a production app, we would link to a 
        // compiled Ed25519 library or use native CryptoKit via a plugin.
        // For now, to allow the project to build:
        return prunedSeed // This will cause address mismatch on iOS for now
    }
}
