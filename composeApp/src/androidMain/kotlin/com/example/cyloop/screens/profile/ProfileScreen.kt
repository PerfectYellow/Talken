// ProfileScreen.kt
package com.example.cyloop.screens.profile

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.tooling.preview.Preview
import com.example.cyloop.screens.profile.ProfileMenuItem
//import com.example.cyloop.rememberNavController

@Composable
fun ProfileScreen(
    userName: String = "kaay",
    userBalance: String = "0.0 Sol",
    walletAddress: String = "9xQq...3fGt",
    qrCodeBitmap: Any? = null, // Replace with actual QR code bitmap/image
    onPasscodeLockClick: () -> Unit,
    onManageLogsClick: () -> Unit,
    onExportWalletClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onQrCodeClick: () -> Unit,
    onCopyAddressClick: () -> Unit,
    onSignOut: () -> Unit // Keep for backward compatibility
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF5F5F5)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            // Header Section with QR Code
            ProfileHeaderWithQR(
                userName = userName,
                userBalance = userBalance,
                walletAddress = walletAddress,
                qrCodeBitmap = qrCodeBitmap,
                onQrCodeClick = onQrCodeClick,
                onCopyAddressClick = onCopyAddressClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Menu Items Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .padding(vertical = 8.dp)
            ) {
                ProfileMenuItem(
                    icon = Icons.Default.Lock,
                    title = "Passcode Lock",
                    onClick = onPasscodeLockClick
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    thickness = 0.5.dp,
                    color = Color(0xFFEEEEEE)
                )

                ProfileMenuItem(
                    icon = Icons.Default.List,
                    title = "Manage Logs",
                    onClick = onManageLogsClick
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    thickness = 0.5.dp,
                    color = Color(0xFFEEEEEE)
                )

                ProfileMenuItem(
                    icon = Icons.Default.Publish,
                    title = "Export Wallet",
                    onClick = onExportWalletClick
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    thickness = 0.5.dp,
                    color = Color(0xFFEEEEEE)
                )

                ProfileMenuItem(
                    icon = Icons.Default.Logout,
                    title = "Logout",
                    onClick = onLogoutClick
                )
            }
        }
    }
}

@Composable
fun ProfileHeaderWithQR(
    userName: String,
    userBalance: String,
    walletAddress: String,
    qrCodeBitmap: Any?,
    onQrCodeClick: () -> Unit,
    onCopyAddressClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color(0xFF1976D2)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, bottom = 32.dp, start = 20.dp, end = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    modifier = Modifier.size(45.dp),
                    tint = Color(0xFF1976D2)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Username
            Column() {
                Text(
                    text = userName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Balance
                Text(
                    text = "Balance: $userBalance",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // QR Code and Wallet Address Row
            Row(
                modifier = Modifier
                    .background(
                        Color.White.copy(alpha = 0.15f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // QR Code Box
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .clickable { onQrCodeClick() }
                        .border(1.dp, Color.White, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (qrCodeBitmap != null) {
                        // Replace with actual QR code image composable
                        // Image(bitmap = qrCodeBitmap, contentDescription = "QR Code")
                        IconButton(
                            onClick = onCopyAddressClick,
                            modifier = Modifier.size(25.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Copy Address",
                                modifier = Modifier.size(30.dp),
                                tint = Color.Black
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "QR Code",
                            modifier = Modifier.size(60.dp),
                            tint = Color(0xFF1976D2)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProfileScreen() {
    MaterialTheme {
        ProfileScreen(
            "John",
            "0.0 $",
            "sssss",
            {},
            {},
            {},
            {},
            {},
            {},
            {},
            {},
        )
    }
}