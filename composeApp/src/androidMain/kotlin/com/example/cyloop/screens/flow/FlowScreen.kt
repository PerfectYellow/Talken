package com.example.cyloop.screens.flow

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Surface
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.ripple

data class FlowContentItem(
    val id: Int,
    val title: String,
    val subtitle: String,
    val description: String,
    val imageUrl: String? = null,
    val date: Date,
    val type: ContentType,
    val readTime: Int,
    val isBookmarked: Boolean = false
)

enum class ContentType {
    NEWS, PAPER, RESEARCH, ARTICLE
}

@Composable
fun FlowScreen(
    onItemClick: (FlowContentItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var items by remember { mutableStateOf(createSampleData()) }
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val onRefresh: () -> Unit = {
        scope.launch {
            isRefreshing = true
            delay(1500)
            items = createSampleData().shuffled()
            isRefreshing = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE3F2FD),
                        Color(0xFFBBDEFB),
                        Color(0xFF90CAF9),
                        Color(0xFFE8EAF6)
                    ),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
    ) {
        // Sticky Top Bar
        TopBar()

        // Scrollable Content with Pull to Refresh
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 16.dp, horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(items) { item ->
                    FlowContentCard(
                        item = item,
                        onClick = { onItemClick(item) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "✨ Stay curious, keep exploring ✨",
                        fontSize = 12.sp,
                        color = Color(0xFF546E7A),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun TopBar() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
//            .background(
//                Brush.verticalGradient(
//                    colors = listOf(
//                        Color(0xFFE3F2FD).copy(alpha = 0.95f),
//                        Color(0xFFBBDEFB).copy(alpha = 0.95f)
//                    )
//                )
//            )
//            .statusBarsPadding()
//            .shadow(4.dp)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Flow",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1565C0),
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun FlowContentCard(
    item: FlowContentItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isBookmarked by remember { mutableStateOf(item.isBookmarked) }
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick,
                indication = ripple(
                    color = Color(0xFF1565C0).copy(alpha = 0.08f),
                    bounded = true,
                    radius = 261.dp
                ),
                interactionSource = interactionSource,
            )
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color(0xFF1565C0).copy(alpha = 0.08f)
            ),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
        ) {
            // Image section (if available)
            if (item.imageUrl != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = item.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                        contentScale = ContentScale.Crop
                    )

                    // Soft gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0xFF1565C0).copy(alpha = 0.2f)
                                    ),
                                    startY = Offset.Zero.y,
                                    endY = Float.POSITIVE_INFINITY
                                )
                            )
                    )

                    // Content type badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                    ) {
                        ContentTypeBadge(type = item.type)
                    }
                }
            } else {
                // Placeholder for items without image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFE3F2FD),
                                    Color(0xFFBBDEFB)
                                )
                            )
                        )
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📚",
                            fontSize = 48.sp
                        )
                    }

                    // Content type badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                    ) {
                        ContentTypeBadge(type = item.type)
                    }
                }
            }

            // Content section (rest remains the same)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Date and read time row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatDate(item.date),
                            fontSize = 12.sp,
                            color = Color(0xFF78909C),
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0xFF90CAF9))
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "${item.readTime} min read",
                            fontSize = 12.sp,
                            color = Color(0xFF78909C),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    IconButton(
                        onClick = { isBookmarked = !isBookmarked },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) Color(0xFF42A5F5) else Color(0xFFB0BEC5),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Title
                Text(
                    text = item.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A237E),
                    lineHeight = 26.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subtitle
                Text(
                    text = item.subtitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF546E7A),
                    lineHeight = 20.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Description
                Text(
                    text = item.description,
                    fontSize = 14.sp,
                    color = Color(0xFF546E7A),
                    lineHeight = 20.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ActionButton(
                        icon = Icons.Default.FavoriteBorder,
                        text = "Like",
                        onClick = { /* Handle like action */ }
                    )
                    ActionButton(
                        icon = Icons.Default.Share,
                        text = "Share",
                        onClick = { /* Handle share action */ }
                    )
                    ActionButton(
                        icon = Icons.Default.BookmarkBorder,
                        text = "Save",
                        onClick = { /* Handle save action */ }
                    )
                }
            }
        }
    }
}

@Composable
fun ContentTypeBadge(type: ContentType) {
    val (label, color) = when (type) {
        ContentType.NEWS -> "News" to Color(0xFF64B5F6)
        ContentType.PAPER -> "Paper" to Color(0xFF81C784)
        ContentType.RESEARCH -> "Research" to Color(0xFFBA68C8)
        ContentType.ARTICLE -> "Article" to Color(0xFFFFB74D)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(9.dp))
            .background(color.copy(alpha = 0.9f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFF0F4F8))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = Color(0xFF5C6BC0),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            color = Color(0xFF5C6BC0),
            fontWeight = FontWeight.Medium
        )
    }
}

// Helper function to format date
fun formatDate(date: Date): String {
    val now = Date()
    val diff = now.time - date.time
    val days = diff / (24 * 60 * 60 * 1000)

    return when {
        days == 0L -> "Today"
        days == 1L -> "Yesterday"
        days < 7 -> "$days days ago"
        else -> SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(date)
    }
}

// Sample data generator
fun createSampleData(): List<FlowContentItem> {
    val today = Date()
    val yesterday = Date(today.time - (24 * 60 * 60 * 1000))
    val twoDaysAgo = Date(today.time - (2 * 24 * 60 * 60 * 1000))

    return listOf(
        FlowContentItem(
            id = 1,
            title = "The Future of AI in Creative Industries",
            subtitle = "How machine learning is transforming art and design",
            description = "Artificial intelligence is no longer just a tool for automation. It's becoming a creative partner, helping artists, designers, and musicians push the boundaries of their craft. This comprehensive analysis explores the latest developments in generative AI, machine learning algorithms, and their impact on creative workflows across various industries.",
            imageUrl = null,
            date = today,
            type = ContentType.ARTICLE,
            readTime = 5
        ),
        FlowContentItem(
            id = 2,
            title = "New Study Reveals Benefits of Mindfulness",
            subtitle = "Research shows meditation improves focus and wellbeing",
            description = "A comprehensive study conducted over 2 years shows that regular mindfulness practice can significantly reduce stress and improve cognitive function. Learn about the science behind meditation and its profound effects on mental health and productivity.",
            imageUrl = null,
            date = yesterday,
            type = ContentType.RESEARCH,
            readTime = 8
        ),
        FlowContentItem(
            id = 3,
            title = "Breaking: Major Tech Announcement",
            subtitle = "Industry leaders unveil revolutionary platform",
            description = "In a surprising turn of events, leading tech companies have come together to announce a new open standard for data sharing and privacy. This marks a significant shift in how user data will be handled across platforms.",
            imageUrl = null,
            date = twoDaysAgo,
            type = ContentType.NEWS,
            readTime = 3
        ),
        FlowContentItem(
            id = 4,
            title = "Sustainable Design Principles",
            subtitle = "Creating products that last and inspire",
            description = "Exploring the intersection of sustainability and user-centered design. Learn how to create products that are both beautiful and environmentally conscious, with practical tips for implementing eco-friendly design practices.",
            imageUrl = null,
            date = twoDaysAgo,
            type = ContentType.PAPER,
            readTime = 12
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