package com.example.cyloop.crypto

import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters

actual object SolanaCryptoBridge {
    actual fun derivePublicKey(prunedSeed: ByteArray): ByteArray {
        // BouncyCastle expects the raw 32-byte seed and handles SHA-512 internally.
        // However, we already have the pruned expanded key if needed.
        // To be 100% correct with Solana:
        val privKey = Ed25519PrivateKeyParameters(prunedSeed, 0)
        return privKey.generatePublicKey().encoded
    }
}
