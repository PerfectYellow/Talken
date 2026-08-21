package com.example.cyloop.api

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Serializable
data class SolanaRpcError(
    val code: Int,
    val message: String
)

@Serializable
data class SolanaAccountResponse(
    val jsonrpc: String,
    val result: AccountResult? = null,
    val error: SolanaRpcError? = null,
    val id: Int
)

@Serializable
data class AccountResult(
    val context: SolanaContext,
    val value: AccountValue?
)

@Serializable
data class SolanaContext(
    val apiVersion: String? = null,
    val slot: Long
)

@Serializable
data class AccountValue(
    val data: JsonElement,
    val executable: Boolean,
    val lamports: Long,
    val owner: String,
    val rentEpoch: JsonElement? = null,
    val space: Long
)

@Serializable
data class AccountData(
    val parsed: ParsedData? = null,
    val program: String? = null,
    val space: Long? = null
)

@Serializable
data class ParsedData(
    val info: ParsedInfo,
    val type: String
)

@Serializable
data class ParsedInfo(
    val authority: String? = null,
    val slot: Long? = null,
    val data: JsonElement? = null
)

@Serializable
data class SolanaSignatureResponse(
    val jsonrpc: String,
    val result: List<SolanaSignatureInfo>? = null,
    val error: SolanaRpcError? = null,
    val id: Int
)

@Serializable
data class SolanaSignatureInfo(
    val signature: String,
    val slot: Long,
    val err: JsonElement? = null,
    val memo: String? = null,
    val blockTime: Long? = null,
    val confirmationStatus: String? = null
)

@Serializable
data class SolanaBalanceResponse(
    val jsonrpc: String,
    val result: BalanceResult? = null,
    val error: SolanaRpcError? = null,
    val id: Int
)

@Serializable
data class BalanceResult(
    val context: SolanaContext,
    val value: Long
)

enum class SolanaNetwork(val url: String, val displayName: String) {
    MAINNET("https://api.mainnet-beta.solana.com", "Mainnet"),
    DEVNET("https://api.devnet.solana.com", "Devnet")
}

object SolanaService {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        isLenient = true
        coerceInputValues = true
    }

    private val client = HttpClient {
        install(HttpTimeout) {
            requestTimeoutMillis = 30000
        }
    }

    private val _currentNetwork = MutableStateFlow(SolanaNetwork.DEVNET)
    val currentNetwork: StateFlow<SolanaNetwork> = _currentNetwork

    fun setNetwork(network: SolanaNetwork) {
        _currentNetwork.value = network
    }

    private val currentUrl: String
        get() = _currentNetwork.value.url

    suspend fun getBalance(address: String): Long {
        val payload = buildJsonObject {
            put("jsonrpc", "2.0")
            put("id", 1)
            put("method", "getBalance")
            put("params", buildJsonArray {
                add(address)
            })
        }

        val response = client.post(currentUrl) {
            header(HttpHeaders.ContentType, "application/json")
            setBody(payload.toString())
        }

        val responseText = response.bodyAsText()
        val responseObj = json.decodeFromString<SolanaBalanceResponse>(responseText)
        
        if (responseObj.error != null) {
            throw Exception(responseObj.error.message)
        }
        
        return responseObj.result?.value ?: 0L
    }

    suspend fun getAccountInfo(address: String): SolanaAccountResponse {
        val payload = buildJsonObject {
            put("jsonrpc", "2.0")
            put("id", 1)
            put("method", "getAccountInfo")
            put("params", buildJsonArray {
                add(address)
                add(buildJsonObject {
                    put("encoding", "jsonParsed")
                })
            })
        }

        val response = client.post(currentUrl) {
            header(HttpHeaders.ContentType, "application/json")
            header(HttpHeaders.Accept, "application/json")
            header(HttpHeaders.UserAgent, "CyLoop/1.0")
            setBody(payload.toString())
        }

        val responseText = response.bodyAsText()

        if (responseText.isBlank()) {
            throw Exception("Empty response (HTTP ${response.status.value})")
        }

        return try {
            val responseObj = json.decodeFromString<SolanaAccountResponse>(responseText)
            if (responseObj.error != null) {
                throw Exception(responseObj.error.message)
            }
            responseObj
        } catch (e: Exception) {
            throw Exception("Parse Error: ${e.message}")
        }
    }

    suspend fun getSignaturesForAddress(address: String, limit: Int = 1): SolanaSignatureResponse {
        val payload = buildJsonObject {
            put("jsonrpc", "2.0")
            put("id", 1)
            put("method", "getSignaturesForAddress")
            put("params", buildJsonArray {
                add(address)
                add(buildJsonObject {
                    put("limit", limit)
                })
            })
        }

        val response = client.post(currentUrl) {
            header(HttpHeaders.ContentType, "application/json")
            header(HttpHeaders.Accept, "application/json")
            header(HttpHeaders.UserAgent, "CyLoop/1.0")
            setBody(payload.toString())
        }

        val responseText = response.bodyAsText()

        if (responseText.isBlank()) {
            throw Exception("Empty response (HTTP ${response.status.value})")
        }

        return try {
            val responseObj = json.decodeFromString<SolanaSignatureResponse>(responseText)
            if (responseObj.error != null) {
                throw Exception(responseObj.error.message)
            }
            responseObj
        } catch (e: Exception) {
            throw Exception("Parse Error: ${e.message}")
        }
    }

    suspend fun getLatestBlockhash(): String {
        val payload = buildJsonObject {
            put("jsonrpc", "2.0")
            put("id", 1)
            put("method", "getLatestBlockhash")
            put("params", buildJsonArray {
                add(buildJsonObject {
                    put("commitment", "finalized") // Use finalized for better compatibility
                })
            })
        }

        val response = client.post(currentUrl) {
            header(HttpHeaders.ContentType, "application/json")
            setBody(payload.toString())
        }

        val responseText = response.bodyAsText()
        val responseObj = json.parseToJsonElement(responseText).jsonObject
        
        if (responseObj.containsKey("error")) {
            val error = responseObj["error"]?.jsonObject
            throw Exception(error?.get("message")?.jsonPrimitive?.content ?: "Blockhash RPC error")
        }

        val result = responseObj["result"]?.jsonObject?.get("value")?.jsonObject
        return result?.get("blockhash")?.jsonPrimitive?.content ?: throw Exception("Failed to get blockhash")
    }

    suspend fun sendTransaction(base64Transaction: String): String {
        val payload = buildJsonObject {
            put("jsonrpc", "2.0")
            put("id", 1)
            put("method", "sendTransaction")
            put("params", buildJsonArray {
                add(base64Transaction)
                add(buildJsonObject {
                    put("encoding", "base64")
                    put("preflightCommitment", "confirmed")
                })
            })
        }

        val response = client.post(currentUrl) {
            header(HttpHeaders.ContentType, "application/json")
            setBody(payload.toString())
        }

        val responseText = response.bodyAsText()
        val responseObj = json.parseToJsonElement(responseText).jsonObject
        
        if (responseObj.containsKey("error")) {
            val error = responseObj["error"]?.jsonObject
            val message = error?.get("message")?.jsonPrimitive?.content ?: "Unknown RPC error"
            // If it's a simulation error, sometimes more details are in 'data'
            val data = error?.get("data")
            throw Exception(if (data != null) "$message: $data" else message)
        }

        return responseObj["result"]?.jsonPrimitive?.content ?: throw Exception("No signature returned")
    }

    fun parseAccountData(data: JsonElement): AccountData? {
        return try {
            if (data is JsonObject) {
                json.decodeFromJsonElement<AccountData>(data)
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
