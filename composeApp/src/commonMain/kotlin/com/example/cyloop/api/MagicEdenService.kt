package com.example.cyloop.api

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class MagicEdenTokenResponse(
    val mintAddress: String,
    val price: Double? = null,
    val name: String? = null
)

object MagicEdenService {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val client = HttpClient {
        install(HttpTimeout) {
            requestTimeoutMillis = 30000
        }
    }

    private const val BASE_URL = "https://api-mainnet.magiceden.dev/v2"

    suspend fun getTokenInfo(mintAddress: String): MagicEdenTokenResponse? {
        return try {
            val response = client.get("$BASE_URL/tokens/$mintAddress")
            if (response.status == HttpStatusCode.OK) {
                val responseText = response.bodyAsText()
                json.decodeFromString<MagicEdenTokenResponse>(responseText)
            } else {
                null
            }
        } catch (e: Exception) {
            println("Error fetching Magic Eden token info: ${e.message}")
            null
        }
    }
}
