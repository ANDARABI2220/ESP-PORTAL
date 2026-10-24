package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.CapabilityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CapabilityDao {

    @Query("SELECT * FROM capabilities WHERE deviceId = :deviceId")
    fun getCapabilitiesFlow(deviceId: String): Flow<List<CapabilityEntity>>

    @Query("SELECT * FROM capabilities WHERE deviceId = :deviceId LIMIT 1")
    suspend fun getCapabilityForDevice(deviceId: String): CapabilityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCapability(capability: CapabilityEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCapabilities(capabilities: List<CapabilityEntity>): List<Long>

    @Query("DELETE FROM capabilities WHERE deviceId = :deviceId")
    suspend fun deleteCapabilitiesForDevice(deviceId: String): Int
}
