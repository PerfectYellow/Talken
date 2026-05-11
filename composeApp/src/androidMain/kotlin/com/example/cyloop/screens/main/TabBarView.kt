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


// Sealed class for screen navigation
sealed class BottomNavScreen(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    object Home : BottomNavScreen("home", "Profile", Icons.Default.Person, Icons.Default.Person)
    object Wallet : BottomNavScreen("wallet", "Wallet", Icons.Default.Wallet, Icons.Default.Wallet)
    object Flow : BottomNavScreen("flow", "Flow", Icons.Default.AreaChart, Icons.Default.AreaChart)
    object Chats : BottomNavScreen("chats", "Chats", Icons.Default.ChatBubbleOutline, Icons.Default.ChatBubble)
}

@Composable
fun TabBarView(
    onSignOut: () -> Unit
) {
    var selectedScreen by remember { mutableStateOf<BottomNavScreen>(BottomNavScreen.Home) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedScreen = selectedScreen,
                onScreenSelected = {
                        selectedScreen = it
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
                BottomNavScreen.Home -> ProfileScreen(onSignOut = onSignOut)
                BottomNavScreen.Wallet -> WalletScreen()
                BottomNavScreen.Flow -> FlowScreen()
                BottomNavScreen.Chats -> ChatScreen()
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
            BottomNavScreen.Home,
            BottomNavScreen.Wallet,
            BottomNavScreen.Flow,
            BottomNavScreen.Chats
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
        TabBarView(onSignOut = {})
    }
}