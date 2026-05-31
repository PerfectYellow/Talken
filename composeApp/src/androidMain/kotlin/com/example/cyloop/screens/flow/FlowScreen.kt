package com.example.cyloop.screens.flow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.cyloop.theme.CyLoopTheme
import com.example.cyloop.theme.getAppBackgroundBrush

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

@Composable
fun FlowScreen(
    modifier: Modifier = Modifier,
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    val feedItems = getMockPosts()

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

            // List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = bottomPadding + 20.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(feedItems) { post ->
                    FlowCell(flowItem = post)
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
            stats = Stats(likes = 0, comments = 0),
            createdAt = System.currentTimeMillis()
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
            stats = Stats(likes = 0, comments = 0),
            createdAt = System.currentTimeMillis()
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
