package com.example.cyloop.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.cyloop.font.UIFont
import com.example.cyloop.theme.getAppBackgroundBrush

@Composable
fun ProfileScreen(
    userName: String = "kaay",
    userBalance: String = "0.0 Sol",
    walletAddress: String = "9xQq...3fGt",
    qrCodeBitmap: Any? = null,
    onPasscodeLockClick: () -> Unit,
    onUploadContentClick: () -> Unit,
    onManageLogsClick: () -> Unit,
    onExportWalletClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onQrCodeClick: () -> Unit,
    onCopyAddressClick: () -> Unit,
    onSignOut: () -> Unit,
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(getAppBackgroundBrush())
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = bottomPadding + 32.dp)
        ) {
            // Header Section
            ProfileHeaderWithQRElegant(
                userName = userName,
                userBalance = userBalance,
                walletAddress = walletAddress,
                qrCodeBitmap = qrCodeBitmap,
                onQrCodeClick = onQrCodeClick,
                onCopyAddressClick = onCopyAddressClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Menu Items Section Grouped
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(28.dp)
                    ),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    ProfileMenuItemModernSurface(
                        icon = Icons.Default.Lock,
                        title = "Passcode Lock",
                        onClick = onPasscodeLockClick
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    )

                    ProfileMenuItemModernSurface(
                        icon = Icons.Default.CloudUpload,
                        title = "Upload Content",
                        onClick = onUploadContentClick
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    )

                    ProfileMenuItemModernSurface(
                        icon = Icons.AutoMirrored.Filled.List,
                        title = "Manage Logs",
                        onClick = onManageLogsClick
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    )

                    ProfileMenuItemModernSurface(
                        icon = Icons.Default.Publish,
                        title = "Export Wallet",
                        onClick = onExportWalletClick
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    )

                    ProfileMenuItemModernSurface(
                        icon = Icons.AutoMirrored.Filled.Logout,
                        title = "Logout",
                        onClick = onLogoutClick
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewProfileScreen() {
    MaterialTheme {
        ProfileScreen(
            onPasscodeLockClick = {},
            onUploadContentClick = {},
            onManageLogsClick = {},
            onExportWalletClick = {},
            onLogoutClick = {},
            onQrCodeClick = {},
            onCopyAddressClick = {},
            onSignOut = {}
        )
    }
}
