package com.example.cyloop.crypto

import com.example.cyloop.api.CoinGeckoService
import com.example.cyloop.api.HeliusService
import com.example.cyloop.api.SolanaService
import com.example.cyloop.storage.SecureStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.pow

object WalletManager {
    private val secureStorage = SecureStorage()
    private val _walletAddress = MutableStateFlow<String?>(null)
    val walletAddress: StateFlow<String?> = _walletAddress

    // Shared State for Balances
    private val _solBalance = MutableStateFlow(0.0)
    val solBalance: StateFlow<Double> = _solBalance

    private val _usdcBalance = MutableStateFlow(0.0)
    val usdcBalance: StateFlow<Double> = _usdcBalance

    private val _solPrice = MutableStateFlow(0.0)
    val solPrice: StateFlow<Double> = _solPrice

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    init {
        _walletAddress.value = secureStorage.getString("wallet_address")
    }

    fun hasWallet(): Boolean = _walletAddress.value != null

    sealed class ImportResult {
        object Success : ImportResult()
        data class InvalidWordCount(val count: Int) : ImportResult()
        data class UnknownWords(val words: List<String>) : ImportResult()
        data class Error(val message: String) : ImportResult()
    }

    suspend fun createWallet(): List<String> {
        // Standard BIP-39: 128 bits of entropy for 12 words
        val entropy = generateSecureRandomBytes(16)
        val mnemonicWords = Bip39.generateMnemonic(entropy)
        val mnemonicString = mnemonicWords.joinToString(" ")
        
        val keypair = deriveKeypairFromMnemonic(mnemonicString)
        
        saveWallet(mnemonicString, keypair.address, keypair.privateKeyHex)
        return mnemonicWords
    }

    suspend fun importWallet(mnemonic: String): ImportResult {
        val trimmedMnemonic = mnemonic.trim().lowercase()
        val words = trimmedMnemonic.split("\\s+".toRegex())
        
        if (words.size != 12 && words.size != 24) {
            return ImportResult.InvalidWordCount(words.size)
        }
        
        if (!Bip39.validateMnemonic(trimmedMnemonic)) {
            val invalidWords = words.filter { it !in Bip39.BIP39_WORDLIST }
            return if (invalidWords.isNotEmpty()) {
                ImportResult.UnknownWords(invalidWords)
            } else {
                ImportResult.Error("Invalid checksum. Please check the order of your words.")
            }
        }
        
        try {
            val keypair = deriveKeypairFromMnemonic(trimmedMnemonic)
            saveWallet(trimmedMnemonic, keypair.address, keypair.privateKeyHex)
            return ImportResult.Success
        } catch (e: Exception) {
            return ImportResult.Error(e.message ?: "Unknown derivation error")
        }
    }

    suspend fun refreshBalances() {
        val addr = _walletAddress.value ?: return
        if (_isRefreshing.value) return
        
        _isRefreshing.value = true
        try {
            // 1. Fetch SOL
            val lamports = SolanaService.getBalance(addr)
            _solBalance.value = lamports / 10.0.pow(9.0)

            // 2. Fetch Assets (USDC)
            val assets = HeliusService.getAssetsByOwner(addr)
            val usdcToken = assets.find { it.token_info?.symbol == "USDC" }
            _usdcBalance.value = usdcToken?.token_info?.let { 
                it.balance.toDouble() / 10.0.pow(it.decimals.toDouble()) 
            } ?: 0.0

            // 3. Fetch Prices
            val coins = CoinGeckoService.getCoins()
            _solPrice.value = coins.find { it.symbol.lowercase() == "sol" }?.current_price ?: 0.0
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _isRefreshing.value = false
        }
    }

    data class DerivedKeypair(
        val address: String,
        val privateKeyHex: String
    )

    private fun deriveKeypairFromMnemonic(mnemonic: String): DerivedKeypair {
        // 1. BIP-39 Mnemonic -> 64-byte Seed
        val seed = Bip39.generateSeed(mnemonic)
        
        // 2. Derive Ed25519 Seed using Solana standard path: m/44'/501'/0'/0'
        val derivedSeed = HDWallet.derivePath(seed, "m/44'/501'/0'/0'")
        
        // 3. Ed25519 Seed -> Public Key (Address)
        val publicKeyBytes = Ed25519.derivePublicKey(derivedSeed)
        val address = CryptoUtils.encodeBase58(publicKeyBytes)
        
        // 4. Format Private Key (Hex)
        val privateKeyHex = derivedSeed.map { it.toInt().and(0xFF).toString(16).padStart(2, '0') }.joinToString("")
        
        return DerivedKeypair(address, privateKeyHex)
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
        _solBalance.value = 0.0
        _usdcBalance.value = 0.0
        _solPrice.value = 0.0
    }
}
