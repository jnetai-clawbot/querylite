package com.jnetaol.querylite.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo as RoomColumnInfo

@Entity(tableName = "query_history")
data class QueryHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @RoomColumnInfo(name = "sql_text") val sqlText: String,
    @RoomColumnInfo(name = "database_path") val databasePath: String,
    @RoomColumnInfo(name = "executed_at") val executedAt: Long = System.currentTimeMillis(),
    @RoomColumnInfo(name = "is_favorite") val isFavorite: Boolean = false,
    @RoomColumnInfo(name = "execution_time_ms") val executionTimeMs: Long = 0,
    @RoomColumnInfo(name = "row_count") val rowCount: Int = 0
)

@Entity(tableName = "saved_databases")
data class SavedDatabase(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @RoomColumnInfo(name = "display_name") val displayName: String,
    @RoomColumnInfo(name = "file_path") val filePath: String,
    @RoomColumnInfo(name = "added_at") val addedAt: Long = System.currentTimeMillis(),
    @RoomColumnInfo(name = "last_opened_at") val lastOpenedAt: Long = System.currentTimeMillis(),
    @RoomColumnInfo(name = "table_count") val tableCount: Int = 0,
    @RoomColumnInfo(name = "file_size_bytes") val fileSizeBytes: Long = 0
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
