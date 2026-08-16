package com.example.cyloop.screens.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyloop.crypto.WalletManager
import com.example.cyloop.font.UIFont
import com.example.cyloop.theme.getAppBackgroundBrush
import kotlinx.coroutines.launch
import com.example.cyloop.components.FloatingNotification
import com.example.cyloop.components.NotificationType
import com.example.cyloop.components.rememberNotificationState
import com.example.cyloop.components.QRScannerView
import com.example.cyloop.utils.hasCameraPermission
import com.example.cyloop.utils.requestCameraPermission
import com.example.cyloop.utils.openAppSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendView(
    onBackClick: () -> Unit,
    fromAddress: String
) {
    var destinationAddress by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var showScanner by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val notificationState = rememberNotificationState()
    
    val solBalance by WalletManager.solBalance.collectAsState()

    if (showScanner) {
        QRScannerView(
            onCodeScanned = { code ->
                if (code.startsWith("solana:")) {
                    // Format: solana:<address>?amount=<amount>
                    val parts = code.removePrefix("solana:").split("?")
                    destinationAddress = parts.firstOrNull() ?: ""
                    
                    if (parts.size > 1) {
                        val queryParams = parts[1].split("&")
                        val amountParam = queryParams.find { it.startsWith("amount=") }
                        amountParam?.let {
                            amount = it.removePrefix("amount=")
                        }
                    }
                } else {
                    destinationAddress = code
                }
                
                showScanner = false
                scope.launch {
                    notificationState.showNotification("Address scanned!", NotificationType.HINT)
                }
            },
            onDismiss = { showScanner = false }
        )
        return
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Camera Permission Required") },
            text = { Text("To scan QR codes, CyLoop needs access to your camera. Please enable it in system settings.") },
            confirmButton = {
                Button(onClick = {
                    showPermissionDialog = false
                    openAppSettings()
                }) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Cancel")
                }
            },
            icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Send SOL", style = UIFont.ChatName) },
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
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Balance Info
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Available Balance",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${solBalance} SOL",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Destination Address Input
                OutlinedTextField(
                    value = destinationAddress,
                    onValueChange = { destinationAddress = it },
                    label = { Text("Destination Address") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    trailingIcon = {
                        FilledIconButton(
                            onClick = {
                                if (hasCameraPermission()) {
                                    showScanner = true
                                } else {
                                    requestCameraPermission()
                                    scope.launch {
                                        kotlinx.coroutines.delay(500)
                                        if (!hasCameraPermission()) {
                                            showPermissionDialog = true
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.padding(end = 8.dp).size(40.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(
                                Icons.Default.QrCodeScanner, 
                                contentDescription = "Scan QR",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    placeholder = { Text("Enter Solana address...") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Amount Input
                OutlinedTextField(
                    value = amount,
                    onValueChange = { 
                        if (it.isEmpty() || it.toDoubleOrNull() != null) {
                            amount = it 
                        }
                    },
                    label = { Text("Amount (SOL)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    suffix = {
                        TextButton(onClick = { amount = solBalance.toString() }) {
                            Text("MAX")
                        }
                    },
                    placeholder = { Text("0.0") }
                )

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = {
                        if (destinationAddress.isBlank() || amount.isBlank()) {
                            scope.launch {
                                notificationState.showNotification("Please fill all fields", NotificationType.ERROR)
                            }
                            return@Button
                        }
                        
                        // Transaction Logic would go here
                        scope.launch {
                            notificationState.showNotification("Transaction initiated to ${destinationAddress.take(4)}...", NotificationType.HINT)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = destinationAddress.isNotBlank() && amount.isNotBlank()
                ) {
                    Text("Continue", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            FloatingNotification(
                state = notificationState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}
