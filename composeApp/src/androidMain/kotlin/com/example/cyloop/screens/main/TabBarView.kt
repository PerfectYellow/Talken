package com.example.cyloop.screens.main

import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.tooling.preview.Preview
import com.example.cyloop.screens.chat.ChatScreen
import com.example.cyloop.screens.flow.FlowScreen
import com.example.cyloop.screens.profile.ProfileScreen
import com.example.cyloop.screens.wallet.WalletScreen

sealed class BottomNavScreen(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    object Profile : BottomNavScreen("profile", "Profile", Icons.Default.Person, Icons.Default.Person)
    object Wallet : BottomNavScreen("wallet", "Wallet", Icons.Default.Wallet, Icons.Default.Wallet)
    object Flow : BottomNavScreen("flow", "Flow", Icons.Default.AreaChart, Icons.Default.AreaChart)
    object Chats : BottomNavScreen("chats", "Chats", Icons.Default.ChatBubbleOutline, Icons.Default.ChatBubble)

    companion object {
        fun fromRoute(route: String): BottomNavScreen {
            return when (route) {
                "chats" -> Chats
                "flow" -> Flow
                "wallet" -> Wallet
                else -> Profile
            }
        }
    }
}

@Composable
fun TabBarView(
    initialTab: String,
    onTabSelected: (String) -> Unit,
    onSignOut: () -> Unit,
    onNavigateToPasscodeLock: () -> Unit,
    onNavigateToChatDetail: (String, String) -> Unit,
    onNavigateToNewChat: () -> Unit
) {
    var selectedScreen by remember(initialTab) { 
        mutableStateOf(BottomNavScreen.fromRoute(initialTab)) 
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedScreen = selectedScreen,
                onScreenSelected = {
                    selectedScreen = it
                    onTabSelected(it.route)
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedScreen) {
                BottomNavScreen.Profile -> ProfileScreen(
                    onSignOut = onSignOut,
                    userName = "kaay",
                    userBalance = "0.0 Sol",
                    qrCodeBitmap = null,
                    onLogoutClick = onSignOut,
                    onQrCodeClick = {},
                    walletAddress = "9xQq...3fGt",
                    onManageLogsClick = {},
                    onCopyAddressClick = {},
                    onExportWalletClick = {},
                    onPasscodeLockClick = onNavigateToPasscodeLock
                )
                BottomNavScreen.Wallet -> WalletScreen()
                BottomNavScreen.Flow -> FlowScreen()
                BottomNavScreen.Chats -> ChatScreen(
                    onChatClick = onNavigateToChatDetail,
                    onNewChatClick = onNavigateToNewChat
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    selectedScreen: BottomNavScreen,
    onScreenSelected: (BottomNavScreen) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            BottomNavScreen.Chats,
            BottomNavScreen.Flow,
            BottomNavScreen.Wallet,
            BottomNavScreen.Profile,
        )

        items.forEach { screen ->
            NavigationBarItem(
                selected = selectedScreen == screen,
                onClick = { onScreenSelected(screen) },
                icon = {
                    Icon(
                        imageVector = if (selectedScreen == screen) screen.selectedIcon else screen.icon,
                        contentDescription = screen.title,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = screen.title,
                        fontSize = 12.sp,
                        fontWeight = if (selectedScreen == screen) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF1976D2),
                    selectedTextColor = Color(0xFF1976D2),
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color(0xFFE3F2FD)
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTabBarView() {
    MaterialTheme {
        TabBarView(
            initialTab = "profile",
            onTabSelected = {},
            onSignOut = {}, 
            onNavigateToPasscodeLock = {},
            onNavigateToChatDetail = { _, _ -> },
            onNavigateToNewChat = {}
        )
    }
}
