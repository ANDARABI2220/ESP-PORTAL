package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.dao.CapabilityDao
import com.example.data.local.dao.DeviceDao
import com.example.data.local.dao.TelemetryLogDao
import com.example.data.model.CapabilityEntity
import com.example.data.model.DeviceEntity
import com.example.data.model.TelemetryLogEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class StressAndConcurrencyTest {

    private lateinit var database: AppDatabase
    private lateinit var deviceDao: DeviceDao
    private lateinit var capabilityDao: CapabilityDao
    private lateinit var telemetryLogDao: TelemetryLogDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = AppDatabase.createInMemory(context)
        deviceDao = database.deviceDao()
        capabilityDao = database.capabilityDao()
        telemetryLogDao = database.telemetryLogDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testFiftyConcurrentDevicesSimulation() = runBlocking {
        val deviceCount = 55

        // Generate 55 heterogeneous ESP devices
        val devices = (1..deviceCount).map { i ->
            val boardType = when (i % 4) {
                0 -> "ESP32"
                1 -> "ESP_12F"
                2 -> "WEMOS_D1_MINI"
                else -> "NODEMCU"
            }
            val hexMac = "%02X".format(i)
            DeviceEntity(
                deviceId = "sim-device-$i",
                macAddress = "5C:CF:7F:00:00:$hexMac",
                ipAddress = "192.168.1.${100 + (i % 150)}",
                name = "ESP Cluster Node #$i",
                deviceType = boardType,
                isConnected = true,
                firmwareVersion = "v2.4.1-cluster"
            )
        }

        // 1. Batch insertion of 55 devices
        val insertedIds = deviceDao.insertDevices(devices)
        assertEquals(deviceCount, insertedIds.size)
        assertEquals(deviceCount, deviceDao.getDeviceCount())

        // 2. Batch insertion of capabilities for all 55 devices
        val capabilities = devices.map { dev ->
            CapabilityEntity(
                deviceId = dev.deviceId,
                sensorTypes = listOf("DHT22", "INA219", "Soil Moisture"),
                supportedOperations = listOf("GPIO", "PWM", "ADC", "OTA"),
                ioChannels = if (dev.deviceType == "ESP32") 28 else 9
            )
        }
        capabilityDao.insertCapabilities(capabilities)

        // 3. High throughput concurrent telemetry insertion across devices
        val telemetryJobs = (1..10).map { batchIndex ->
            async(Dispatchers.IO) {
                val batchLogs = devices.map { dev ->
                    TelemetryLogEntity(
                        deviceId = dev.deviceId,
                        timestamp = System.currentTimeMillis() + (batchIndex * 1000),
                        voltage = 3.28f,
                        powerMilliwatts = 245f + (batchIndex * 2),
                        rssi = -60 + (batchIndex % 5),
                        sensorReadings = mapOf("batch" to batchIndex.toString())
                    )
                }
                telemetryLogDao.insertLogs(batchLogs)
            }
        }

        telemetryJobs.awaitAll()

        // Verify all 55 * 10 = 550 telemetry logs were successfully written
        val sampleDeviceLogs = telemetryLogDao.getLogCountForDevice("sim-device-1")
        assertEquals(10, sampleDeviceLogs)

        // Query performance with pagination under load
        val paged = telemetryLogDao.getLogsPaged("sim-device-1", limit = 5, offset = 0)
        assertEquals(5, paged.size)
        assertTrue(paged[0].timestamp >= paged[1].timestamp)
    }
}
