package com.example.cyloop.screens.main

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
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
    onNavigateToChatDetail: (String, String, String?, String?) -> Unit,
    onNavigateToNewChat: () -> Unit,
    onNavigateToUploadContent: () -> Unit = {},
    onNavigateToWalletDetail: () -> Unit = {},
    onNavigateToPayment: () -> Unit = {}
) {
    var selectedScreen by remember(initialTab) { 
        mutableStateOf(BottomNavScreen.fromRoute(initialTab)) 
    }
    val saveableStateHolder = rememberSaveableStateHolder()

    val isDark = true // isSystemInDarkTheme()
    val shadowColor = if (isDark) Color.Black else Color.White

    // Calculate dynamic padding for the bottom safe area (gesture pill or buttons)
    val navigationBarsPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val bottomBarHeight = 90.dp + navigationBarsPadding

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(getAppBackgroundBrush())
    ) {
        // Content area
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            Crossfade(targetState = selectedScreen, animationSpec = tween(400)) { screen ->
                saveableStateHolder.SaveableStateProvider(screen.route) {
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
                            onUploadContentClick = onNavigateToUploadContent,
                            bottomPadding = bottomBarHeight
                        )
                        BottomNavScreen.Wallet -> WalletScreen(
                            onWalletDetailClick = onNavigateToWalletDetail,
                            onPaymentClick = onNavigateToPayment,
                            bottomPadding = bottomBarHeight - 24.dp
                        )
                        BottomNavScreen.Flow -> FlowScreen(
                            bottomPadding = bottomBarHeight
                        )
                        BottomNavScreen.Chats -> ChatScreen(
                            onChatClick = { id, name, imageUrl, nftAddress ->
                                onNavigateToChatDetail(id, name, imageUrl, nftAddress)
                            },
                            onNewChatClick = onNavigateToNewChat,
                            bottomPadding = bottomBarHeight
                        )
                    }
                }
            }
        }

        // Floating Bottom Navigation Bar with a strong bottom-of-screen shadow to obscure content
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(70.dp + navigationBarsPadding) // Accounts for system navigation buttons height
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            shadowColor.copy(alpha = 0.2f),
                            shadowColor.copy(alpha = 0.95f)
                        ),
                        startY = 0f
                    )
                )
                .padding(bottom = navigationBarsPadding), // Keeps the bar 19dp above the system buttons
            contentAlignment = Alignment.BottomCenter
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
    val isDark = isSystemInDarkTheme()
    val shadowColor = if (isDark) Color.Black else Color.White

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
        // Dedicated shadow layer with offset to emphasize the shadow below the bar
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(42.dp)
                .offset(y = 10.dp)
                .shadow(
                    elevation = 25.dp,
                    shape = RoundedCornerShape(32.dp),
                    spotColor = shadowColor.copy(alpha = 0.75f),
                    ambientColor = shadowColor.copy(alpha = 0.5f)
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp) // Slightly more compact height
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(32.dp),
                    spotColor = shadowColor.copy(alpha = 0.1f)
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
                .padding(vertical = 2.dp), // Inner padding
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
            .scale(animatedScale)
            .width(52.dp)
    ) {
        Icon(
            imageVector = if (isSelected) screen.selectedIcon else screen.icon,
            contentDescription = screen.title,
            tint = animatedColor,
            modifier = Modifier.size(25.dp)
        )
        
        Spacer(modifier = Modifier.height(0.dp))
        
        Text(
            text = screen.title,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = animatedColor,
            style = TextStyle(
                lineHeight = 10.sp
            )
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
            onNavigateToChatDetail = { _, _, _, _ -> },
            onNavigateToNewChat = {}
        )
    }
}
