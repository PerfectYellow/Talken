package com.example.cyloop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.activity.OnBackPressedCallback
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.compose.material3.darkColorScheme
import com.example.cyloop.theme.CyLoopTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.cyloop.screens.auth.WelcomeScreen
import com.example.cyloop.screens.main.TabBarView
import com.example.cyloop.nav.rememberNavController
import androidx.fragment.app.FragmentActivity
import com.example.cyloop.nav.NavHost
import com.example.cyloop.nav.Route
import com.example.cyloop.storage.AuthPreferences
import com.example.cyloop.storage.ContactPreferences
import com.example.cyloop.storage.IpfsPreferences
import com.example.cyloop.storage.createDataStore

class MainActivity : FragmentActivity() {
    private var backPressedTime: Long = 0
    private val BACK_PRESS_INTERVAL = 2000 // 2 seconds
    private lateinit var navControllerRef: com.example.cyloop.nav.NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
        
        // Initialize Preferences
        AuthPreferences.init(createDataStore(this, "auth_prefs"))
        ContactPreferences.init(createDataStore(this, "contact_prefs"))
        IpfsPreferences.init(createDataStore(this, "ipfs_prefs"))

        setContent {
            CyLoopTheme(darkTheme = true) {
                // Create the navigation controller
                val navController = rememberNavController()
                navControllerRef = navController

                // Handle system back button and gesture navigation
                onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        handleBackNavigation(navController)
                    }
                })

                // Let NavHost handle all routing
                NavHost(navController = navController)
            }
        }
    }

    // Override onBackPressed to catch gesture-based navigation on older Android versions
    // Suppressed because: We support both OnBackPressedDispatcher (modern) and onBackPressed (legacy)
    // This ensures compatibility across all Android versions and navigation methods
    @Deprecated("Deprecated in Java")
    @Suppress("MissingSuperCall", "GestureBackNavigation")
    override fun onBackPressed() {
        handleBackNavigation(navControllerRef)
    }

    private fun handleBackNavigation(navController: com.example.cyloop.nav.NavController) {
        when (val currentRoute = navController.currentRoute) {
            is Route.TabView -> {
                // Double-tap to exit logic
                val currentTime = System.currentTimeMillis()
                if (currentTime - backPressedTime < BACK_PRESS_INTERVAL) {
                    // Second tap within interval - exit app
                    finish()
                } else {
                    // First tap - show toast and update timestamp
                    backPressedTime = currentTime
                    Toast.makeText(
                        this@MainActivity,
                        "Press back again to exit",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            else -> {
                // For other routes, navigate back normally
                if (!navController.goBack()) {
                    // If can't go back in navigation stack, allow default behavior (exit app)
                    super.onBackPressed()
                }
            }
        }
    }
}
