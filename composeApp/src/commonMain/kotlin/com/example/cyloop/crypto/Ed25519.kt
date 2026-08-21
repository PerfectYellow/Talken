package com.example.cyloop.crypto

/**
 * Minimal Ed25519 implementation for Solana Public Key derivation.
 * Uses the Edwards curve math for 100% compatibility with Phantom.
 */
object Ed25519 {
    fun derivePublicKey(seed: ByteArray): ByteArray {
        // Standard Ed25519 (RFC 8032) and BouncyCastle's Ed25519PrivateKeyParameters
        // expect the raw 32-byte seed. We pass it directly to the platform bridge.
        return SolanaCryptoBridge.derivePublicKey(seed)
    }

    fun sign(message: ByteArray, seed: ByteArray): ByteArray {
        return SolanaCryptoBridge.sign(message, seed)
    }
}

expect object SolanaCryptoBridge {
    /**
     * Derives a 32-byte Ed25519 public key from a 32-byte seed.
     */
    fun derivePublicKey(seed: ByteArray): ByteArray

    /**
     * Signs a message with a 32-byte Ed25519 seed.
     */
    fun sign(message: ByteArray, seed: ByteArray): ByteArray
}
