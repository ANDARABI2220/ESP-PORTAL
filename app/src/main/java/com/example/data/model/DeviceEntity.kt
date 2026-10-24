package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "devices",
    indices = [
        Index(value = ["macAddress"], unique = true),
        Index(value = ["ipAddress"])
    ]
)
data class DeviceEntity(
    @PrimaryKey val deviceId: String, // Unique identifier / UUID or MAC
    val macAddress: String,
    val ipAddress: String,
    val name: String,
    val deviceType: String, // ESP32, ESP_12F, ESP_01, NODEMCU, WEMOS_D1, etc.
    val isConnected: Boolean = true,
    val lastSeen: Long = System.currentTimeMillis(),
    val firmwareVersion: String = "v2.4.1",
    val port: Int = 8080,
    val flashMb: Int = 4,
    val authKey: String = "esp_lan_key_demo"
)
