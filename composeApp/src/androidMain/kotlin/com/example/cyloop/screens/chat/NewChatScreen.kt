package com.example.cyloop.screens.chat

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.cyloop.api.HeliusAssetResult
import com.example.cyloop.api.HeliusService
import com.example.cyloop.api.MagicEdenService
import com.example.cyloop.api.SolanaService
import com.example.cyloop.storage.ContactPreferences
import com.example.cyloop.storage.SavedContact
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewChatScreen(
    onBackClick: () -> Unit,
    onContactSelected: (String, String, String?, String?) -> Unit
) {
    val context = LocalContext.current
    val contactPrefs = remember { ContactPreferences(context) }
    val savedContacts by contactPrefs.savedContacts.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    
    var searchQuery by remember { mutableStateOf("") }
    var showAddContactSheet by remember { mutableStateOf(false) }
    var selectedSavedContact by remember { mutableStateOf<SavedContact?>(null) }
    
    val hardcodedContacts = listOf(
        "Alice Johnson", "Bob Smith", "Carol Davis", "David Wilson", 
        "Emma Brown", "Frank Miller", "Grace Lee", "Henry Ford"
    )
    val filteredHardcoded = hardcodedContacts.filter { it.contains(searchQuery, ignoreCase = true) }
    val filteredSaved = savedContacts.filter { it.name.contains(searchQuery, ignoreCase = true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Chat") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search contacts...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = CircleShape,
                singleLine = true
            )

            ListItem(
                headlineContent = { Text("Add New Contact", color = Color(0xFF2196F3)) },
                leadingContent = {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE3F2FD)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color(0xFF2196F3))
                    }
                },
                modifier = Modifier.clickable { showAddContactSheet = true }
            )

            HorizontalDivider()

            Text(
                text = "Saved Contacts",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.labelLarge,
                color = Color.Gray
            )

            LazyColumn {
                items(filteredSaved) { contact ->
                    ListItem(
                        headlineContent = { Text(contact.name) },
                        supportingContent = { Text(contact.ownerAddress, maxLines = 1) },
                        leadingContent = {
                            if (contact.imageUrl != null) {
                                AsyncImage(
                                    model = contact.imageUrl,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp).clip(CircleShape)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color.LightGray),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(contact.name.take(1))
                                }
                            }
                        },
                        modifier = Modifier.clickable { selectedSavedContact = contact },
                        trailingContent = {
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        contactPrefs.deleteContact(contact.nftAddress)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove Contact",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )
                }

                item {
                    Text(
                        text = "Other Contacts",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.Gray
                    )
                }

                items(filteredHardcoded) { contact ->
                    ListItem(
                        headlineContent = { Text(contact) },
                        leadingContent = {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.LightGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(contact.take(1))
                            }
                        },
                        modifier = Modifier.clickable { 
                            onContactSelected(contact.lowercase().replace(" ", "_"), contact, null, null) 
                        }
                    )
                }
            }
        }
    }

    if (showAddContactSheet) {
        AddContactSheet(
            savedContacts = savedContacts,
            onDismiss = { showAddContactSheet = false },
            onSave = { result ->
                val newContact = SavedContact(
                    id = result.id,
                    name = result.content.metadata.name,
                    nftAddress = result.id,
                    ownerAddress = result.ownership.owner,
                    imageUrl = result.content.links?.image
                )
                scope.launch {
                    contactPrefs.saveContact(newContact)
                }
                showAddContactSheet = false
            }
        )
    }

    selectedSavedContact?.let { contact ->
        ContactDetailSheet(
            contact = contact,
            onDismiss = { selectedSavedContact = null },
            onStartChat = {
                val imageUrl = selectedSavedContact?.imageUrl
                val nftAddr = selectedSavedContact?.nftAddress
                selectedSavedContact = null
                onContactSelected(contact.ownerAddress, contact.name, imageUrl, nftAddr)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactSheet(
    savedContacts: List<SavedContact>,
    onDismiss: () -> Unit,
    onSave: (HeliusAssetResult) -> Unit
) {
    var nftAddress by remember { mutableStateOf("") }
    var assetResult by remember { mutableStateOf<HeliusAssetResult?>(null) }
    var nftPrice by remember { mutableStateOf<Double?>(null) }
    var walletBalance by remember { mutableStateOf<Long?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Box(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .animateContentSize()
                .then(if (assetResult != null) Modifier.fillMaxHeight(0.8f) else Modifier)
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Add New Contact", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

                assetResult?.let { result ->
                    AsyncImage(
                        model = result.content.links?.image,
                        contentDescription = null,
                        modifier = Modifier
                            .size(160.dp)
                            .clip(MaterialTheme.shapes.large)
                            .background(Color.LightGray)
                    )
                }

                Column(
                    modifier = Modifier
                        .then(if (assetResult != null) Modifier.weight(1f) else Modifier)
                        .verticalScroll(scrollState)
                        .padding(bottom = if (assetResult != null) 72.dp else 0.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = nftAddress,
                        onValueChange = { 
                            nftAddress = it
                            if (it.isBlank()) {
                                assetResult = null
                                nftPrice = null
                                walletBalance = null
                                errorMessage = null
                            }
                        },
                        label = { Text("NFT Address") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    if (assetResult == null && !isLoading) {
                        Button(
                            onClick = {
                                scope.launch {
                                    isLoading = true
                                    errorMessage = null
                                    try {
                                        val result = HeliusService.getAsset(nftAddress)
                                        if (savedContacts.any { it.nftAddress == result.id }) {
                                            errorMessage = "This contact is already in your list."
                                        } else {
                                            assetResult = result
                                            // Fetch price from Magic Eden
                                            val priceInfo = MagicEdenService.getTokenInfo(result.id)
                                            nftPrice = priceInfo?.price ?: 0.0
                                            
                                            // Fetch wallet balance
                                            walletBalance = SolanaService.getBalance(result.ownership.owner)
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = e.message
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = nftAddress.isNotBlank()
                        ) {
                            Text("Search")
                        }
                    }

                    if (isLoading) {
                        CircularProgressIndicator()
                    }

                    errorMessage?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }

                    assetResult?.let { result ->
                        Text(result.content.metadata.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                DetailItem(label = "Owner Wallet", value = result.ownership.owner)
                                HorizontalDivider()

                                Text("NFT Price", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                                Text(
                                    text = "${"%.2f".format(nftPrice ?: 0.0)} SOL",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                HorizontalDivider()

                                Text("Wallet Balance", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                                Text(
                                    text = if (walletBalance != null) "${"%.4f".format(walletBalance!! / 1_000_000_000.0)} SOL" else "0.0000 SOL",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }

            assetResult?.let { result ->
                Button(
                    onClick = {
                        onSave(result)
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Save Contact", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetailSheet(
    contact: SavedContact,
    onDismiss: () -> Unit,
    onStartChat: () -> Unit
) {
    var balance by remember { mutableStateOf<Long?>(null) }
    var nftPrice by remember { mutableStateOf<Double?>(null) }
    var isLoadingBalance by remember { mutableStateOf(true) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    LaunchedEffect(contact.ownerAddress, contact.nftAddress) {
        try {
            // Fetch balance
            balance = SolanaService.getBalance(contact.ownerAddress)

            // Fetch NFT Price
            val priceInfo = MagicEdenService.getTokenInfo(contact.nftAddress)
            nftPrice = priceInfo?.price ?: 0.0
        } catch (e: Exception) {
            balance = null
            nftPrice = 0.0
        } finally {
            isLoadingBalance = false
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Box(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .fillMaxHeight(0.8f) // Keeps it stable at a high height
                .animateContentSize() // Smooths out height changes from loading
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(bottom = 76.dp), // Space for the floating button
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                AsyncImage(
                    model = contact.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(200.dp)
                        .clip(MaterialTheme.shapes.extraLarge)
                        .background(Color.LightGray)
                )

                Text(contact.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        DetailItem(label = "NFT Address", value = contact.nftAddress)
                        HorizontalDivider()
                        DetailItem(label = "Owner Wallet", value = contact.ownerAddress)
                        HorizontalDivider()

                        Text("NFT Price", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                        Text(
                            text = "${"%.2f".format(nftPrice ?: 0.0)} SOL",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.ExtraBold
                        )
                        HorizontalDivider()

                        Text("Wallet Balance", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                        if (isLoadingBalance) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        } else {
                            Text(
                                text = if (balance != null) "${"%.4f".format(balance!! / 1_000_000_000.0)} SOL" else "Error loading balance",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            // Floating Overlay Button
            Button(
                onClick = onStartChat,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Chat", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewNewChatScreen() {
    MaterialTheme {
        NewChatScreen({}, {one, two, three, four->})
    }
}