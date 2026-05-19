package com.jnetaol.querylite

import android.app.Application
import com.jnetaol.querylite.data.db.AppDatabase
import com.jnetaol.querylite.logger.DebugLogger

class QueryLiteApp : Application() {

    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.getInstance(this)
        DebugLogger.init(this)
        DebugLogger.i("QL-001", "QueryLite Application started")
    }

    companion object {
        lateinit var instance: QueryLiteApp
            private set
    }
}
