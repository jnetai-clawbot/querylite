package com.jnetaol.querylite.ui.screens.query

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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jnetaol.querylite.data.model.QueryHistory
import com.jnetaol.querylite.ui.components.*
import com.jnetaol.querylite.ui.screens.AppViewModel
import com.jnetaol.querylite.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueryScreen(
    viewModel: AppViewModel,
    onNavigateBack: () -> Unit
) {
    val queryResults by viewModel.queryResults.collectAsState()
    val queryError by viewModel.queryError.collectAsState()
    val queryHistory by viewModel.queryHistory.collectAsState()
    val favoriteQueries by viewModel.favoriteQueries.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentDbName by viewModel.currentDbName.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    var sqlText by remember { mutableStateOf(TextFieldValue("")) }
    var showHistory by remember { mutableStateOf(false) }
    var showFavoritesOnly by remember { mutableStateOf(false) }

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
                title = { Text("SQL Query", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = AccentPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { showHistory = !showHistory }) {
                        Icon(
                            if (showHistory) Icons.Default.History else Icons.Default.HistoryToggleOff,
                            "History",
                            tint = if (showHistory) AccentPrimary else TextSecondary
                        )
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
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Storage, null, tint = StatusSuccess, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(currentDbName, color = TextSecondary, fontSize = 13.sp)
                Spacer(modifier = Modifier.weight(1f))
                if (favoriteQueries.isNotEmpty()) {
                    FilterChip(
                        selected = showFavoritesOnly,
                        onClick = { showFavoritesOnly = !showFavoritesOnly },
                        label = { Text("Favorites", fontSize = 11.sp) },
                        modifier = Modifier.height(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (showHistory) {
                // History panel
                NeonCard {
                    Text("Query History", color = AccentPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    val displayList = if (showFavoritesOnly) favoriteQueries else queryHistory.take(50)

                    if (displayList.isEmpty()) {
                        Text("No query history", color = TextDisabled, fontSize = 13.sp)
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 250.dp)) {
                            items(displayList, key = { it.id }) { history ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { sqlText = TextFieldValue(history.sqlText) }
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Code, null,
                                        tint = if (history.isFavorite) NeonAmber else TextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            history.sqlText.take(80),
                                            color = TextPrimary,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            maxLines = 1
                                        )
                                        Row {
                                            Text(
                                                "${history.executionTimeMs}ms",
                                                color = TextDisabled,
                                                fontSize = 10.sp
                                            )
                                            if (history.rowCount >= 0) {
                                                Text(
                                                    " • ${history.rowCount} rows",
                                                    color = TextDisabled,
                                                    fontSize = 10.sp
                                                )
                                            }
                                            if (history.rowCount == -1) {
                                                Text(" • Error", color = StatusError, fontSize = 10.sp)
                                            }
                                        }
                                    }
                                    IconButton(
                                        onClick = { viewModel.toggleFavoriteQuery(history) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            if (history.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                            "Favorite",
                                            tint = if (history.isFavorite) NeonAmber else TextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteQueryHistory(history) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            "Delete",
                                            tint = StatusError.copy(alpha = 0.5f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Divider(color = DarkBorder.copy(alpha = 0.3f))
                            }
                        }

                        if (displayList.size > 30) {
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = { viewModel.clearQueryHistory() }) {
                                Text("Clear All History", color = StatusError, fontSize = 12.sp)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // SQL Editor
            SqlEditor(
                value = sqlText,
                onValueChange = { sqlText = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GlowButton(
                    text = "Run",
                    onClick = { viewModel.runQuery(sqlText.text) },
                    icon = Icons.Default.PlayArrow,
                    modifier = Modifier.weight(1f),
                    enabled = sqlText.text.isNotBlank() && !isLoading,
                    color = StatusSuccess
                )
                GlowButton(
                    text = "Clear",
                    onClick = { sqlText = TextFieldValue(""); viewModel.runQuery(""); },
                    icon = Icons.Default.Clear,
                    modifier = Modifier.weight(1f),
                    enabled = sqlText.text.isNotBlank(),
                    color = StatusWarning
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Error
            queryError?.let { error ->
                NeonCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ErrorOutline, null, tint = StatusError, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(error, color = StatusError, fontSize = 13.sp)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Results
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentPrimary)
                }
            } else {
                queryResults?.let { results ->
                    if (results.columns.isNotEmpty()) {
                        Text(
                            "${results.rows.size} rows returned",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        DataGrid(
                            columns = results.columns.map { it.name },
                            rows = results.rows.map { it.values },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        NeonCard {
                            Text("Query executed successfully (no results returned)", color = StatusSuccess, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
