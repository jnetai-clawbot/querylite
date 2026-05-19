package com.jnetaol.querylite.ui.screens

import android.database.sqlite.SQLiteDatabase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jnetaol.querylite.QueryLiteApp
import com.jnetaol.querylite.data.model.*
import com.jnetaol.querylite.engine.CsvHandler
import com.jnetaol.querylite.engine.DatabaseEngine
import com.jnetaol.querylite.engine.DiffEngine
import com.jnetaol.querylite.logger.DebugLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class AppViewModel : ViewModel() {

    private val engine = DatabaseEngine()
    private val csvHandler = CsvHandler()
    private val diffEngine = DiffEngine()
    private val db = QueryLiteApp.instance.database

    private var currentSqliteDb: SQLiteDatabase? = null
    private var diffDb1: SQLiteDatabase? = null
    private var diffDb2: SQLiteDatabase? = null

    // Database state
    private val _currentDbPath = MutableStateFlow<String?>(null)
    val currentDbPath: StateFlow<String?> = _currentDbPath.asStateFlow()

    private val _currentDbName = MutableStateFlow("No database open")
    val currentDbName: StateFlow<String> = _currentDbName.asStateFlow()

    private val _tables = MutableStateFlow<List<String>>(emptyList())
    val tables: StateFlow<List<String>> = _tables.asStateFlow()

    private val _tableInfos = MutableStateFlow<List<TableInfo>>(emptyList())
    val tableInfos: StateFlow<List<TableInfo>> = _tableInfos.asStateFlow()

    // Selected table state
    private val _selectedTable = MutableStateFlow<String?>(null)
    val selectedTable: StateFlow<String?> = _selectedTable.asStateFlow()

    private val _tableData = MutableStateFlow<TableData?>(null)
    val tableData: StateFlow<TableData?> = _tableData.asStateFlow()

    // Query state
    private val _queryResults = MutableStateFlow<TableData?>(null)
    val queryResults: StateFlow<TableData?> = _queryResults.asStateFlow()

    private val _queryError = MutableStateFlow<String?>(null)
    val queryError: StateFlow<String?> = _queryError.asStateFlow()

    private val _queryHistory = MutableStateFlow<List<QueryHistory>>(emptyList())
    val queryHistory: StateFlow<List<QueryHistory>> = _queryHistory.asStateFlow()

    private val _favoriteQueries = MutableStateFlow<List<QueryHistory>>(emptyList())
    val favoriteQueries: StateFlow<List<QueryHistory>> = _favoriteQueries.asStateFlow()

    // Schema state
    private val _indexes = MutableStateFlow<List<IndexInfo>>(emptyList())
    val indexes: StateFlow<List<IndexInfo>> = _indexes.asStateFlow()

    // Saved databases
    private val _savedDatabases = MutableStateFlow<List<SavedDatabase>>(emptyList())
    val savedDatabases: StateFlow<List<SavedDatabase>> = _savedDatabases.asStateFlow()

    // Diff state
    private val _diffPath1 = MutableStateFlow<String?>(null)
    val diffPath1: StateFlow<String?> = _diffPath1.asStateFlow()

    private val _diffPath2 = MutableStateFlow<String?>(null)
    val diffPath2: StateFlow<String?> = _diffPath2.asStateFlow()

    private val _diffResults = MutableStateFlow<List<DiffResult>>(emptyList())
    val diffResults: StateFlow<List<DiffResult>> = _diffResults.asStateFlow()

    // UI state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    init {
        viewModelScope.launch {
            db.queryHistoryDao().getAll().collect { _queryHistory.value = it }
        }
        viewModelScope.launch {
            db.queryHistoryDao().getFavorites().collect { _favoriteQueries.value = it }
        }
        viewModelScope.launch {
            db.savedDatabaseDao().getAll().collect { _savedDatabases.value = it }
        }
    }

    fun loadDatabase(path: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                withContext(Dispatchers.IO) {
                    currentSqliteDb?.close()
                    currentSqliteDb = engine.openDatabase(path)
                    val tables = engine.getTableList(currentSqliteDb!!)
                    val infos = tables.map { engine.getTableInfo(currentSqliteDb!!, it) }
                    val fileInfo = File(path)

                    _currentDbPath.value = path
                    _currentDbName.value = fileInfo.name
                    _tables.value = tables
                    _tableInfos.value = infos
                    _tableData.value = null
                    _selectedTable.value = null
                    _queryResults.value = null
                    _queryError.value = null

                    // Save/update in history
                    val existing = db.savedDatabaseDao().getByPath(path)
                    if (existing != null) {
                        db.savedDatabaseDao().update(
                            existing.copy(
                                lastOpenedAt = System.currentTimeMillis(),
                                tableCount = tables.size,
                                fileSizeBytes = fileInfo.length()
                            )
                        )
                    } else {
                        db.savedDatabaseDao().insert(
                            SavedDatabase(
                                displayName = fileInfo.name,
                                filePath = path,
                                tableCount = tables.size,
                                fileSizeBytes = fileInfo.length()
                            )
                        )
                    }

                    _indexes.value = try { engine.getIndexes(currentSqliteDb!!) } catch (_: Exception) { emptyList() }
                }
                _statusMessage.value = "Loaded: ${File(path).name} (${_tables.value.size} tables)"
            } catch (e: Exception) {
                DebugLogger.e("QL-400", "Failed to load database: ${e.message}", e)
                _statusMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectTable(tableName: String) {
        viewModelScope.launch {
            val sqliteDb = currentSqliteDb ?: return@launch
            _isLoading.value = true
            try {
                withContext(Dispatchers.IO) {
                    _selectedTable.value = tableName
                    _tableData.value = engine.getTableData(sqliteDb, tableName)
                }
            } catch (e: Exception) {
                _statusMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMoreRows() {
        viewModelScope.launch {
            val sqliteDb = currentSqliteDb ?: return@launch
            val table = _selectedTable.value ?: return@launch
            val currentData = _tableData.value ?: return@launch
            try {
                withContext(Dispatchers.IO) {
                    val moreData = engine.getTableData(sqliteDb, table, offset = currentData.rows.size)
                    _tableData.value = currentData.copy(rows = currentData.rows + moreData.rows)
                }
            } catch (e: Exception) {
                _statusMessage.value = "Error: ${e.message}"
            }
        }
    }

    fun runQuery(sql: String) {
        viewModelScope.launch {
            val sqliteDb = currentSqliteDb ?: run {
                _queryError.value = "No database open"
                return@launch
            }
            _queryError.value = null
            _isLoading.value = true
            val startTime = System.currentTimeMillis()
            try {
                var results: TableData? = null
                withContext(Dispatchers.IO) {
                    results = engine.runQuery(sqliteDb, sql)
                }
                val execTime = System.currentTimeMillis() - startTime
                val rowCount = results?.rows?.size ?: 0
                _queryResults.value = results

                val path = _currentDbPath.value ?: ""
                db.queryHistoryDao().insert(
                    QueryHistory(
                        sqlText = sql,
                        databasePath = path,
                        executionTimeMs = execTime,
                        rowCount = rowCount
                    )
                )
                if (results != null) {
                    _statusMessage.value = "Query OK: $rowCount rows in ${execTime}ms"
                } else {
                    _statusMessage.value = "Executed successfully in ${execTime}ms"
                }
            } catch (e: Exception) {
                val execTime = System.currentTimeMillis() - startTime
                _queryError.value = e.message ?: "Unknown error"
                _queryResults.value = null
                val path = _currentDbPath.value ?: ""
                db.queryHistoryDao().insert(
                    QueryHistory(
                        sqlText = sql,
                        databasePath = path,
                        executionTimeMs = execTime,
                        rowCount = -1
                    )
                )
                _statusMessage.value = "Query error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleFavoriteQuery(query: QueryHistory) {
        viewModelScope.launch {
            db.queryHistoryDao().setFavorite(query.id, !query.isFavorite)
        }
    }

    fun deleteQueryHistory(query: QueryHistory) {
        viewModelScope.launch {
            db.queryHistoryDao().delete(query)
        }
    }

    fun clearQueryHistory() {
        viewModelScope.launch {
            db.queryHistoryDao().deleteAll()
        }
    }

    fun importCsv(tableName: String, csvPath: String, createTable: Boolean) {
        viewModelScope.launch {
            val sqliteDb = currentSqliteDb ?: return@launch
            _isLoading.value = true
            try {
                var count = 0
                withContext(Dispatchers.IO) {
                    count = csvHandler.importCsv(sqliteDb, tableName, File(csvPath), createTable)
                }
                _statusMessage.value = "Imported $count rows into $tableName"
                // Refresh table list
                withContext(Dispatchers.IO) {
                    _tables.value = engine.getTableList(sqliteDb)
                }
            } catch (e: Exception) {
                _statusMessage.value = "Import error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun exportTableToCsv(tableName: String, outputPath: String) {
        viewModelScope.launch {
            val sqliteDb = currentSqliteDb ?: return@launch
            _isLoading.value = true
            try {
                var count = 0
                withContext(Dispatchers.IO) {
                    count = csvHandler.exportTableToCsv(sqliteDb, tableName, File(outputPath))
                }
                _statusMessage.value = "Exported $count rows to ${File(outputPath).name}"
            } catch (e: Exception) {
                _statusMessage.value = "Export error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun exportQueryToCsv(sql: String, outputPath: String) {
        viewModelScope.launch {
            val sqliteDb = currentSqliteDb ?: return@launch
            _isLoading.value = true
            try {
                var count = 0
                withContext(Dispatchers.IO) {
                    count = csvHandler.exportQueryToCsv(sqliteDb, sql, File(outputPath))
                }
                _statusMessage.value = "Exported $count rows"
            } catch (e: Exception) {
                _statusMessage.value = "Export error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setDiffDatabase1(path: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    diffDb1?.close()
                    diffDb1 = engine.openDatabase(path)
                    _diffPath1.value = path
                }
                _statusMessage.value = "DB1: ${File(path).name}"
            } catch (e: Exception) {
                _statusMessage.value = "Error opening DB1: ${e.message}"
            }
        }
    }

    fun setDiffDatabase2(path: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    diffDb2?.close()
                    diffDb2 = engine.openDatabase(path)
                    _diffPath2.value = path
                }
                _statusMessage.value = "DB2: ${File(path).name}"
            } catch (e: Exception) {
                _statusMessage.value = "Error opening DB2: ${e.message}"
            }
        }
    }

    fun runDiff() {
        viewModelScope.launch {
            val db1 = diffDb1 ?: run {
                _statusMessage.value = "Select Database 1 first"
                return@launch
            }
            val db2 = diffDb2 ?: run {
                _statusMessage.value = "Select Database 2 first"
                return@launch
            }
            _isLoading.value = true
            try {
                var results: List<DiffResult> = emptyList()
                withContext(Dispatchers.IO) {
                    results = diffEngine.diffAllTables(db1, db2)
                }
                _diffResults.value = results
                _statusMessage.value = "Diff complete: ${results.size} tables compared"
            } catch (e: Exception) {
                _statusMessage.value = "Diff error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadSchema() {
        viewModelScope.launch {
            val sqliteDb = currentSqliteDb ?: return@launch
            _isLoading.value = true
            try {
                withContext(Dispatchers.IO) {
                    _tableInfos.value = engine.getSchemaInfo(sqliteDb)
                    _indexes.value = engine.getIndexes(sqliteDb)
                }
            } catch (e: Exception) {
                _statusMessage.value = "Schema error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createNewDatabase(path: String): Boolean {
        return try {
            val file = File(path)
            file.parentFile?.mkdirs()
            SQLiteDatabase.openOrCreateDatabase(file, null).close()
            loadDatabase(path)
            true
        } catch (e: Exception) {
            DebugLogger.e("QL-401", "Create DB error: ${e.message}", e)
            _statusMessage.value = "Create error: ${e.message}"
            false
        }
    }

    fun removeSavedDatabase(savedDb: SavedDatabase) {
        viewModelScope.launch {
            db.savedDatabaseDao().delete(savedDb)
        }
    }

    fun clearStatus() {
        _statusMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        currentSqliteDb?.close()
        diffDb1?.close()
        diffDb2?.close()
    }
}
