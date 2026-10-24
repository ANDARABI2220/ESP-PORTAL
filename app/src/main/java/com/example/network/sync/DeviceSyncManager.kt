package com.example.network.sync

import android.util.Log
import com.example.data.local.dao.CapabilityDao
import com.example.data.local.dao.DeviceDao
import com.example.data.local.dao.TelemetryLogDao
import com.example.data.model.CapabilityEntity
import com.example.data.model.DeviceEntity
import com.example.data.model.TelemetryLogEntity
import com.example.network.mdns.DiscoveredEspService
import com.example.network.mdns.MdnsDiscoveryService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

private const val TAG = "DeviceSyncManager"

class DeviceSyncManager(
    private val deviceDao: DeviceDao,
    private val capabilityDao: CapabilityDao,
    private val telemetryLogDao: TelemetryLogDao,
    private val mdnsService: MdnsDiscoveryService,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private var syncJob: Job? = null
    private var pruningJob: Job? = null

    // Telemetry retention window: 7 days in milliseconds
    var telemetryRetentionMs: Long = 7 * 24 * 60 * 60 * 1000L

    fun startSync() {
        if (syncJob?.isActive == true) return

        // 1. Listen for mDNS discovered devices and persist into Room
        scope.launch {
            mdnsService.discoveredServices.collect { servicesMap ->
                servicesMap.values.forEach { service ->
                    syncDiscoveredServiceToDatabase(service)
                }
            }
        }

        // 2. Periodic pruning job (runs every 6 hours)
        pruningJob = scope.launch {
            while (isActive) {
                try {
                    val cutoff = System.currentTimeMillis() - telemetryRetentionMs
                    val deletedCount = telemetryLogDao.purgeOldLogs(cutoff)
                    if (deletedCount > 0) {
                        Log.i(TAG, "Pruned $deletedCount telemetry logs older than cutoff $cutoff")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to prune old telemetry logs: ${e.message}")
                }
                delay(6 * 3600 * 1000L) // 6 hours
            }
        }
    }

    suspend fun syncDiscoveredServiceToDatabase(service: DiscoveredEspService) {
        try {
            val mac = service.attributes["mac"] ?: ("5C:CF:7F:" + service.serviceName.hashCode().toString(16).take(6).uppercase().chunked(2).joinToString(":"))
            val existing = deviceDao.getDeviceByMac(mac)
            val deviceId = existing?.deviceId ?: UUID.randomUUID().toString()

            val model = service.attributes["model"] ?: "ESP32"
            val fw = service.attributes["fw"] ?: "v2.4.1"

            val device = DeviceEntity(
                deviceId = deviceId,
                macAddress = mac,
                ipAddress = service.hostAddress,
                name = service.serviceName.replace("-", " ").capitalize(),
                deviceType = model,
                isConnected = true,
                lastSeen = System.currentTimeMillis(),
                firmwareVersion = fw,
                port = service.port
            )

            deviceDao.insertDevice(device)

            // Sync capabilities
            if (capabilityDao.getCapabilityForDevice(deviceId) == null) {
                val caps = CapabilityEntity(
                    deviceId = deviceId,
                    sensorTypes = when (model) {
                        "ESP32" -> listOf("DHT22", "INA219", "Hall Effect", "Capacitive Touch")
                        "WEMOS_D1" -> listOf("DHT22", "BMP280")
                        else -> listOf("Relay 2CH", "Builtin LED")
                    },
                    supportedOperations = listOf("GPIO_DIGITAL", "PWM", "ADC", "OTA", "DEEP_SLEEP"),
                    ioChannels = if (model == "ESP32") 28 else 9,
                    analogChannels = if (model == "ESP32") 2 else 1,
                    hasBluetooth = model == "ESP32"
                )
                capabilityDao.insertCapability(caps)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing discovered service to DB: ${e.message}", e)
        }
    }

    suspend fun recordTelemetry(
        deviceId: String,
        voltage: Float,
        powerMilliwatts: Float,
        rssi: Int,
        heapBytes: Int,
        temperature: Float,
        humidity: Float,
        sensorReadings: Map<String, String> = emptyMap()
    ): Long {
        val entity = TelemetryLogEntity(
            deviceId = deviceId,
            timestamp = System.currentTimeMillis(),
            rssi = rssi,
            heapBytes = heapBytes,
            voltage = voltage,
            powerMilliwatts = powerMilliwatts,
            temperature = temperature,
            humidity = humidity,
            sensorReadings = sensorReadings
        )
        return telemetryLogDao.insertLog(entity)
    }

    fun stopSync() {
        syncJob?.cancel()
        pruningJob?.cancel()
    }
}
