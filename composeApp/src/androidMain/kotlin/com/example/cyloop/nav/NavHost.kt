// commonMain/kotlin/com/example/cyloop/nav/NavHost.kt
package com.example.cyloop.nav

import androidx.compose.runtime.Composable
import com.example.cyloop.screens.auth.WelcomeScreen
import com.example.cyloop.screens.main.TabBarView
import com.example.cyloop.screens.profile.PasscodeLockScreen

@Composable
fun NavHost(
    navController: NavController,
    startDestination: Route = Route.Welcome
) {
    val currentRoute = navController.currentRoute
    println("NavHost: currentRoute = $currentRoute")

    // Ensure we start at the correct route if backstack is empty
    if (navController.isBackStackEmpty()) {
        navController.navigateAndClearStack(startDestination)
    }

    when (val route = currentRoute) {
        is Route.Welcome -> {
            WelcomeScreen(
                onSignInClick = {
                    navController.navigate(Route.TabView)
                },
                onMainNetClick = { /* Navigate to MainNet screen */ },
                onDevNetClick = { /* Navigate to DevNet screen */ }
            )
        }

        is Route.TabView -> {
            TabBarView(
                onSignOut = {
                    navController.navigateAndClearStack(Route.Welcome)
                },
                onNavigateToPasscodeLock = {
                    navController.navigate(Route.PasscodeLock)
                }
            )
        }

        is Route.PasscodeLock -> {
            PasscodeLockScreen(
                onBackClick = {
                    navController.goBack()
                }
            )
        }

        is Route.Detail -> {
            // DetailScreen(id = route.id, onBack = { navController.goBack() })
        }
    }
}
