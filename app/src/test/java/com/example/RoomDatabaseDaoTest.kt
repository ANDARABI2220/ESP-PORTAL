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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RoomDatabaseDaoTest {

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
    fun testDeviceCrudOperations() = runBlocking {
        val device = DeviceEntity(
            deviceId = "dev-esp32-001",
            macAddress = "24:6F:28:B1:9C:01",
            ipAddress = "192.168.1.110",
            name = "Living Room ESP32",
            deviceType = "ESP32",
            isConnected = true
        )

        // 1. Insert
        val rowId = deviceDao.insertDevice(device)
        assertTrue(rowId > 0)

        // 2. Query
        val fetched = deviceDao.getDeviceById("dev-esp32-001")
        assertNotNull(fetched)
        assertEquals("Living Room ESP32", fetched?.name)
        assertEquals("24:6F:28:B1:9C:01", fetched?.macAddress)

        // 3. Update
        val updated = fetched!!.copy(name = "Living Room ESP32 Pro", ipAddress = "192.168.1.111")
        val updateCount = deviceDao.updateDevice(updated)
        assertEquals(1, updateCount)

        val refetched = deviceDao.getDeviceByMac("24:6F:28:B1:9C:01")
        assertEquals("Living Room ESP32 Pro", refetched?.name)
        assertEquals("192.168.1.111", refetched?.ipAddress)

        // 4. Delete
        val deleteCount = deviceDao.deleteDeviceById("dev-esp32-001")
        assertEquals(1, deleteCount)
        assertNull(deviceDao.getDeviceById("dev-esp32-001"))
    }

    @Test
    fun testForeignKeyCascadeDeletion() = runBlocking {
        val device = DeviceEntity(
            deviceId = "dev-esp12-cascade",
            macAddress = "5C:CF:7F:AA:BB:CC",
            ipAddress = "192.168.1.125",
            name = "Weather Station ESP-12F",
            deviceType = "ESP_12F"
        )
        deviceDao.insertDevice(device)

        val capability = CapabilityEntity(
            deviceId = "dev-esp12-cascade",
            sensorTypes = listOf("DHT22", "BMP280"),
            supportedOperations = listOf("GPIO", "PWM", "ADC"),
            ioChannels = 9
        )
        val capId = capabilityDao.insertCapability(capability)
        assertTrue(capId > 0)

        val telemetryLog = TelemetryLogEntity(
            deviceId = "dev-esp12-cascade",
            timestamp = System.currentTimeMillis(),
            voltage = 3.3f,
            temperature = 22.4f,
            sensorReadings = mapOf("dht" to "22.4C")
        )
        val logId = telemetryLogDao.insertLog(telemetryLog)
        assertTrue(logId > 0)

        // Verify inserted
        assertNotNull(capabilityDao.getCapabilityForDevice("dev-esp12-cascade"))
        assertEquals(1, telemetryLogDao.getLogCountForDevice("dev-esp12-cascade"))

        // Delete parent device
        deviceDao.deleteDeviceById("dev-esp12-cascade")

        // Assert CASCADE deleted child capability and telemetry_log
        assertNull(capabilityDao.getCapabilityForDevice("dev-esp12-cascade"))
        assertEquals(0, telemetryLogDao.getLogCountForDevice("dev-esp12-cascade"))
    }

    @Test
    fun testTelemetryPaginationAndPruning() = runBlocking {
        val device = DeviceEntity(
            deviceId = "dev-time-series",
            macAddress = "60:01:94:11:22:33",
            ipAddress = "192.168.1.130",
            name = "Power Logger",
            deviceType = "NODEMCU"
        )
        deviceDao.insertDevice(device)

        val now = System.currentTimeMillis()
        val logs = (1..25).map { i ->
            TelemetryLogEntity(
                deviceId = "dev-time-series",
                timestamp = now - (25 - i) * 60_000L, // 1 minute intervals
                voltage = 3.2f + (i * 0.01f),
                powerMilliwatts = 200f + i,
                sensorReadings = mapOf("sample" to i.toString())
            )
        }
        telemetryLogDao.insertLogs(logs)

        // Test Pagination: Page 1 (limit 10, offset 0)
        val page1 = telemetryLogDao.getLogsPaged("dev-time-series", limit = 10, offset = 0)
        assertEquals(10, page1.size)
        // Most recent first
        assertTrue(page1[0].timestamp >= page1[1].timestamp)

        // Page 2 (limit 10, offset 10)
        val page2 = telemetryLogDao.getLogsPaged("dev-time-series", limit = 10, offset = 10)
        assertEquals(10, page2.size)

        // Page 3 (limit 10, offset 20)
        val page3 = telemetryLogDao.getLogsPaged("dev-time-series", limit = 10, offset = 20)
        assertEquals(5, page3.size)

        // Test Pruning: Purge records older than 10 minutes ago
        val cutoff = now - (10 * 60_000L)
        val purgedCount = telemetryLogDao.purgeOldLogs(cutoff)
        assertTrue(purgedCount >= 14)

        val remainingCount = telemetryLogDao.getLogCountForDevice("dev-time-series")
        assertEquals(25 - purgedCount, remainingCount)
    }

    @Test
    fun testDeviceWithCapabilitiesRelationalQuery() = runBlocking {
        val device = DeviceEntity(
            deviceId = "dev-esp32-rel",
            macAddress = "A0:20:A6:99:88:77",
            ipAddress = "192.168.1.140",
            name = "ESP32 Gateway",
            deviceType = "ESP32"
        )
        deviceDao.insertDevice(device)

        val cap = CapabilityEntity(
            deviceId = "dev-esp32-rel",
            sensorTypes = listOf("DHT22", "INA219", "Hall Effect"),
            supportedOperations = listOf("GPIO", "PWM", "ADC", "OTA"),
            hasBluetooth = true
        )
        capabilityDao.insertCapability(cap)

        val rel = deviceDao.getDeviceWithCapabilities("dev-esp32-rel")
        assertNotNull(rel)
        assertEquals("ESP32 Gateway", rel?.device?.name)
        assertEquals(1, rel?.capabilities?.size)
        assertTrue(rel?.capabilities?.first()?.sensorTypes?.contains("INA219") == true)
        assertTrue(rel?.capabilities?.first()?.hasBluetooth == true)
    }
}
