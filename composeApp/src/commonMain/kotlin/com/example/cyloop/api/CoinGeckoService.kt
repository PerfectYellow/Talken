package com.example.cyloop.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Coin(
    val id: String,
    val symbol: String,
    val name: String,
    val image: String,
    val current_price: Double,
    val price_change_percentage_24h: Double? = 0.0
)

@Serializable
data class MarketChart(
    val prices: List<List<Double>>
)

object CoinGeckoService {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 30000
            connectTimeoutMillis = 15000
            socketTimeoutMillis = 15000
        }

        defaultRequest {
            header("Accept", "application/json")
            header("User-Agent", "Ktor client")
        }
    }

    private const val API_KEY = "CG-Z1ASMjuxEc3b5c5Z5Fyjvj3K"

    suspend fun getCoins(
        vsCurrency: String = "usd",
        order: String = "market_cap_desc",
        perPage: Int = 12,
        page: Int = 1
    ): List<Coin> {
        val response = client.get("https://api.coingecko.com/api/v3/coins/markets") {
            parameter("vs_currency", vsCurrency)
            parameter("order", order)
            parameter("per_page", perPage)
            parameter("page", page)
            parameter("sparkline", "false")
            parameter("price_change_percentage", "24h")
            parameter("x_cg_demo_api_key", API_KEY)
        }
        return response.body()
    }

    suspend fun getMarketChart(
        coinId: String,
        vsCurrency: String = "usd",
        days: String = "7"
    ): MarketChart {
        val response = client.get("https://api.coingecko.com/api/v3/coins/$coinId/market_chart") {
            parameter("vs_currency", vsCurrency)
            parameter("days", days)
            parameter("x_cg_demo_api_key", API_KEY)
        }
        return response.body()
    }
}
