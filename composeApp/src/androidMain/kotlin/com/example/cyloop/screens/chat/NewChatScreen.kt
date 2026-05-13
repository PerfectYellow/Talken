package com.example.cyloop.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewChatScreen(
    onBackClick: () -> Unit,
    onContactSelected: (String, String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val contacts = listOf(
        "Alice Johnson", "Bob Smith", "Carol Davis", "David Wilson", 
        "Emma Brown", "Frank Miller", "Grace Lee", "Henry Ford"
    )
    val filteredContacts = contacts.filter { it.contains(searchQuery, ignoreCase = true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Chat") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search contacts...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = CircleShape,
                singleLine = true
            )

            ListItem(
                headlineContent = { Text("Add New Contact", color = Color(0xFF2196F3)) },
                leadingContent = {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE3F2FD)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color(0xFF2196F3))
                    }
                },
                modifier = Modifier.clickable { /* TODO */ }
            )

            HorizontalDivider()

            Text(
                text = "Contacts",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.labelLarge,
                color = Color.Gray
            )

            LazyColumn {
                items(filteredContacts) { contact ->
                    ListItem(
                        headlineContent = { Text(contact) },
                        leadingContent = {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.LightGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(contact.take(1))
                            }
                        },
                        modifier = Modifier.clickable { 
                            onContactSelected(contact.lowercase().replace(" ", "_"), contact) 
                        }
                    )
                }
            }
        }
    }
}
