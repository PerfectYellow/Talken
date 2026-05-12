package com.example.cyloop.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import com.example.cyloop.storage.AuthPreferences


object AuthSettings {
    var isBiometricEnabled by mutableStateOf(false)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasscodeLockScreen(
    onBackClick: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    val options = listOf("Biometric Login", "Password")
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Derived state for the radio button selection
    val selectedOption = if (AuthSettings.isBiometricEnabled) options[0] else options[1]

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Passcode Lock") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Select Authentication Method",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Radio Button Group
            Column(Modifier.selectableGroup()) {
                options.forEach { text ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .selectable(
                                selected = (text == selectedOption),
                                onClick = {
                                    val enabled = (text == options[0])

                                    AuthSettings.isBiometricEnabled = enabled

                                    scope.launch {
                                        AuthPreferences.setBiometricEnabled(
                                            context,
                                            enabled
                                        )
                                    }
                                },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (text == selectedOption),
                            onClick = null // null recommended for accessibility with selectable modifier
                        )
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Password specific UI
            if (!AuthSettings.isBiometricEnabled) {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Enter Password") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = { /* TODO: Reset password logic */ },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Reset Password", color = MaterialTheme.colorScheme.primary)
                }
            } else {
                Text(
                    text = "Biometric authentication is currently active for logging in.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPasscodeLockScreen() {
    MaterialTheme {
        PasscodeLockScreen(onBackClick = {})
    }
}
