package com.example.cyloop.storage

actual suspend fun getLegacyPassword(context: Any?): String? {
    // No legacy password on iOS if it was Android-only
    return null
}
