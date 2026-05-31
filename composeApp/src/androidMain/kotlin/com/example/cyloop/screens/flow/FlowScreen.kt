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
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.cyloop.theme.CyLoopTheme
import com.example.cyloop.theme.getAppBackgroundBrush
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    val comments: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlowScreen(
    modifier: Modifier = Modifier,
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var feedItems by remember { mutableStateOf(getMockPosts()) }
    val listState = rememberLazyListState()

    // Load more logic
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItemIndex >= feedItems.size - 1 && !isLoadingMore
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) {
            isLoadingMore = true
            // Simulate network delay
            delay(2000)
            val newItems = getMockPosts().map { it.copy(postId = it.postId + "_" + System.currentTimeMillis()) }
            feedItems = feedItems + newItems
            isLoadingMore = false
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
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
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
                        style = MaterialTheme.typography.bodyMedium.copy(
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
                        delay(1500) // Simulate refresh
                        feedItems = getMockPosts()
                        isRefreshing = false
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = bottomPadding + 20.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(feedItems, key = { it.postId }) { post ->
                        FlowCell(flowItem = post)
                    }

                    if (isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 3.dp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getMockPosts(): List<PostItem> {
    return listOf(
        PostItem(
            postId = "1",
            signature = "FpZ...K9vB",
            author = Author(
                wallet = "sol_engineer_wallet",
                username = "sol_engineer.sol",
                avatar = null
            ),
            content = Content(
                text = "Deploying Program Derived Address structures for expirable micro-leases. De-bloating Web3 storage feels massive!"
            ),
            stats = Stats(likes = 124, comments = 23),
            createdAt = System.currentTimeMillis() - 3600000 // 1 hour ago
        ),
        PostItem(
            postId = "2",
            signature = "Xyz...77aP",
            author = Author(
                wallet = "nft_guru_wallet",
                username = "nft_guru",
                avatar = null
            ),
            content = Content(
                text = "Solana microblogging model: PDA closes, original publisher recovers 100% rent-exemption lamports. Brilliant incentive structure."
            ),
            stats = Stats(likes = 89, comments = 12),
            createdAt = System.currentTimeMillis() - 7200000 // 2 hours ago
        ),
        PostItem(
            postId = "3",
            signature = "Mno...33qR",
            author = Author(
                wallet = "defi_dao_wallet",
                username = "defi_dao.sol",
                avatar = null
            ),
            content = Content(
                text = "Just launched our new staking pool with 15% APY! LP tokens can now be used as collateral across 5 different protocols. 🚀"
            ),
            stats = Stats(likes = 342, comments = 56),
            createdAt = System.currentTimeMillis() - 43200000 // 12 hours ago
        ),
        PostItem(
            postId = "4",
            signature = "Pqr...88mS",
            author = Author(
                wallet = "validator_whale",
                username = "validator_whale",
                avatar = null
            ),
            content = Content(
                text = "Solana just hit 2000 TPS sustained for over an hour. Mainnet is more stable than ever. Validators are crushing it 💪"
            ),
            stats = Stats(likes = 567, comments = 89),
            createdAt = System.currentTimeMillis() - 86400000 // 1 day ago
        ),
        PostItem(
            postId = "5",
            signature = "Stu...44nT",
            author = Author(
                wallet = "meme_coin_wallet",
                username = "meme_coin_lord",
                avatar = null
            ),
            content = Content(
                text = "Airdrop incoming for BONK holders who've staked for 30+ days! Snapshot in 48 hours. Don't sleep on this one 🔥"
            ),
            stats = Stats(likes = 1234, comments = 245),
            createdAt = System.currentTimeMillis() - 172800000 // 2 days ago
        ),
        PostItem(
            postId = "6",
            signature = "Vwx...11nU",
            author = Author(
                wallet = "nft_artist_wallet",
                username = "digital_sol_art",
                avatar = null
            ),
            content = Content(
                text = "New generative art collection dropping on Thursday! 500 unique pieces, all metadata stored permanently on Arweave. Sneak peek in my profile 🎨"
            ),
            stats = Stats(likes = 456, comments = 78),
            createdAt = System.currentTimeMillis() - 259200000 // 3 days ago
        ),
        PostItem(
            postId = "7",
            signature = "Yza...99nW",
            author = Author(
                wallet = "sol_dev_dao_wallet",
                username = "solana_devs.sol",
                avatar = null
            ),
            content = Content(
                text = "Workshop tomorrow: 'Build Your First Solana Smart Contract in Rust' - Free for all DAO members. 500 participants already registered! 🦀"
            ),
            stats = Stats(likes = 789, comments = 145),
            createdAt = System.currentTimeMillis() - 345600000 // 4 days ago
        )
    )
}

@Preview(showBackground = true)
@Composable
fun FlowScreenPreview() {
    CyLoopTheme(darkTheme = true) {
        FlowScreen()
    }
}
