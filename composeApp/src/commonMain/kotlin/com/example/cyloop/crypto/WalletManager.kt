package com.example.cyloop.crypto

import com.example.cyloop.api.CoinGeckoService
import com.example.cyloop.api.HeliusService
import com.example.cyloop.api.SolanaService
import com.example.cyloop.api.SolanaNetwork
import com.example.cyloop.storage.SecureStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.math.pow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

@Serializable
enum class WalletType {
    MNEMONIC,
    PRIVATE_KEY
}

@Serializable
data class WalletInfo(
    val address: String,
    val name: String,
    val type: WalletType,
    val network: String = "DEVNET"
)

object WalletManager {
    private val secureStorage = SecureStorage()
    private val json = Json { ignoreUnknownKeys = true }
    private val managerScope = CoroutineScope(Dispatchers.Default)
    
    private val _wallets = MutableStateFlow<List<WalletInfo>>(emptyList())
    val allWallets: StateFlow<List<WalletInfo>> = _wallets

    val wallets: StateFlow<List<WalletInfo>> = combine(_wallets, SolanaService.currentNetwork) { list, network ->
        list.filter { it.network == network.name }
    }.stateIn(managerScope, SharingStarted.Eagerly, emptyList())

    private val _activeWalletAddress = MutableStateFlow<String?>(null)
    val walletAddress: StateFlow<String?> = _activeWalletAddress

    private val _solBalance = MutableStateFlow(0.0)
    val solBalance: StateFlow<Double> = _solBalance

    private val _usdcBalance = MutableStateFlow(0.0)
    val usdcBalance: StateFlow<Double> = _usdcBalance

    private val _solPrice = MutableStateFlow(0.0)
    val solPrice: StateFlow<Double> = _solPrice

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    init {
        loadWallets()
        observeNetworkChanges()
    }

    private fun loadWallets() {
        val walletsJson = secureStorage.getString("wallets_list")
        if (!walletsJson.isNullOrBlank()) {
            try {
                val list = json.decodeFromString<List<WalletInfo>>(walletsJson)
                _wallets.value = list
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val activeAddr = secureStorage.getString("active_wallet_address")
        if (activeAddr != null && _wallets.value.any { it.address == activeAddr }) {
            _activeWalletAddress.value = activeAddr
        } else {
            _activeWalletAddress.value = wallets.value.firstOrNull()?.address
        }
    }

    private fun observeNetworkChanges() {
        managerScope.launch {
            var isFirstEmission = true
            SolanaService.currentNetwork.collect { network ->
                _solBalance.value = 0.0
                _usdcBalance.value = 0.0
                
                val currentActive = _wallets.value.find { it.address == _activeWalletAddress.value }
                val needsSwitch = currentActive == null || currentActive.network != network.name
                
                if (!isFirstEmission || needsSwitch) {
                    val firstOnNetwork = _wallets.value.find { it.network == network.name }
                    _activeWalletAddress.value = firstOnNetwork?.address
                }
                
                isFirstEmission = false
                refreshBalances()
            }
        }
    }

    fun hasWallet(): Boolean = _activeWalletAddress.value != null

    sealed class ImportResult {
        object Success : ImportResult()
        data class InvalidWordCount(val count: Int) : ImportResult()
        data class UnknownWords(val words: List<String>) : ImportResult()
        data class Error(val message: String) : ImportResult()
    }

    suspend fun createWallet(name: String = "Wallet ${_wallets.value.size + 1}"): List<String> {
        val entropy = generateSecureRandomBytes(16)
        val mnemonicWords = Bip39.generateMnemonic(entropy)
        val mnemonicString = mnemonicWords.joinToString(" ")
        val keypair = deriveKeypairFromMnemonic(mnemonicString)
        saveNewWallet(
            WalletInfo(keypair.address, name, WalletType.MNEMONIC, SolanaService.currentNetwork.value.name),
            mnemonicString,
            keypair.privateKeyBase58
        )
        return mnemonicWords
    }

    suspend fun importWallet(mnemonic: String, name: String = "Wallet ${_wallets.value.size + 1}"): ImportResult {
        val trimmedMnemonic = mnemonic.trim().lowercase()
        val words = trimmedMnemonic.split("\\s+".toRegex())
        if (words.size != 12 && words.size != 24) return ImportResult.InvalidWordCount(words.size)
        if (!Bip39.validateMnemonic(trimmedMnemonic)) {
            val invalidWords = words.filter { it !in Bip39.getWordList() }
            return if (invalidWords.isNotEmpty()) ImportResult.UnknownWords(invalidWords) else ImportResult.Error("Invalid checksum")
        }
        try {
            val keypair = deriveKeypairFromMnemonic(trimmedMnemonic)
            saveNewWallet(
                WalletInfo(keypair.address, name, WalletType.MNEMONIC, SolanaService.currentNetwork.value.name),
                trimmedMnemonic,
                keypair.privateKeyBase58
            )
            return ImportResult.Success
        } catch (e: Exception) {
            return ImportResult.Error(e.message ?: "Derivation error")
        }
    }

    suspend fun importWalletByPrivateKey(privateKeyBase58: String, name: String = "Wallet ${_wallets.value.size + 1}"): Boolean {
        try {
            val decoded = CryptoUtils.decodeBase58(privateKeyBase58)
            if (decoded.size != 64 && decoded.size != 32) return false
            val publicKeyBytes = if (decoded.size == 64) decoded.copyOfRange(32, 64) else Ed25519.derivePublicKey(decoded)
            val address = CryptoUtils.encodeBase58(publicKeyBytes)
            saveNewWallet(
                WalletInfo(address, name, WalletType.PRIVATE_KEY, SolanaService.currentNetwork.value.name),
                null,
                privateKeyBase58
            )
            return true
        } catch (e: Exception) {
            return false
        }
    }

    private fun saveNewWallet(info: WalletInfo, mnemonic: String?, privateKey: String) {
        val currentList = _wallets.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.address == info.address && it.network == info.network }
        if (existingIndex >= 0) currentList[existingIndex] = info else currentList.add(info)
        _wallets.value = currentList
        secureStorage.saveString("wallets_list", json.encodeToString(currentList))
        if (mnemonic != null) secureStorage.saveString("mnemonic_${info.address}", mnemonic)
        secureStorage.saveString("pkey_${info.address}", privateKey)
        switchWallet(info.address)
    }

    fun switchWallet(address: String) {
        if (_wallets.value.any { it.address == address }) {
            _activeWalletAddress.value = address
            secureStorage.saveString("active_wallet_address", address)
            _solBalance.value = 0.0
            _usdcBalance.value = 0.0
            managerScope.launch { refreshBalances() }
        }
    }

    fun renameWallet(address: String, newName: String) {
        val currentList = _wallets.value.toMutableList()
        val index = currentList.indexOfFirst { it.address == address }
        if (index >= 0) {
            currentList[index] = currentList[index].copy(name = newName)
            _wallets.value = currentList
            secureStorage.saveString("wallets_list", json.encodeToString(currentList))
        }
    }

    suspend fun refreshBalances() {
        val addr = _activeWalletAddress.value ?: return
        if (_isRefreshing.value) return
        _isRefreshing.value = true
        try {
            val lamports = SolanaService.getBalance(addr)
            _solBalance.value = lamports / 10.0.pow(9.0)
            val assets = HeliusService.getAssetsByOwner(addr)
            val usdcToken = assets.find { it.token_info?.symbol == "USDC" }
            _usdcBalance.value = usdcToken?.token_info?.let { it.balance.toDouble() / 10.0.pow(it.decimals.toDouble()) } ?: 0.0
            val coins = CoinGeckoService.getCoins()
            _solPrice.value = coins.find { it.symbol.lowercase() == "sol" }?.current_price ?: 0.0
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _isRefreshing.value = false
        }
    }

    data class DerivedKeypair(val address: String, val privateKeyBase58: String)

    private fun deriveKeypairFromMnemonic(mnemonic: String): DerivedKeypair {
        val seed = Bip39.generateSeed(mnemonic)
        val derivedSeed = HDWallet.derivePath(seed, "m/44'/501'/0'/0'")
        val publicKeyBytes = Ed25519.derivePublicKey(derivedSeed)
        val address = CryptoUtils.encodeBase58(publicKeyBytes)
        val fullPrivateKeyBytes = derivedSeed + publicKeyBytes
        val privateKeyBase58 = CryptoUtils.encodeBase58(fullPrivateKeyBytes)
        return DerivedKeypair(address, privateKeyBase58)
    }

    fun getMnemonic(): String? {
        val addr = _activeWalletAddress.value ?: return null
        return secureStorage.getString("mnemonic_$addr") ?: secureStorage.getString("wallet_mnemonic")
    }
    
    fun getPrivateKey(): String? {
        val addr = _activeWalletAddress.value ?: return null
        return secureStorage.getString("pkey_$addr") ?: secureStorage.getString("wallet_private_key")
    }
    
    fun deleteWallet() {
        val addr = _activeWalletAddress.value ?: return
        val currentList = _wallets.value.toMutableList()
        currentList.removeAll { it.address == addr }
        _wallets.value = currentList
        secureStorage.saveString("wallets_list", json.encodeToString(currentList))
        secureStorage.delete("mnemonic_$addr")
        secureStorage.delete("pkey_$addr")
        if (currentList.isEmpty()) {
            secureStorage.delete("active_wallet_address")
            _activeWalletAddress.value = null
        } else {
            _activeWalletAddress.value = wallets.value.firstOrNull()?.address
        }
        _solBalance.value = 0.0
        _usdcBalance.value = 0.0
        _solPrice.value = 0.0
    }

    suspend fun sendSOL(receiverAddress: String, amountSOL: Double): String {
        val senderAddress = _activeWalletAddress.value ?: throw Exception("No active wallet")
        val privateKeyBase58 = getPrivateKey() ?: throw Exception("Private key not found")
        val privateKeyBytes = CryptoUtils.decodeBase58(privateKeyBase58)
        
        val lamports = (amountSOL * 10.0.pow(9.0)).toLong()
        val blockhash = SolanaService.getLatestBlockhash()
        
        val base64Tx = SolanaTransaction.createTransferTransaction(
            senderAddress = senderAddress,
            receiverAddress = receiverAddress,
            lamports = lamports,
            recentBlockhash = blockhash,
            privateKey = privateKeyBytes
        )
        
        val signature = SolanaService.sendTransaction(base64Tx)
        
        managerScope.launch {
            kotlinx.coroutines.delay(2000)
            refreshBalances()
        }
        
        return signature
    }
}
