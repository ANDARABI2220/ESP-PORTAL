package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.DeviceEntity
import com.example.data.model.DeviceWithCapabilities
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {

    @Query("SELECT * FROM devices ORDER BY lastSeen DESC")
    fun getAllDevicesFlow(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE isConnected = 1 ORDER BY lastSeen DESC")
    fun getConnectedDevicesFlow(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE deviceId = :deviceId LIMIT 1")
    suspend fun getDeviceById(deviceId: String): DeviceEntity?

    @Query("SELECT * FROM devices WHERE macAddress = :macAddress LIMIT 1")
    suspend fun getDeviceByMac(macAddress: String): DeviceEntity?

    @Query("SELECT COUNT(*) FROM devices")
    suspend fun getDeviceCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: DeviceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevices(devices: List<DeviceEntity>): List<Long>

    @Update
    suspend fun updateDevice(device: DeviceEntity): Int

    @Query("DELETE FROM devices WHERE deviceId = :deviceId")
    suspend fun deleteDeviceById(deviceId: String): Int

    @Delete
    suspend fun deleteDevice(device: DeviceEntity): Int

    @Transaction
    @Query("SELECT * FROM devices WHERE deviceId = :deviceId LIMIT 1")
    suspend fun getDeviceWithCapabilities(deviceId: String): DeviceWithCapabilities?

    @Transaction
    @Query("SELECT * FROM devices ORDER BY lastSeen DESC")
    fun getAllDevicesWithCapabilitiesFlow(): Flow<List<DeviceWithCapabilities>>
}
