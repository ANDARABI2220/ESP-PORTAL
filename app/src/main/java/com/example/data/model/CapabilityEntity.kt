package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "capabilities",
    foreignKeys = [
        ForeignKey(
            entity = DeviceEntity::class,
            parentColumns = ["deviceId"],
            childColumns = ["deviceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["deviceId"])
    ]
)
data class CapabilityEntity(
    @PrimaryKey(autoGenerate = true) val capabilityId: Long = 0,
    val deviceId: String,
    val sensorTypes: List<String> = emptyList(), // e.g. ["DHT22", "INA219", "BMP280"]
    val supportedOperations: List<String> = emptyList(), // e.g. ["GPIO_DIGITAL", "PWM", "ADC", "OTA", "DEEP_SLEEP"]
    val ioChannels: Int = 16,
    val analogChannels: Int = 1,
    val maxPwmFreqHz: Int = 5000,
    val hasHardwareSpi: Boolean = true,
    val hasHardwareI2c: Boolean = true,
    val hasBluetooth: Boolean = false
)
