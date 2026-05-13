package com.example.cyloop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.OnBackPressedCallback
import android.widget.Toast
import androidx.compose.material3.MaterialTheme
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

class MainActivity : FragmentActivity() {
    private var backPressedTime: Long = 0
    private val BACK_PRESS_INTERVAL = 2000 // 2 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                // Create the navigation controller
                val navController = rememberNavController()

                // Handle system back button
                onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
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
                                    isEnabled = false
                                    onBackPressedDispatcher.onBackPressed()
                                }
                            }
                        }
                    }
                })

                // Let NavHost handle all routing
                NavHost(navController = navController)
            }
        }
    }
}
