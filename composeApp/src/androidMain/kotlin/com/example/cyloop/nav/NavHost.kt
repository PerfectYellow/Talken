// commonMain/kotlin/com/example/cyloop/nav/NavHost.kt
package com.example.cyloop.nav

import androidx.compose.runtime.Composable
import com.example.cyloop.screens.auth.WelcomeScreen
import com.example.cyloop.screens.main.TabBarView
import com.example.cyloop.screens.profile.PasscodeLockScreen
import com.example.cyloop.screens.chat.ChatDetailScreen
import com.example.cyloop.screens.chat.NewChatScreen
import com.example.cyloop.screens.chat.UserInfoScreen
import com.example.cyloop.screens.wallet.WalletInfoView
import com.example.cyloop.screens.wallet.PaymentInfoView
import com.example.cyloop.screens.wallet.NewTransactionView

@Composable
fun NavHost(
    navController: NavController,
    startDestination: Route = Route.Welcome
) {
    val currentRoute = navController.currentRoute

    // Ensure we start at the correct route if backstack is empty
    if (navController.isBackStackEmpty()) {
        navController.navigateAndClearStack(startDestination)
    }

    when (val route = currentRoute) {
        is Route.Welcome -> {
            WelcomeScreen(
                onSignInClick = {
                    navController.navigate(Route.TabView(tab = "profile"))
                },
                onMainNetClick = { /* Navigate to MainNet screen */ },
                onDevNetClick = { /* Navigate to DevNet screen */ }
            )
        }

        is Route.TabView -> {
            TabBarView(
                initialTab = route.tab,
                onTabSelected = { newTab ->
                    navController.replaceCurrent(Route.TabView(tab = newTab))
                },
                onSignOut = {
                    navController.navigateAndClearStack(Route.Welcome)
                },
                onNavigateToPasscodeLock = {
                    navController.navigate(Route.PasscodeLock)
                },
                onNavigateToChatDetail = { id, name ->
                    navController.navigate(Route.ChatDetail(id, name))
                },
                onNavigateToNewChat = {
                    navController.navigate(Route.NewChat)
                },
                onNavigateToWalletDetail = {
                    navController.navigate(Route.WalletDetail)
                },
                onNavigateToPayment = {
                    navController.navigate(Route.Payment)
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

        is Route.ChatDetail -> {
            ChatDetailScreen(
                chatId = route.chatId,
                chatName = route.chatName,
                onBackClick = {
                    navController.goBack()
                },
                onUserInfoClick = {
                    navController.navigate(Route.UserInfo(route.chatName))
                }
            )
        }

        is Route.UserInfo -> {
            UserInfoScreen(
                chatName = route.chatName,
                onBackClick = {
                    navController.goBack()
                }
            )
        }

        is Route.NewChat -> {
            NewChatScreen(
                onBackClick = {
                    navController.goBack()
                },
                onContactSelected = { id, name ->
                    // Navigate to detail and pop the NewChat screen
                    navController.navigate(Route.ChatDetail(id, name), popUpTo = Route.TabView())
                }
            )
        }

        is Route.Detail -> {
            // DetailScreen(id = route.id, onBack = { navController.goBack() })
        }

        is Route.WalletDetail -> {
            WalletInfoView(
                onBackClick = {
                    navController.goBack()
                }
            )
        }

        is Route.Payment -> {
            PaymentInfoView(
                onBackClick = {
                    navController.goBack()
                },
                onNewTransactionClick = {
                    navController.navigate(Route.NewTransactionRoute)
                }
            )
        }

        is Route.NewTransactionRoute -> {
            NewTransactionView(
                onBackClick = {
                    navController.goBack()
                }
            )
        }
    }
}
