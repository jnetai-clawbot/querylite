package com.jnetaol.querylite.ui.screens.importexport

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jnetaol.querylite.ui.components.*
import com.jnetaol.querylite.ui.screens.AppViewModel
import com.jnetaol.querylite.ui.theme.*
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportExportScreen(
    viewModel: AppViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val tables by viewModel.tables.collectAsState()
    val currentDbName by viewModel.currentDbName.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    var selectedExportTable by remember { mutableStateOf<String?>(null) }
    var importTableName by remember { mutableStateOf("") }
    var createTableForImport by remember { mutableStateOf(true) }
    var selectedImportCsv by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatus()
        }
    }

    val csvPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            val path = copyCsvFromUri(context, it)
            if (path != null) {
                selectedImportCsv = path
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Import / Export", color = TextPrimary, fontWeight = FontWeight.Bold) },
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Storage, null, tint = StatusSuccess, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(currentDbName, color = TextSecondary, fontSize = 14.sp)
                }
            }

            // Export section
            item {
                SectionHeader(title = "Export Table to CSV")
            }

            item {
                NeonCard {
                    Text("Select table to export:", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (tables.isEmpty()) {
                        Text("No tables available", color = TextDisabled, fontSize = 12.sp)
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                            items(tables) { table ->
                                val isSelected = selectedExportTable == table
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedExportTable = table }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedExportTable = table },
                                        colors = RadioButtonDefaults.colors(selectedColor = AccentPrimary)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(table, color = if (isSelected) AccentPrimary else TextPrimary, fontSize = 14.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    GlowButton(
                        text = "Export to CSV",
                        onClick = {
                            selectedExportTable?.let { table ->
                                val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "QueryLite")
                                dir.mkdirs()
                                val outPath = File(dir, "${table}_${System.currentTimeMillis()}.csv").absolutePath
                                viewModel.exportTableToCsv(table, outPath)
                            }
                        },
                        icon = Icons.Default.FileDownload,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedExportTable != null && !isLoading,
                        color = StatusInfo
                    )
                }
            }

            // Import section
            item {
                SectionHeader(title = "Import CSV to Table")
            }

            item {
                NeonCard {
                    OutlinedTextField(
                        value = importTableName,
                        onValueChange = { importTableName = it },
                        label = { Text("Target table name") },
                        placeholder = { Text("my_table") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentPrimary,
                            unfocusedBorderColor = DarkBorder,
                            cursorColor = AccentPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = createTableForImport,
                            onCheckedChange = { createTableForImport = it },
                            colors = CheckboxDefaults.colors(checkedColor = AccentPrimary)
                        )
                        Text("Auto-create table from CSV headers", color = TextSecondary, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Selected CSV file
                    if (selectedImportCsv != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Description, null, tint = StatusSuccess, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                File(selectedImportCsv!!).name,
                                color = StatusSuccess,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { selectedImportCsv = null }) {
                                Text("Clear", color = StatusError, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GlowButton(
                            text = "Select CSV",
                            onClick = { csvPickerLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "*/*")) },
                            icon = Icons.Default.FileOpen,
                            modifier = Modifier.weight(1f),
                            color = StatusWarning
                        )
                        GlowButton(
                            text = "Import",
                            onClick = {
                                if (importTableName.isNotBlank() && selectedImportCsv != null) {
                                    viewModel.importCsv(importTableName, selectedImportCsv!!, createTableForImport)
                                    selectedImportCsv = null
                                }
                            },
                            icon = Icons.Default.FileUpload,
                            modifier = Modifier.weight(1f),
                            enabled = importTableName.isNotBlank() && selectedImportCsv != null && !isLoading,
                            color = StatusSuccess
                        )
                    }
                }
            }

            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentPrimary)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

private fun copyCsvFromUri(context: android.content.Context, uri: Uri): String? {
    return try {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        val displayName = cursor?.use {
            if (it.moveToFirst()) {
                val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) it.getString(idx) else "import.csv"
            } else "import.csv"
        } ?: "import.csv"

        val dir = File(context.getExternalFilesDir(null), "csv")
        dir.mkdirs()
        val destFile = File(dir, "import_${System.currentTimeMillis()}_$displayName")
        context.contentResolver.openInputStream(uri)?.use { input ->
            destFile.outputStream().use { output -> input.copyTo(output) }
        }
        destFile.absolutePath
    } catch (e: Exception) {
        null
    }
}
