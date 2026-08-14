package com.example.cyloop.crypto

import korlibs.crypto.PBKDF2
import korlibs.crypto.SHA256
import korlibs.crypto.SHA512

object Bip39 {
    private val wordlist = BIP39_ENGLISH_WORDS

    fun generateMnemonic(entropy: ByteArray): List<String> {
        val entropyBits = bytesToBinary(entropy)
        val checksumBits = deriveChecksumBits(entropy)
        val allBits = entropyBits + checksumBits
        
        return allBits.chunked(11).map { binary ->
            val index = binary.toInt(2)
            wordlist[index]
        }
    }

    fun validateMnemonic(mnemonic: String): Boolean {
        val words = mnemonic.trim().lowercase().split("\\s+".toRegex())
        if (words.size !in listOf(12, 15, 18, 21, 24)) return false
        
        try {
            val bits = words.map { word ->
                val index = wordlist.indexOf(word)
                if (index == -1) return false
                index.toString(2).padStart(11, '0')
            }.joinToString("")
            
            val divider = bits.length * 32 / 33
            val entropyBits = bits.substring(0, divider)
            val checksumBits = bits.substring(divider)
            
            val entropyBytes = binaryToBytes(entropyBits)
            val calculatedChecksum = deriveChecksumBits(entropyBytes)
            
            return checksumBits == calculatedChecksum
        } catch (e: Exception) {
            return false
        }
    }

    fun generateSeed(mnemonic: String, passphrase: String = ""): ByteArray {
        val salt = "mnemonic$passphrase".encodeToByteArray()
        // Standard BIP-39 uses 2048 iterations of HMAC-SHA512 and 512 bits (64 bytes) output
        return PBKDF2.pbkdf2(mnemonic.encodeToByteArray(), salt, 2048, 512, SHA512()).bytes
    }

    private fun deriveChecksumBits(entropy: ByteArray): String {
        val hash = SHA256.digest(entropy).bytes
        val checksumLength = entropy.size * 8 / 32
        return bytesToBinary(hash).substring(0, checksumLength)
    }

    private fun bytesToBinary(bytes: ByteArray): String {
        return bytes.joinToString("") { byte ->
            (byte.toInt() and 0xFF).toString(2).padStart(8, '0')
        }
    }

    private fun binaryToBytes(binary: String): ByteArray {
        return binary.chunked(8).map { 
            it.toInt(2).toByte() 
        }.toByteArray()
    }
    
    fun getWordList(): List<String> = wordlist
}
