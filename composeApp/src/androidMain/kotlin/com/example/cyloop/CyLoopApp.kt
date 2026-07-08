package com.example.cyloop

import android.app.Application
import android.content.Context

class CyLoopApp : Application() {
    companion object {
        lateinit var instance: CyLoopApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
