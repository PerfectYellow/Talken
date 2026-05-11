package com.example.cyloop.screens.auth

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import cyloop.composeapp.generated.resources.Res
import cyloop.composeapp.generated.resources.logo
import org.jetbrains.compose.resources.painterResource

@Composable
fun WelcomeScreen(
    onSignInClick: () -> Unit,
    onMainNetClick: () -> Unit,
    onDevNetClick: () -> Unit
) {
    // State for popup visibility
    var showPopup by remember { mutableStateOf(false) }

    // Animated gradient offset for live gradient effect
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradientOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F2027),
                        Color(0xFF203A43),
                        Color(0xFF2C5364)
                    )
                )
            )
    ) {
        // Decorative circles in background
        DecorativeCircles()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Animated cycling icon with curved background
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.sweepGradient(
                            colors = listOf(
                                Color(0xFF4A90E2),
                                Color(0xFF357ABD),
                                Color(0xFF4A90E2),
                                Color(0xFF6BB5FF),
                                Color(0xFF4A90E2)
                            )
                        )
                    )
                    .shadow(
                        elevation = 15.dp,
                        shape = CircleShape,
                        ambientColor = Color(0xFF4A90E2),
                        spotColor = Color(0xFF4A90E2)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Inner circle
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.95f))
                        .border(
                            width = 3.dp,
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF4A90E2), Color(0xFF6BB5FF))
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Logo image with curved shape
                    Image(
                        painter = painterResource(Res.drawable.logo),
                        contentDescription = "Cyloop Logo",
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Animated gradient text for "Cyloop"
            val animatedBrush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF6BB5FF),
                    Color(0xFF9BFFFF),
                    Color(0xFF6BB5FF),
                    Color(0xFF4A90E2),
                    Color(0xFF6BB5FF)
                )
            )

            Text(
                text = "Stay Invisible",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                style = TextStyle(
                    brush = animatedBrush,
                    shadow = Shadow(
                        color = Color(0xFF4A90E2).copy(alpha = 0.3f),
                        offset = Offset(4f, 4f),
                        blurRadius = 8f
                    )
                ),
                letterSpacing = 3.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tagline with curved design
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.2f),
                                Color.White.copy(alpha = 0.1f),
                                Color.White.copy(alpha = 0.2f)
                            )
                        ),
                        shape = RoundedCornerShape(50.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "To get started, Create a new account or SignIn an existing one",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(60.dp))

            // Row for Sign In and Gear button
            Box {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sign In Button
                    Button(
                        onClick = onSignInClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(30.dp),
                                ambientColor = Color(0xFF4A90E2),
                                spotColor = Color(0xFF4A90E2)
                            ),
                        shape = RoundedCornerShape(30.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF203A43)
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "CREATE A NEW ACCOUNT",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.5.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    // Gear/Settings Button
                    Button(
                        onClick = { showPopup = !showPopup },
                        modifier = Modifier
                            .size(52.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = CircleShape,
                                ambientColor = Color(0xFF4A90E2),
                                spotColor = Color(0xFF4A90E2)
                            ),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF203A43)
                        ),
                        contentPadding = PaddingValues(0.dp)  // Add this - removes default padding
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            modifier = Modifier
                                .fillMaxSize()  // This will make icon fill the button
                                .padding(14.dp),  // Adjust padding to control visible icon size
                            tint = Color(0xFF203A43)
                        )
                    }
                }

                // Popup Menu
                VerticalPopupMenu(
                    isVisible = showPopup,
                    onDismiss = { showPopup = false },
                    menuItems = listOf(
                        PopupMenuItem(
                            title = "Main-Net",
                            onClick = onMainNetClick
                        ),
                        PopupMenuItem(
                            title = "Dev-Net",
                            onClick = onDevNetClick
                        ),
                    ),
                    offsetX = 0,
                    offsetY = (-60) // Adjust position above the button
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Spacer(modifier = Modifier.height(32.dp))

        }
    }
}

@Composable
fun DecorativeCircles() {
    // Decorative Circle 1 - Top Right
    Box(
        modifier = Modifier
            .size(200.dp)
            .offset(x = 80.dp, y = (-60).dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF4A90E2).copy(alpha = 0.2f),
                        Color(0xFF4A90E2).copy(alpha = 0f)
                    )
                ),
                shape = CircleShape
            )
    )

    // Decorative Circle 2 - Bottom Left
    Box(
        modifier = Modifier
            .size(250.dp)
            .offset(x = (-100).dp, y = 80.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF6BB5FF).copy(alpha = 0.15f),
                        Color(0xFF6BB5FF).copy(alpha = 0f)
                    )
                ),
                shape = CircleShape
            )
    )

    // Decorative Circle 3 - Center Right
    Box(
        modifier = Modifier
            .size(150.dp)
            .offset(x = 60.dp, y = (-40).dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF9BFFFF).copy(alpha = 0.1f),
                        Color(0xFF9BFFFF).copy(alpha = 0f)
                    )
                ),
                shape = CircleShape
            )
    )
}

@Composable
fun VerticalPopupMenu(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    menuItems: List<PopupMenuItem>,
    offsetX: Int = 0,
    offsetY: Int = (-60)
) {
    if (isVisible) {
        Popup(
            alignment = Alignment.TopEnd,
            offset = IntOffset(offsetX, offsetY),
            onDismissRequest = onDismiss,
            properties = PopupProperties(
                focusable = true,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(12.dp),
                        ambientColor = Color.Black.copy(alpha = 0.3f),
                        spotColor = Color.Black.copy(alpha = 0.3f)
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        color = Color(0xFF203A43),
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    menuItems.forEachIndexed { index, item ->
                        TextButton(
                            onClick = {
                                item.onClick()
                                onDismiss()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = item.title,
                                color = item.textColor ?: Color.White,
                                fontSize = (item.fontSize ?: 14).sp,
                                fontWeight = item.fontWeight ?: FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }

                        if (index < menuItems.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                thickness = 0.5.dp,
                                color = Color.White.copy(alpha = 0.1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

data class PopupMenuItem(
    val title: String,
    val onClick: () -> Unit,
    val textColor: Color? = null,
    val fontSize: Int? = null,
    val fontWeight: FontWeight? = null
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewWelcomeScreen() {
    MaterialTheme {
        WelcomeScreen(
            onSignInClick = {},
            onMainNetClick = {},
            onDevNetClick = {}
        )
    }
}