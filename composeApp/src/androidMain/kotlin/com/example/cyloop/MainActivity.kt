package com.example.cyloop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.cyloop.screens.auth.SignInScreen
import com.example.cyloop.screens.auth.WelcomeScreen
import com.example.cyloop.screens.main.TabBarView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                MyApp()
            }
        }
    }
}

enum class Screen {
    WELCOME,
    SIGN_IN,
    TAB_VIEW;

    companion object {
        fun fromRoute(route: String?): Screen {
            return when (route) {
                "welcome" -> WELCOME
                "sign_in" -> SIGN_IN
                "tab_view" -> TAB_VIEW
                else -> WELCOME
            }
        }
    }

    fun toRoute(): String = when (this) {
        WELCOME -> "welcome"
        SIGN_IN -> "sign_in"
        TAB_VIEW -> "tab_view"
    }
}

class NavController(
    private val currentScreenState: MutableState<Screen>
) {
    var currentScreen: Screen
        get() = currentScreenState.value
        set(value) {
            currentScreenState.value = value
        }

    fun navigateTo(screen: Screen) {
        currentScreen = screen
    }

    fun goBack() {
        when (currentScreen) {
            Screen.SIGN_IN -> currentScreen = Screen.WELCOME
            Screen.TAB_VIEW -> currentScreen = Screen.SIGN_IN
            else -> { }
        }
    }
}

@Composable
fun rememberNavController(): NavController {
    val currentScreenState = remember { mutableStateOf(Screen.WELCOME) }
    return remember(currentScreenState) {
        NavController(currentScreenState)
    }
}

@Composable  // ← THIS IS MISSING!
fun MyApp() {
    val navController = rememberNavController()
    when (navController.currentScreen) {
        Screen.WELCOME -> {
            WelcomeScreen(
                onSignInClick = {
                    println("Navigation: Sign in clicked")
                    navController.navigateTo(Screen.SIGN_IN)
                },
                onMainNetClick = { /* TODO */ },
                onDevNetClick = { /* TODO */ }
            )
        }

        Screen.SIGN_IN -> {
            SignInScreen(
                onBackClick = {
                    println("Navigation: Back clicked")
                    navController.goBack()
                },
                onSignInSuccess = {
                    println("Navigation: Sign in success")
                    navController.navigateTo(Screen.TAB_VIEW)
                }
            )
        }

        Screen.TAB_VIEW -> {
            TabBarView(
                onSignOut = {
                    println("Navigation: Sign out")
                    navController.navigateTo(Screen.WELCOME)
                }
            )
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    MyApp()
}