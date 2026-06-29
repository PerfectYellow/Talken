package com.example.cyloop.screens.flow

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.cyloop.api.SolanaAccountResponse
import com.example.cyloop.api.SolanaService
import com.example.cyloop.api.SolanaSignatureResponse
import com.example.cyloop.font.UIFont
import com.example.cyloop.theme.CyLoopTheme
import com.example.cyloop.theme.getAppBackgroundBrush
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

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
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    var feedItems by remember { mutableStateOf<List<PostItem>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()

    val targetAddress = "CV1vESFrRPhXdZVtG7vcvitnmYgXBoxbzasb9po4UaC"

    suspend fun fetchData() {
        try {
            errorMessage = null
            val (accountResponse, sigResponse) = withContext(Dispatchers.IO) {
                val acc = SolanaService.getAccountInfo(targetAddress)
                val sig = SolanaService.getSignaturesForAddress(targetAddress)
                acc to sig
            }
            val post = mapResponseToPost(accountResponse, sigResponse, targetAddress)
            feedItems = listOf(post)
        } catch (e: Exception) {
            e.printStackTrace()
            errorMessage = e.message ?: "An unknown error occurred"
        }
    }

    LaunchedEffect(Unit) {
        isRefreshing = true
        fetchData()
        isRefreshing = false
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
                modifier = Modifier.fillMaxSize()
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
                        contentPadding = PaddingValues(bottom = bottomPadding + 20.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(feedItems, key = { it.postId }) { post ->
                            FlowCell(flowItem = post)
                        }
                    }
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
        String.format("%.0f SOL", solAmount)
    } else {
        String.format("%.2f SOL", solAmount)
    }

    // Calculate time from blockTime
    val blockTime = sigResponse.result?.firstOrNull()?.blockTime
    val timeLeftStr = if (blockTime != null) {
        val currentTimeSec = System.currentTimeMillis() / 1000
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
        createdAt = (blockTime ?: (System.currentTimeMillis() / 1000)) * 1000
    )
}

@Preview(showBackground = true)
@Composable
fun FlowScreenPreview() {
    CyLoopTheme(darkTheme = true) {
        FlowScreen()
    }
}
