package com.example.cyloop.screens.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

enum class TransactionType {
    SEND, RECEIVE
}

data class Transaction(
    val id: String,
    val type: TransactionType,
    val amount: String,
    val date: String,
    val recipientOrSender: String,
    val status: String
)

val mockTransactions = listOf(
    Transaction("1", TransactionType.SEND, "-$50.00", "Oct 24, 2023", "To: John Doe", "Completed"),
    Transaction("2", TransactionType.RECEIVE, "+$120.00", "Oct 23, 2023", "From: Alice Smith", "Completed"),
    Transaction("3", TransactionType.SEND, "-$15.50", "Oct 22, 2023", "To: Starbucks", "Completed"),
    Transaction("4", TransactionType.RECEIVE, "+$1,000.00", "Oct 20, 2023", "From: Employer Inc", "Completed"),
    Transaction("5", TransactionType.SEND, "-$200.00", "Oct 18, 2023", "To: Rent", "Pending"),
    Transaction("6", TransactionType.RECEIVE, "+$45.00", "Oct 15, 2023", "From: Bob", "Completed"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentInfoView(
    onBackClick: () -> Unit,
    onNewTransactionClick: () -> Unit,
    onBillMakerClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payments") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ExtendedFloatingActionButton(
                    onClick = onBillMakerClick,
                    icon = { Icon(Icons.Filled.Receipt, "Bill Maker") },
                    text = { Text("Bill Maker") },
                    modifier = Modifier.weight(1f)
                )
//                ExtendedFloatingActionButton(
//                    onClick = onNewTransactionClick,
//                    icon = { Icon(Icons.Filled.Add, "New Transaction") },
//                    text = { Text("New Transaction") },
//                    modifier = Modifier.weight(1f)
//                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Text(
                text = "Transaction history",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp, bottom = 80.dp), // Space for FAB
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(mockTransactions) { transaction ->
                    TransactionItem(transaction)
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon: ImageVector
            val iconColor: Color
            val backgroundColor: Color

            if (transaction.type == TransactionType.SEND) {
                icon = Icons.Default.ArrowUpward
                iconColor = MaterialTheme.colorScheme.error
                backgroundColor = MaterialTheme.colorScheme.errorContainer
            } else {
                icon = Icons.Default.ArrowDownward
                iconColor = MaterialTheme.colorScheme.primary
                backgroundColor = MaterialTheme.colorScheme.primaryContainer
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(backgroundColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.recipientOrSender,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = transaction.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = transaction.amount,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (transaction.type == TransactionType.RECEIVE) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = transaction.status,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (transaction.status == "Pending") 
                        MaterialTheme.colorScheme.onSurfaceVariant 
                    else 
                        MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPaymentInfoView() {
    MaterialTheme {
        PaymentInfoView(
            onBackClick = {},
            onNewTransactionClick = {},
            onBillMakerClick = {}
        )
    }
}
