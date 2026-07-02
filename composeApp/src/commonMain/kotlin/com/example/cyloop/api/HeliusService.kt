package com.example.cyloop.api

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable
data class HeliusAssetResponse(
    val jsonrpc: String,
    val result: HeliusAssetResult? = null,
    val error: JsonObject? = null,
    val id: Int
)

@Serializable
data class HeliusAssetResult(
    val id: String,
    val content: HeliusAssetContent,
    val ownership: HeliusAssetOwnership
)

@Serializable
data class HeliusAssetContent(
    val metadata: HeliusAssetMetadata,
    val links: HeliusAssetLinks? = null
)

@Serializable
data class HeliusAssetMetadata(
    val name: String,
    val symbol: String? = null
)

@Serializable
data class HeliusAssetLinks(
    val image: String? = null
)

@Serializable
data class HeliusAssetOwnership(
    val owner: String
)

object HeliusService {
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

    private const val API_KEY = "705be33f-3a32-4d05-81ea-c13c684c19a6"
    private const val BASE_URL = "https://mainnet.helius-rpc.com/?api-key=$API_KEY"

    suspend fun getAsset(assetId: String): HeliusAssetResult {
        val payload = buildJsonObject {
            put("jsonrpc", "2.0")
            put("id", 1)
            put("method", "getAsset")
            put("params", buildJsonObject {
                put("id", assetId)
            })
        }

        val response = client.post(BASE_URL) {
            header(HttpHeaders.ContentType, "application/json")
            setBody(payload.toString())
        }

        val responseText = response.bodyAsText()

        if (responseText.isBlank()) {
            throw Exception("Empty response from Helius")
        }

        val responseObj = json.decodeFromString<HeliusAssetResponse>(responseText)
        
        if (responseObj.error != null) {
            throw Exception("Helius Error: ${responseObj.error}")
        }

        return responseObj.result ?: throw Exception("No asset found for ID: $assetId")
    }
}
