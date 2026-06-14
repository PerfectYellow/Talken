package com.example.cyloop.screens.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyloop.font.UIFont
import com.example.cyloop.theme.getAppBackgroundBrush
import cyloop.composeapp.generated.resources.Res
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import java.util.Date

data class Chat(
    val id: String,
    val name: String,
    val lastMessage: String,
    val time: Date,
    val unreadCount: Int,
    val avatarColor: Color,
)

@Composable
fun ChatScreen(
    onChatClick: (String, String) -> Unit,
    onNewChatClick: () -> Unit,
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    // Sample data updated to match image
    val chats = remember {
        mutableStateListOf(
            Chat(
                id = "1",
                name = "Alex Crypto",
                lastMessage = "Sent you 1.5 SOL for lunch",
                time = Date(System.currentTimeMillis() - 3600000), // 1 hour ago
                unreadCount = 1,
                avatarColor = Color(0xFFB39DDB), // Purple
            ),
            Chat(
                id = "2",
                name = "Mia SNS Handle",
                lastMessage = "swap request is completed!",
                time = Date(System.currentTimeMillis() - 7200000), // 2 hours ago
                unreadCount = 0,
                avatarColor = Color(0xFFF5D45E), // Gold
            ),
            Chat(
                id = "3",
                name = "Dinner Group Split",
                lastMessage = "Invoice Split: $75.00 Due",
                time = Date(System.currentTimeMillis() - 86400000), // 1 day ago
                unreadCount = 0,
                avatarColor = Color(0xFF90A4AE), // Gray
            ),
            Chat(
                id = "4",
                name = "Sarah DeFi",
                lastMessage = "LP rewards are ready to claim! 🎉",
                time = Date(System.currentTimeMillis() - 43200000), // 12 hours ago
                unreadCount = 3,
                avatarColor = Color(0xFF81C784), // Green
            ),
            Chat(
                id = "5",
                name = "Web3 Collective",
                lastMessage = "New proposal: Increase treasury allocation",
                time = Date(System.currentTimeMillis() - 172800000), // 2 days ago
                unreadCount = 12,
                avatarColor = Color(0xFF64B5F6), // Blue
            ),
            Chat(
                id = "6",
                name = "James NFT",
                lastMessage = "Your bid on 'Cosmic Ape #42' was accepted!",
                time = Date(System.currentTimeMillis() - 21600000), // 6 hours ago
                unreadCount = 0,
                avatarColor = Color(0xFFFF8A65), // Coral
            ),
            Chat(
                id = "7",
                name = "Solana Stakers",
                lastMessage = "Staking APY increased to 7.2%",
                time = Date(System.currentTimeMillis() - 5400000), // 1.5 hours ago
                unreadCount = 2,
                avatarColor = Color(0xFFBA68C8), // Lavender
            ),
            Chat(
                id = "8",
                name = "Rachel (Merchant)",
                lastMessage = "Payment received: 0.5 SOL for coffee ☕",
                time = Date(System.currentTimeMillis() - 10800000), // 3 hours ago
                unreadCount = 0,
                avatarColor = Color(0xFFFFD54F), // Amber
            ),
            Chat(
                id = "9",
                name = "DAO Treasury Bot",
                lastMessage = "Weekly report: +124 SOL in fees",
                time = Date(System.currentTimeMillis() - 259200000), // 3 days ago
                unreadCount = 0,
                avatarColor = Color(0xFF4DB6AC), // Teal
            ),
            Chat(
                id = "10",
                name = "Chris (Support)",
                lastMessage = "Ticket #5042 has been resolved ✅",
                time = Date(System.currentTimeMillis() - 14400000), // 4 hours ago
                unreadCount = 0,
                avatarColor = Color(0xFFE57373), // Light Red
            ),
            Chat(
                id = "11",
                name = "Elena Web3",
                lastMessage = "Thanks for the NFT! 🎨",
                time = Date(System.currentTimeMillis() - 7200000), // 2 hours ago
                unreadCount = 0,
                avatarColor = Color(0xFF9575CD), // Deep Purple
            ),
            Chat(
                id = "12",
                name = "Validator Node",
                lastMessage = "Your delegation rewards: 0.23 SOL",
                time = Date(System.currentTimeMillis() - 21600000), // 6 hours ago
                unreadCount = 0,
                avatarColor = Color(0xFF4FC3F7), // Light Blue
            ),
            Chat(
                id = "13",
                name = "Metaverse Group",
                lastMessage = "Land auction starts tomorrow at 3PM UTC",
                time = Date(System.currentTimeMillis() - 3600000), // 1 hour ago
                unreadCount = 5,
                avatarColor = Color(0xFFFF8A80), // Light Red
            ),
            Chat(
                id = "14",
                name = "Staking Pool",
                lastMessage = "New pool APY: 8.5% for SOL",
                time = Date(System.currentTimeMillis() - 86400000), // 1 day ago
                unreadCount = 0,
                avatarColor = Color(0xFFAED581), // Light Green
            ),
            Chat(
                id = "15",
                name = "Dev DAO",
                lastMessage = "Hackathon winners announced! 🏆",
                time = Date(System.currentTimeMillis() - 129600000), // 1.5 days ago
                unreadCount = 2,
                avatarColor = Color(0xFFFFB74D), // Orange
            )
        )
    }

    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val pullToRefreshState = rememberPullToRefreshState()
    val scrollState = rememberLazyListState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(getAppBackgroundBrush())
    ) {
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding()
        ) {
            // Header with title and New Chat button at top right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Chats",
                    style = UIFont.LargeTitle,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                IconButton(
                    onClick = onNewChatClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "New Chat",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Pull-to-refresh enabled chat list
            PullToRefreshBox(
                state = pullToRefreshState,
                isRefreshing = isRefreshing,
                onRefresh = {
                    coroutineScope.launch {
                        isRefreshing = true
                        delay(1500)
                        isRefreshing = false
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    state = scrollState,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            val scale = 1f + (pullToRefreshState.distanceFraction * 0.1f).coerceAtMost(0.15f)
                            scaleY = scale
                            transformOrigin = TransformOrigin(0.5f, 0f)
                        },
                    contentPadding = PaddingValues(top = 16.dp, start = 16.dp, end = 16.dp, bottom = bottomPadding + 32.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    items(
                        items = chats,
                        key = { chat -> chat.id }
                    ) { chat ->
                        ChatCell(
                            chat = chat,
                            onClick = { onChatClick(chat.id, chat.name) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatCell(
    chat: Chat,
    onClick: () -> Unit
) {
    val isDark = true //isSystemInDarkTheme()
    val borderColor =
        if (isDark) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.04f)

    val cellBackground =
        MaterialTheme.colorScheme.surface

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp), // Extra breathing room
        shape = RoundedCornerShape(20.dp),
        color = cellBackground,
        tonalElevation = if (isDark) 4.dp else 0.dp,
        border = BorderStroke( 1.dp, borderColor),
        shadowElevation = if (isDark) 0.dp else 4.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with letter
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(chat.avatarColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = chat.name.take(1).uppercase(),
                    style = UIFont.AvatarLabel,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Chat info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = chat.name,
                    style = UIFont.ChatName,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = chat.lastMessage,
                    style = UIFont.ChatMessage,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            if (chat.unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = chat.unreadCount.toString(),
                        style = UIFont.Badge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewChatScreen() {
    MaterialTheme {
        ChatScreen(onChatClick = { _, _ -> }, onNewChatClick = {})
    }
}
