package com.example.cyloop.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.draw.blur
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
    onNavigateToNewChat: () -> Unit,
    onNavigateToWalletDetail: () -> Unit = {},
    onNavigateToPayment: () -> Unit = {}
) {
    var selectedScreen by remember(initialTab) { 
        mutableStateOf(BottomNavScreen.fromRoute(initialTab)) 
    }

    Scaffold(
        modifier = Modifier.background(Color.Transparent),
        bottomBar = {
            FloatingBottomNavigationBar(
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
                .background(
                    Color(0xFFE3F2FD)
                )
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
                BottomNavScreen.Wallet -> WalletScreen(
                    onWalletDetailClick = onNavigateToWalletDetail,
                    onPaymentClick = onNavigateToPayment
                )
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
fun FloatingBottomNavigationBar(
    selectedScreen: BottomNavScreen,
    onScreenSelected: (BottomNavScreen) -> Unit
) {
    val items = listOf(
        BottomNavScreen.Chats,
        BottomNavScreen.Flow,
        BottomNavScreen.Wallet,
        BottomNavScreen.Profile,
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 14.dp)
            .padding(bottom = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(40.dp),
                    spotColor = Color.Black.copy(alpha = 0.2f)
                )
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.92f),
                            Color.White.copy(alpha = 0.82f)
                        )
                    ),
                    shape = RoundedCornerShape(40.dp)
                )
                .border(
                    width = 0.5.dp,
                    color = Color.White.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(40.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { screen ->
                FloatingNavItem(
                    screen = screen,
                    isSelected = selectedScreen == screen,
                    onClick = { onScreenSelected(screen) }
                )
            }
        }
    }
}

@Composable
fun FloatingNavItem(
    screen: BottomNavScreen,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val contentColor = if (isSelected) Color(0xFF007AFF) else Color(0xFF8E8E93)
    val backgroundColor = if (isSelected) Color(0xFF007AFF).copy(alpha = 0.12f) else Color.Transparent
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 2.dp)
    ) {
        Icon(
            imageVector = if (isSelected) screen.selectedIcon else screen.icon,
            contentDescription = screen.title,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = screen.title,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = contentColor,
            letterSpacing = 0.2.sp
        )
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
