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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
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
    modifier: Modifier = Modifier
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

    // Charming, peaceful, quiet gradient blue background for the whole screen
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFE8F5E9),
            Color(0xFFE3F2FD)
        ),
        startY = 0f,
        endY = Float.POSITIVE_INFINITY
    )

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
            .background(backgroundGradient)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header with title - using a slightly deeper but still light blue
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent,//Color(0xFFB8D0E8).copy(alpha = 0.92f), // Soft blue-gray, distinct from background
//                shadowElevation = 4.dp
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
                            color = Color(0xFF0D47A1) //Color(0xFF1A3B5C) // Deep navy for contrast
                        )

                        Row {
                            IconButton(onClick = { /* Search action */ }) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = Color(0xFF2C5A7A), // Muted blue
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            IconButton(onClick = { /* Settings action */ }) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = Color(0xFF2C5A7A), // Muted blue
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
                            color = Color(0xFF5A9BC7), // Calm blue
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Diving into the Flow...",
                            color = Color(0xFF2C5A7A), // Muted blue
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
                            contentPadding = PaddingValues(vertical = 8.dp),
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
                                            color = Color(0xFF5A9BC7),
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
                                Text("Dismiss", color = Color(0xFF3A7CA5))
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

@Composable
fun <T> RefreshableLazyColumn(
    items: List<T>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
) {
    val listState = rememberLazyListState()

    // Pull to refresh state
    var refreshState by remember { mutableStateOf(RefreshState.IDLE) }
    var dragOffset by remember { mutableFloatStateOf(0f) }

    Box(modifier = modifier) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // REMOVED the top Spacer that was causing overlap

            items(items) { item ->
                content(item)
            }

            if (isRefreshing) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF5A9BC7),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Pull to refresh indicator - adjust positioning to not overlap header
        if (refreshState != RefreshState.IDLE || dragOffset > 0f) {
            val progress = (dragOffset / 100f).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .offset(y = (-60 + dragOffset).dp)
                    .background(Color(0xFFD0E2F2).copy(alpha = 0.95f))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                when (refreshState) {
                    RefreshState.DRAGGING -> {
                        if (progress >= 1f) {
                            Text("Release to refresh", color = Color(0xFF1A3B5C), fontSize = 14.sp)
                        } else {
                            Row(horizontalArrangement = Arrangement.Center) {
                                CircularProgressIndicator(
                                    color = Color(0xFF5A9BC7),
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Pull to refresh", color = Color(0xFF1A3B5C), fontSize = 14.sp)
                            }
                        }
                    }
                    RefreshState.REFRESHING -> {
                        Row(horizontalArrangement = Arrangement.Center) {
                            CircularProgressIndicator(
                                color = Color(0xFF5A9BC7),
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Refreshing...", color = Color(0xFF1A3B5C), fontSize = 14.sp)
                        }
                    }
                    else -> {}
                }
            }
        }

        // Handle pull to refresh gestures
        LaunchedEffect(listState.isScrollInProgress) {
            snapshotFlow { listState.firstVisibleItemScrollOffset }
                .collect { offset ->
                    if (listState.firstVisibleItemIndex == 0 && offset > 0 && !isRefreshing) {
                        dragOffset = offset.toFloat()
                        if (dragOffset > 100f && refreshState != RefreshState.DRAGGING) {
                            refreshState = RefreshState.DRAGGING
                        }
                    } else if (listState.firstVisibleItemIndex > 0 && refreshState != RefreshState.IDLE) {
                        refreshState = RefreshState.IDLE
                        dragOffset = 0f
                    }
                }
        }

        // Handle refresh on release
        LaunchedEffect(dragOffset) {
            if (dragOffset > 100f && !isRefreshing && !listState.isScrollInProgress && refreshState == RefreshState.DRAGGING) {
                refreshState = RefreshState.REFRESHING
                onRefresh()
                delay(500)
                refreshState = RefreshState.IDLE
                dragOffset = 0f
            }
        }
    }
}

// Custom Tooltip
@Composable
fun CustomTooltip(
    text: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Box(
            modifier = Modifier.clickable { expanded = !expanded }
        ) {
            content()
        }

        androidx.compose.animation.AnimatedVisibility(
            visible = expanded,
            enter = androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-40).dp)
                    .wrapContentSize(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE8F0FE).copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Text(
                    text = text,
                    color = Color(0xFF1A3B5C),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

// Beautiful Post Card - using a distinct soft card color that doesn't match the background
@Composable
fun PostCard(postItem: PostItem) {
    // Card gradient that stands out from the background - softer off-white with a hint of warmth
    val cardGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFFFFFF), // Pure white top
            Color(0xFFF8FBFE)  // Very subtle icy blue-white bottom
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .background(cardGradient)
                .padding(16.dp)
        ) {
            Column(
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
                                .background(Color(0xFFC5D9F0).copy(alpha = 0.5f))
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
                                    tint = Color(0xFF5A9BC7),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "@${postItem.author.username}",
                                color = Color(0xFF1A3B5C),
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
                                    tint = Color(0xFF5A9BC7),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "${postItem.author.wallet.take(6)}...${postItem.author.wallet.takeLast(4)}",
                                    color = Color(0xFF5A7B9C),
                                    fontSize = 11.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                            }
                        }
                    }

                    Text(
                        text = formatTimestamp(postItem.createdAt),
                        color = Color(0xFF7A9BBA),
                        fontSize = 11.sp
                    )
                }

                // Post Content
                Text(
                    text = postItem.content.text,
                    color = Color(0xFF2C3E50), // Dark slate for readability
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
                                        containerColor = Color(0xFFE0ECF8)
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayCircle,
                                            contentDescription = "Play Video",
                                            tint = Color(0xFF5A9BC7),
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Text(
                                            text = "Video Content",
                                            color = Color(0xFF3A7CA5),
                                            modifier = Modifier.align(Alignment.BottomCenter)
                                                .padding(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

//                HorizontalDivider(
//                    Modifier,
//                    thickness = 0.5.dp,
//                    color = Color(0xFFC5D9F0)
//                )

                // Stats Row (commented out in original, but updating colors in case it's uncommented)
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceEvenly
//                ) {
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.spacedBy(6.dp)
//                    ) {
//                        IconButton(
//                            onClick = { /* Like action */ },
//                            modifier = Modifier.size(32.dp)
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.FavoriteBorder,
//                                contentDescription = "Like",
//                                tint = Color(0xFFFF8A8A),
//                                modifier = Modifier.size(20.dp)
//                            )
//                        }
//                        Text(
//                            text = "${postItem.stats.likes}",
//                            color = Color(0xFF4A6A8A),
//                            fontSize = 13.sp
//                        )
//                    }
//
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.spacedBy(6.dp)
//                    ) {
//                        IconButton(
//                            onClick = { /* Comment action */ },
//                            modifier = Modifier.size(32.dp)
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.ChatBubbleOutline,
//                                contentDescription = "Comment",
//                                tint = Color(0xFF5A9BC7),
//                                modifier = Modifier.size(20.dp)
//                            )
//                        }
//                        Text(
//                            text = "${postItem.stats.comments}",
//                            color = Color(0xFF4A6A8A),
//                            fontSize = 13.sp
//                        )
//                    }
//
//                    IconButton(
//                        onClick = { /* Share action */ },
//                        modifier = Modifier.size(32.dp)
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.Share,
//                            contentDescription = "Share",
//                            tint = Color(0xFF7A9BBA),
//                            modifier = Modifier.size(20.dp)
//                        )
//                    }
//
//                    CustomTooltip(
//                        text = "Solana Signature: ${postItem.signature}",
//                        modifier = Modifier
//                    ) {
//                        IconButton(
//                            onClick = {},
//                            modifier = Modifier.size(32.dp)
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.Verified,
//                                contentDescription = "Blockchain Verified",
//                                tint = Color(0xFF5A9BC7),
//                                modifier = Modifier.size(20.dp)
//                            )
//                        }
//                    }
//                }
            }
        }
    }
}

// Mock Data Functions
suspend fun loadMockData() {
    delay(800)
    // This would be replaced with actual API call
}

fun getMockPosts(): List<PostItem> {
    return listOf(
        PostItem(
            postId = "1",
            signature = "5z6sKxqZjQpZv9WxYvZKJ3tL5qXqXqXqXqXqXqXqXqXqXqXqXqXqXqXqXqX",
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
            signature = "7yLtMpRnS2uWxZ4bDfGhJkLmNpQrStUvWxYz1A2B3C4D5E6F7G8H9I0J1K",
            author = Author(
                wallet = "9AbCdEfGhIjKlMnOpQrSt",
                username = "nft_artist",
                avatar = "https://i.pravatar.cc/150?img=3"
            ),
            content = Content(
                text = "Dropping my latest NFT collection tomorrow! 🌊 Ocean-inspired generative art on Solana. 10% of proceeds go to ocean conservation. Link in bio!"
            ),
            media = listOf(
                MediaItem("image", "https://picsum.photos/id/15/400/300", 400, 300),
                MediaItem("image", "https://picsum.photos/id/26/400/300", 400, 300)
            ),
            stats = Stats(likes = 567, comments = 89),
            createdAt = System.currentTimeMillis() - 7200000
        ),
        PostItem(
            postId = "3",
            signature = "8zNqStUvWxYz1B3D5F7H9J1L3N5P7R9T1V3X5Z7B9D1F3H5J7L9N1P",
            author = Author(
                wallet = "3XyZwVuTsRqPoNmLkJhGf",
                username = "solana_dev",
                avatar = null
            ),
            content = Content(
                text = "Just built my first program! 🎉 The learning curve is steep but rewarding. Check out the GitHub repo. #SolanaDev"
            ),
            media = emptyList(),
            stats = Stats(likes = 892, comments = 156),
            createdAt = System.currentTimeMillis() - 86400000
        ),
        PostItem(
            postId = "4",
            signature = "4AbCdEfGhIjKlMnOpQrStUvWxYz1A2B3C4D5E6F7G8H9I0J1K2L3M",
            author = Author(
                wallet = "5MnBvCxZaLkQjWhGtFdSe",
                username = "defi_daily",
                avatar = "https://i.pravatar.cc/150?img=7"
            ),
            content = Content(
                text = "Solana ecosystem update: TVL up 45% this month! 🚀 Here's a thread on the top performing protocols and what's coming next. 🧵"
            ),
            media = listOf(
                MediaItem("image", "https://picsum.photos/id/0/400/300", 400, 300)
            ),
            stats = Stats(likes = 1234, comments = 234),
            createdAt = System.currentTimeMillis() - 172800000
        ),
        PostItem(
            postId = "5",
            signature = "6BcDeFgHiJkLmNoPqRsTuVwXyZ1A2B3C4D5E6F7G8H9I0J1K2L3M4N",
            author = Author(
                wallet = "8PlQwErTyUiOpAsDfGhJk",
                username = "validator_zone",
                avatar = null
            ),
            content = Content(
                text = "Validator rewards hit new ATH! 🎯 Staking APY currently at 7.2%. Check your stake accounts and consider redelegating for better returns. #SolanaStaking"
            ),
            media = listOf(
                MediaItem("video", "https://example.com/video.mp4", 1920, 1080)
            ),
            stats = Stats(likes = 445, comments = 67),
            createdAt = System.currentTimeMillis() - 259200000
        ),
        PostItem(
            postId = "6",
            signature = "9CdEfGhIjKlMnOpQrStUvWxYz1A2B3C4D5E6F7G8H9I0J1K2L3M4N5O",
            author = Author(
                wallet = "2XyZwVuTsRqPoNmLkJhGfD",
                username = "memelord",
                avatar = "https://i.pravatar.cc/150?img=9"
            ),
            content = Content(
                text = "When you finally know 🤯\n\n*taps head* \nDon't need to store data on-chain if you derive it from seeds! \n\n#SolanaDev #BlockchainLife"
            ),
            media = listOf(
                MediaItem("image", "https://picsum.photos/id/1/400/300", 400, 300)
            ),
            stats = Stats(likes = 3456, comments = 789),
            createdAt = System.currentTimeMillis() - 345600000
        ),
        PostItem(
            postId = "7",
            signature = "1DeFgHiJkLmNoPqRsTuVwXyZ1A2B3C4D5E6F7G8H9I0J1K2L3M4N5O6P",
            author = Author(
                wallet = "7MnBvCxZaLkQjWhGtFdSeR",
                username = "solana_gamer",
                avatar = null
            ),
            content = Content(
                text = "Play-to-earn gaming on Solana is evolving FAST! 🎮 Just tried the new racing game and earned $50 in tokens. Who's playing? Drop your gamertags! 👇"
            ),
            media = listOf(
                MediaItem("image", "https://picsum.photos/id/42/400/300", 400, 300),
                MediaItem("image", "https://picsum.photos/id/96/400/300", 400, 300),
                MediaItem("image", "https://picsum.photos/id/77/400/300", 400, 300)
            ),
            stats = Stats(likes = 678, comments = 123),
            createdAt = System.currentTimeMillis() - 432000000
        )
    )
}

// For preview with mock data - Call this in LaunchedEffect
suspend fun initializeWithMockData(
    onDataLoaded: (List<PostItem>, String?) -> Unit
) {
    delay(500) // Simulate network delay
    val mockPosts = getMockPosts()
    onDataLoaded(mockPosts, "next_cursor_123")
}

// Update the loadMockData function in FlowFeedScreen to actually load data
// Replace the existing LaunchedEffect with this:
suspend fun loadMockDataForScreen(
    setFeedItems: (List<PostItem>) -> Unit,
    setCursor: (String?) -> Unit,
    setIsLoading: (Boolean) -> Unit
) {
    setIsLoading(true)
    delay(800)
    val mockPosts = getMockPosts()
    setFeedItems(mockPosts)
    setCursor("next_cursor_123")
    setIsLoading(false)
}

suspend fun loadMoreMockDataForScreen(
    currentItems: List<PostItem>,
    setFeedItems: (List<PostItem>) -> Unit,
    setCursor: (String?) -> Unit,
    setIsLoading: (Boolean) -> Unit
) {
    setIsLoading(true)
    delay(800)
    val morePosts = getMockPosts().map { post ->
        post.copy(
            postId = "${System.currentTimeMillis()}_${post.postId}",
            createdAt = System.currentTimeMillis() - (currentItems.size * 3600000L)
        )
    }
    setFeedItems(currentItems + morePosts)
    setCursor(if (currentItems.size > 20) null else "next_cursor_${currentItems.size}")
    setIsLoading(false)
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

enum class RefreshState {
    IDLE, DRAGGING, REFRESHING
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewFlowScreen() {
    MaterialTheme {
        FlowScreen()
    }
}