package com.example.cyloop.screens.wallet

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.cyloop.format
import com.example.cyloop.api.Coin
import com.example.cyloop.api.CoinGeckoService
import com.example.cyloop.components.FloatingNotification
import com.example.cyloop.components.NotificationType
import com.example.cyloop.components.rememberNotificationState
import com.example.cyloop.font.UIFont
import com.example.cyloop.storage.AuthPreferences
import com.example.cyloop.theme.getAppBackgroundBrush
import com.example.cyloop.screens.main.TabBarView
import com.example.cyloop.crypto.WalletManager
import kotlinx.coroutines.launch
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import coil3.request.ImageRequest
import coil3.request.crossfade

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    onWalletDetailClick: (String?) -> Unit = {},
    onPaymentClick: () -> Unit = {},
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    var coins by remember { mutableStateOf<List<Coin>>(emptyList()) }
    var selectedCoin by remember { mutableStateOf<Coin?>(null) }
    var chartData by remember { mutableStateOf<List<Double>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isChartLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val notificationState = rememberNotificationState()

    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()

    val walletAddress by WalletManager.walletAddress.collectAsState()
    var showOnboarding by remember { mutableStateOf(false) }
    val isWalletOnboarded by AuthPreferences.isWalletOnboarded().collectAsState(initial = false)

    val isBalanceVisible by AuthPreferences.isBalanceVisible().collectAsState(initial = true)
    val userBalance = if (walletAddress != null) "0.00" else "0.00" // Should fetch real balance

    val isDark = true //isSystemInDarkTheme()
    val backgroundGradient = getAppBackgroundBrush()

    LaunchedEffect(Unit) {
        if (isLoading) {
            try {
                val result = CoinGeckoService.getCoins()
                coins = result
                if (selectedCoin == null) {
                    selectedCoin = result.firstOrNull()
                }
            } catch (e: Exception) {
                errorMessage = "Failed to load market data: ${e.message}"
                notificationState.showNotification(errorMessage!!, NotificationType.ERROR)
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(selectedCoin) {
        selectedCoin?.let { coin ->
            isChartLoading = true
            try {
                val result = CoinGeckoService.getMarketChart(coin.id)
                if (result.prices.isNotEmpty()) {
                    chartData = result.prices.map { it[1] }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                chartData = emptyList()
            } finally {
                isChartLoading = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        Column(modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()) {
            // Part 1: Top Section - Chart Card
            val priceChange = selectedCoin?.price_change_percentage_24h ?: 0.0
            val isPositive = priceChange >= 0
            val chartColor = if (isPositive) Color(0xFF00E676) else Color(0xFFFF5252)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 6.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = if (isDark) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)) else null
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (isDark) {
                                Brush.verticalGradient(
                                    listOf(Color(0xFF151525), Color(0xFF0B0B15))
                                )
                            } else {
                                Brush.verticalGradient(
                                    listOf(Color(0xFFF5F9FF), Color.White)
                                )
                            }
                        )
                        .padding(vertical = 20.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                            selectedCoin?.let { coin ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = coin.name,
                                            color = if (isDark) Color.White else Color.Black,
                                            style = UIFont.ChatName
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${if (isPositive) "+" else ""}${
                                                priceChange.format(2)
                                            }% (24h)",
                                            color = chartColor,
                                            style = UIFont.Metadata.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                            contentDescription = null,
                                            tint = chartColor,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "$${formatPrice(coin.current_price)}",
                                    color = if (isDark) Color.White else Color.Black,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Column(
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                                .weight(1f)
                        ) {
                            if (isChartLoading) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = chartColor.copy(alpha = 0.5f))
                                }
                            } else if (chartData.isNotEmpty()) {
                                CoinChart(
                                    data = chartData,
                                    modifier = Modifier.fillMaxSize(),
                                    color = chartColor
                                )
                            } else if (!isLoading && errorMessage == null) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No chart data", color = Color.White.copy(alpha = 0.5f))
                                }
                            }
                        }
                    }
                }
            }

            // Part 3: Bottom Section
            val goldColor = Color(0xFFF5D45E)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Wallet Button with Balance
                Surface(
                    modifier = Modifier
                        .weight(1.4f)
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDark) Color.Black else Color.White,
                    border = BorderStroke(1.dp, if (isDark) goldColor.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    shadowElevation = if (isDark) 4.dp else 2.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { 
                                    if (walletAddress != null) {
                                        onWalletDetailClick(null)
                                    } else {
                                        showOnboarding = true
                                    }
                                }
                                .padding(start = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = if (isDark) goldColor else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    if (walletAddress != null) "My Wallet" else "Setup Wallet",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp,
                                    color = if (isDark) goldColor.copy(alpha = 0.7f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = if (walletAddress != null) {
                                        if (isBalanceVisible) "$$userBalance" else "$ ****"
                                    } else {
                                        "Connect"
                                    },
                                    modifier = Modifier.blur(if (isBalanceVisible || walletAddress == null) 0.dp else 4.dp),
                                    style = UIFont.ChatName,
                                    color = if (isDark) Color.White else Color.Black
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                scope.launch {
                                    AuthPreferences.setBalanceVisible(!isBalanceVisible)
                                }
                            },
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isBalanceVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Balance",
                                tint = if (isDark) goldColor.copy(alpha = 0.6f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Payment Button
                Button(
                    onClick = onPaymentClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .shadow(if (isDark) 4.dp else 2.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) goldColor else MaterialTheme.colorScheme.primary,
                        contentColor = if (isDark) Color.Black else Color.White
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Payment", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                }
            }

            // Part 2: Middle Section - Scrollable List
            Box(modifier = Modifier.weight(1f)) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (errorMessage != null && coins.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
//                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Red)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = errorMessage!!, textAlign = TextAlign.Center, color = Color.Gray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { isLoading = true }) {
                            Text("Retry")
                        }
                        Spacer(modifier = Modifier.height(200.dp))
                    }
                } else {
                    PullToRefreshBox(
                        state = pullToRefreshState,
                        isRefreshing = isRefreshing,
                        onRefresh = {
                            scope.launch {
                                isRefreshing = true
                                try {
                                    coins = CoinGeckoService.getCoins()
                                } catch (e: Exception) {
                                    notificationState.showNotification("Refresh failed: ${e.message}", NotificationType.ERROR)
                                } finally {
                                    isRefreshing = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    // Stretch effect based on pull distance
                                    val scale =
                                        1f + (pullToRefreshState.distanceFraction * 0.1f).coerceAtMost(
                                            0.15f
                                        )
                                    scaleY = scale
                                    transformOrigin = TransformOrigin(0.5f, 0f)
                                },
                            contentPadding = PaddingValues(bottom = bottomPadding + 110.dp)
                        ) {
                            items(coins) { coin ->
                                CoinListItem(
                                    coin = coin,
                                    isSelected = selectedCoin?.id == coin.id,
                                    onClick = { selectedCoin = coin }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        FloatingNotification(state = notificationState)

        if (showOnboarding) {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                WalletOnboardingFlow(
                    onFinish = { msg ->
                        scope.launch {
                            AuthPreferences.setWalletOnboarded(true)
                            showOnboarding = false
                            // Immediately open the wallet info view after onboarding
                            onWalletDetailClick(msg)
                        }
                    },
                    onBack = { showOnboarding = false }
                )
            }
        }
    }
}

private fun formatPrice(price: Double): String {
    return price.format(2)
}

@Composable
fun CoinChart(
    data: List<Double>,
    modifier: Modifier = Modifier,
    color: Color = Color.White
) {
    if (data.size < 2) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(
                text = "No historical data available",
                color = color.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
        }
        return
    }

    val isDark = isSystemInDarkTheme()
    val goldColor = Color(0xFFF5D45E)

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(data) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
        )
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(data) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            selectedIndex = (offset.x / (size.width / (data.size - 1))).toInt()
                                .coerceIn(0, data.size - 1)
                        },
                        onDrag = { change, _ ->
                            selectedIndex =
                                (change.position.x / (size.width / (data.size - 1))).toInt()
                                    .coerceIn(0, data.size - 1)
                        },
                        onDragEnd = { selectedIndex = null },
                        onDragCancel = { selectedIndex = null }
                    )
                }
                .pointerInput(data) {
                    detectTapGestures(
                        onPress = { offset ->
                            selectedIndex = (offset.x / (size.width / (data.size - 1))).toInt()
                                .coerceIn(0, data.size - 1)
                            tryAwaitRelease()
                            selectedIndex = null
                        }
                    )
                }
        ) {
            val min = data.minOrNull() ?: 0.0
            val max = data.maxOrNull() ?: 0.0
            val range = (max - min).coerceAtLeast(0.000001)
            val width = size.width
            val height = size.height
            val spacing = width / (data.size - 1)

            val points = data.mapIndexed { index, price ->
                Offset(
                    x = index * spacing,
                    y = height - ((price - min) / range * height).toFloat()
                )
            }

            val path = Path().apply {
                if (points.isNotEmpty()) {
                    moveTo(points[0].x, points[0].y)
                    for (i in 1 until points.size) {
                        val p0 = points[i - 1]
                        val p1 = points[i]
                        // Smooth curves using cubic splines
                        cubicTo(
                            x1 = (p0.x + p1.x) / 2, y1 = p0.y,
                            x2 = (p0.x + p1.x) / 2, y2 = p1.y,
                            x3 = p1.x, y3 = p1.y
                        )
                    }
                }
            }

            val fillPath = Path().apply {
                addPath(path)
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }

            // Clip the drawing for entrance animation
            clipRect(right = width * animationProgress.value) {
                // Background Gradient Fill
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = if (isDark) {
                            listOf(
                                color.copy(alpha = 0.4f),      // Trend color (Green/Red)
                                goldColor.copy(alpha = 0.15f),  // Gold yellow middle
                                Color.Black.copy(alpha = 0.8f) // Black bottom
                            )
                        } else {
                            listOf(
                                color.copy(alpha = 0.35f),
                                color.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        }
                    )
                )

                // Main Smooth Line
                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Last Point Pulse Effect (when not interacting)
                if (selectedIndex == null && points.isNotEmpty() && animationProgress.value > 0.99f) {
                    val lastPoint = points.last()
                    drawCircle(color, 4.dp.toPx(), lastPoint)
                    drawCircle(color.copy(alpha = 0.2f), 10.dp.toPx(), lastPoint)
                }
            }

            // Interaction Overlays
            selectedIndex?.let { index ->
                val point = points[index]
                
                // Vertical guide line
                drawLine(
                    color = color.copy(alpha = 0.4f),
                    start = Offset(point.x, 0f),
                    end = Offset(point.x, height),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f))
                )

                // Highlighted data point
                drawCircle(
                    color = Color.White.copy(alpha = 0.5f),
                    radius = 8.dp.toPx(),
                    center = point
                )
                drawCircle(
                    color = color,
                    radius = 4.dp.toPx(),
                    center = point
                )
            }
        }

        // Floating Price Tooltip during interaction
        selectedIndex?.let { index ->
            val price = data[index]
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 4.dp)
                    .shadow(8.dp, RoundedCornerShape(8.dp)),
                color = Color.Black.copy(alpha = 0.8f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "$${formatPrice(price)}",
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CoinListItem(
    coin: Coin,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .clickable(onClick = onClick)
            .then(
                if (isSelected) Modifier.border(
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp)
                ) else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        } else {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        },
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon section
            AsyncImage(
                model = coin.image,
                contentDescription = null,
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Fit,
                onError = {
                    println("Error loading image: ${it.result.throwable}")
                    // This will show you the actual error
                }
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = coin.name,
                    style = UIFont.ChatName.copy(fontSize = 15.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = coin.symbol.uppercase(),
                    style = UIFont.Metadata.copy(fontSize = 10.sp),
                    color = Color.Gray
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${formatPrice(coin.current_price)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val priceChange = coin.price_change_percentage_24h ?: 0.0
                Text(
                    text = "${if (priceChange >= 0) "+" else ""}${priceChange.format(2)}%",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (priceChange >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun CoinImage(imageUrl: String) {
    AsyncImage(
        model = imageUrl,
        contentDescription = "Coin image",
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape),
        colorFilter = null // Remove any tint
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewWalletScreenView() {
    MaterialTheme {
        WalletScreen({}, {})
    }
}