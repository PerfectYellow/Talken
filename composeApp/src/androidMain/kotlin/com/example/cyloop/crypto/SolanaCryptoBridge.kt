package com.example.cyloop.crypto

import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer

actual object SolanaCryptoBridge {
    actual fun derivePublicKey(seed: ByteArray): ByteArray {
        // BouncyCastle expects the raw 32-byte seed and handles SHA-512 internally.
        val privKey = Ed25519PrivateKeyParameters(seed, 0)
        return privKey.generatePublicKey().encoded
    }

    actual fun sign(message: ByteArray, seed: ByteArray): ByteArray {
        val privKey = Ed25519PrivateKeyParameters(seed, 0)
        val signer = Ed25519Signer()
        signer.init(true, privKey)
        signer.update(message, 0, message.size)
        return signer.generateSignature()
    }
}
