package com.example.cyloop.crypto

import korlibs.crypto.HMAC
import korlibs.crypto.SHA512

object HDWallet {
    private const val ED25519_CURVE = "ed25519 seed"

    fun derivePath(seed: ByteArray, path: String): ByteArray {
        val master = HMAC.hmacSHA512(ED25519_CURVE.encodeToByteArray(), seed).bytes
        var result = master
        
        // path format: m/44'/501'/0'/0'
        val segments = path.split("/").drop(1)
        for (segment in segments) {
            val hardened = segment.endsWith("'")
            val index = if (hardened) {
                segment.dropLast(1).toLong() or 0x80000000L
            } else {
                segment.toLong()
            }
            result = deriveChild(result, index)
        }
        
        return result.copyOfRange(0, 32)
    }

    private fun deriveChild(parent: ByteArray, index: Long): ByteArray {
        val key = parent.copyOfRange(0, 32)
        val chainCode = parent.copyOfRange(32, 64)
        
        val data = ByteArray(1 + 32 + 4)
        data[0] = 0
        key.copyInto(data, 1, 0, 32)
        data[33] = (index shr 24).toByte()
        data[34] = (index shr 16).toByte()
        data[35] = (index shr 8).toByte()
        data[36] = index.toByte()
        
        return HMAC.hmacSHA512(chainCode, data).bytes
    }
}
