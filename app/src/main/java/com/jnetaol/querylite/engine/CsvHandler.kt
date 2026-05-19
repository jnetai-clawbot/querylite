package com.jnetaol.querylite.engine

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.jnetaol.querylite.data.model.ColumnInfo
import com.jnetaol.querylite.data.model.TableRow
import com.jnetaol.querylite.logger.DebugLogger
import java.io.BufferedReader
import java.io.File
import java.io.FileWriter

class CsvHandler {

    fun importCsv(
        db: SQLiteDatabase,
        tableName: String,
        csvFile: File,
        createTable: Boolean = false,
        columns: List<ColumnInfo> = emptyList()
    ): Int {
        DebugLogger.i("QL-200", "Importing CSV to $tableName from ${csvFile.absolutePath}")

        val reader = csvFile.bufferedReader()
        val headerLine = reader.readLine() ?: throw IllegalStateException("CSV file is empty")
        val headers = parseCsvLine(headerLine)

        if (createTable) {
            createTableFromCsv(db, tableName, headers, columns)
        }

        var importedCount = 0
        db.beginTransaction()
        try {
            reader.forEachLine { line ->
                val values = parseCsvLine(line)
                if (values.size >= headers.size) {
                    val cv = ContentValues()
                    headers.forEachIndexed { index, header ->
                        if (index < values.size) {
                            cv.put(header, values[index].trim())
                        }
                    }
                    db.insert(tableName, null, cv)
                    importedCount++
                }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }

        DebugLogger.i("QL-201", "Imported $importedCount rows into $tableName")
        return importedCount
    }

    private fun createTableFromCsv(
        db: SQLiteDatabase,
        tableName: String,
        headers: List<String>,
        columnInfos: List<ColumnInfo>
    ) {
        val colDefs = headers.joinToString(", ") { header ->
            val colInfo = columnInfos.find { it.name == header }
            val type = colInfo?.type ?: "TEXT"
            "`$header` $type"
        }
        val sql = "CREATE TABLE IF NOT EXISTS `$tableName` ($colDefs)"
        db.execSQL(sql)
        DebugLogger.i("QL-202", "Created table: $sql")
    }

    fun exportTableToCsv(
        db: SQLiteDatabase,
        tableName: String,
        outputFile: File
    ): Int {
        DebugLogger.i("QL-203", "Exporting $tableName to ${outputFile.absolutePath}")

        val columns = mutableListOf<String>()
        val pragmaCursor = db.rawQuery("PRAGMA table_info(`$tableName`)", null)
        pragmaCursor.use {
            while (it.moveToNext()) {
                columns.add(it.getString(it.getColumnIndexOrThrow("name")))
            }
        }

        val writer = FileWriter(outputFile)
        writer.use { fw ->
            fw.write(columns.joinToString(",") { escapeCsvField(it) })
            fw.write("\n")

            val cursor = db.rawQuery("SELECT * FROM `$tableName`", null)
            var count = 0
            cursor.use {
                while (it.moveToNext()) {
                    val line = columns.joinToString(",") { colName ->
                        val colIndex = it.getColumnIndex(colName)
                        if (colIndex == -1 || it.isNull(colIndex)) ""
                        else escapeCsvField(it.getString(colIndex) ?: "")
                    }
                    fw.write(line)
                    fw.write("\n")
                    count++
                }
            }
            DebugLogger.i("QL-204", "Exported $count rows to CSV")
            return count
        }
    }

    fun exportQueryToCsv(
        db: SQLiteDatabase,
        sql: String,
        outputFile: File
    ): Int {
        DebugLogger.i("QL-205", "Exporting query results to ${outputFile.absolutePath}")

        val cursor = db.rawQuery(sql, null)
        val writer = FileWriter(outputFile)
        return writer.use { fw ->
            val colNames = cursor.columnNames
            fw.write(colNames.joinToString(",") { escapeCsvField(it) })
            fw.write("\n")

            var count = 0
            cursor.use {
                while (it.moveToNext()) {
                    val line = colNames.joinToString(",") { name ->
                        val index = it.getColumnIndex(name)
                        if (index == -1 || it.isNull(index)) ""
                        else escapeCsvField(it.getString(index) ?: "")
                    }
                    fw.write(line)
                    fw.write("\n")
                    count++
                }
            }
            count
        }
    }

    private fun escapeCsvField(field: String): String {
        return if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            "\"${field.replace("\"", "\"\"")}\""
        } else {
            field
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false

        for (char in line) {
            when {
                char == '"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    result.add(current.toString().trim('"'))
                    current = StringBuilder()
                }
                else -> current.append(char)
            }
        }
        result.add(current.toString().trim('"'))
        return result
    }
}
