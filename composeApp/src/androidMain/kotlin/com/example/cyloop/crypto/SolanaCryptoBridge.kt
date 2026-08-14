package com.example.cyloop.crypto

import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters

actual object SolanaCryptoBridge {
    actual fun derivePublicKey(seed: ByteArray): ByteArray {
        // BouncyCastle expects the raw 32-byte seed and handles SHA-512 internally.
        val privKey = Ed25519PrivateKeyParameters(seed, 0)
        return privKey.generatePublicKey().encoded
    }
}
