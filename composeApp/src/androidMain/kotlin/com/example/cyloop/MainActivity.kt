package com.example.cyloop

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.activity.OnBackPressedCallback
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.cyloop.theme.CyLoopTheme
import com.example.cyloop.theme.getAppBackgroundBrush
import com.example.cyloop.nav.rememberNavController
import androidx.fragment.app.FragmentActivity
import com.example.cyloop.nav.NavHost
import com.example.cyloop.nav.Route
import com.example.cyloop.api.SolanaNetwork
import com.example.cyloop.api.SolanaService
import com.example.cyloop.storage.AuthPreferences
import com.example.cyloop.storage.ContactPreferences
import com.example.cyloop.storage.IpfsPreferences
import com.example.cyloop.storage.createDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {
    companion object {
        var currentActivity: MainActivity? = null
            private set
    }

    private var backPressedTime: Long = 0
    private val BACK_PRESS_INTERVAL = 2000 // 2 seconds
    private var navControllerRef: com.example.cyloop.nav.NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
        currentActivity = this
        
        // Initialize Preferences
        AuthPreferences.init(createDataStore(this, "auth_prefs"))
        ContactPreferences.init(createDataStore(this, "contact_prefs"))
        IpfsPreferences.init(createDataStore(this, "ipfs_prefs"))

        setContent {
            val savedNetworkName by AuthPreferences.getSolanaNetwork().collectAsState(initial = null)
            
            if (savedNetworkName == null) {
                // Show splash while loading network preference
                Box(modifier = Modifier.fillMaxSize().background(getAppBackgroundBrush()))
            } else {
                // Once loaded, set the network and show the app
                val network = remember(savedNetworkName) {
                    try {
                        SolanaNetwork.valueOf(savedNetworkName!!)
                    } catch (e: Exception) {
                        SolanaNetwork.DEVNET
                    }
                }
                
                // Update global service
                LaunchedEffect(network) {
                    SolanaService.setNetwork(network)
                }

                CyLoopTheme(darkTheme = true) {
                    val navController = rememberNavController()
                    navControllerRef = navController

                    // Handle system back button
                    DisposableEffect(navController) {
                        val callback = object : OnBackPressedCallback(true) {
                            override fun handleOnBackPressed() {
                                handleBackNavigation(navController)
                            }
                        }
                        onBackPressedDispatcher.addCallback(callback)
                        onDispose { callback.remove() }
                    }

                    NavHost(navController = navController)
                }
            }
        }
    }

    override fun onDestroy() {
        if (currentActivity == this) currentActivity = null
        super.onDestroy()
    }

    private fun handleBackNavigation(navController: com.example.cyloop.nav.NavController) {
        when (val currentRoute = navController.currentRoute) {
            is Route.TabView -> {
                val currentTime = System.currentTimeMillis()
                if (currentTime - backPressedTime < BACK_PRESS_INTERVAL) {
                    finish()
                } else {
                    backPressedTime = currentTime
                    Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                if (!navController.goBack()) {
                    onBackPressedDispatcher.onBackPressed() // Fallback to system
                }
            }
        }
    }
}
