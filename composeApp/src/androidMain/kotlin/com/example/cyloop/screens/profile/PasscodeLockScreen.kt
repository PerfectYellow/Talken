package com.example.cyloop.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.cyloop.storage.AuthPreferences
import kotlinx.coroutines.launch
import androidx.compose.ui.text.input.ImeAction

import com.example.cyloop.theme.getAppBackgroundBrush

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasscodeLockScreen(
    onBackClick: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var resetPassword by remember { mutableStateOf("") }
    var confirmResetPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val options = listOf("Biometric Login", "Password")
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val isBiometricEnabled by AuthPreferences
        .isBiometricEnabled(context)
        .collectAsState(initial = false)

    val selectedOption = if (isBiometricEnabled) options[0] else options[1]

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Passcode Lock",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(getAppBackgroundBrush())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top
            ) {
                // Header Section
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.secondary
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Text(
                                text = "Authentication Method",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Choose how you want to secure your account",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Radio Button Group with Card Design
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.selectableGroup()) {
                        options.forEach { text ->
                            val isSelected = (text == selectedOption)
                            val isBiometric = text == options[0]

                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .height(72.dp)
                                    .selectable(
                                        selected = isSelected,
                                        onClick = {
                                            val enabled = (text == options[0])
                                            scope.launch {
                                                AuthPreferences.setBiometricEnabled(
                                                    context,
                                                    enabled
                                                )
                                                if (!enabled) {
                                                    // Reset password when switching to password mode
                                                    password = ""
                                                }
                                            }
                                        },
                                        role = Role.RadioButton
                                    )
                                    .padding(horizontal = 20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Icon based on option
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected)
                                                Brush.linearGradient(
                                                    colors = listOf(
                                                        MaterialTheme.colorScheme.primary,
                                                        MaterialTheme.colorScheme.secondary
                                                    )
                                                )
                                            else
                                                Brush.linearGradient(
                                                    colors = listOf(
                                                        MaterialTheme.colorScheme.surfaceVariant,
                                                        MaterialTheme.colorScheme.surfaceVariant
                                                    )
                                                )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isBiometric)
                                            Icons.Default.Fingerprint
                                        else
                                            Icons.Default.Lock,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = text,
                                        fontSize = 16.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isBiometric) {
                                        Text(
                                            text = "Use fingerprint or face recognition",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else {
                                        Text(
                                            text = "Use a secure password",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                RadioButton(
                                    selected = isSelected,
                                    onClick = null,
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }

                            if (text != options.last()) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 76.dp),
                                    thickness = 1.dp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Password specific UI with enhanced design
                if (!isBiometricEnabled) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Reset Password",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = password,
                                onValueChange = {
                                    password = it
                                    passwordError = null // Clear error when user types
                                },
                                label = { Text("Current Password") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                isError = passwordError != null,
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Done
                                ),
                                supportingText = {
                                    if (passwordError != null) {
                                        Text(
                                            text = passwordError!!,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                },
                                visualTransformation = if (showPassword)
                                    VisualTransformation.None
                                else
                                    PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { showPassword = !showPassword }) {
                                        Icon(
                                            imageVector = if (showPassword)
                                                Icons.Default.VisibilityOff
                                            else
                                                Icons.Default.Visibility,
                                            contentDescription = if (showPassword) "Hide password" else "Show password"
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    errorBorderColor = MaterialTheme.colorScheme.error,
                                    errorLabelColor = MaterialTheme.colorScheme.error
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = {
                                        if (password.isNotEmpty()) {
                                            scope.launch {
                                                val storedPassword = AuthPreferences.getPassword(context)
                                                if (storedPassword == null) {
                                                    passwordError = "No password exists. Please set a password first."
                                                } else if (password != storedPassword) {
                                                    passwordError = "Incorrect password"
                                                } else {
                                                    passwordError = null
                                                    showResetDialog = true
                                                }
                                            }
                                        } else {
                                            passwordError = "Please enter your password"
                                        }
                                    },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Reset Password")
                                }
                            }
                        }
                    }
                } else {
                    // Biometric info card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE8F5E9)//MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Biometric Active",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Biometric authentication is currently active for logging in.",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Reset Password Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    text = "Reset Password",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter your new password",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = resetPassword,
                        onValueChange = { resetPassword = it },
                        label = { Text("New Password") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = confirmResetPassword,
                        onValueChange = { confirmResetPassword = it },
                        label = { Text("Confirm Password") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        isError = confirmResetPassword.isNotBlank() && resetPassword != confirmResetPassword,
                        supportingText = {
                            if (confirmResetPassword.isNotBlank() && resetPassword != confirmResetPassword) {
                                Text(
                                    text = "Passwords do not match",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (resetPassword.isNotBlank() && resetPassword == confirmResetPassword) {
                            scope.launch {
                                AuthPreferences.setPassword(context, resetPassword)
                                password = resetPassword
                                showResetDialog = false
                                resetPassword = ""
                                confirmResetPassword = ""
                            }
                        }
                    },
                    enabled = resetPassword.isNotBlank() && resetPassword == confirmResetPassword,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResetDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Extension function to save password (add to AuthPreferences)
suspend fun AuthPreferences.setPassword(context: android.content.Context, password: String) {
    // Implement password saving logic here
    // For example, using EncryptedSharedPreferences
}

@Preview(showBackground = true)
@Composable
fun PreviewPasscodeLockScreen() {
    MaterialTheme {
        PasscodeLockScreen(onBackClick = {})
    }
}