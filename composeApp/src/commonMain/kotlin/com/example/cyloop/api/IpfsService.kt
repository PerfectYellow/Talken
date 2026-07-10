package com.example.cyloop.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import com.example.cyloop.storage.IpfsPreferences
import kotlinx.coroutines.flow.first
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class PinataResponse(
    @SerialName("IpfsHash") val ipfsHash: String,
    @SerialName("PinSize") val pinSize: Long,
    @SerialName("Timestamp") val timestamp: String,
)

@Serializable
data class PinListResponse(
    val count: Int? = 0,
    val rows: List<PinItem>? = emptyList()
)

@Serializable
data class PinItem(
    val id: String? = null,
    @SerialName("ipfs_pin_hash") val ipfsPinHash: String = "",
    val size: Long? = 0,
    @SerialName("date_pinned") val datePinned: String? = null,
    val metadata: PinMetadata? = null
)

@Serializable
data class PinMetadata(
    val name: String? = null
)

object IpfsService {
    private val client = HttpClient {
        expectSuccess = true
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 60000
        }
    }

    private suspend fun getConfig() = IpfsPreferences.getConfig().first()

    suspend fun uploadFile(fileBytes: ByteArray, fileName: String): String {
        val config = getConfig()
        val response = client.post("${config.baseUrl}/pinning/pinFileToIPFS") {
            header("Authorization", "Bearer ${config.jwt}")
            setBody(MultiPartFormDataContent(
                formData {
                    append("file", fileBytes, Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                    })
                    // Add Pinata Metadata so the name shows up in the list
                    append("pinataMetadata", "{\"name\":\"$fileName\"}")
                }
            ))
        }
        return response.body<PinataResponse>().ipfsHash
    }

    suspend fun getPinnedFiles(): List<PinItem> {
        val config = getConfig()
        val response = client.get("${config.baseUrl}/data/pinList") {
            header("Authorization", "Bearer ${config.jwt}")
            parameter("status", "pinned")
        }
        return response.body<PinListResponse>().rows ?: emptyList()
    }

    suspend fun deletePinnedFile(hash: String) {
        val config = getConfig()
        client.delete("${config.baseUrl}/pinning/unpin/$hash") {
            header("Authorization", "Bearer ${config.jwt}")
        }
    }

    fun getIpfsUrl(hash: String): String {
        return "https://gateway.pinata.cloud/ipfs/$hash"
    }
}
