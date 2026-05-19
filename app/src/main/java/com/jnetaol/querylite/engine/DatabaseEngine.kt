package com.jnetaol.querylite.engine

import android.database.sqlite.SQLiteDatabase
import com.jnetaol.querylite.data.model.ColumnInfo
import com.jnetaol.querylite.data.model.IndexInfo
import com.jnetaol.querylite.data.model.TableData
import com.jnetaol.querylite.data.model.TableInfo
import com.jnetaol.querylite.data.model.TableRow
import com.jnetaol.querylite.logger.DebugLogger
import java.io.File

class DatabaseEngine {

    fun openDatabase(path: String): SQLiteDatabase {
        DebugLogger.i("QL-100", "Opening database: $path")
        val file = File(path)
        if (!file.exists()) throw IllegalStateException("Database file not found: $path")
        return SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE)
    }

    fun getTableList(db: SQLiteDatabase): List<String> {
        val tables = mutableListOf<String>()
        val cursor = db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' AND name NOT LIKE 'android_%' AND name NOT LIKE 'room_%' ORDER BY name",
            null
        )
        cursor.use {
            while (it.moveToNext()) {
                tables.add(it.getString(0))
            }
        }
        DebugLogger.i("QL-101", "Found ${tables.size} tables: ${tables.joinToString()}")
        return tables
    }

    fun getTableInfo(db: SQLiteDatabase, tableName: String): TableInfo {
        val columns = mutableListOf<ColumnInfo>()
        val pragmaCursor = db.rawQuery("PRAGMA table_info(`$tableName`)", null)
        pragmaCursor.use {
            while (it.moveToNext()) {
                columns.add(
                    ColumnInfo(
                        name = it.getString(it.getColumnIndexOrThrow("name")),
                        type = it.getString(it.getColumnIndexOrThrow("type")).ifEmpty { "TEXT" },
                        isPrimaryKey = it.getInt(it.getColumnIndexOrThrow("pk")) == 1,
                        isNullable = it.getInt(it.getColumnIndexOrThrow("notnull")) == 0,
                        defaultValue = it.getString(it.getColumnIndexOrThrow("dflt_value"))
                    )
                )
            }
        }

        val countCursor = db.rawQuery("SELECT COUNT(*) FROM `$tableName`", null)
        var rowCount = 0
        countCursor.use {
            if (it.moveToFirst()) rowCount = it.getInt(0)
        }

        DebugLogger.i("QL-102", "Table $tableName: ${columns.size} columns, $rowCount rows")
        return TableInfo(name = tableName, rowCount = rowCount, columns = columns)
    }

    fun getTableData(
        db: SQLiteDatabase,
        tableName: String,
        limit: Int = 200,
        offset: Int = 0
    ): TableData {
        val tableInfo = getTableInfo(db, tableName)
        val columns = tableInfo.columns
        val columnNames = columns.map { "`${it.name}`" }.joinToString(", ")

        val rows = mutableListOf<TableRow>()
        val cursor = db.rawQuery(
            "SELECT $columnNames FROM `$tableName` LIMIT $limit OFFSET $offset",
            null
        )
        cursor.use {
            while (it.moveToNext()) {
                val values = mutableMapOf<String, String?>()
                columns.forEachIndexed { index, col ->
                    values[col.name] = when {
                        it.isNull(index) -> null
                        else -> it.getString(index) ?: "NULL"
                    }
                }
                rows.add(TableRow(values))
            }
        }

        DebugLogger.i("QL-103", "Fetched ${rows.size} rows from $tableName (offset=$offset, limit=$limit)")
        return TableData(columns = columns, rows = rows)
    }

    fun runQuery(db: SQLiteDatabase, sql: String): TableData? {
        DebugLogger.i("QL-104", "Running query: ${sql.take(200)}")
        val trimmed = sql.trim()

        return try {
            if (trimmed.uppercase().startsWith("SELECT") || trimmed.uppercase().startsWith("PRAGMA")
                || trimmed.uppercase().startsWith("EXPLAIN")
            ) {
                runSelectQuery(db, sql)
            } else {
                db.execSQL(sql)
                DebugLogger.i("QL-105", "Non-SELECT executed successfully")
                null
            }
        } catch (e: Exception) {
            DebugLogger.e("QL-106", "Query error: ${e.message}", e)
            throw QueryExecuteException(e.message ?: "Unknown error", e)
        }
    }

    private fun runSelectQuery(db: SQLiteDatabase, sql: String): TableData {
        val cursor = db.rawQuery(sql, null)
        cursor.use {
            val columns = mutableListOf<ColumnInfo>()
            val colNames = it.columnNames
            for (name in colNames) {
                columns.add(ColumnInfo(name = name, type = "TEXT"))
            }

            val rows = mutableListOf<TableRow>()
            while (it.moveToNext()) {
                val values = mutableMapOf<String, String?>()
                colNames.forEachIndexed { index, name ->
                    values[name] = when {
                        it.isNull(index) -> null
                        else -> it.getString(index)
                    }
                }
                rows.add(TableRow(values))
            }
            DebugLogger.i("QL-107", "Select returned ${rows.size} rows, ${columns.size} columns")
            return TableData(columns = columns, rows = rows)
        }
    }

    fun getSchemaInfo(db: SQLiteDatabase): List<TableInfo> {
        val tables = getTableList(db)
        return tables.map { getTableInfo(db, it) }
    }

    fun getIndexes(db: SQLiteDatabase): List<IndexInfo> {
        val indexes = mutableListOf<IndexInfo>()
        val cursor = db.rawQuery(
            "SELECT name, tbl_name FROM sqlite_master WHERE type='index' AND name NOT LIKE 'sqlite_%' ORDER BY tbl_name, name",
            null
        )
        cursor.use {
            while (it.moveToNext()) {
                val indexName = it.getString(0)
                val tableName = it.getString(1)
                val pragmaCursor = db.rawQuery("PRAGMA index_info(`$indexName`)", null)
                val columns = mutableListOf<String>()
                pragmaCursor.use { pc ->
                    while (pc.moveToNext()) {
                        columns.add(pc.getString(pc.getColumnIndexOrThrow("name")))
                    }
                }
                val isUnique = try {
                    val p2 = db.rawQuery("PRAGMA index_list(`$tableName`)", null)
                    var unique = false
                    p2.use { p2c ->
                        while (p2c.moveToNext()) {
                            if (p2c.getString(p2c.getColumnIndexOrThrow("name")) == indexName
                                && p2c.getInt(p2c.getColumnIndexOrThrow("unique")) == 1
                            ) {
                                unique = true
                            }
                        }
                    }
                    unique
                } catch (_: Exception) { false }

                indexes.add(IndexInfo(name = indexName, tableName = tableName, columns = columns, isUnique = isUnique))
            }
        }
        DebugLogger.i("QL-108", "Found ${indexes.size} indexes")
        return indexes
    }

    fun getTableRowCount(db: SQLiteDatabase, tableName: String): Int {
        val cursor = db.rawQuery("SELECT COUNT(*) FROM `$tableName`", null)
        return cursor.use {
            if (it.moveToFirst()) it.getInt(0) else 0
        }
    }
}

class QueryExecuteException(message: String, cause: Throwable? = null) : Exception(message, cause)
