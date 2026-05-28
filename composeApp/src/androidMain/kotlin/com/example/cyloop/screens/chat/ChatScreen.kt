package com.example.cyloop.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import java.util.Date
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import cyloop.composeapp.generated.resources.Res
import cyloop.composeapp.generated.resources.person2
import org.jetbrains.compose.resources.painterResource

data class Chat(
    val id: String,
    val name: String,
    val lastMessage: String,
    val time: Date,
    val unreadCount: Int,
    val avatarColor: Color,
    val isOnline: Boolean = false
)

@Composable
fun ChatScreen(
    onChatClick: (String, String) -> Unit,
    onNewChatClick: () -> Unit,
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    // Sample data - in real app, this would come from ViewModel
    val chats = remember {
        mutableStateListOf(
            Chat(
                id = "1",
                name = "Alice Johnson",
                lastMessage = "Hey! How are you doing?",
                time = Date(),
                unreadCount = 3,
                avatarColor = Color(0xFF4CAF50),
                isOnline = true
            ),
            Chat(
                id = "2",
                name = "Bob Smith",
                lastMessage = "See you tomorrow at 5pm",
                time = Date(System.currentTimeMillis() - 3600000),
                unreadCount = 0,
                avatarColor = Color(0xFF2196F3),
                isOnline = false
            ),
            Chat(
                id = "3",
                name = "Carol Davis",
                lastMessage = "Thanks for the help! 👍",
                time = Date(System.currentTimeMillis() - 7200000),
                unreadCount = 1,
                avatarColor = Color(0xFFFF9800),
                isOnline = true
            ),
            Chat(
                id = "4",
                name = "David Wilson",
                lastMessage = "Did you check the document?",
                time = Date(System.currentTimeMillis() - 86400000),
                unreadCount = 0,
                avatarColor = Color(0xFF9C27B0),
                isOnline = false
            ),
            Chat(
                id = "5",
                name = "Emma Brown",
                lastMessage = "Let's catch up this weekend! 🎉",
                time = Date(System.currentTimeMillis() - 172800000),
                unreadCount = 5,
                avatarColor = Color(0xFFF44336),
                isOnline = true
            )
        )
    }

    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val pullToRefreshState = rememberPullToRefreshState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE8F5E9),  // Top
                        Color(0xFFE3F2FD)   // Bottom
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding()
        ) {
            // Header with title
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Chats",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D47A1)
                )
            }

            // Pull-to-refresh enabled chat list
            PullToRefreshBox(
                state = pullToRefreshState,
                isRefreshing = isRefreshing,
                onRefresh = {
                    coroutineScope.launch {
                        isRefreshing = true
                        // Simulate network request or data refresh
                        delay(1500) // Replace with actual refresh logic

                        // Example: Update chat times to simulate refresh
                        val updatedChats = chats.mapIndexed { index, chat ->
                            if (index == 0) {
                                chat.copy(
                                    lastMessage = "Just refreshed! ✅",
                                    time = Date(),
                                    unreadCount = (chat.unreadCount + 1) % 10
                                )
                            } else {
                                chat
                            }
                        }
                        chats.clear()
                        chats.addAll(updatedChats)

                        isRefreshing = false
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            // Apply a stretch effect based on pull-to-refresh distance
                            val scale = 1f + (pullToRefreshState.distanceFraction * 0.1f).coerceAtMost(0.15f)
                            scaleY = scale
                            transformOrigin = TransformOrigin(0.5f, 0f)
                        },
                    contentPadding = PaddingValues(top = 16.dp, start = 16.dp, end = 16.dp, bottom = bottomPadding + 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(chats) { chat ->
                        ChatCell(
                            chat = chat,
                            onClick = { onChatClick(chat.id, chat.name) }
                        )
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = onNewChatClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = bottomPadding + 16.dp, end = 16.dp)
                .size(56.dp),
            shape = CircleShape,
            containerColor = Color(0xFF2196F3),
            contentColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "New Chat",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun ChatCell(
    chat: Chat,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.15f)
            )
            .background(
                Color.White.copy(alpha = 0.95f),
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar with online indicator
        Box(
            modifier = Modifier.size(52.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(chat.avatarColor),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(Res.drawable.person2),
                    contentDescription = chat.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
//                Text(
//                    text = chat.name.take(1).uppercase(),
//                    fontSize = 20.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = Color.White
//                )
            }

            // Online indicator
//            if (chat.isOnline) {
//                Box(
//                    modifier = Modifier
//                        .align(Alignment.BottomEnd)
//                        .size(14.dp)
//                        .clip(CircleShape)
//                        .background(Color(0xFF4CAF50))
//                        .padding(2.dp)
//                )
//            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Chat info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chat.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A1A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = formatTime(chat.time),
                    fontSize = 11.sp,
                    color = Color(0xFF888888)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chat.lastMessage,
                    fontSize = 13.sp,
                    color = if (chat.unreadCount > 0) Color(0xFF1A1A1A) else Color(0xFF666666),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = if (chat.unreadCount > 0) FontWeight.Medium else FontWeight.Normal,
                    modifier = Modifier.weight(1f)
                )

//                if (chat.unreadCount > 0) {
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Box(
//                        modifier = Modifier
//                            .size(20.dp)
//                            .clip(CircleShape)
//                            .background(Color(0xFF2196F3)),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text(
//                            text = if (chat.unreadCount > 9) "9+" else chat.unreadCount.toString(),
//                            fontSize = 11.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = Color.White
//                        )
//                    }
//                }
            }
        }
    }
}

private fun formatTime(date: Date): String {
    val now = Date()
    val diff = now.time - date.time

    return when {
        diff < 3600000 -> { // Less than 1 hour
            val minutes = (diff / 60000).toInt()
            if (minutes < 1) "Just now" else "${minutes}m"
        }
        diff < 86400000 -> { // Less than 24 hours
            val hours = (diff / 3600000).toInt()
            "${hours}h"
        }
        diff < 172800000 -> { // Yesterday
            "Yesterday"
        }
        else -> {
            val pattern = java.text.SimpleDateFormat("MM/dd", java.util.Locale.getDefault())
            pattern.format(date)
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