package com.example.cyloop.screens.flow

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.example.cyloop.theme.getAppBackgroundBrush

// Data Models
data class FeedResponse(
    val cursor: String?,
    val posts: List<PostItem>
)

data class PostItem(
    val postId: String,
    val signature: String,
    val author: Author,
    val content: Content,
    val media: List<MediaItem>,
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

data class MediaItem(
    val type: String,
    val url: String,
    val width: Int?,
    val height: Int?
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
    val listState = rememberLazyListState()

    // State management
    var feedItems by remember { mutableStateOf<List<PostItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var cursor by remember { mutableStateOf<String?>(null) }

    // Pull to refresh state
    val pullToRefreshState = rememberPullToRefreshState()

    // Load initial data
    LaunchedEffect(Unit) {
        isLoading = true
        delay(800)
        feedItems = getMockPosts()
        cursor = "next_cursor_123"
        isLoading = false
    }

    // Load more when reaching near bottom
    val loadMoreThreshold = 3
    LaunchedEffect(listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index) {
        val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
        val totalItems = feedItems.size

        if (totalItems > 0 && lastVisibleIndex >= totalItems - loadMoreThreshold && !isLoading && cursor != null) {
            isLoading = true
            delay(800)
            val morePosts = getMockPosts().map { post ->
                post.copy(
                    postId = "${System.currentTimeMillis()}_${post.postId}",
                    createdAt = System.currentTimeMillis() - (feedItems.size * 3600000L)
                )
            }
            feedItems = feedItems + morePosts
            cursor = if (feedItems.size > 20) null else "next_cursor_${feedItems.size}"
            isLoading = false
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
            // Header with title
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Flow",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Row {
                            IconButton(onClick = { /* Search action */ }) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            IconButton(onClick = { /* Settings action */ }) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Content area with pull to refresh
            Box(modifier = Modifier.fillMaxSize()) {
                if (feedItems.isEmpty() && isLoading) {
                    // Loading state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Diving into the Flow...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp
                        )
                    }
                } else {
                    // PullToRefreshBox wrapper
                    PullToRefreshBox(
                        state = pullToRefreshState,
                        isRefreshing = isRefreshing,
                        onRefresh = {
                            scope.launch {
                                isRefreshing = true
                                // Simulate refresh
                                delay(1000)
                                feedItems = getMockPosts()
                                cursor = "next_cursor_123"
                                isRefreshing = false
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = 8.dp, bottom = bottomPadding + 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(feedItems) { post ->
                                PostCard(postItem = post)
                            }

                            if (isLoading) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }

                // Error message if any
                errorMessage?.let { error ->
                    Snackbar(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        action = {
                            TextButton(onClick = { errorMessage = null }) {
                                Text("Dismiss", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    ) {
                        Text(error)
                    }
                }
            }
        }
    }
}

// Beautiful Post Card
@Composable
fun PostCard(postItem: PostItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Author Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        if (postItem.author.avatar != null && postItem.author.avatar.isNotEmpty()) {
                            AsyncImage(
                                model = postItem.author.avatar,
                                contentDescription = "Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Default Avatar",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "@${postItem.author.username}",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Wallet,
                                contentDescription = "Wallet",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "${postItem.author.wallet.take(6)}...${postItem.author.wallet.takeLast(4)}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }
                }

                Text(
                    text = formatTimestamp(postItem.createdAt),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }

            // Post Content
            Text(
                text = postItem.content.text,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                maxLines = 10,
                overflow = TextOverflow.Ellipsis
            )

            // Media items
            if (postItem.media.isNotEmpty()) {
                postItem.media.forEach { media ->
                    when (media.type.lowercase()) {
                        "image" -> {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                AsyncImage(
                                    model = media.url,
                                    contentDescription = "Post Media",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        "video" -> {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayCircle,
                                        contentDescription = "Play Video",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper functions
fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60000 -> "${diff / 1000}s ago"
        diff < 3600000 -> "${diff / 60000}m ago"
        diff < 86400000 -> "${diff / 3600000}h ago"
        diff < 604800000 -> "${diff / 86400000}d ago"
        else -> {
            val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
            dateFormat.format(Date(timestamp))
        }
    }
}

fun getMockPosts(): List<PostItem> {
    return listOf(
        PostItem(
            postId = "1",
            signature = "signature_1",
            author = Author(
                wallet = "7xKj9zLmN4pQrS2tUvW5yZ",
                username = "cryptowhale",
                avatar = null
            ),
            content = Content(
                text = "Just discovered an amazing new protocol on Solana! #Solana"
            ),
            media = listOf(
                MediaItem("image", "https://cryptologos.cc/logos/bitcoin-btc-logo.png", 400, 300)
            ),
            stats = Stats(likes = 234, comments = 45),
            createdAt = System.currentTimeMillis() - 3600000
        ),
        PostItem(
            postId = "2",
            signature = "signature_2",
            author = Author(
                wallet = "9AbCdEfGhIjKlMnOpQrSt",
                username = "nft_artist",
                avatar = "https://i.pravatar.cc/150?img=3"
            ),
            content = Content(
                text = "Dropping my latest NFT collection tomorrow! 🌊 Ocean-inspired generative art on Solana. 10% of proceeds go to ocean conservation."
            ),
            media = listOf(
                MediaItem("image", "https://picsum.photos/id/15/400/300", 400, 300)
            ),
            stats = Stats(likes = 567, comments = 89),
            createdAt = System.currentTimeMillis() - 7200000
        )
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewFlowScreen() {
    MaterialTheme {
        FlowScreen()
    }
}