package com.example.cyloop.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyloop.theme.getAppBackgroundBrush

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserInfoScreen(
    chatName: String,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* More actions */ }) {
                        Icon(
                            Icons.Default.MoreVert, 
                            contentDescription = "More",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(getAppBackgroundBrush())
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Large Profile Image
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = chatName.take(1).uppercase(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Name
            Text(
                text = chatName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Status
            Text(
                text = "online",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Additional Info sections (like Telegram)
            InfoItem(label = "Wallet", value = "34xp4vRoCGJym3xR7yCVPFHoCNxv4Twseo")
            InfoItem(label = "Mobile", value = "+1 234 567 8900")
            InfoItem(label = "Username", value = "@${chatName.lowercase().replace(" ", "_")}")
            InfoItem(label = "Bio", value = "Available")
        }
    }
}

@Composable
fun InfoItem(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Text(
            text = value,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HorizontalDivider(
            modifier = Modifier.padding(top = 12.dp), 
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewUserInfoScreen() {
    MaterialTheme {
        UserInfoScreen("Alice Johnson", {})
    }
}
