package com.example.cyloop.storage

expect class SecureStorage() {
    fun saveString(key: String, value: String)
    fun getString(key: String): String?
    fun delete(key: String)
    fun clear()
    
    fun savePassword(password: String)
    fun getPassword(): String?
}
