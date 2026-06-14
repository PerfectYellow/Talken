package com.example.cyloop.screens.flow

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyloop.font.UIFont
import com.example.cyloop.theme.CyLoopTheme

@Composable
fun FlowCell(
    flowItem: PostItem,
    modifier: Modifier = Modifier,
) {
    val isDark = true //isSystemInDarkTheme()

    fun getColorForPostId(postId: String): Color {
        val colors = listOf(
            Color(0xFFA18BFF), // Purple
            Color(0xFFF5D45E), // Gold
            Color(0xFF81C784), // Green
            Color(0xFF64B5F6), // Blue
            Color(0xFFFF8A65), // Coral
            Color(0xFFBA68C8), // Lavender
            Color(0xFFFFD54F), // Amber
            Color(0xFF4DB6AC), // Teal
            Color(0xFFE57373), // Light Red
            Color(0xFF9575CD), // Deep Purple
            Color(0xFF4FC3F7), // Light Blue
            Color(0xFFFF8A80), // Light Red
            Color(0xFFAED581), // Light Green
            Color(0xFFFFB74D)  // Orange
        )

        val index = postId.hashCode().mod(colors.size)
        return colors[index]
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .then(
                if (!isDark) {
                    Modifier.shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(24.dp),
                        spotColor = Color.Black.copy(alpha = 0.1f),
                        ambientColor = Color.Black.copy(alpha = 0.05f)
                    )
                } else Modifier
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isDark) {
                Color.White.copy(alpha = 0.08f)
            } else {
                Color.Black.copy(alpha = 0.05f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Row: Avatar, Username, Time Left
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar (Placeholder)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(getColorForPostId(flowItem.postId))
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = flowItem.author.username,
                    style = UIFont.ChatName.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.weight(1f)
                )

                // Time Left Pill
                Surface(
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (flowItem.postId == "1") "34h left" else "12h left",
                            style = UIFont.Badge.copy(
                                color = MaterialTheme.colorScheme.secondary
                            )
                        )
                    }
                }
            }

            // Content
            Text(
                text = flowItem.content.text,
                style = UIFont.ChatMessage.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            // PDA Tag
            Surface(
                color = if (isDark) Color.Black.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.05f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "PDA: ${flowItem.signature.take(3)}...${flowItem.signature.takeLast(4)}",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = UIFont.Metadata.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                )
            }

            // Bottom Row: Backed amount and Back Flow button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Backed: ",
                        style = UIFont.Body.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = if (flowItem.postId == "1") "4.82 SOL" else "12.40 SOL",
                        style = UIFont.Body.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                Button(
                    onClick = { /* TODO */ },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Back Flow",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FlowCellPreview() {
    CyLoopTheme(darkTheme = true) {
        FlowCell(
            flowItem = PostItem(
                postId = "7",
                signature = "Yza...99nW",
                author = Author(
                    wallet = "sol_dev_dao_wallet",
                    username = "solana_devs.sol",
                    avatar = null
                ),
                content = Content(
                    text = "Workshop tomorrow: 'Build Your First Solana Smart Contract in Rust' - Free for all DAO members. 500 participants already registered! 🦀"
                ),
                stats = Stats(likes = 789, comments = 145),
                createdAt = System.currentTimeMillis() - 345600000 // 4 days ago
            )
        )
    }
}