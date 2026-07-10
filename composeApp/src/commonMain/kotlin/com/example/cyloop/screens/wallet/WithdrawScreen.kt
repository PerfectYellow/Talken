package com.example.cyloop.screens.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.cyloop.format
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawScreen(
    onBackClick: () -> Unit,
    onWithdrawComplete: () -> Unit,
    availableBalance: Double = 45.000,
    tokenSymbol: String = "SOL"
) {
    var address by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    // Parse amount safely
    val amountValue = amount.toDoubleOrNull() ?: 0.0
    val isFormValid = address.isNotBlank() && amountValue > 0 && amountValue <= availableBalance

    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = {
//                    Text(
//                        text = "Withdraw",
//                        fontSize = 20.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = Color(0xFF0D47A1)
//                    )
//                },
//                navigationIcon = {
//                    IconButton(onClick = onBackClick) {
//                        Icon(
//                            imageVector = Icons.Default.ArrowBack,
//                            contentDescription = "Back",
//                            tint = Color(0xFF0D47A1)
//                        )
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = Color.White
//                )
//            )
//        },
        containerColor = Color(0xFFF5F7FA)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB))
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB))
                        )
                    )
            ) {
                // Address Input Field
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = {
                        Text(
                            text = "To: address",
                            color = Color.Gray
                        )
                    },
                    placeholder = {
                        Text(
                            text = "Enter wallet address",
                            color = Color.LightGray
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0D47A1),
                        unfocusedBorderColor = Color.LightGray,
                        focusedLabelColor = Color(0xFF0D47A1)
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    },
                    isError = address.isNotBlank() && address.length < 32,
                    supportingText = {
                        if (address.isNotBlank() && address.length < 32) {
                            Text(
                                text = "Address seems too short",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Amount Input Field
                OutlinedTextField(
                    value = amount,
                    onValueChange = { newAmount ->
                        // Allow only numbers and decimal point
                        if (newAmount.isEmpty() || newAmount.matches(Regex("^\\d*\\.?\\d{0,6}$"))) {
                            amount = newAmount
                        }
                    },
                    label = {
                        Text(
                            text = "Amount:",
                            color = Color.Gray
                        )
                    },
                    placeholder = {
                        Text(
                            text = "how much...",
                            color = Color.LightGray
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0D47A1),
                        unfocusedBorderColor = Color.LightGray,
                        focusedLabelColor = Color(0xFF0D47A1)
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AttachMoney,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    },
                    trailingIcon = {
                        if (amount.isNotBlank()) {
                            TextButton(
                                onClick = { amount = "" },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    modifier = Modifier.size(20.dp),
                                    tint = Color.Gray
                                )
                            }
                        }
                    },
                    isError = amount.isNotBlank() && (amountValue <= 0 || amountValue > availableBalance),
                    supportingText = {
                        when {
                            amount.isNotBlank() && amountValue <= 0 -> {
                                Text(
                                    text = "Amount must be greater than 0",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp
                                )
                            }
                            amount.isNotBlank() && amountValue > availableBalance -> {
                                Text(
                                    text = "Insufficient balance",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Available Balance Display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Available: ",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = availableBalance.format(6),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D47A1),
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = " $tokenSymbol",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Max Button
//                    TextButton(
//                        onClick = {
//                            amount = availableBalance.toString()
//                        },
//                        modifier = Modifier
//                            .height(32.dp)
//                            .clip(RoundedCornerShape(8.dp))
//                            .background(Color(0xFFE3F2FD)),
//                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
//                    ) {
//                        Text(
//                            text = "MAX",
//                            fontSize = 12.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = Color(0xFF0D47A1)
//                        )
//                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Send Button
                Button(
                    onClick = {
                        if (isFormValid) {
                            coroutineScope.launch {
                                isLoading = true
                                // Simulate withdraw API call
                                val success = performWithdraw(address, amountValue)
                                delay(1500) // Simulate network delay
                                isLoading = false

                                if (success) {
                                    onWithdrawComplete()
                                } else {
                                    errorMessage = "Withdrawal failed. Please try again."
                                    showErrorDialog = true
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = isFormValid && !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFormValid) Color(0xFF0D47A1) else Color.Gray,
                        disabledContainerColor = Color.LightGray
                    )
                ) {
                    if (isLoading) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Processing...",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Text(
                            text = "Send",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Warning Dialog
            if (showErrorDialog) {
                AlertDialog(
                    onDismissRequest = { showErrorDialog = false },
                    title = {
                        Text(
                            text = "Withdrawal Failed",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF44336)
                        )
                    },
                    text = {
                        Text(text = errorMessage)
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { showErrorDialog = false }
                        ) {
                            Text("OK", color = Color(0xFF0D47A1))
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = Color(0xFFF44336)
                        )
                    }
                )
            }
        }
    }
}

// Simulate withdraw API call
private suspend fun performWithdraw(address: String, amount: Double): Boolean {
    // Here you would call your actual withdrawal API
    // For demo purposes, we'll assume success
    delay(1000)
    return true
}

// Preview function
@Preview(showBackground = true)
@Composable
fun PreviewWithdrawScreen() {
    MaterialTheme {
        WithdrawScreenCardDesign(
            onBackClick = {},
            onWithdrawComplete = {},
            availableBalance = 3.5,
            tokenSymbol = "SOL"
        )
    }
}

// Alternative design with Card layout (more modern)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawScreenCardDesign(
    onBackClick: () -> Unit,
    onWithdrawComplete: () -> Unit,
    availableBalance: Double = 24.000,
    tokenSymbol: String = "SOL"
) {
    var address by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val amountValue = amount.toDoubleOrNull() ?: 0.0
    val isFormValid = address.isNotBlank() && amountValue > 0 && amountValue <= availableBalance

    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = {
//                    Text(
//                        text = "Withdraw",
//                        fontSize = 20.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = Color(0xFF0D47A1)
//                    )
//                },
//                navigationIcon = {
//                    IconButton(onClick = onBackClick) {
//                        Icon(
//                            Icons.Default.ArrowBack,
//                            contentDescription = "Back",
//                            tint = Color(0xFF0D47A1)
//                        )
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = Color.White
//                )
//            )
//        },
        containerColor = Color.Transparent //(0xFFF5F7FA) // Solid light background for Scaffold
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Card with gradient background
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent // Make card transparent to show gradient
                )
            ) {
                // Apply gradient to the inner box of the card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFE8F5E9),  // Top
                                    Color(0xFFE3F2FD)   // Bottom
                                )
                            )
                        )
                        .clip(RoundedCornerShape(16.dp)) // Ensure corners stay rounded
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        // Address Field
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Recipient Address") },
                            placeholder = { Text("Enter wallet address") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = {
                                Icon(Icons.Default.ArrowForward, contentDescription = null)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0D47A1),
                                unfocusedBorderColor = Color(0xFF90CAF9),
                                focusedLabelColor = Color(0xFF0D47A1)
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Amount Field
                        OutlinedTextField(
                            value = amount,
                            onValueChange = { newAmount ->
                                if (newAmount.isEmpty() || newAmount.matches(Regex("^\\d*\\.?\\d{0,6}$"))) {
                                    amount = newAmount
                                }
                            },
                            label = { Text("Amount") },
                            placeholder = { Text("0.00") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            leadingIcon = {
                                Icon(Icons.Default.AttachMoney, contentDescription = null)
                            },
                            trailingIcon = {
                                TextButton(onClick = { amount = availableBalance.toString() }) {
                                    Text("MAX", fontWeight = FontWeight.Bold, color = Color(0xFF0D47A1))
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0D47A1),
                                unfocusedBorderColor = Color(0xFF90CAF9),
                                focusedLabelColor = Color(0xFF0D47A1)
                            )
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Available Balance Card (without gradient, solid color)
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.8f) // Semi-transparent white
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Available Balance",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = availableBalance.format(6),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0D47A1),
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = tokenSymbol,
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        if (amountValue > availableBalance) {
                            Text(
                                text = "Insufficient balance",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                            )
                        }

                        if (address.isNotBlank() && address.length < 32 && address.length > 0) {
                            Text(
                                text = "Please enter a valid wallet address",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                            )
                        }
                    }
                }
            }

            // Send Button
            Button(
                onClick = {
                    if (isFormValid) {
                        isLoading = true
                        // Simulate withdrawal processing
                        // Add your actual withdrawal logic here
                        // onWithdrawComplete() // Call this when done
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = isFormValid && !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0D47A1),
                    disabledContainerColor = Color(0xFF90CAF9)
                )
            ) {
                if (isLoading) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Processing...",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = "Send ${if (amountValue > 0) amountValue.format(6) else ""} $tokenSymbol".trim(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// Preview function