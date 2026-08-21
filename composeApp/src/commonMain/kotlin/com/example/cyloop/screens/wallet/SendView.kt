package com.example.cyloop.screens.wallet

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.style.TextOverflow
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
    
    var isSending by remember { mutableStateOf(false) }
    var transactionSignature by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    val notificationState = rememberNotificationState()
    val clipboardManager = LocalClipboardManager.current
    
    val solBalance by WalletManager.solBalance.collectAsState()

    if (showScanner) {
        QRScannerView(
            onCodeScanned = { code ->
                if (code.startsWith("solana:")) {
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
                    IconButton(onClick = onBackClick, enabled = !isSending) {
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
                    enabled = !isSending && transactionSignature == null,
                    shape = RoundedCornerShape(16.dp),
                    trailingIcon = {
                        IconButton(
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
                            enabled = !isSending && transactionSignature == null
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan QR")
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
                    enabled = !isSending && transactionSignature == null,
                    shape = RoundedCornerShape(16.dp),
                    suffix = {
                        TextButton(
                            onClick = { amount = solBalance.toString() },
                            enabled = !isSending && transactionSignature == null
                        ) {
                            Text("MAX")
                        }
                    },
                    placeholder = { Text("0.0") }
                )

                Spacer(modifier = Modifier.height(48.dp))

                if (transactionSignature == null) {
                    Button(
                        onClick = {
                            val amountDouble = amount.toDoubleOrNull()
                            if (amountDouble == null || amountDouble <= 0) {
                                scope.launch { notificationState.showNotification("Invalid amount", NotificationType.ERROR) }
                                return@Button
                            }

                            if (amountDouble > solBalance) {
                                scope.launch { notificationState.showNotification("Insufficient balance", NotificationType.ERROR) }
                                return@Button
                            }

                            scope.launch {
                                try {
                                    isSending = true
                                    notificationState.showNotification("Signing & Sending...", NotificationType.HINT)
                                    val signature = WalletManager.sendSOL(destinationAddress, amountDouble)
                                    transactionSignature = signature
                                    isSending = false
                                } catch (e: Exception) {
                                    isSending = false
                                    notificationState.showNotification(e.message ?: "Transaction failed", NotificationType.ERROR)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = destinationAddress.isNotBlank() && amount.isNotBlank() && !isSending
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Sending...")
                        } else {
                            Text("Send SOL", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }

                // Success Section
                AnimatedVisibility(
                    visible = transactionSignature != null,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(24.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(48.dp)
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text(
                                    text = "Transaction Sent Successfully!",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                Text(
                                    text = "Transaction Signature",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Row(
                                    modifier = Modifier
                                        .padding(top = 8.dp)
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                                        .clickable {
                                            transactionSignature?.let {
                                                clipboardManager.setText(AnnotatedString(it))
                                                scope.launch {
                                                    notificationState.showNotification("Signature copied!", NotificationType.HINT)
                                                }
                                            }
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = transactionSignature ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Button(
                            onClick = onBackClick,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Done", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Full-screen loading overlay to prevent actions
            if (isSending) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.1f))
                        .clickable(enabled = false) {} // Intercept clicks
                )
            }

            FloatingNotification(
                state = notificationState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}
