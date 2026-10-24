package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.TelemetryLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TelemetryLogDao {

    @Query("SELECT * FROM telemetry_logs WHERE deviceId = :deviceId ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogsFlow(deviceId: String, limit: Int = 50): Flow<List<TelemetryLogEntity>>

    @Query("SELECT * FROM telemetry_logs WHERE deviceId = :deviceId ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    suspend fun getLogsPaged(deviceId: String, limit: Int, offset: Int): List<TelemetryLogEntity>

    @Query("SELECT COUNT(*) FROM telemetry_logs WHERE deviceId = :deviceId")
    suspend fun getLogCountForDevice(deviceId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: TelemetryLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<TelemetryLogEntity>): List<Long>

    @Query("DELETE FROM telemetry_logs WHERE timestamp < :cutoffTimestamp")
    suspend fun purgeOldLogs(cutoffTimestamp: Long): Int

    @Query("DELETE FROM telemetry_logs WHERE deviceId = :deviceId")
    suspend fun deleteAllLogsForDevice(deviceId: String): Int
}
