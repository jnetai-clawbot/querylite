package com.jnetaol.querylite.engine

import android.database.sqlite.SQLiteDatabase
import com.jnetaol.querylite.data.model.ChangedRow
import com.jnetaol.querylite.data.model.DiffResult
import com.jnetaol.querylite.data.model.TableRow
import com.jnetaol.querylite.logger.DebugLogger

class DiffEngine {

    fun diffTables(
        db1: SQLiteDatabase,
        db2: SQLiteDatabase,
        tableName: String,
        keyColumn: String = "id"
    ): DiffResult {
        DebugLogger.i("QL-300", "Diffing table $tableName between two databases")

        val engine = DatabaseEngine()
        val data1 = engine.getTableData(db1, tableName, limit = 10000)
        val data2 = engine.getTableData(db2, tableName, limit = 10000)

        val map1 = data1.rows.associateBy { it.values[keyColumn] ?: "" }
        val map2 = data2.rows.associateBy { it.values[keyColumn] ?: "" }

        val keys1 = map1.keys.toSet()
        val keys2 = map2.keys.toSet()

        val addedKeys = keys2 - keys1
        val removedKeys = keys1 - keys2
        val commonKeys = keys1.intersect(keys2)

        val addedRows = addedKeys.mapNotNull { map2[it] }
        val removedRows = removedKeys.mapNotNull { map1[it] }

        val changedRows = commonKeys.mapNotNull { key ->
            val row1 = map1[key]!!
            val row2 = map2[key]!!
            val changedColumns = row1.values.entries
                .filter { (k, v) -> row2.values[k] != v }
                .map { it.key }

            if (changedColumns.isNotEmpty()) {
                ChangedRow(db1Row = row1, db2Row = row2, changedColumns = changedColumns)
            } else null
        }

        val result = DiffResult(
            tableName = tableName,
            addedRows = addedRows,
            removedRows = removedRows,
            changedRows = changedRows,
            totalAdded = addedRows.size,
            totalRemoved = removedRows.size,
            totalChanged = changedRows.size
        )
        DebugLogger.i("QL-301", "Diff: +${result.totalAdded} -${result.totalRemoved} ~${result.totalChanged}")
        return result
    }

    fun diffAllTables(
        db1: SQLiteDatabase,
        db2: SQLiteDatabase,
        keyColumn: String = "id"
    ): List<DiffResult> {
        val engine = DatabaseEngine()
        val tables1 = engine.getTableList(db1)
        val tables2 = engine.getTableList(db2)
        val commonTables = tables1.intersect(tables2.toSet())

        DebugLogger.i("QL-302", "Diffing ${commonTables.size} common tables")

        return commonTables.map { tableName ->
            try {
                diffTables(db1, db2, tableName, keyColumn)
            } catch (e: Exception) {
                DebugLogger.e("QL-303", "Error diffing table $tableName: ${e.message}", e)
                DiffResult(tableName = tableName, totalAdded = 0, totalRemoved = 0, totalChanged = 0)
            }
        }.toList()
    }
}
