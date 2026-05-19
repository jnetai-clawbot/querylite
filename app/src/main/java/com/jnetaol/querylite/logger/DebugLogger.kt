package com.jnetaol.querylite.logger

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DebugLogger {
    private const val TAG = "QueryLite"
    private var logFile: File? = null
    private var initialized = false
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    fun init(context: Context) {
        if (initialized) return
        initialized = true
        try {
            val dir = File(context.filesDir, "logs")
            dir.mkdirs()
            logFile = File(dir, "querylite_debug.log")
            if (logFile!!.length() > 5 * 1024 * 1024) {
                logFile!!.delete()
                logFile!!.createNewFile()
            }
            logFile?.let {
                d("QL-000", "DebugLogger initialized at ${it.absolutePath}")
            }
        } catch (_: Exception) {
        }
    }

    private fun write(level: String, code: String, message: String) {
        val line = "[${dateFormat.format(Date())}] $level $code $message\n"
        Log.d(TAG, line.trim())
        try {
            logFile?.let { FileWriter(it, true).use { fw -> fw.write(line) } }
        } catch (_: Exception) {
        }
    }

    fun v(code: String, message: String) = write("V", code, message)
    fun d(code: String, message: String) = write("D", code, message)
    fun i(code: String, message: String) = write("I", code, message)
    fun w(code: String, message: String) = write("W", code, message)
    fun e(code: String, message: String, throwable: Throwable? = null) {
        write("E", code, "$message${throwable?.let { " | ${it.message}" } ?: ""}")
    }

    fun getLogPath(): String = logFile?.absolutePath ?: "Not initialized"
}
