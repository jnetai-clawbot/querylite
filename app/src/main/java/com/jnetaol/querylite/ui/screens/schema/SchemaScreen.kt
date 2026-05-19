package com.jnetaol.querylite.ui.screens.schema

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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jnetaol.querylite.data.model.ColumnInfo
import com.jnetaol.querylite.ui.components.*
import com.jnetaol.querylite.ui.screens.AppViewModel
import com.jnetaol.querylite.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchemaScreen(
    viewModel: AppViewModel,
    onNavigateBack: () -> Unit
) {
    val tableInfos by viewModel.tableInfos.collectAsState()
    val indexes by viewModel.indexes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentDbName by viewModel.currentDbName.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    var expandedTable by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadSchema()
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
                title = { Text("Schema", color = TextPrimary, fontWeight = FontWeight.Bold) },
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
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentPrimary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storage, null, tint = StatusSuccess, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(currentDbName, color = TextSecondary, fontSize = 14.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        Text("${tableInfos.size} tables", color = TextSecondary, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    SectionHeader(title = "Tables")
                }

                if (tableInfos.isEmpty()) {
                    item {
                        EmptyState(icon = Icons.Default.Schema, title = "No tables found")
                    }
                } else {
                    items(tableInfos, key = { it.name }) { tableInfo ->
                        val isExpanded = expandedTable == tableInfo.name
                        NeonCard {
                            Column(
                                modifier = Modifier.clickable {
                                    expandedTable = if (isExpanded) null else tableInfo.name
                                }
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.TableChart,
                                        null,
                                        tint = AccentPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(tableInfo.name, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                                        Text(
                                            "${tableInfo.rowCount} rows • ${tableInfo.columns.size} columns",
                                            color = TextSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Icon(
                                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                if (isExpanded) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = DarkBorder)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Column details
                                    tableInfo.columns.forEach { col ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(modifier = Modifier.width(100.dp)) {
                                                Text(
                                                    col.name,
                                                    color = TextPrimary,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                            StatusBadge(text = col.type.ifEmpty { "TEXT" }, color = AccentPrimary, modifier = Modifier.width(72.dp))

                                            if (col.isPrimaryKey) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                StatusBadge(text = "PK", color = StatusWarning)
                                            }
                                            if (!col.isNullable) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                StatusBadge(text = "NOT NULL", color = StatusInfo)
                                            }
                                            if (col.defaultValue != null) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("default: ${col.defaultValue}", color = TextDisabled, fontSize = 11.sp)
                                            }
                                        }
                                    }

                                    // Show indexes for this table
                                    val tableIndexes = indexes.filter { it.tableName == tableInfo.name }
                                    if (tableIndexes.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        HorizontalDivider(color = DarkBorder.copy(alpha = 0.5f))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Indexes", color = NeonAmberLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        tableIndexes.forEach { idx ->
                                            Row(
                                                modifier = Modifier.padding(vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    if (idx.isUnique) Icons.Default.Fingerprint else Icons.Default.Search,
                                                    null,
                                                    tint = if (idx.isUnique) StatusWarning else TextSecondary,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(idx.name, color = TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    "(${idx.columns.joinToString(", ")})",
                                                    color = TextSecondary,
                                                    fontSize = 11.sp,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                                if (idx.isUnique) {
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    StatusBadge("UNIQUE", color = StatusWarning)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Indexes section
                if (indexes.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        SectionHeader(title = "All Indexes")
                    }
                    items(indexes, key = { it.name }) { index ->
                        NeonCard {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (index.isUnique) Icons.Default.Lock else Icons.Default.Search,
                                    null,
                                    tint = if (index.isUnique) StatusWarning else TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(index.name, color = TextPrimary, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                                    Text(
                                        "Table: ${index.tableName} • (${index.columns.joinToString(", ")})",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                                if (index.isUnique) {
                                    StatusBadge("UNIQUE", color = StatusWarning)
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}
