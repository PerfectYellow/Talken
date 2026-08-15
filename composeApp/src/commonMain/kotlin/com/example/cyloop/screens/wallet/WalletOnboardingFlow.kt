package com.example.cyloop.screens.wallet

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyloop.crypto.WalletManager
import com.example.cyloop.components.FloatingNotification
import com.example.cyloop.components.NotificationType
import com.example.cyloop.components.rememberNotificationState
import io.github.vinceglb.filekit.compose.rememberFileSaverLauncher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class OnboardingStep {
    WELCOME,
    CREATE_GENERATE,
    IMPORT_MNEMONIC,
    IMPORT_PRIVATE_KEY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletOnboardingFlow(
    initialStep: OnboardingStep = OnboardingStep.WELCOME,
    onFinish: (String?) -> Unit,
    onBack: () -> Unit
) {
    var currentStep by remember { mutableStateOf(initialStep) }
    var generatedMnemonic by remember { mutableStateOf<List<String>>(emptyList()) }
    var importText by remember { mutableStateOf("") }
    var walletName by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    val notificationState = rememberNotificationState()
    var showCreateConfirm by remember { mutableStateOf(false) }

    if (showCreateConfirm) {
        BasicAlertDialog(
            onDismissRequest = { showCreateConfirm = false }
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
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(
                            Icons.Default.AddCard,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Create New Wallet?",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "A new wallet will be generated immediately. Please ensure you are ready to write down your 12-word recovery phrase.",
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
                            onClick = { showCreateConfirm = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancel")
                        }
                        
                        Button(
                            onClick = {
                                showCreateConfirm = false
                                scope.launch {
                                    isLoading = true
                                    generatedMnemonic = WalletManager.createWallet()
                                    isLoading = false
                                    currentStep = OnboardingStep.CREATE_GENERATE
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Create")
                        }
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Wallet Setup") },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (currentStep == OnboardingStep.WELCOME) onBack()
                            else currentStep = OnboardingStep.WELCOME
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    when (currentStep) {
                        OnboardingStep.WELCOME -> WelcomeStep(
                            onCreateNew = {
                                showCreateConfirm = true
                            },
                            onImportMnemonic = {
                                currentStep = OnboardingStep.IMPORT_MNEMONIC
                            },
                            onImportPrivateKey = {
                                currentStep = OnboardingStep.IMPORT_PRIVATE_KEY
                            }
                        )
                        OnboardingStep.CREATE_GENERATE -> MnemonicDisplayStep(
                            mnemonic = generatedMnemonic,
                            onNext = { 
                                onFinish("Wallet created successfully!")
                            }
                        )
                        OnboardingStep.IMPORT_MNEMONIC -> ImportMnemonicStep(
                            mnemonicText = importText,
                            onMnemonicChange = { importText = it },
                            onImport = {
                                scope.launch {
                                    when (val result = WalletManager.importWallet(importText)) {
                                        is WalletManager.ImportResult.Success -> {
                                            onFinish("Wallet imported successfully!")
                                        }
                                        is WalletManager.ImportResult.InvalidWordCount -> {
                                            notificationState.showNotification(
                                                "Invalid phrase: Expected 12 or 24 words, but found ${result.count}.",
                                                NotificationType.WARNING
                                            )
                                        }
                                        is WalletManager.ImportResult.UnknownWords -> {
                                            notificationState.showNotification(
                                                "Invalid words found: ${result.words.joinToString(", ")}. Please check your spelling.",
                                                NotificationType.ERROR
                                            )
                                        }
                                        is WalletManager.ImportResult.Error -> {
                                            notificationState.showNotification(
                                                "Import failed: ${result.message}",
                                                NotificationType.ERROR
                                            )
                                        }
                                    }
                                }
                            }
                        )
                        OnboardingStep.IMPORT_PRIVATE_KEY -> ImportPrivateKeyStep(
                            pkText = importText,
                            onPkChange = { importText = it },
                            onImport = {
                                scope.launch {
                                    isLoading = true
                                    val success = WalletManager.importWalletByPrivateKey(importText)
                                    isLoading = false
                                    if (success) {
                                        onFinish("Wallet imported successfully!")
                                    } else {
                                        notificationState.showNotification(
                                            "Invalid Private Key. Please check the format.",
                                            NotificationType.ERROR
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
        
        FloatingNotification(
            state = notificationState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
fun WelcomeStep(
    onCreateNew: () -> Unit, 
    onImportMnemonic: () -> Unit,
    onImportPrivateKey: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        ),
                        shape = RoundedCornerShape(30.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                "Welcome to CyLoop",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                "Securely manage your assets on the blockchain with ease.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = onCreateNew,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create New Wallet", fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = onImportMnemonic,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Password, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Import Recovery Phrase", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onImportPrivateKey) {
                Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Import Private Key")
            }
        }
    }
}

@Composable
fun MnemonicDisplayStep(mnemonic: List<String>, onNext: () -> Unit) {
    val address by WalletManager.walletAddress.collectAsState()

    val saverLauncher = rememberFileSaverLauncher { _ ->
        // Handle success if needed
    }
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color(0xFF4CAF50)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Wallet Created Successfully!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4CAF50)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Address Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Your Public Address",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = address ?: "Generating...",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            "Recovery Phrase",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        // Warning Banner
        Surface(
            modifier = Modifier.padding(vertical = 12.dp),
            color = MaterialTheme.colorScheme.errorContainer,
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "IMPORTANT: Write down these 12 words. If you lose them, you lose access to your funds forever.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                mnemonic.chunked(3).forEachIndexed { rowIndex, row ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        row.forEachIndexed { index, word ->
                            val wordNumber = (rowIndex * 3) + index + 1
                            Row(
                                modifier = Modifier.padding(4.dp).weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$wordNumber.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = word,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    val mnemonicStr = mnemonic.joinToString(" ")
                    val content = "Wallet Backup\n\nAddress: $address\nRecovery Phrase: $mnemonicStr"
                    saverLauncher.launch(
                        bytes = content.encodeToByteArray(),
                        baseName = "cyloop_wallet_backup",
                        extension = "txt"
                    )
                },
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Backup")
            }
            
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Continue")
            }
        }
    }
}

@Composable
fun ImportMnemonicStep(mnemonicText: String, onMnemonicChange: (String) -> Unit, onImport: () -> Unit) {
    val wordCount = if (mnemonicText.isBlank()) 0 else mnemonicText.trim().split("\\s+".toRegex()).size

    Text("Import Recovery Phrase", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Text("Enter your 12 or 24 word recovery phrase in the correct order.", modifier = Modifier.padding(vertical = 16.dp))
    
    OutlinedTextField(
        value = mnemonicText,
        onValueChange = onMnemonicChange,
        modifier = Modifier.fillMaxWidth().height(180.dp),
        label = { Text("Recovery Phrase") },
        placeholder = { Text("e.g. apple banana cherry date ...") },
        shape = RoundedCornerShape(16.dp),
        supportingText = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Exactly 12 or 24 words required")
                Text("$wordCount words", fontWeight = FontWeight.Bold, color = if (wordCount == 12 || wordCount == 24) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error)
            }
        }
    )
    
    Spacer(modifier = Modifier.height(32.dp))
    Button(
        onClick = onImport,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        enabled = mnemonicText.isNotBlank(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text("Import Wallet")
    }
}

@Composable
fun ImportPrivateKeyStep(pkText: String, onPkChange: (String) -> Unit, onImport: () -> Unit) {
    Text("Import Private Key", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Text("Paste your Solana private key in Base58 format.", modifier = Modifier.padding(vertical = 16.dp))
    
    OutlinedTextField(
        value = pkText,
        onValueChange = onPkChange,
        modifier = Modifier.fillMaxWidth().height(120.dp),
        label = { Text("Private Key (Base58)") },
        placeholder = { Text("Enter your private key...") },
        shape = RoundedCornerShape(16.dp),
        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
    )
    
    Spacer(modifier = Modifier.height(32.dp))
    Button(
        onClick = onImport,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        enabled = pkText.isNotBlank(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text("Import Wallet")
    }
}
