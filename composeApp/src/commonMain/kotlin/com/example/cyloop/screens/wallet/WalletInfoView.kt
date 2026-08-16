package com.example.cyloop.screens.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.cyloop.crypto.WalletManager
import com.example.cyloop.crypto.WalletInfo
import com.example.cyloop.components.FloatingNotification
import com.example.cyloop.components.NotificationType
import com.example.cyloop.components.rememberNotificationState
import io.github.vinceglb.filekit.compose.rememberFileSaverLauncher
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.sp
import com.example.cyloop.api.HeliusService
import com.example.cyloop.api.CoinGeckoService
import com.example.cyloop.format
import kotlin.math.pow

import com.example.cyloop.api.SolanaNetwork
import com.example.cyloop.api.SolanaService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletInfoView(
    successMessage: String? = null,
    onBackClick: () -> Unit,
    onDepositClick: (String) -> Unit = {},
    onSendClick: (String) -> Unit = {}
) {
    val address by WalletManager.walletAddress.collectAsState()
    val wallets by WalletManager.wallets.collectAsState()
    // Use allWallets to find the active one if it's currently filtered out (shouldn't happen with logic update)
    val allWallets by WalletManager.allWallets.collectAsState()
    val activeWallet = allWallets.find { it.address == address }
    val currentNetwork by SolanaService.currentNetwork.collectAsState()
    
    val scope = rememberCoroutineScope()
    val notificationState = rememberNotificationState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var walletToRename by remember { mutableStateOf<WalletInfo?>(null) }
    var newWalletName by remember { mutableStateOf("") }
    
    var showMenu by remember { mutableStateOf(false) }
    var showWalletSwitcher by remember { mutableStateOf(false) }
    var showAddWalletOverlay by remember { mutableStateOf(false) }
    var initialOnboardingStep by remember { mutableStateOf(OnboardingStep.WELCOME) }
    val clipboardManager = LocalClipboardManager.current

    // Use Shared State from WalletManager
    val solBalance by WalletManager.solBalance.collectAsState()
    val usdcBalance by WalletManager.usdcBalance.collectAsState()
    val solPrice by WalletManager.solPrice.collectAsState()
    val isLoading by WalletManager.isRefreshing.collectAsState()
    val usdcPrice = 1.0 // USDC is stable
    
    var hasShownSuccessMessage by rememberSaveable(successMessage) { mutableStateOf(false) }

    LaunchedEffect(address) {
        if (address != null) {
            WalletManager.refreshBalances()
        }
    }

    LaunchedEffect(successMessage) {
        if (!successMessage.isNullOrBlank() && !hasShownSuccessMessage) {
            notificationState.showNotification(successMessage, NotificationType.HINT)
            hasShownSuccessMessage = true
        }
    }

    val saverLauncher = rememberFileSaverLauncher { _ ->
        scope.launch {
            notificationState.showNotification("Backup saved successfully!", NotificationType.HINT)
        }
    }
    
    if (showRenameDialog && walletToRename != null) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Wallet") },
            text = {
                OutlinedTextField(
                    value = newWalletName,
                    onValueChange = { newWalletName = it },
                    label = { Text("Wallet Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newWalletName.isNotBlank()) {
                            WalletManager.renameWallet(walletToRename!!.address, newWalletName)
                            showRenameDialog = false
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeleteDialog) {
        BasicAlertDialog(
            onDismissRequest = { showDeleteDialog = false }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = CircleShape,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Remove Wallet?",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "This will remove '${activeWallet?.name ?: "this wallet"}' from this device. You will lose access unless you have your recovery phrase or private key saved elsewhere.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showDeleteDialog = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancel")
                        }
                        
                        Button(
                            onClick = {
                                scope.launch {
                                    WalletManager.deleteWallet()
                                    showDeleteDialog = false
                                    if (!WalletManager.hasWallet()) {
                                        onBackClick()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Remove")
                        }
                    }
                }
            }
        }
    }

    if (showWalletSwitcher) {
        ModalBottomSheet(
            onDismissRequest = { showWalletSwitcher = false },
            dragHandle = { BottomSheetDefaults.DragHandle() },
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Manage Wallets",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
                
                wallets.forEach { wallet ->
                    val isActive = wallet.address == address
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                WalletManager.switchWallet(wallet.address)
                                showWalletSwitcher = false
                            },
                        color = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) 
                                else Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = wallet.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${wallet.address.take(8)}...${wallet.address.takeLast(8)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            if (isActive) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Active",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                            
                            IconButton(
                                onClick = {
                                    walletToRename = wallet
                                    newWalletName = wallet.name
                                    showRenameDialog = true
                                }
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Rename",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                TextButton(
                    onClick = {
                        showWalletSwitcher = false
                        initialOnboardingStep = OnboardingStep.WELCOME
                        showAddWalletOverlay = true
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Another Wallet")
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showWalletSwitcher = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                modifier = Modifier.size(30.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    activeWallet?.name ?: "My Wallet",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (currentNetwork == SolanaNetwork.MAINNET) 
                                                    Color(0xFF14F195) // Solana Green
                                                else 
                                                    Color(0xFFF5D45E) // Gold/Yellow
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        currentNetwork.displayName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp).padding(start = 4.dp)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
//                                if (wallets.size > 1) {
//                                    DropdownMenuItem(
//                                        text = { Text("Switch Wallet") },
//                                        leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null) },
//                                        onClick = {
//                                            showMenu = false
//                                            showWalletSwitcher = true
//                                        }
//                                    )
//                                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
//                                }
//                                DropdownMenuItem(
//                                    text = { Text("Create Another Wallet") },
//                                    leadingIcon = { Icon(Icons.Default.AddCircleOutline, contentDescription = null) },
//                                    onClick = {
//                                        showMenu = false
//                                        initialOnboardingStep = OnboardingStep.CREATE_GENERATE
//                                        showAddWalletOverlay = true
//                                    }
//                                )
//                                DropdownMenuItem(
//                                    text = { Text("Import Another Wallet") },
//                                    leadingIcon = { Icon(Icons.Default.FileUpload, contentDescription = null) },
//                                    onClick = {
//                                        showMenu = false
//                                        initialOnboardingStep = OnboardingStep.WELCOME
//                                        showAddWalletOverlay = true
//                                    }
//                                )
//                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                DropdownMenuItem(
                                    text = { Text("Save Backup") },
                                    leadingIcon = { Icon(Icons.Default.Save, contentDescription = null) },
                                    onClick = {
                                        showMenu = false
                                        val mnemonic = WalletManager.getMnemonic() ?: "No mnemonic found"
                                        val privateKey = WalletManager.getPrivateKey() ?: ""
                                        val content = "Wallet Backup\n\nName: ${activeWallet?.name}\nAddress: $address\nRecovery Phrase: $mnemonic\nPrivate Key: $privateKey"
                                        saverLauncher.launch(
                                            bytes = content.encodeToByteArray(),
                                            baseName = "cyloop_wallet_backup_${activeWallet?.name?.replace(" ", "_")}",
                                            extension = "txt"
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Remove Account", color = MaterialTheme.colorScheme.error) },
                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        showMenu = false
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .background(MaterialTheme.colorScheme.background),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Main Balance Card with Gradient
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Total Balance",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(32.dp).padding(4.dp)
                                )
                            } else {
                                val totalValue = (solBalance * solPrice) + (usdcBalance * usdcPrice)
                                Text(
                                    text = "$${totalValue.format(2)}",
                                    style = MaterialTheme.typography.displayMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = (-1).sp
                                    ),
                                    color = Color.White
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Surface(
                                color = Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        address?.let {
                                            clipboardManager.setText(AnnotatedString(it))
                                            scope.launch {
                                                notificationState.showNotification("Address copied to clipboard", NotificationType.HINT)
                                            }
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = address?.let { "${it.take(12)}...${it.takeLast(12)}" } ?: "No Wallet",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Assets Section
                SectionHeader(title = "Your Assets")
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssetItem(
                        name = "Solana",
                        symbol = "SOL",
                        balance = solBalance.format(4),
                        value = "$${(solBalance * solPrice).format(2)}",
                        icon = Icons.Default.CurrencyBitcoin, // Placeholder
                        color = Color(0xFF14F195)
                    )
                    AssetItem(
                        name = "USD Coin",
                        symbol = "USDC",
                        balance = usdcBalance.format(2),
                        value = "$${(usdcBalance * usdcPrice).format(2)}",
                        icon = Icons.Default.MonetizationOn,
                        color = Color(0xFF2775CA)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Actions
                SectionHeader(title = "Quick Actions")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionButton(
                        icon = Icons.Default.SouthWest,
                        label = "Receive",
                        onClick = { 
                            address?.let { onDepositClick(it) }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionButton(
                        icon = Icons.Default.NorthEast,
                        label = "Send",
                        onClick = { 
                            address?.let { onSendClick(it) }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
        
        FloatingNotification(
            state = notificationState,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        if (showAddWalletOverlay) {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                WalletOnboardingFlow(
                    initialStep = initialOnboardingStep,
                    onFinish = { msg ->
                        showAddWalletOverlay = false
                        if (msg != null) {
                            scope.launch {
                                notificationState.showNotification(msg, NotificationType.HINT)
                            }
                        }
                    },
                    onBack = { showAddWalletOverlay = false }
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.SemiBold
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
fun AssetItem(
    name: String,
    symbol: String,
    balance: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$balance $symbol",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "0.00%",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}
