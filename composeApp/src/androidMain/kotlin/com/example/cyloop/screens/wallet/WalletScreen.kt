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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.cyloop.storage.AuthPreferences
import com.example.cyloop.screens.main.TabBarView
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import coil3.request.ImageRequest
import coil3.request.crossfade

@Serializable
data class Coin(
    val id: String,
    val symbol: String,
    val name: String,
    val image: String,
    val current_price: Double,
    val price_change_percentage_24h: Double? = 0.0
)

@Serializable
data class MarketChart(
    val prices: List<List<Double>>
)

private val client = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = true
        })
    }

    install(HttpTimeout) {
        requestTimeoutMillis = 30000
        connectTimeoutMillis = 15000
        socketTimeoutMillis = 15000
    }

    defaultRequest {
        header("Accept", "application/json")
        header("User-Agent", "Ktor client")
    }
}

private const val API_KEY = "CG-Z1ASMjuxEc3b5c5Z5Fyjvj3K"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    onWalletDetailClick: () -> Unit = {},
    onPaymentClick: () -> Unit = {},
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    var coins by remember { mutableStateOf<List<Coin>>(emptyList()) }
    var selectedCoin by remember { mutableStateOf<Coin?>(null) }
    var chartData by remember { mutableStateOf<List<Double>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isChartLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()

    val context = LocalContext.current
    val isBalanceVisible by AuthPreferences.isBalanceVisible(context).collectAsState(initial = true)
    val userBalance = "1,234.56"

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFE8F5E9),
            Color(0xFFE3F2FD)
        )
    )

    LaunchedEffect(Unit) {
        if (isLoading) {
            refreshCoins(
                onSuccess = { result ->
                    coins = result
                    if (selectedCoin == null) {
                        selectedCoin = result.firstOrNull()
                    }
                },
                onError = { msg ->
                    errorMessage = msg
                },
                onFinished = {
                    isLoading = false
                }
            )
        }
    }

    LaunchedEffect(selectedCoin) {
        selectedCoin?.let { coin ->
            isChartLoading = true
            try {
                val response = client.get("https://api.coingecko.com/api/v3/coins/${coin.id}/market_chart") {
                    parameter("vs_currency", "usd")
                    parameter("days", "7")
                    parameter("x_cg_demo_api_key", API_KEY)
                }
                val result = response.body<MarketChart>()
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
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            // Part 1: Top Section - Chart Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF0D47A1), Color(0xFF1976D2))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        selectedCoin?.let { coin ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = coin.name,
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = coin.symbol.uppercase(),
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 14.sp
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "$${formatPrice(coin.current_price)}",
                                color = Color.White,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            val priceChange = coin.price_change_percentage_24h ?: 0.0
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (priceChange >= 0) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = if (priceChange >= 0) Color(0xFF81C784) else Color(0xFFF28B82),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${String.format("%.2f", Math.abs(priceChange))}% (24h)",
                                    color = if (priceChange >= 0) Color(0xFF81C784) else Color(0xFFF28B82),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (isChartLoading) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Color.White.copy(alpha = 0.5f))
                            }
                        } else if (chartData.isNotEmpty()) {
                            CoinChart(
                                data = chartData,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                color = Color.White
                            )
                        } else if (!isLoading && errorMessage == null) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No chart data", color = Color.White.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }

//            Text(
//                text = "Market Trends",
//                fontSize = 20.sp,
//                fontWeight = FontWeight.ExtraBold,
//                modifier = Modifier
//                    .padding(horizontal = 24.dp)
//                    .padding(bottom = 8.dp),
//                color = Color(0xFF1A237E)
//            )

            // Part 2: Middle Section - Scrollable List
            Box(modifier = Modifier.weight(1f)) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF1976D2))
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
                                refreshCoins(
                                    onSuccess = { result ->
                                        coins = result
                                    },
                                    onError = { /* Keep existing data but maybe show a toast */ },
                                    onFinished = {
                                        isRefreshing = false
                                    }
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    // Stretch effect based on pull distance
                                    val scale = 1f + (pullToRefreshState.distanceFraction * 0.1f).coerceAtMost(0.15f)
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

        // Part 3: Bottom Section - Full Width Buttons (Floating Overlay)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = bottomPadding + 16.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp))
                    .background(Color(0xFF1565C0), RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onWalletDetailClick() }
                        .padding(start = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Wallet", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = if (isBalanceVisible) "$$userBalance" else "$ *****",
                        modifier = Modifier.blur(if (isBalanceVisible) 0.dp else 4.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }

                IconButton(
                    onClick = {
                        scope.launch {
                            AuthPreferences.setBalanceVisible(context, !isBalanceVisible)
                        }
                    },
                    modifier = Modifier.padding(end = 8.dp).size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isBalanceVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle Balance",
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Button(
                onClick = onPaymentClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Icon(Icons.Default.Payments, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Payment", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

private suspend fun refreshCoins(
    onSuccess: (List<Coin>) -> Unit,
    onError: (String) -> Unit,
    onFinished: () -> Unit
) {
    try {
        val response = client.get("https://api.coingecko.com/api/v3/coins/markets") {
            parameter("vs_currency", "usd")
            parameter("order", "market_cap_desc")
            parameter("per_page", 50)
            parameter("page", 1)
            parameter("sparkline", "false")
            parameter("price_change_percentage", "24h")
            parameter("x_cg_demo_api_key", API_KEY)
        }
        val result = response.body<List<Coin>>()
        if (result.isNotEmpty()) {
            onSuccess(result)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        onError("Failed to load market data: ${e.message}")
    } finally {
        onFinished()
    }
}

private fun formatPrice(price: Double): String {
    return try {
        String.format("%,.2f", price)
    } catch (_: Exception) {
        price.toString()
    }
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
                            selectedIndex = (offset.x / (size.width / (data.size - 1))).toInt().coerceIn(0, data.size - 1)
                        },
                        onDrag = { change, _ ->
                            selectedIndex = (change.position.x / (size.width / (data.size - 1))).toInt().coerceIn(0, data.size - 1)
                        },
                        onDragEnd = { selectedIndex = null },
                        onDragCancel = { selectedIndex = null }
                    )
                }
                .pointerInput(data) {
                    detectTapGestures(
                        onPress = { offset ->
                            selectedIndex = (offset.x / (size.width / (data.size - 1))).toInt().coerceIn(0, data.size - 1)
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
                        colors = listOf(
                            color.copy(alpha = 0.35f),
                            color.copy(alpha = 0.1f),
                            Color.Transparent
                        )
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
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Color.White else Color.LightGray.copy(alpha = 0.3f),
//        shadowElevation = if (isSelected) 4.dp else 1.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon section
            AsyncImage(
                model = coin.image,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
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
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1A237E)
                )
                Text(
                    text = coin.symbol.uppercase(),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${formatPrice(coin.current_price)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = Color(0xFF1A237E)
                )
                val priceChange = coin.price_change_percentage_24h ?: 0.0
                Text(
                    text = "${if (priceChange >= 0) "+" else ""}${String.format("%.2f", priceChange)}%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (priceChange >= 0) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                )
            }
        }
    }
}

@Composable
fun CoinImage(imageUrl: String) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        contentDescription = "Coin image",
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape),
        colorFilter = null // Remove any tint
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewTabBarView() {
    MaterialTheme {
        WalletScreen({}, {})
    }
}