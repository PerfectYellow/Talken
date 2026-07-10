package com.example.cyloop.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.cyloop.font.UIFont
import kotlinx.coroutines.delay

enum class NotificationType {
    ERROR, WARNING, HINT, INFO
}

data class NotificationData(
    val message: String,
    val type: NotificationType,
    val duration: Long = 3000L
)

class NotificationState {
    var notificationData by mutableStateOf<NotificationData?>(null)
        private set

    fun showNotification(message: String, type: NotificationType, duration: Long = 3000L) {
        notificationData = NotificationData(message, type, duration)
    }

    fun dismiss() {
        notificationData = null
    }
}

@Composable
fun rememberNotificationState() = remember { NotificationState() }

@Composable
fun FloatingNotification(
    state: NotificationState,
    modifier: Modifier = Modifier
) {
    val data = state.notificationData
    val isVisible = data != null

    val backgroundColor = when (data?.type) {
        NotificationType.ERROR -> Color(0xFFE53935)
        NotificationType.WARNING -> Color(0xFFFFA000)
        NotificationType.HINT -> Color(0xFF43A047)
        NotificationType.INFO -> Color(0xFF1E88E5)
        null -> Color.Transparent
    }

    val icon = when (data?.type) {
        NotificationType.ERROR -> Icons.Default.Error
        NotificationType.WARNING -> Icons.Default.Warning
        NotificationType.HINT -> Icons.Default.Lightbulb
        NotificationType.INFO -> Icons.Default.Info
        null -> Icons.Default.Info
    }

    LaunchedEffect(data) {
        if (data != null) {
            delay(data.duration)
            state.dismiss()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .zIndex(100f)
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = backgroundColor,
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = data?.message ?: "",
                        color = Color.White,
                        style = UIFont.ChatMessage,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { state.dismiss() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
