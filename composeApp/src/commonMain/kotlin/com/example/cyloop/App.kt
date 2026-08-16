package com.example.cyloop

import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.example.cyloop.theme.CyLoopTheme
import com.example.cyloop.nav.rememberNavController
import com.example.cyloop.nav.NavHost

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.cyloop.api.SolanaNetwork
import com.example.cyloop.api.SolanaService
import com.example.cyloop.storage.AuthPreferences
import kotlinx.coroutines.flow.first

@Composable
@Preview
fun App() {
    var isInitialized by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        // Initialize network from preferences before showing any UI
        val networkName = AuthPreferences.getSolanaNetwork().first()
        val network = try {
            SolanaNetwork.valueOf(networkName)
        } catch (e: Exception) {
            SolanaNetwork.DEVNET
        }
        SolanaService.setNetwork(network)
        isInitialized = true
    }

    if (!isInitialized) {
        // Show a clean background or splash while loading preferences
        Box(modifier = Modifier.fillMaxSize().background(com.example.cyloop.theme.getAppBackgroundBrush()))
        return
    }

    CyLoopTheme(darkTheme = true) {
        val navController = rememberNavController()
        NavHost(navController = navController)
    }
}
