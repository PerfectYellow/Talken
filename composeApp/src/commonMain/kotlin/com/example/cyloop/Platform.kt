package com.example.cyloop

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform