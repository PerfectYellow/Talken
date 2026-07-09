package com.example.cyloop.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
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
    private const val API_KEY = "fa6fea5cb6e1f100cad6"
    private const val API_SECRET = "50b6fffeccf6d7a2825237f4734cc3bb2bc702fa476a1e4c2451be34bc08489c"
    private const val JWT = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySW5mb3JtYXRpb24iOnsiaWQiOiI4MzY5M2I1Ni00OTBmLTQwMzItYmI3Yi1lNmNiNTM4YTdmMGUiLCJlbWFpbCI6Im5lby5tb2hhbW1hZC5hZnNoYXJAZ21haWwuY29tIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsInBpbl9wb2xpY3kiOnsicmVnaW9ucyI6W3siZGVzaXJlZFJlcGxpY2F0aW9uQ291bnQiOjEsImlkIjoiRlJBMSJ9LHsiZGVzaXJlZFJlcGxpY2F0aW9uQ291bnQiOjEsImlkIjoiTllDMSJ9XSwidmVyc2lvbiI6MX0sIm1mYV9lbmFibGVkIjpmYWxzZSwic3RhdHVzIjoiQUNUSVZFIn0sImF1dGhlbnRpY2F0aW9uVHlwZSI6InNjb3BlZEtleSIsInNjb3BlZEtleUtleSI6ImZhNmZlYTVjYjZlMWYxMDBjYWQ2Iiwic2NvcGVkS2V5U2VjcmV0IjoiNTBiNmZmZmVjY2Y2ZDdhMjgyNTIzN2Y0NzM0Y2MzYmIyYmM3MDJmYTQ3NmExZTRjMjQ1MWJlMzRiYzA4NDg5YyIsImV4cCI6MTgxNTE0NDg2NH0.c46dZaO74Kh27Uw2ZzXhsP65ihdQJPEWxoD0i7QE6xs"

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

    suspend fun uploadFile(fileBytes: ByteArray, fileName: String): String {
        val response = client.post("https://api.pinata.cloud/pinning/pinFileToIPFS") {
            header("Authorization", "Bearer $JWT")
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
        val response = client.get("https://api.pinata.cloud/data/pinList") {
            header("Authorization", "Bearer $JWT")
            parameter("status", "pinned")
        }
        return response.body<PinListResponse>().rows ?: emptyList()
    }

    suspend fun deletePinnedFile(hash: String) {
        client.delete("https://api.pinata.cloud/pinning/unpin/$hash") {
            header("Authorization", "Bearer $JWT")
        }
    }

    fun getIpfsUrl(hash: String): String {
        return "https://gateway.pinata.cloud/ipfs/$hash"
    }
}
