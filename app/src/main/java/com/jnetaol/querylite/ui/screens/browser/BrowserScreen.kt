package com.jnetaol.querylite.ui.screens.browser

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jnetaol.querylite.data.model.ColumnInfo
import com.jnetaol.querylite.ui.components.*
import com.jnetaol.querylite.ui.screens.AppViewModel
import com.jnetaol.querylite.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(
    viewModel: AppViewModel,
    onNavigateBack: () -> Unit
) {
    val tables by viewModel.tables.collectAsState()
    val tableInfos by viewModel.tableInfos.collectAsState()
    val selectedTable by viewModel.selectedTable.collectAsState()
    val tableData by viewModel.tableData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentDbName by viewModel.currentDbName.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    var showCreateTableDialog by remember { mutableStateOf(false) }

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
                title = { Text("Browser", color = TextPrimary, fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Database header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Storage, null, tint = StatusSuccess, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(currentDbName, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Spacer(modifier = Modifier.weight(1f))
                Text("${tables.size} tables", color = TextSecondary, fontSize = 12.sp)
            }

            // Table tabs
            if (tables.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(tables) { table ->
                        val isSelected = table == selectedTable
                        val tableInfo = tableInfos.find { it.name == table }
                        NeonCard(
                            modifier = Modifier.clickable { viewModel.selectTable(table) },
                            glowColor = if (isSelected) AccentPrimary.copy(alpha = 0.3f) else DarkBorder
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.TableChart,
                                    null,
                                    tint = if (isSelected) AccentPrimary else TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        table,
                                        color = if (isSelected) AccentPrimary else TextPrimary,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp,
                                        maxLines = 1
                                    )
                                    if (tableInfo != null) {
                                        Text(
                                            "${tableInfo.rowCount} rows • ${tableInfo.columns.size} cols",
                                            color = TextSecondary,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Table data
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentPrimary)
                }
            } else if (tableData != null) {
                Column(modifier = Modifier.weight(1f)) {
                    // Column headers info
                    val cols = tableData!!.columns.map { it.name }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${tableData!!.rows.size} rows • ${cols.size} cols",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        if ((tableData?.rows?.size ?: 0) >= 200) {
                            GlowButton(
                                text = "Load More",
                                onClick = { viewModel.loadMoreRows() },
                                icon = Icons.Default.Add,
                                color = StatusInfo
                            )
                        }
                    }

                    DataGrid(
                        columns = cols,
                        rows = tableData!!.rows.map { it.values },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }
            } else if (tables.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = Icons.Default.TableChart,
                        title = "No tables found",
                        subtitle = "This database appears to be empty"
                    )
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = Icons.Default.TouchApp,
                        title = "Select a table",
                        subtitle = "Tap a table above to view its data"
                    )
                }
            }
        }
    }
}
