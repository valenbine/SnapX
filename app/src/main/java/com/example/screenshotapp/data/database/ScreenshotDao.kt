package com.example.screenshotapp.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScreenshotDao {
    
    @Query("SELECT * FROM screenshots ORDER BY timestamp DESC")
    fun getAll(): Flow<List<ScreenshotEntity>>
    
    @Query("SELECT * FROM screenshots WHERE id = :id")
    suspend fun getById(id: Long): ScreenshotEntity?
    
    @Query("SELECT * FROM screenshots WHERE mode = :mode ORDER BY timestamp DESC")
    fun getByMode(mode: String): Flow<List<ScreenshotEntity>>
    
    @Query("SELECT * FROM screenshots WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getByTimeRange(startTime: Long, endTime: Long): Flow<List<ScreenshotEntity>>
    
    @Query("SELECT COUNT(*) FROM screenshots")
    suspend fun getCount(): Int
    
    @Query("SELECT SUM(fileSize) FROM screenshots")
    suspend fun getTotalFileSize(): Long?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(screenshot: ScreenshotEntity): Long
    
    @Delete
    suspend fun delete(screenshot: ScreenshotEntity)
    
    @Query("DELETE FROM screenshots WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("DELETE FROM screenshots WHERE timestamp < :thresholdTime")
    suspend fun deleteOlderThan(thresholdTime: Long): Int
    
    @Query("DELETE FROM screenshots WHERE mode = :mode AND timestamp < :thresholdTime")
    suspend fun deleteOlderThanByMode(mode: String, thresholdTime: Long): Int
}