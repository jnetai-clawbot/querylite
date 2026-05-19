package com.jnetaol.querylite.ui.screens.diff

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jnetaol.querylite.data.model.ChangedRow
import com.jnetaol.querylite.data.model.DiffResult
import com.jnetaol.querylite.ui.components.*
import com.jnetaol.querylite.ui.screens.AppViewModel
import com.jnetaol.querylite.ui.theme.*
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiffScreen(
    viewModel: AppViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val diffPath1 by viewModel.diffPath1.collectAsState()
    val diffPath2 by viewModel.diffPath2.collectAsState()
    val diffResults by viewModel.diffResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    var expandedResult by remember { mutableStateOf<String?>(null) }

    val filePicker1 = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            val path = copyFileFromUri(context, it)
            if (path != null) viewModel.setDiffDatabase1(path)
        }
    }

    val filePicker2 = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            val path = copyFileFromUri(context, it)
            if (path != null) viewModel.setDiffDatabase2(path)
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
        topBar = {
            TopAppBar(
                title = { Text("DB Diff", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = AccentPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
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
                SectionHeader(title = "Compare Two Databases")
            }

            // DB 1 selector
            item {
                NeonCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LooksOne, null, tint = StatusInfo, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Database 1", color = AccentPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            if (diffPath1 != null) {
                                Text(File(diffPath1!!).name, color = TextPrimary, fontSize = 13.sp)
                            } else {
                                Text("No file selected", color = TextDisabled, fontSize = 12.sp)
                            }
                        }
                        GlowButton(
                            text = "Select",
                            onClick = { filePicker1.launch(arrayOf("application/octet-stream", "*/*")) },
                            icon = Icons.Default.FolderOpen,
                            color = StatusInfo
                        )
                    }
                }
            }

            // DB 2 selector
            item {
                NeonCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LooksTwo, null, tint = StatusWarning, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Database 2", color = NeonAmberLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            if (diffPath2 != null) {
                                Text(File(diffPath2!!).name, color = TextPrimary, fontSize = 13.sp)
                            } else {
                                Text("No file selected", color = TextDisabled, fontSize = 12.sp)
                            }
                        }
                        GlowButton(
                            text = "Select",
                            onClick = { filePicker2.launch(arrayOf("application/octet-stream", "*/*")) },
                            icon = Icons.Default.FolderOpen,
                            color = StatusWarning
                        )
                    }
                }
            }

            // Compare button
            item {
                GlowButton(
                    text = "Compare Databases",
                    onClick = { viewModel.runDiff() },
                    icon = Icons.Default.CompareArrows,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = diffPath1 != null && diffPath2 != null && !isLoading,
                    color = AccentPrimary
                )
            }

            // Loading
            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentPrimary)
                    }
                }
            }

            // Summary
            if (diffResults.isNotEmpty()) {
                item {
                    val totalAdded = diffResults.sumOf { it.totalAdded }
                    val totalRemoved = diffResults.sumOf { it.totalRemoved }
                    val totalChanged = diffResults.sumOf { it.totalChanged }
                    SectionHeader(title = "Results: $totalAdded added, $totalRemoved removed, $totalChanged changed")
                }
            }

            // Diff results per table
            items(diffResults, key = { it.tableName }) { result ->
                val isExpanded = expandedResult == result.tableName
                NeonCard {
                    Column(
                        modifier = Modifier.clickable {
                            expandedResult = if (isExpanded) null else result.tableName
                        }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TableChart, null, tint = AccentPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(result.tableName, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 15.sp, modifier = Modifier.weight(1f))
                            StatusBadge("+${result.totalAdded}", color = StatusSuccess)
                            Spacer(modifier = Modifier.width(6.dp))
                            StatusBadge("-${result.totalRemoved}", color = StatusError)
                            Spacer(modifier = Modifier.width(6.dp))
                            StatusBadge("~${result.totalChanged}", color = StatusWarning)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                null,
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        if (isExpanded) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = DarkBorder)
                            Spacer(modifier = Modifier.height(8.dp))

                            if (result.addedRows.isNotEmpty()) {
                                Text("Added Rows (${result.totalAdded})", color = StatusSuccess, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                result.addedRows.take(5).forEach { row ->
                                    Text(
                                        formatRowCompact(row),
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        maxLines = 1
                                    )
                                }
                                if (result.totalAdded > 5) Text("... and ${result.totalAdded - 5} more", color = TextDisabled, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            if (result.removedRows.isNotEmpty()) {
                                Text("Removed Rows (${result.totalRemoved})", color = StatusError, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                result.removedRows.take(5).forEach { row ->
                                    Text(
                                        formatRowCompact(row),
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        maxLines = 1
                                    )
                                }
                                if (result.totalRemoved > 5) Text("... and ${result.totalRemoved - 5} more", color = TextDisabled, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            if (result.changedRows.isNotEmpty()) {
                                Text("Changed Rows (${result.totalChanged})", color = StatusWarning, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                result.changedRows.take(5).forEach { changed ->
                                    Text(
                                        "Changed cols: ${changed.changedColumns.joinToString(", ")}",
                                        color = StatusWarning,
                                        fontSize = 11.sp,
                                        maxLines = 1
                                    )
                                }
                                if (result.totalChanged > 5) Text("... and ${result.totalChanged - 5} more", color = TextDisabled, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            if (diffResults.isEmpty() && diffPath1 != null && diffPath2 != null && !isLoading) {
                item {
                    EmptyState(
                        icon = Icons.Default.CompareArrows,
                        title = "No common tables found",
                        subtitle = "The two databases have no tables in common to compare"
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
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
        val destFile = File(dir, "diff_${System.currentTimeMillis()}_$displayName")
        context.contentResolver.openInputStream(uri)?.use { input ->
            destFile.outputStream().use { output -> input.copyTo(output) }
        }
        destFile.absolutePath
    } catch (e: Exception) {
        null
    }
}

private fun formatRowCompact(row: com.jnetaol.querylite.data.model.TableRow): String {
    val entries = row.values.entries.take(3).joinToString(", ") { "${it.key}=${it.value}" }
    return if (row.values.size > 3) "$entries ..." else entries
}
