package com.example.cyloop.screens.flow

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.cyloop.api.SolanaAccountResponse
import com.example.cyloop.api.SolanaService
import com.example.cyloop.api.SolanaSignatureResponse
import com.example.cyloop.components.FloatingNotification
import com.example.cyloop.components.NotificationType
import com.example.cyloop.components.rememberNotificationState
import com.example.cyloop.font.UIFont
import com.example.cyloop.theme.CyLoopTheme
import com.example.cyloop.theme.getAppBackgroundBrush
import com.example.cyloop.format
import kotlin.time.Clock
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.ui.graphics.graphicsLayer

// Data Models
data class PostItem(
    val postId: String,
    val signature: String,
    val author: Author,
    val content: Content,
    val stats: Stats,
    val createdAt: Long
)

data class Author(
    val wallet: String,
    val username: String,
    val avatar: String?
)

data class Content(
    val text: String
)

data class Stats(
    val likes: Int,
    val comments: Int,
    val backedAmount: String,
    val timeLeft: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlowScreen(
    modifier: Modifier = Modifier,
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp,
    initialFeed: List<PostItem>? = null
) {
    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    var feedItems by remember { mutableStateOf<List<PostItem>>(initialFeed ?: emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val notificationState = rememberNotificationState()
    val listState = rememberLazyListState()
    var messageText by remember { mutableStateOf("") }

    var showDonationSheet by remember { mutableStateOf(false) }
    var selectedPostForDonation by remember { mutableStateOf<PostItem?>(null) }
    val donationSheetState = rememberModalBottomSheetState()
    var donationAmount by remember { mutableStateOf("") }

    val targetAddress = "CV1vESFrRPhXdZVtG7vcvitnmYgXBoxbzasb9po4UaC"

    suspend fun fetchData() {
        try {
            errorMessage = null
            val acc = SolanaService.getAccountInfo(targetAddress)
            val sig = SolanaService.getSignaturesForAddress(targetAddress)
            val post = mapResponseToPost(acc, sig, targetAddress)
            feedItems = listOf(post)
        } catch (e: Exception) {
            e.printStackTrace()
            errorMessage = e.message ?: "An unknown error occurred"
            notificationState.showNotification("Flow Error: $errorMessage", NotificationType.ERROR)
        }
    }

    LaunchedEffect(Unit) {
        if (initialFeed == null) {
            isRefreshing = true
            fetchData()
            isRefreshing = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(getAppBackgroundBrush())
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Active Flows",
                    style = UIFont.LargeTitle.copy(
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    )
                    Text(
                        text = "Sync Live",
                        style = UIFont.Metadata.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    scope.launch {
                        isRefreshing = true
                        fetchData()
                        isRefreshing = false
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                if (errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Error Loading Flows",
                                style = UIFont.ChatName.copy(color = MaterialTheme.colorScheme.error),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = errorMessage!!,
                                style = UIFont.Metadata.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = {
                                scope.launch {
                                    isRefreshing = true
                                    fetchData()
                                    isRefreshing = false
                                }
                            }) {
                                Text("Retry")
                            }
                        }
                    }
                } else if (feedItems.isEmpty() && !isRefreshing) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No active flows found",
                            style = UIFont.Body.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(feedItems, key = { it.postId }) { post ->
                            FlowCell(
                                flowItem = post,
                                onBackFlowClick = { selectedPost ->
                                    selectedPostForDonation = selectedPost
                                    showDonationSheet = true
                                }
                            )
                        }
                    }
                }
            }

            // Message Input
            Surface(
                modifier = Modifier
                    .fillMaxWidth(),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp, bottom = 10.dp)
                        .padding(bottom = (bottomPadding - 28.dp).coerceAtLeast(0.dp)),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(
                                "Type a message...",
                                style = UIFont.Body.copy(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                            )
                        },
                        textStyle = UIFont.Body.copy(color = MaterialTheme.colorScheme.onSurface),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4
                    )

                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                // TODO: Handle message sending logic
                                messageText = ""
                            }
                        },
                        enabled = messageText.isNotBlank(),
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary,
                            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send"
                        )
                    }
                }
            }
        }

        FloatingNotification(state = notificationState)

        if (showDonationSheet && selectedPostForDonation != null) {
            ModalBottomSheet(
                onDismissRequest = {
                    showDonationSheet = false
                    donationAmount = ""
                },
                sheetState = donationSheetState,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Back this Flow",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "How much do you want to donate to ${selectedPostForDonation!!.author.username}?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    OutlinedTextField(
                        value = donationAmount,
                        onValueChange = { 
                            if (it.isEmpty() || it.toDoubleOrNull() != null) {
                                donationAmount = it
                            }
                        },
                        label = { Text("Amount (SOL)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        placeholder = { Text("0.0") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Button(
                        onClick = {
                            val amount = donationAmount.toDoubleOrNull() ?: 0.0
                            if (amount > 0) {
                                scope.launch {
                                    notificationState.showNotification(
                                        "Sending $amount SOL to ${selectedPostForDonation!!.author.username}...",
                                        NotificationType.HINT
                                    )
                                    showDonationSheet = false
                                    donationAmount = ""
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = donationAmount.isNotEmpty() && (donationAmount.toDoubleOrNull() ?: 0.0) > 0
                    ) {
                        Text(
                            text = "Send Donation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

fun mapResponseToPost(
    response: SolanaAccountResponse,
    sigResponse: SolanaSignatureResponse,
    address: String
): PostItem {
    val result = response.result?.value ?: throw Exception("Account data not found")
    val accountData = SolanaService.parseAccountData(result.data)
    val info = accountData?.parsed?.info
    
    fun formatAddr(addr: String) = if (addr.length > 8) "${addr.take(4)}...${addr.takeLast(4)}" else addr

    val rawContent = when (val d = info?.data) {
        is JsonArray -> {
            if (d.size > 0 && d[0] is JsonPrimitive) {
                d[0].jsonPrimitive.content
            } else {
                d.toString()
            }
        }
        is JsonPrimitive -> d.content
        is JsonObject -> d.toString()
        else -> "No data available"
    }

    // Truncate string to prevent Layout/Measure crashes with huge data
    val safeContent = if (rawContent.length > 1000) rawContent.take(1000) + "..." else rawContent

    val solAmount = result.lamports / 1_000_000_000.0
    val formattedSol = if (solAmount % 1.0 == 0.0) {
        "${solAmount.format(0)} SOL"
    } else {
        "${solAmount.format(2)} SOL"
    }

    // Calculate time from blockTime
    val blockTime = sigResponse.result?.firstOrNull()?.blockTime
    val timeLeftStr = if (blockTime != null) {
        val currentTimeSec = Clock.System.now().toEpochMilliseconds() / 1000
        val diff = currentTimeSec - blockTime
        if (diff < 60) "${diff}s ago"
        else if (diff < 3600) "${diff / 60}m ago"
        else if (diff < 86400) "${diff / 3600}h ago"
        else "${diff / 86400}d ago"
    } else {
        "unknown"
    }

    return PostItem(
        postId = address,
        signature = address,
        author = Author(
            wallet = result.owner,
            username = formatAddr(result.owner),
            avatar = null
        ),
        content = Content(text = safeContent),
        stats = Stats(
            likes = 999,
            comments = 123,
            backedAmount = formattedSol,
            timeLeft = timeLeftStr
        ),
        createdAt = (blockTime ?: (Clock.System.now().toEpochMilliseconds() / 1000)) * 1000
    )
}

@Preview(showBackground = true)
@Composable
fun FlowScreenPreview() {
    CyLoopTheme(darkTheme = true) {
        val mockFeed = listOf(
            PostItem(
                postId = "mock_1",
                signature = "sig_1",
                author = Author(
                    wallet = "wallet_1",
                    username = "solana_fan",
                    avatar = null
                ),
                content = Content(text = "Just bridged some SOL to CyLoop! 🚀"),
                stats = Stats(
                    likes = 42,
                    comments = 5,
                    backedAmount = "2.5 SOL",
                    timeLeft = "12h ago"
                ),
                createdAt = Clock.System.now().toEpochMilliseconds()
            ),
            PostItem(
                postId = "mock_2",
                signature = "sig_2",
                author = Author(
                    wallet = "wallet_2",
                    username = "crypto_builder",
                    avatar = null
                ),
                content = Content(text = "Building the future of social finance on Solana."),
                stats = Stats(
                    likes = 128,
                    comments = 24,
                    backedAmount = "15.0 SOL",
                    timeLeft = "2d ago"
                ),
                createdAt = Clock.System.now().toEpochMilliseconds() - 86400000
            )
        )
        FlowScreen(initialFeed = mockFeed)
    }
}
