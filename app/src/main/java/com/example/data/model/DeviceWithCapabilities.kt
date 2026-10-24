package com.example.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class DeviceWithCapabilities(
    @Embedded val device: DeviceEntity,
    @Relation(
        parentColumn = "deviceId",
        entityColumn = "deviceId"
    )
    val capabilities: List<CapabilityEntity>,
    @Relation(
        parentColumn = "deviceId",
        entityColumn = "deviceId"
    )
    val recentTelemetry: List<TelemetryLogEntity> = emptyList()
)
