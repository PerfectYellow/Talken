// commonMain/kotlin/com/example/cyloop/nav/NavHost.kt
package com.example.cyloop.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import com.example.cyloop.screens.auth.WelcomeScreen
import com.example.cyloop.screens.main.TabBarView
import com.example.cyloop.screens.profile.PasscodeLockScreen
import com.example.cyloop.screens.chat.ChatDetailScreen
import com.example.cyloop.screens.chat.NewChatScreen
import com.example.cyloop.screens.chat.UserInfoScreen
import com.example.cyloop.screens.wallet.WalletInfoView
import com.example.cyloop.screens.wallet.PaymentInfoView
import com.example.cyloop.screens.wallet.NewTransactionView
import com.example.cyloop.screens.wallet.BillMakerView
import com.example.cyloop.screens.upload.UploadContentScreen

@Composable
fun NavHost(
    navController: NavController,
    startDestination: Route = Route.Welcome
) {
    val currentRoute = navController.currentRoute
    val saveableStateHolder = rememberSaveableStateHolder()

    // Ensure we start at the correct route if backstack is empty
    if (navController.isBackStackEmpty()) {
        navController.navigateAndClearStack(startDestination)
    }

    // Use a unique key for the route to preserve state.
    // TabView should have the same key regardless of the tab to preserve inner tab states.
    val routeKey = when(currentRoute) {
        is Route.TabView -> "tab_view"
        is Route.ChatDetail -> "chat_detail_${currentRoute.chatId}"
        else -> currentRoute::class.simpleName ?: "default"
    }

    saveableStateHolder.SaveableStateProvider(routeKey) {
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
                    onNavigateToChatDetail = { id, name, imageUrl, nftAddress ->
                        navController.navigate(Route.ChatDetail(id, name, imageUrl, nftAddress))
                    },
                    onNavigateToNewChat = {
                        navController.navigate(Route.NewChat)
                    },
                    onNavigateToUploadContent = {
                        navController.navigate(Route.UploadContent)
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
                    imageUrl = route.imageUrl,
                    nftAddress = route.nftAddress,
                    onBackClick = {
                        navController.goBack()
                    },
                    onUserInfoClick = {
                        navController.navigate(Route.UserInfo(route.chatName, route.chatId, route.imageUrl, route.nftAddress))
                    }
                )
            }

            is Route.UserInfo -> {
                UserInfoScreen(
                    chatName = route.chatName,
                    walletAddress = route.walletAddress,
                    imageUrl = route.imageUrl,
                    nftAddress = route.nftAddress,
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
                    onContactSelected = { id, name, imageUrl, nftAddress ->
                        // Navigate to detail and pop the NewChat screen
                        navController.navigate(Route.ChatDetail(id, name, imageUrl, nftAddress), popUpTo = Route.TabView())
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
                    },
                    onBillMakerClick = {
                        navController.navigate(Route.BillMaker)
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

            is Route.BillMaker -> {
                BillMakerView(
                    onBackClick = {
                        navController.goBack()
                    }
                )
            }

            is Route.UploadContent -> {
                UploadContentScreen(
                    onBackClick = {
                        navController.goBack()
                    }
                )
            }
        }
    }
}
