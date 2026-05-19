package com.jnetaol.querylite.ui.screens.home

import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jnetaol.querylite.data.model.SavedDatabase
import com.jnetaol.querylite.ui.components.*
import com.jnetaol.querylite.ui.screens.AppViewModel
import com.jnetaol.querylite.ui.theme.*
import java.io.File

@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onNavigatetoBrowser: () -> Unit,
    onNavigateToQuery: () -> Unit,
    onNavigateToSchema: () -> Unit,
    onNavigateToImportExport: () -> Unit
) {
    val context = LocalContext.current
    val savedDatabases by viewModel.savedDatabases.collectAsState()
    val currentDbPath by viewModel.currentDbPath.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    var showNewDbDialog by remember { mutableStateOf(false) }
    var newDbPath by remember { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf<SavedDatabase?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val path = copyFileFromUri(context, it)
            if (path != null) {
                viewModel.loadDatabase(path)
                onNavigatetoBrowser()
            }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatus()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "QueryLite",
                    style = MaterialTheme.typography.headlineLarge,
                    color = AccentPrimary
                )
                Text(
                    "SQLite Database Browser",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            // Quick actions
            item {
                NeonCard {
                    Text("Quick Actions", color = AccentPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GlowButton(
                            text = "Open DB",
                            onClick = { filePickerLauncher.launch(arrayOf("application/octet-stream", "*/*")) },
                            icon = Icons.Default.FolderOpen,
                            modifier = Modifier.weight(1f)
                        )
                        GlowButton(
                            text = "New DB",
                            onClick = { showNewDbDialog = true },
                            icon = Icons.Default.CreateNewFolder,
                            modifier = Modifier.weight(1f),
                            color = NeonYellow
                        )
                    }
                    if (currentDbPath != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            GlowButton(
                                text = "Browser",
                                onClick = onNavigatetoBrowser,
                                icon = Icons.Default.TableChart,
                                modifier = Modifier.weight(1f),
                                color = StatusSuccess
                            )
                            GlowButton(
                                text = "SQL Query",
                                onClick = onNavigateToQuery,
                                icon = Icons.Default.Code,
                                modifier = Modifier.weight(1f),
                                color = StatusInfo
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            GlowButton(
                                text = "Schema",
                                onClick = onNavigateToSchema,
                                icon = Icons.Default.Schema,
                                modifier = Modifier.weight(1f),
                                color = StatusWarning
                            )
                            GlowButton(
                                text = "I/E CSV",
                                onClick = onNavigateToImportExport,
                                icon = Icons.Default.ImportExport,
                                modifier = Modifier.weight(1f),
                                color = NeonAmberLight
                            )
                        }
                    }
                }
            }

            // Current database info
            if (currentDbPath != null) {
                item {
                    NeonCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storage, null, tint = StatusSuccess, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Connected", color = StatusSuccess, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    File(currentDbPath!!).name,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    "${viewModel.tables.collectAsState().value.size} tables",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            StatusBadge("Active", color = StatusSuccess)
                        }
                    }
                }
            }

            // Recent/Saved databases
            item {
                SectionHeader(title = "Saved Databases")
            }

            if (savedDatabases.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.Storage,
                        title = "No saved databases",
                        subtitle = "Open a database file to get started"
                    )
                }
            } else {
                items(savedDatabases, key = { it.id }) { savedDb ->
                    NeonCard(
                        modifier = Modifier.clickable {
                            viewModel.loadDatabase(savedDb.filePath)
                            onNavigatetoBrowser()
                        }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Dns, null, tint = AccentPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    savedDb.displayName,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    "${savedDb.tableCount} tables • ${formatFileSize(savedDb.fileSizeBytes)}",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            IconButton(onClick = { showDeleteConfirm = savedDb }) {
                                Icon(Icons.Default.Close, "Remove", tint = StatusError.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // New DB dialog
    if (showNewDbDialog) {
        AlertDialog(
            onDismissRequest = { showNewDbDialog = false },
            title = { Text("Create New Database", color = AccentPrimary) },
            text = {
                Column {
                    Text("Enter database name:", color = TextSecondary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newDbPath,
                        onValueChange = { newDbPath = it },
                        label = { Text("Database name") },
                        placeholder = { Text("my_database.db") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentPrimary,
                            unfocusedBorderColor = DarkBorder,
                            cursorColor = AccentPrimary
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newDbPath.isNotBlank()) {
                        val dir = File(context.getExternalFilesDir(null), "databases")
                        dir.mkdirs()
                        val name = if (newDbPath.endsWith(".db") || newDbPath.endsWith(".sqlite")) newDbPath else "$newDbPath.db"
                        val fullPath = File(dir, name).absolutePath
                        if (viewModel.createNewDatabase(fullPath)) {
                            showNewDbDialog = false
                            newDbPath = ""
                            onNavigatetoBrowser()
                        }
                    }
                }) { Text("Create", color = AccentPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showNewDbDialog = false }) { Text("Cancel", color = TextSecondary) }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Delete confirm dialog
    showDeleteConfirm?.let { dbToDelete ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Remove Database", color = StatusError) },
            text = { Text("Remove \"${dbToDelete.displayName}\" from saved list? The file will not be deleted.", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.removeSavedDatabase(dbToDelete)
                    showDeleteConfirm = null
                }) { Text("Remove", color = StatusError) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) { Text("Cancel", color = TextSecondary) }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

private fun copyFileFromUri(context: android.content.Context, uri: Uri): String? {
    return try {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        val displayName = cursor?.use {
            if (it.moveToFirst()) {
                val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) it.getString(idx) else "imported.db"
            } else "imported.db"
        } ?: "imported.db"

        val dir = File(context.getExternalFilesDir(null), "databases")
        dir.mkdirs()
        val destFile = File(dir, "${System.currentTimeMillis()}_$displayName")
        context.contentResolver.openInputStream(uri)?.use { input ->
            destFile.outputStream().use { output -> input.copyTo(output) }
        }
        destFile.absolutePath
    } catch (e: Exception) {
        null
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${"%.1f".format(bytes / (1024.0 * 1024.0))} MB"
        else -> "${"%.2f".format(bytes / (1024.0 * 1024.0 * 1024.0))} GB"
    }
}
