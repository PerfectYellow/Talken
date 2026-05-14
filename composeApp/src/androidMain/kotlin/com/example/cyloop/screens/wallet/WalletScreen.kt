package com.example.cyloop.screens.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Data models
data class TokenBalance(
    val symbol: String,
    val amount: Double,
    val iconRes: Int? = null
)

data class Transaction(
    val id: String,
    val title: String,
    val date: String,
    val amount: Double,
    val isPositive: Boolean,
    val type: TransactionType
)

enum class TransactionType {
    SEND, RECEIVE, SWAP, AIRDROP
}

sealed class WalletUiState {
    object Loading : WalletUiState()
    data class Success(
        val balances: List<TokenBalance>,
        val transactions: List<Transaction>
    ) : WalletUiState()
    data class Error(val message: String) : WalletUiState()
    object Empty : WalletUiState()
}

// Main Wallet Screen with Custom Top Bar
@Composable
fun WalletScreen(
    onWithdrawClick: () -> Unit = {},
    onDepositClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    // Local state management instead of ViewModel
    var uiState by remember { mutableStateOf<WalletUiState>(WalletUiState.Success(emptyList(), emptyList()))}
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Load data on first composition
    LaunchedEffect(Unit) {
        loadWalletData { newState ->
            uiState = newState
        }
    }

    fun refreshBalances() {
        coroutineScope.launch {
            isRefreshing = true
            loadWalletData { newState ->
                uiState = newState
            }
            delay(500)
            isRefreshing = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB))
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Custom Header with title and settings button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
//                    .background(Color.White.copy(alpha = 0.95f))
                    .background(Color.Transparent)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Wallet",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D47A1)
                    )

                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color(0xFF0D47A1)
                        )
                    }
                }
            }

            // Main Content
            SwipeRefresh(
                isRefreshing = isRefreshing,
                onRefresh = { refreshBalances() },
                modifier = Modifier.fillMaxSize()
            ) {
                when (uiState) {
                    is WalletUiState.Loading -> LoadingState()
                    is WalletUiState.Success -> {
                        val successState = uiState as WalletUiState.Success
                        WalletContent(
                            balances = successState.balances,
                            transactions = successState.transactions,
                            onWithdrawClick = onWithdrawClick,
                            onDepositClick = onDepositClick
                        )
                    }
                    is WalletUiState.Error -> {
                        val errorState = uiState as WalletUiState.Error
                        ErrorState(
                            message = errorState.message,
                            onRetry = { refreshBalances() }
                        )
                    }
                    is WalletUiState.Empty -> EmptyState()
                }
            }
        }
    }
}

// Function to load data (temporary)
private suspend fun loadWalletData(onResult: (WalletUiState) -> Unit) {
    delay(500)
    onResult(
        WalletUiState.Success(
            balances = listOf(
                TokenBalance("SOL", 3.5),
                TokenBalance("ARX", 1250.75)
            ),
            transactions = listOf(
                Transaction(
                    id = "1",
                    title = "App Transaction",
                    date = "May 21, 2024",
                    amount = 5.054684,
                    isPositive = false,
                    type = TransactionType.SEND
                ),
                Transaction(
                    id = "2",
                    title = "Received SOL",
                    date = "May 20, 2024",
                    amount = 10.0,
                    isPositive = true,
                    type = TransactionType.RECEIVE
                ),
                Transaction(
                    id = "3",
                    title = "Airdrop",
                    date = "May 19, 2024",
                    amount = 1.5,
                    isPositive = true,
                    type = TransactionType.AIRDROP
                )
            )
        )
    )
}

@Composable
fun WalletContent(
    balances: List<TokenBalance>,
    transactions: List<Transaction>,
    onWithdrawClick: () -> Unit,
    onDepositClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Action Buttons (Withdraw/Deposit)
        item {
            ActionButtonsRow(
                onWithdrawClick = onWithdrawClick,
                onDepositClick = onDepositClick
            )
        }

        // Balance Cards
        items(balances) { balance ->
            BalanceCard(
                symbol = balance.symbol,
                amount = balance.amount,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Transaction History Section
        item {
            SectionHeader(
                title = "Transaction History",
                modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
            )
        }

        if (transactions.isEmpty()) {
            item {
                EmptyTransactionsState(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                )
            }
        } else {
            items(transactions) { transaction ->
                TransactionHistoryItem(
                    transaction = transaction,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ActionButtonsRow(
    onWithdrawClick: () -> Unit,
    onDepositClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ActionButton(
            text = "Withdraw",
            onClick = onWithdrawClick,
            textColor = Color.Gray,
            modifier = Modifier.weight(1f)
        )

        ActionButton(
            text = "Deposit",
            onClick = onDepositClick,
            textColor = Color.Gray,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ActionButton(
    text: String,
    onClick: () -> Unit,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.textButtonColors(
            contentColor = textColor
        )
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif
        )
    }
}

@Composable
fun BalanceCard(
    symbol: String,
    amount: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Token Icon
            Icon(
                imageVector = Icons.Default.AccountBalanceWallet,
                contentDescription = "$symbol Icon",
                modifier = Modifier.size(48.dp),
                tint = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Balance Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "$symbol Balance",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = String.format("%.6f", amount),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color.Black
                    )

                    Text(
                        text = " $symbol",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.DarkGray,
        modifier = modifier
    )
}

@Composable
fun TransactionHistoryItem(
    transaction: Transaction,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Transaction Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (transaction.type) {
                        TransactionType.SEND -> Icons.Default.ArrowUpward
                        TransactionType.RECEIVE -> Icons.Default.ArrowDownward
                        TransactionType.SWAP -> Icons.Default.SwapHoriz
                        TransactionType.AIRDROP -> Icons.Default.Flight
                    },
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color(0xFF666666)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Transaction Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = transaction.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    fontFamily = FontFamily.SansSerif
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = transaction.date,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    fontFamily = FontFamily.SansSerif
                )
            }

            // Amount
            Text(
                text = "${if (transaction.isPositive) "+" else "-"} ${String.format("%.6f", transaction.amount)}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (transaction.isPositive) Color(0xFF4CAF50) else Color(0xFFF44336),
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading wallet...",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun EmptyTransactionsState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.LightGray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No transactions yet",
            fontSize = 16.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "Your transaction history will appear here",
            fontSize = 14.sp,
            color = Color.LightGray
        )
    }
}

@Composable
fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No data available",
            fontSize = 16.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Retry")
            }
        }
    }
}

// SwipeRefresh wrapper
@Composable
fun SwipeRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        content()

        if (isRefreshing) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// Preview
@Preview(showBackground = true)
@Composable
fun PreviewWalletScreen() {
    MaterialTheme {
        WalletScreen()
    }
}