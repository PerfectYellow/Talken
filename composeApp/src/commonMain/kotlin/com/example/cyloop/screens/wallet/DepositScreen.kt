package com.example.cyloop.screens.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyloop.components.QRCodeView
import com.example.cyloop.font.UIFont
import com.example.cyloop.theme.getAppBackgroundBrush

import com.example.cyloop.api.SolanaNetwork
import com.example.cyloop.api.SolanaService
import androidx.compose.foundation.shape.CircleShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositScreen(
    onBackClick: () -> Unit = {},
    walletAddress: String
) {
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val currentNetwork by SolanaService.currentNetwork.collectAsState()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Receive SOL", style = UIFont.ChatName) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(getAppBackgroundBrush())
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // QR Code
                Surface(
                    modifier = Modifier
                        .size(260.dp)
                        .clip(RoundedCornerShape(32.dp)),
                    color = Color.White,
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(20.dp)) {
                        QRCodeView(
                            data = walletAddress,
                            size = 220.dp,
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Scan this QR code to receive SOL or SPL tokens in your wallet.",
                    style = UIFont.Metadata,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Wallet Address Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Your Public Address",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = walletAddress,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                        IconButton(
                            onClick = { 
                                clipboardManager.setText(AnnotatedString(walletAddress))
                                // Note: We should ideally use the app's floating notification, 
                                // but for now a snackbar is fine or we can use FloatingNotification if we pass its state.
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                Surface(
                    color = if (currentNetwork == SolanaNetwork.MAINNET) 
                        Color(0xFF14F195).copy(alpha = 0.1f) 
                    else 
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (currentNetwork == SolanaNetwork.MAINNET) Color(0xFF14F195) else Color(0xFFF5D45E)
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Only send Solana ${currentNetwork.displayName} assets to this address.",
                            style = UIFont.Badge.copy(fontSize = 11.sp),
                            color = if (currentNetwork == SolanaNetwork.MAINNET) Color(0xFF14F195) else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewDepositScreen() {
    MaterialTheme {
        DepositScreen(walletAddress = "CV1vESFrRPhXdZVtG7vcvitnmYgXBoxbzasb9po4UaC")
    }
}
