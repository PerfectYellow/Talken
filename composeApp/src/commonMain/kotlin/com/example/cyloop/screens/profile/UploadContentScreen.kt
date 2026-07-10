package com.example.cyloop.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.cyloop.api.IpfsService
import com.example.cyloop.api.PinItem
import com.example.cyloop.components.FloatingNotification
import com.example.cyloop.components.NotificationType
import com.example.cyloop.components.rememberNotificationState
import com.example.cyloop.font.UIFont
import com.example.cyloop.storage.IpfsPreferences
import com.example.cyloop.theme.getAppBackgroundBrush
import androidx.compose.ui.tooling.preview.Preview
import com.example.cyloop.theme.CyLoopTheme
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadContentScreen(
    onBackClick: () -> Unit,
) {
    var isUploading by remember { mutableStateOf(false) }
    var uploadStatus by remember { mutableStateOf("") }
    var selectedFileBytes by remember { mutableStateOf<ByteArray?>(null) }
    var selectedFileName by remember { mutableStateOf("") }
    var pinnedFiles by remember { mutableStateOf<List<PinItem>>(emptyList()) }
    var isRefreshing by remember { mutableStateOf(false) }
    val notificationState = rememberNotificationState()
    
    // IPFS Config State
    var showConfigSheet by remember { mutableStateOf(false) }
    var configUrl by remember { mutableStateOf("") }
    var configJwt by remember { mutableStateOf("") }
    var configSecret by remember { mutableStateOf("") }
    var configKey by remember { mutableStateOf("") }
    
    var showMenu by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    val pullRefreshState = rememberPullToRefreshState()

    val launcher = rememberFilePickerLauncher(
        type = PickerType.File(),
        title = "Select Content"
    ) { file ->
        if (file != null) {
            scope.launch {
                selectedFileBytes = file.readBytes()
                selectedFileName = file.name
                uploadStatus = "File selected: $selectedFileName"
            }
        }
    }

    fun refreshFiles() {
        scope.launch {
            isRefreshing = true
            try {
                pinnedFiles = IpfsService.getPinnedFiles()
            } catch (e: Exception) {
                notificationState.showNotification("Error refreshing: ${e.message}", NotificationType.ERROR)
            } finally {
                isRefreshing = false
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshFiles()
        // Load current config
        val currentConfig = IpfsPreferences.getConfig().first()
        configUrl = currentConfig.baseUrl
        configJwt = currentConfig.jwt
        configSecret = currentConfig.apiSecret
        configKey = currentConfig.apiKey
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(getAppBackgroundBrush())
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Upload Content",
                            style = UIFont.LargeTitle.copy(color = MaterialTheme.colorScheme.onSurface)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Set IPFS Address") },
                                    leadingIcon = { Icon(Icons.Default.Settings, null) },
                                    onClick = {
                                        showMenu = false
                                        showConfigSheet = true
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { refreshFiles() },
                state = pullRefreshState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // File Selection Area
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clickable { 
                                launcher.launch()
                            }
                            .border(
                                width = 1.dp,
                                color = if (selectedFileBytes != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.05f)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = if (selectedFileBytes != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                if (selectedFileBytes != null) "File: $selectedFileName" else "Select any File",
                                style = UIFont.ChatName.copy(color = MaterialTheme.colorScheme.onSurface)
                            )
                        }
                    }

                    if (uploadStatus.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = uploadStatus,
                            style = UIFont.Metadata.copy(
                                color = if (uploadStatus.contains("Error")) Color.Red else MaterialTheme.colorScheme.primary
                            ),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (selectedFileBytes == null) {
                                uploadStatus = "Please select a file first"
                                return@Button
                            }

                            scope.launch {
                                isUploading = true
                                uploadStatus = "Uploading file to IPFS..."
                                try {
                                    IpfsService.uploadFile(selectedFileBytes!!, selectedFileName)
                                    notificationState.showNotification("Success! File pinned.", NotificationType.HINT)
                                    uploadStatus = ""
                                    selectedFileBytes = null
                                    selectedFileName = ""
                                    refreshFiles()
                                } catch (e: Exception) {
                                    notificationState.showNotification("Upload Error: ${e.message}", NotificationType.ERROR)
                                    uploadStatus = ""
                                } finally {
                                    isUploading = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        enabled = !isUploading && selectedFileBytes != null
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Upload to IPFS",
                                style = UIFont.ChatName.copy(color = MaterialTheme.colorScheme.onPrimary)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Your Content",
                        style = UIFont.ChatName.copy(color = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isRefreshing && pinnedFiles.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(pinnedFiles) { item ->
                                ContentItem(
                                    item = item,
                                    onDownloadClick = {
                                        val url = IpfsService.getIpfsUrl(item.ipfsPinHash)
                                        uriHandler.openUri(url)
                                    },
                                    onDeleteClick = {
                                        scope.launch {
                                            uploadStatus = "Deleting content..."
                                            try {
                                                IpfsService.deletePinnedFile(item.ipfsPinHash)
                                                notificationState.showNotification("Content removed.", NotificationType.INFO)
                                                uploadStatus = ""
                                                refreshFiles()
                                            } catch (e: Exception) {
                                                notificationState.showNotification("Delete Error: ${e.message}", NotificationType.ERROR)
                                                uploadStatus = ""
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (showConfigSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showConfigSheet = false },
                    containerColor = MaterialTheme.colorScheme.surface,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .padding(bottom = 32.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "IPFS Configuration",
                            style = UIFont.ChatName.copy(color = MaterialTheme.colorScheme.onSurface)
                        )
                        
                        OutlinedTextField(
                            value = configUrl,
                            onValueChange = { configUrl = it },
                            label = { Text("Base URL (e.g. https://api.pinata.cloud)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        OutlinedTextField(
                            value = configJwt,
                            onValueChange = { configJwt = it },
                            label = { Text("JWT") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        OutlinedTextField(
                            value = configSecret,
                            onValueChange = { configSecret = it },
                            label = { Text("API Secret") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        OutlinedTextField(
                            value = configKey,
                            onValueChange = { configKey = it },
                            label = { Text("API Key") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        Button(
                            onClick = {
                                scope.launch {
                                    IpfsPreferences.setConfig(
                                        IpfsPreferences.IpfsConfig(
                                            baseUrl = configUrl,
                                            jwt = configJwt,
                                            apiSecret = configSecret,
                                            apiKey = configKey
                                        )
                                    )
                                    showConfigSheet = false
                                    refreshFiles()
                                    notificationState.showNotification("Settings updated", NotificationType.INFO)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Set IPFS Settings")
                        }
                    }
                }
            }
        }

        FloatingNotification(state = notificationState)
    }
}

@Preview
@Composable
fun UploadContentScreenPreview() {
    CyLoopTheme(darkTheme = true) {
        UploadContentScreen(
            onBackClick = {}
        )
    }
}

@Composable
fun ContentItem(
    item: PinItem,
    onDownloadClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.05f),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp, 
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.metadata?.name ?: "Unnamed File",
                    style = UIFont.ChatName.copy(color = MaterialTheme.colorScheme.onSurface),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = "Hash: ${item.ipfsPinHash.take(8)}...${item.ipfsPinHash.takeLast(8)}",
                    style = UIFont.Metadata.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                )
            }
            
            Row {
                IconButton(onClick = onDownloadClick) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "View/Download",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
