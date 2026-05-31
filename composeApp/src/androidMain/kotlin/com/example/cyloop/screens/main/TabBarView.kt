package com.example.cyloop.screens.main

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.tooling.preview.Preview
import com.example.cyloop.screens.chat.ChatScreen
import com.example.cyloop.screens.flow.FlowScreen
import com.example.cyloop.screens.profile.ProfileScreen
import com.example.cyloop.screens.wallet.WalletScreen
import com.example.cyloop.theme.getAppBackgroundBrush

sealed class BottomNavScreen(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    object Profile : BottomNavScreen("profile", "Profile", Icons.Outlined.Person, Icons.Filled.Person)
    object Wallet : BottomNavScreen("wallet", "Wallet", Icons.Outlined.AccountBalanceWallet, Icons.Filled.AccountBalanceWallet)
    object Flow : BottomNavScreen("flow", "Flow", Icons.Outlined.BarChart, Icons.Filled.BarChart)
    object Chats : BottomNavScreen("chats", "Chats", Icons.Outlined.QuestionAnswer, Icons.Filled.QuestionAnswer)

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

    val bottomBarHeight = 90.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(getAppBackgroundBrush())
            .padding(bottom = 3.dp)
    ) {
        // Content area
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Crossfade(targetState = selectedScreen, animationSpec = tween(400)) { screen ->
                when (screen) {
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
                        onPasscodeLockClick = onNavigateToPasscodeLock,
                        bottomPadding = bottomBarHeight
                    )
                    BottomNavScreen.Wallet -> WalletScreen(
                        onWalletDetailClick = onNavigateToWalletDetail,
                        onPaymentClick = onNavigateToPayment,
                        bottomPadding = bottomBarHeight
                    )
                    BottomNavScreen.Flow -> FlowScreen(
                        bottomPadding = bottomBarHeight
                    )
                    BottomNavScreen.Chats -> ChatScreen(
                        onChatClick = onNavigateToChatDetail,
                        onNewChatClick = onNavigateToNewChat,
                        bottomPadding = bottomBarHeight
                    )
                }
            }
        }

        // Floating Bottom Navigation Bar matching image
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 16.dp) // Added padding to avoid system navigation bar overlap
        ) {
            FloatingBottomNavigationBar(
                selectedScreen = selectedScreen,
                onScreenSelected = {
                    selectedScreen = it
                    onTabSelected(it.route)
                }
            )
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
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp) // Shrunk height
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(32.dp),
                    spotColor = Color.Black
                )
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(32.dp)
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
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
    val animatedColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF8E8E93),
        animationSpec = tween(300)
    )
    
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1.0f,
        animationSpec = tween(300)
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
            .scale(animatedScale)
            .width(52.dp) // Shrunk width
    ) {
        Icon(
            imageVector = if (isSelected) screen.selectedIcon else screen.icon,
            contentDescription = screen.title,
            tint = animatedColor,
            modifier = Modifier.size(20.dp) // Shrunk icon
        )
        
        Spacer(modifier = Modifier.height(2.dp))
        
        Text(
            text = screen.title,
            fontSize = 11.sp, // Shrunk font size
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = animatedColor
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
