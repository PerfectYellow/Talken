package com.example.cyloop.screens.wallet

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.cyloop.format
import com.example.cyloop.crypto.WalletManager
import com.example.cyloop.components.QRCodeView
import kotlin.random.Random

data class BillItem(
    val id: Long = Random.nextLong(),
    val name: String,
    val number: Int,
    val value: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillMakerView(
    onBackClick: () -> Unit
) {
    val billItems = remember { mutableStateListOf<BillItem>() }
    var showSheet by remember { mutableStateOf(false) }
    var showQRDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val walletAddress by WalletManager.walletAddress.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bill Maker") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            if (billItems.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
                    TotalAndPaySection(
                        total = billItems.sumOf { it.value },
                        onPayClick = { /* Handle pay logic */ },
                        onQrClick = { showQRDialog = true }
                    )
                    FloatingActionButton(
                        onClick = { showSheet = true },
                        modifier = Modifier
                            .padding(end = 32.dp)
                            .offset(y = (-50).dp),
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Item")
                    }
                }
            }
        },
        floatingActionButton = {
            if (billItems.isEmpty()) {
                FloatingActionButton(
                    onClick = { showSheet = true },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Item")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = if (billItems.isNotEmpty()) 120.dp else 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = billItems,
                    key = { it.id }
                ) { item ->
                    SwipeToDeleteContainer(
                        onDelete = { billItems.remove(item) }
                    ) {
                        BillItemRow(item)
                    }
                }
                
                if (billItems.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillParentMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No items added yet. Click + to start.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }

        if (showSheet) {
            AddItemSheet(
                sheetState = sheetState,
                onDismiss = { showSheet = false },
                onAdd = { newItem ->
                    billItems.add(newItem)
                }
            )
        }

        if (showQRDialog && walletAddress != null) {
            val total = billItems.sumOf { it.value }
            PaymentQRDialog(
                address = walletAddress!!,
                amount = total,
                onDismiss = { showQRDialog = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteContainer(
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val isDismissing = dismissState.targetValue == SwipeToDismissBoxValue.EndToStart
            val backgroundColor by animateColorAsState(
                if (isDismissing) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
            )
            val iconScale by animateFloatAsState(
                if (isDismissing) 1.3f else 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(backgroundColor)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = if (isDismissing) Color.White else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp + (8.dp * (iconScale - 1f)))
                )
            }
        },
        content = { content() }
    )
}

@Composable
fun PaymentQRDialog(
    address: String,
    amount: Double,
    onDismiss: () -> Unit
) {
    val qrData = "solana:$address?amount=$amount"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Payment Request",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Surface(
                    modifier = Modifier
                        .size(240.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    color = Color.White
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(16.dp)) {
                        QRCodeView(
                            data = qrData,
                            size = 200.dp,
                            color = Color.Black
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Total Amount",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "${amount.format(2)} SOL",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Scan this code with CyLoop to complete the payment.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun BillItemRow(item: BillItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Quantity: ${item.number}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Text(
                text = "$${item.value.format(2)}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
fun TotalAndPaySection(
    total: Double,
    onPayClick: () -> Unit,
    onQrClick: () -> Unit
) {
    Surface(
        tonalElevation = 12.dp,
        shadowElevation = 12.dp,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Amount",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$${total.format(2)}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Colorful QR Button with vibrant yellow gradient
                Surface(
                    onClick = onQrClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(18.dp)),
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFFFE082), // Light Yellow
                                        Color(0xFFF5D45E), // App Gold
                                        Color(0xFFFFB300)  // Vibrant Amber
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "Show Payment QR",
                            tint = Color(0xFF3E2723), // Dark brown for better contrast on yellow
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                // Modern Pay Now Button
                Button(
                    onClick = onPayClick,
                    modifier = Modifier
                        .weight(4f)
                        .height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        "Pay Now",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onAdd: (BillItem) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Add New Item",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Item Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = number,
                    onValueChange = { number = it.filter { c -> c.isDigit() } },
                    label = { Text("Quantity") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Price/Value") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val n = number.toIntOrNull() ?: 0
                    val v = value.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank()) {
                        onAdd(BillItem(name = name, number = n, value = v))
                        name = ""
                        number = ""
                        value = ""
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Add to Bill")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewBillMakerView() {
    MaterialTheme {
        BillMakerView({})
    }
}
