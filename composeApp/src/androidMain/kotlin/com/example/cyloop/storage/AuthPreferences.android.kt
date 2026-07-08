package com.example.cyloop.storage

import android.content.Context

actual suspend fun getLegacyPassword(context: Any?): String? {
    if (context !is Context) return null
    val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    return prefs.getString("user_password", null)
}
