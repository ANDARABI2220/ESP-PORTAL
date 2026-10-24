package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.Update
import com.example.data.local.converters.RoomConverters
import com.example.data.local.dao.CapabilityDao
import com.example.data.local.dao.DeviceDao
import com.example.data.local.dao.TelemetryLogDao
import com.example.data.model.CapabilityEntity
import com.example.data.model.DeviceEntity
import com.example.data.model.TelemetryLogEntity
import kotlinx.coroutines.flow.Flow

// Legacy entity aliases kept for UI & ViewModels
data class EspDeviceEntity(
    val id: String,
    val name: String,
    val boardType: String,
    val ipAddress: String,
    val port: Int = 8080,
    val isConnected: Boolean = true,
    val lastSeen: Long = System.currentTimeMillis(),
    val flashMb: Int = 4,
    val firmwareVersion: String = "v2.4.1",
    val authKey: String = "esp_lan_key_demo"
)

data class TelemetryRecordEntity(
    val id: Long = 0,
    val deviceId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val rssi: Int,
    val heapBytes: Int,
    val voltage: Float,
    val powerMilliwatts: Float,
    val temperature: Float,
    val humidity: Float
)

fun DeviceEntity.toEspDevice(): EspDeviceEntity = EspDeviceEntity(
    id = this.deviceId,
    name = this.name,
    boardType = this.deviceType,
    ipAddress = this.ipAddress,
    port = this.port,
    isConnected = this.isConnected,
    lastSeen = this.lastSeen,
    flashMb = this.flashMb,
    firmwareVersion = this.firmwareVersion,
    authKey = this.authKey
)

fun TelemetryLogEntity.toTelemetryRecord(): TelemetryRecordEntity = TelemetryRecordEntity(
    id = this.logId,
    deviceId = this.deviceId,
    timestamp = this.timestamp,
    rssi = this.rssi,
    heapBytes = this.heapBytes,
    voltage = this.voltage,
    powerMilliwatts = this.powerMilliwatts,
    temperature = this.temperature,
    humidity = this.humidity
)

@Entity(tableName = "pin_states")
data class PinStateEntity(
    @PrimaryKey val id: String, // deviceId_gpio
    val deviceId: String,
    val gpio: Int,
    val mode: String, // INPUT, OUTPUT, PWM, ADC
    val digitalState: Int = 0, // 0 for LOW, 1 for HIGH
    val pwmValue: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Dao
interface PinStateDao {
    @Query("SELECT * FROM pin_states WHERE deviceId = :deviceId ORDER BY gpio ASC")
    fun getPinStatesFlow(deviceId: String): Flow<List<PinStateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPinState(pinState: PinStateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPinStates(pinStates: List<PinStateEntity>)

    @Query("DELETE FROM pin_states WHERE deviceId = :deviceId")
    suspend fun deletePinStatesForDevice(deviceId: String)
}

@Database(
    entities = [
        DeviceEntity::class,
        CapabilityEntity::class,
        TelemetryLogEntity::class,
        PinStateEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun deviceDao(): DeviceDao
    abstract fun capabilityDao(): CapabilityDao
    abstract fun telemetryLogDao(): TelemetryLogDao
    abstract fun pinStateDao(): PinStateDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "esp_iot_manager.db"
                )
                    // Enable Write-Ahead Logging (WAL) for high concurrency read/write
                    .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun createInMemory(context: Context): AppDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                AppDatabase::class.java
            )
                .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                .allowMainThreadQueries()
                .build()
        }
    }
}
