package com.example.cyloop.api

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

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

    private const val MAINNET_URL = "https://api.mainnet-beta.solana.com"
    private const val DEVNET_URL = "https://api.devnet.solana.com"

    suspend fun getBalance(address: String): Long {
        val payload = buildJsonObject {
            put("jsonrpc", "2.0")
            put("id", 1)
            put("method", "getBalance")
            put("params", buildJsonArray {
                add(address)
            })
        }

        val response = client.post(MAINNET_URL) {
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

        val response = client.post(DEVNET_URL) {
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

        val response = client.post(DEVNET_URL) {
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
