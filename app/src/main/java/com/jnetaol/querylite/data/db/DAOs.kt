package com.jnetaol.querylite.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jnetaol.querylite.data.model.QueryHistory
import com.jnetaol.querylite.data.model.SavedDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface QueryHistoryDao {
    @Query("SELECT * FROM query_history ORDER BY executed_at DESC")
    fun getAll(): Flow<List<QueryHistory>>

    @Query("SELECT * FROM query_history WHERE is_favorite = 1 ORDER BY executed_at DESC")
    fun getFavorites(): Flow<List<QueryHistory>>

    @Query("SELECT * FROM query_history WHERE sql_text LIKE '%' || :search || '%' ORDER BY executed_at DESC")
    suspend fun search(search: String): List<QueryHistory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(query: QueryHistory): Long

    @Update
    suspend fun update(query: QueryHistory)

    @Delete
    suspend fun delete(query: QueryHistory)

    @Query("DELETE FROM query_history")
    suspend fun deleteAll()

    @Query("UPDATE query_history SET is_favorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: Long, favorite: Boolean)
}

@Dao
interface SavedDatabaseDao {
    @Query("SELECT * FROM saved_databases ORDER BY last_opened_at DESC")
    fun getAll(): Flow<List<SavedDatabase>>

    @Query("SELECT * FROM saved_databases WHERE id = :id")
    suspend fun getById(id: Long): SavedDatabase?

    @Query("SELECT * FROM saved_databases WHERE file_path = :path LIMIT 1")
    suspend fun getByPath(path: String): SavedDatabase?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(db: SavedDatabase): Long

    @Update
    suspend fun update(db: SavedDatabase)

    @Delete
    suspend fun delete(db: SavedDatabase)

    @Query("UPDATE saved_databases SET last_opened_at = :timestamp WHERE id = :id")
    suspend fun touchLastOpened(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM saved_databases")
    suspend fun deleteAll()
}
