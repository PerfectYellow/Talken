package com.example.cyloop.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun QRScannerView(
    modifier: Modifier = Modifier,
    onCodeScanned: (String) -> Unit,
    onDismiss: () -> Unit
)
