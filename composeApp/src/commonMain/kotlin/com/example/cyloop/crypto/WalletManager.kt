package com.example.cyloop.crypto

import com.example.cyloop.storage.SecureStorage
import korlibs.crypto.SHA256
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object WalletManager {
    private val secureStorage = SecureStorage()
    private val _walletAddress = MutableStateFlow<String?>(null)
    val walletAddress: StateFlow<String?> = _walletAddress

    init {
        _walletAddress.value = secureStorage.getString("wallet_address")
    }

    fun hasWallet(): Boolean = _walletAddress.value != null

    val supportedWords = listOf("apple", "banana", "cherry", "date", "elderberry", "fig", "grape", "honeydew", "kiwi", "lemon", "mango", "nectarine")

    suspend fun createWallet(): List<String> {
        // Simple random mnemonic generation for demonstration
        val mnemonicWords = (1..12).map { supportedWords.random() }
        val mnemonicString = mnemonicWords.joinToString(" ")
        
        val privateKeyBytes = deriveKeyFromMnemonic(mnemonicString)
        val privateKeyHex = privateKeyBytes.map { it.toInt().and(0xFF).toString(16).padStart(2, '0') }.joinToString("")
        
        // Address is Base58 encoded bytes
        val address = CryptoUtils.encodeBase58(privateKeyBytes)
        
        saveWallet(mnemonicString, address, privateKeyHex)
        return mnemonicWords
    }

    sealed class ImportResult {
        object Success : ImportResult()
        data class InvalidWordCount(val count: Int) : ImportResult()
        data class UnknownWords(val words: List<String>) : ImportResult()
        data class Error(val message: String) : ImportResult()
    }

    suspend fun importWallet(mnemonic: String): ImportResult {
        val words = mnemonic.trim().lowercase().split("\\s+".toRegex())
        
        if (words.size != 12 && words.size != 24) {
            return ImportResult.InvalidWordCount(words.size)
        }
        
        val invalidWords = words.filter { it !in supportedWords }
        if (invalidWords.isNotEmpty()) {
            return ImportResult.UnknownWords(invalidWords)
        }
        
        try {
            val privateKeyBytes = deriveKeyFromMnemonic(mnemonic.trim())
            val privateKeyHex = privateKeyBytes.map { it.toInt().and(0xFF).toString(16).padStart(2, '0') }.joinToString("")
            val address = CryptoUtils.encodeBase58(privateKeyBytes)
            
            saveWallet(mnemonic.trim(), address, privateKeyHex)
            return ImportResult.Success
        } catch (e: Exception) {
            return ImportResult.Error(e.message ?: "Unknown derivation error")
        }
    }

    private fun deriveKeyFromMnemonic(mnemonic: String): ByteArray {
        // Deterministic derivation: hash the mnemonic string
        return SHA256.digest(mnemonic.trim().lowercase().encodeToByteArray()).bytes
    }

    private fun saveWallet(mnemonic: String, address: String, privateKey: String) {
        secureStorage.saveString("wallet_mnemonic", mnemonic)
        secureStorage.saveString("wallet_address", address)
        secureStorage.saveString("wallet_private_key", privateKey)
        _walletAddress.value = address
    }

    fun getMnemonic(): String? = secureStorage.getString("wallet_mnemonic")
    fun getPrivateKey(): String? = secureStorage.getString("wallet_private_key")
    
    fun deleteWallet() {
        secureStorage.delete("wallet_mnemonic")
        secureStorage.delete("wallet_address")
        secureStorage.delete("wallet_private_key")
        _walletAddress.value = null
    }
}
