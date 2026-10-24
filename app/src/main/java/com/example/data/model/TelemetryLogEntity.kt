package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "telemetry_logs",
    foreignKeys = [
        ForeignKey(
            entity = DeviceEntity::class,
            parentColumns = ["deviceId"],
            childColumns = ["deviceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["deviceId"]),
        Index(value = ["timestamp"]),
        Index(value = ["deviceId", "timestamp"])
    ]
)
data class TelemetryLogEntity(
    @PrimaryKey(autoGenerate = true) val logId: Long = 0,
    val deviceId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val rssi: Int = -55,
    val heapBytes: Int = 42800,
    val voltage: Float = 3.3f,
    val powerMilliwatts: Float = 260f,
    val temperature: Float = 24.5f,
    val humidity: Float = 45f,
    val uptimeSeconds: Long = 3600L,
    val sensorReadings: Map<String, String> = emptyMap()
)
