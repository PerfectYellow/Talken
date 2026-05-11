package com.example.cyloop.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SignInScreen(
    onBackClick: () -> Unit,
    onSignInSuccess: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE3F2FD),  // Very light blue
                        Color(0xFFBBDEFB),  // Light blue
                        Color(0xFF90CAF9)   // Slightly deeper light blue
                    )
                )
            )
    ) {
        // Decorative subtle circles
        DecorativeLightCircles(isSignUp = false)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Back button
            TextButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.Start)
            ) {
                Text("← Back", color = Color(0xFF1976D2))
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Header with subtle shadow
            Text(
                text = "Log In / Sign Up",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0D47A1),
                modifier = Modifier.shadow(
                    elevation = 2.dp,
                    ambientColor = Color(0xFF42A5F5).copy(alpha = 0.3f),
                    spotColor = Color(0xFF42A5F5).copy(alpha = 0.3f)
                )
            )

            Spacer(modifier = Modifier.height(60.dp))

            // Email field with light styling
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username", color = Color(0xFF1976D2)) },
                placeholder = { Text("Enter your username") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )


            Spacer(modifier = Modifier.height(30.dp))

            Spacer(modifier = Modifier.height(32.dp))

            // Sign In button with gradient
            Button(
                onClick = onSignInSuccess,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(12.dp),
                        ambientColor = Color(0xFF42A5F5),
                        spotColor = Color(0xFF42A5F5)
                    ),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1976D2),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Continue",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

        }
    }
}

@Composable
fun DecorativeLightCircles(isSignUp: Boolean) {
    // Different circle positions for Sign In vs Sign Up
    val offsetX1 = if (isSignUp) 100.dp else 80.dp
    val offsetY1 = if (isSignUp) (-50).dp else (-70).dp
    val size1 = if (isSignUp) 250.dp else 200.dp

    val offsetX2 = if (isSignUp) (-80).dp else (-100).dp
    val offsetY2 = if (isSignUp) 60.dp else 80.dp
    val size2 = if (isSignUp) 300.dp else 250.dp

    // Decorative Circle 1 - Top Right
    Box(
        modifier = Modifier
            .size(size1)
//            .align(Alignment.TopEnd)
            .offset(x = offsetX1, y = offsetY1)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF64B5F6).copy(alpha = 0.15f),
                        Color(0xFF64B5F6).copy(alpha = 0f)
                    )
                ),
                shape = androidx.compose.foundation.shape.CircleShape
            )
    )

    // Decorative Circle 2 - Bottom Left
    Box(
        modifier = Modifier
            .size(size2)
//            .align(Alignment.BottomStart)
            .offset(x = offsetX2, y = offsetY2)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF42A5F5).copy(alpha = 0.1f),
                        Color(0xFF42A5F5).copy(alpha = 0f)
                    )
                ),
                shape = androidx.compose.foundation.shape.CircleShape
            )
    )
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun PreviewSignInScreen() {
    MaterialTheme {
        SignInScreen(
            onBackClick = {},
            onSignInSuccess = {}
        )
    }
}