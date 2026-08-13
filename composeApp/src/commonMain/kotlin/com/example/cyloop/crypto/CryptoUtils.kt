package com.example.cyloop.crypto

expect fun generateSecureRandomBytes(size: Int): ByteArray

object CryptoUtils {
    private val ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray()
    private val ENCODED_ZERO = ALPHABET[0]
    private val INDEXES = IntArray(128) { -1 }

    init {
        for (i in ALPHABET.indices) {
            INDEXES[ALPHABET[i].code] = i
        }
    }

    fun encodeBase58(input: ByteArray): String {
        if (input.isEmpty()) return ""
        var zeros = 0
        while (zeros < input.size && input[zeros].toInt() == 0) {
            zeros++
        }
        val encoded = CharArray(input.size * 2)
        var outputStart = encoded.size
        val inputCopy = input.copyOf()
        var start = zeros
        while (start < inputCopy.size) {
            var remainder = 0
            for (i in start until inputCopy.size) {
                val digit = inputCopy[i].toInt() and 0xFF
                val temp = remainder * 256 + digit
                inputCopy[i] = (temp / 58).toByte()
                remainder = temp % 58
            }
            encoded[--outputStart] = ALPHABET[remainder]
            if (inputCopy[start].toInt() == 0) {
                start++
            }
        }
        while (outputStart < encoded.size && encoded[outputStart] == ENCODED_ZERO) {
            outputStart++
        }
        while (--zeros >= 0) {
            encoded[--outputStart] = ENCODED_ZERO
        }
        return encoded.concatToString(outputStart, outputStart + (encoded.size - outputStart))
    }

    fun decodeBase58(input: String): ByteArray {
        if (input.isEmpty()) return ByteArray(0)
        val input58 = ByteArray(input.length)
        for (i in input.indices) {
            val c = input[i]
            val digit = if (c.code < 128) INDEXES[c.code] else -1
            if (digit < 0) throw IllegalArgumentException("Invalid Base58 character: $c")
            input58[i] = digit.toByte()
        }
        var zeros = 0
        while (zeros < input58.size && input58[zeros].toInt() == 0) {
            zeros++
        }
        val decoded = ByteArray(input.length)
        var outputStart = decoded.size
        var start = zeros
        while (start < input58.size) {
            var remainder = 0
            for (i in start until input58.size) {
                val digit = input58[i].toInt() and 0xFF
                val temp = remainder * 58 + digit
                input58[i] = (temp / 256).toByte()
                remainder = temp % 256
            }
            decoded[--outputStart] = remainder.toByte()
            if (input58[start].toInt() == 0) {
                start++
            }
        }
        while (outputStart < decoded.size && decoded[outputStart].toInt() == 0) {
            outputStart++
        }
        return decoded.copyOfRange(outputStart - zeros, decoded.size)
    }
}
