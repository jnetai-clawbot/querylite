package com.jnetaol.querylite.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "query_history")
data class QueryHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "sql_text") val sqlText: String,
    @ColumnInfo(name = "database_path") val databasePath: String,
    @ColumnInfo(name = "executed_at") val executedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean = false,
    @ColumnInfo(name = "execution_time_ms") val executionTimeMs: Long = 0,
    @ColumnInfo(name = "row_count") val rowCount: Int = 0
)

@Entity(tableName = "saved_databases")
data class SavedDatabase(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "display_name") val displayName: String,
    @ColumnInfo(name = "file_path") val filePath: String,
    @ColumnInfo(name = "added_at") val addedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "last_opened_at") val lastOpenedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "table_count") val tableCount: Int = 0,
    @ColumnInfo(name = "file_size_bytes") val fileSizeBytes: Long = 0
)

data class TableInfo(
    val name: String,
    val rowCount: Int = 0,
    val columns: List<ColumnInfo> = emptyList()
)

data class ColumnInfo(
    val name: String,
    val type: String = "TEXT",
    val isPrimaryKey: Boolean = false,
    val isNullable: Boolean = true,
    val defaultValue: String? = null
)

data class TableRow(
    val values: Map<String, String?>
)

data class TableData(
    val columns: List<com.jnetaol.querylite.data.model.ColumnInfo>,
    val rows: List<TableRow>
)

data class DiffResult(
    val tableName: String,
    val addedRows: List<TableRow> = emptyList(),
    val removedRows: List<TableRow> = emptyList(),
    val changedRows: List<ChangedRow> = emptyList(),
    val totalAdded: Int = 0,
    val totalRemoved: Int = 0,
    val totalChanged: Int = 0
)

data class ChangedRow(
    val db1Row: TableRow,
    val db2Row: TableRow,
    val changedColumns: List<String> = emptyList()
)

data class IndexInfo(
    val name: String,
    val tableName: String,
    val columns: List<String> = emptyList(),
    val isUnique: Boolean = false
)
