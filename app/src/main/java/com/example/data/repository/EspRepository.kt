package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.PinStateEntity
import com.example.data.local.dao.CapabilityDao
import com.example.data.local.dao.DeviceDao
import com.example.data.local.EspDeviceEntity
import com.example.data.local.PinStateDao
import com.example.data.local.TelemetryRecordEntity
import com.example.data.local.toEspDevice
import com.example.data.local.toTelemetryRecord
import com.example.data.local.dao.TelemetryLogDao
import com.example.data.model.CapabilityEntity
import com.example.data.model.DeviceEntity
import com.example.data.model.TelemetryLogEntity
import com.example.data.model.BoardProfiles
import com.example.data.model.EspBoardType
import com.example.data.model.JsonFrame
import com.example.data.model.PinMode
import com.example.data.model.TelemetryData
import com.example.network.mdns.MdnsDiscoveryService
import com.example.network.sync.DeviceSyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

class EspRepository(
    val database: AppDatabase,
    val mdnsDiscoveryService: MdnsDiscoveryService? = null,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    val deviceDao: DeviceDao = database.deviceDao()
    val capabilityDao: CapabilityDao = database.capabilityDao()
    val telemetryLogDao: TelemetryLogDao = database.telemetryLogDao()
    val pinStateDao: PinStateDao = database.pinStateDao()

    val syncManager: DeviceSyncManager? = mdnsDiscoveryService?.let {
        DeviceSyncManager(deviceDao, capabilityDao, telemetryLogDao, it, scope)
    }

    val allDevices: Flow<List<EspDeviceEntity>> = deviceDao.getAllDevicesFlow().map { list ->
        list.map { it.toEspDevice() }
    }

    val connectedDevices: Flow<List<EspDeviceEntity>> = deviceDao.getConnectedDevicesFlow().map { list ->
        list.map { it.toEspDevice() }
    }

    private val _liveTelemetry = MutableStateFlow<Map<String, TelemetryData>>(emptyMap())
    val liveTelemetry: StateFlow<Map<String, TelemetryData>> = _liveTelemetry.asStateFlow()

    private val _recentLogs = MutableStateFlow<List<JsonFrame>>(emptyList())
    val recentLogs: StateFlow<List<JsonFrame>> = _recentLogs.asStateFlow()

    private var frameSequence = 1000L

    init {
        scope.launch {
            seedDefaultDevicesIfEmpty()
            syncManager?.startSync()
            startTelemetrySimulation()
        }
    }

    private suspend fun seedDefaultDevicesIfEmpty() {
        val existing = deviceDao.getAllDevicesFlow().first()
        if (existing.isEmpty()) {
            val defaults = listOf(
                DeviceEntity(
                    deviceId = "24:6F:28:B1:9C:01",
                    macAddress = "24:6F:28:B1:9C:01",
                    name = "ESP32 سرور مرکزی IoT",
                    deviceType = EspBoardType.ESP32.name,
                    ipAddress = "192.168.1.110",
                    port = 8080,
                    isConnected = true,
                    flashMb = 4,
                    firmwareVersion = "v2.4.1-dualcore"
                ),
                DeviceEntity(
                    deviceId = "5C:CF:7F:12:4A:82",
                    macAddress = "5C:CF:7F:12:4A:82",
                    name = "Wemos D1 ایستگاه هواشناسی",
                    deviceType = EspBoardType.WEMOS_D1_MINI.name,
                    ipAddress = "192.168.1.115",
                    port = 8080,
                    isConnected = true,
                    flashMb = 4,
                    firmwareVersion = "v2.1.0-dht22"
                ),
                DeviceEntity(
                    deviceId = "A0:20:A6:14:BB:33",
                    macAddress = "A0:20:A6:14:BB:33",
                    name = "NodeMCU کنترل رله‌ها",
                    deviceType = EspBoardType.NODEMCU.name,
                    ipAddress = "192.168.1.120",
                    port = 8080,
                    isConnected = true,
                    flashMb = 4,
                    firmwareVersion = "v2.3.0-relay4ch"
                ),
                DeviceEntity(
                    deviceId = "60:01:94:07:3E:44",
                    macAddress = "60:01:94:07:3E:44",
                    name = "ESP-12F مانیتور انرژی",
                    deviceType = EspBoardType.ESP_12F.name,
                    ipAddress = "192.168.1.125",
                    port = 8080,
                    isConnected = true,
                    flashMb = 4,
                    firmwareVersion = "v2.0.8-ina219"
                ),
                DeviceEntity(
                    deviceId = "18:FE:34:01:AA:55",
                    macAddress = "18:FE:34:01:AA:55",
                    name = "ESP-01S سنسور درب ورودی",
                    deviceType = EspBoardType.ESP_01.name,
                    ipAddress = "192.168.1.130",
                    port = 8080,
                    isConnected = true,
                    flashMb = 1,
                    firmwareVersion = "v1.9.4-deepsleep"
                )
            )

            // Batch insertion using insertDevices
            deviceDao.insertDevices(defaults)

            defaults.forEach { dev ->
                val boardEnum = runCatching { EspBoardType.valueOf(dev.deviceType) }.getOrDefault(EspBoardType.ESP_12F)

                // Insert capabilities
                capabilityDao.insertCapability(
                    CapabilityEntity(
                        deviceId = dev.deviceId,
                        sensorTypes = when (boardEnum) {
                            EspBoardType.ESP32 -> listOf("DHT22", "INA219", "Hall Effect")
                            EspBoardType.WEMOS_D1_MINI -> listOf("DHT22", "BMP280")
                            else -> listOf("Relay 2CH", "Builtin LED")
                        },
                        supportedOperations = listOf("GPIO_DIGITAL", "PWM", "ADC", "OTA", "DEEP_SLEEP"),
                        ioChannels = if (boardEnum == EspBoardType.ESP32) 28 else 9,
                        analogChannels = if (boardEnum == EspBoardType.ESP32) 2 else 1,
                        hasBluetooth = boardEnum == EspBoardType.ESP32
                    )
                )

                val pins = BoardProfiles.getPinsForBoard(boardEnum)
                val pinEntities = pins.map { pin ->
                    PinStateEntity(
                        id = "${dev.deviceId}_${pin.gpio}",
                        deviceId = dev.deviceId,
                        gpio = pin.gpio,
                        mode = pin.defaultMode.name,
                        digitalState = if (pin.gpio == 2 || pin.gpio == 16) 1 else 0,
                        pwmValue = 0
                    )
                }
                pinStateDao.upsertPinStates(pinEntities)
            }
        }
    }

    fun getPinsFlow(deviceId: String): Flow<List<PinStateEntity>> {
        return pinStateDao.getPinStatesFlow(deviceId)
    }

    fun getDeviceTelemetryHistory(deviceId: String, limit: Int = 50): Flow<List<TelemetryRecordEntity>> {
        return telemetryLogDao.getRecentLogsFlow(deviceId, limit).map { logs ->
            logs.map { it.toTelemetryRecord() }
        }
    }

    suspend fun getDeviceTelemetryPaged(deviceId: String, limit: Int, offset: Int): List<TelemetryLogEntity> {
        return telemetryLogDao.getLogsPaged(deviceId, limit, offset)
    }

    suspend fun setPinState(deviceId: String, gpio: Int, state: Int) {
        val pinId = "${deviceId}_$gpio"
        pinStateDao.upsertPinState(
            PinStateEntity(
                id = pinId,
                deviceId = deviceId,
                gpio = gpio,
                mode = PinMode.OUTPUT.name,
                digitalState = state,
                pwmValue = if (state == 1) 1023 else 0,
                lastUpdated = System.currentTimeMillis()
            )
        )

        logFrame(
            deviceId = deviceId,
            type = "GPIO_SET",
            payload = mapOf("gpio" to gpio, "state" to state, "mode" to "OUTPUT")
        )
    }

    suspend fun setPwmValue(deviceId: String, gpio: Int, pwmValue: Int) {
        val pinId = "${deviceId}_$gpio"
        pinStateDao.upsertPinState(
            PinStateEntity(
                id = pinId,
                deviceId = deviceId,
                gpio = gpio,
                mode = PinMode.PWM.name,
                digitalState = if (pwmValue > 0) 1 else 0,
                pwmValue = pwmValue,
                lastUpdated = System.currentTimeMillis()
            )
        )

        logFrame(
            deviceId = deviceId,
            type = "GPIO_SET",
            payload = mapOf("gpio" to gpio, "pwm" to pwmValue, "mode" to "PWM")
        )
    }

    suspend fun togglePin(deviceId: String, gpio: Int, currentState: Int) {
        setPinState(deviceId, gpio, if (currentState == 1) 0 else 1)
    }

    suspend fun addDevice(name: String, boardType: EspBoardType, ipAddress: String, port: Int = 8080) {
        val randomMac = "5C:CF:7F:" + (10..99).random() + ":" + (10..99).random() + ":" + (10..99).random()
        val deviceId = UUID.randomUUID().toString()

        val device = DeviceEntity(
            deviceId = deviceId,
            macAddress = randomMac,
            name = name.ifBlank { "${boardType.displayName} New" },
            deviceType = boardType.name,
            ipAddress = ipAddress.ifBlank { "192.168.1." + (140..250).random() },
            port = port,
            isConnected = true,
            flashMb = boardType.defaultFlashMb
        )

        deviceDao.insertDevice(device)

        capabilityDao.insertCapability(
            CapabilityEntity(
                deviceId = deviceId,
                sensorTypes = listOf("DHT22", "Relay"),
                supportedOperations = listOf("GPIO_DIGITAL", "PWM", "ADC", "OTA"),
                ioChannels = if (boardType == EspBoardType.ESP32) 28 else 9,
                hasBluetooth = boardType == EspBoardType.ESP32
            )
        )

        val pins = BoardProfiles.getPinsForBoard(boardType)
        val pinEntities = pins.map { pin ->
            PinStateEntity(
                id = "${deviceId}_${pin.gpio}",
                deviceId = deviceId,
                gpio = pin.gpio,
                mode = pin.defaultMode.name,
                digitalState = 0,
                pwmValue = 0
            )
        }
        pinStateDao.upsertPinStates(pinEntities)
        logFrame(deviceId, "DISCOVERY", mapOf("mDNS" to "_esp._tcp.local", "status" to "REGISTERED"))
    }

    suspend fun removeDevice(id: String) {
        // Cascade deletion will automatically delete capability and telemetry_logs!
        deviceDao.deleteDeviceById(id)
        pinStateDao.deletePinStatesForDevice(id)
    }

    suspend fun toggleDeviceConnection(id: String, isConnected: Boolean) {
        val dev = deviceDao.getDeviceById(id) ?: return
        deviceDao.updateDevice(dev.copy(isConnected = isConnected))
        logFrame(id, "CONFIG", mapOf("connection" to if (isConnected) "CONNECTED" else "DISCONNECTED"))
    }

    fun purgeOldTelemetry(retentionPeriodMs: Long = 7 * 24 * 3600 * 1000L) {
        scope.launch {
            val cutoff = System.currentTimeMillis() - retentionPeriodMs
            telemetryLogDao.purgeOldLogs(cutoff)
        }
    }

    private fun logFrame(deviceId: String, type: String, payload: Map<String, Any?>) {
        frameSequence++
        val frame = JsonFrame(
            deviceId = deviceId,
            messageId = UUID.randomUUID().toString().take(8),
            sequence = frameSequence,
            timestamp = System.currentTimeMillis(),
            type = type,
            payload = payload
        )
        val current = _recentLogs.value.toMutableList()
        current.add(0, frame)
        if (current.size > 80) current.removeAt(current.lastIndex)
        _recentLogs.value = current
    }

    private fun startTelemetrySimulation() {
        scope.launch {
            while (isActive) {
                delay(3000L)
                val devices = deviceDao.getAllDevicesFlow().first()
                val currentMap = _liveTelemetry.value.toMutableMap()

                devices.filter { it.isConnected }.take(5).forEach { dev ->
                    val prev = currentMap[dev.deviceId] ?: TelemetryData()
                    val newRssi = (prev.rssi + Random.nextInt(-2, 3)).coerceIn(-85, -40)
                    val newHeap = (prev.heapBytes + Random.nextInt(-400, 400)).coerceIn(12000, 80000)
                    val newVolt = (3.25f + Random.nextFloat() * 0.08f).coerceIn(3.18f, 3.35f)
                    val newPower = (newVolt * (75f + Random.nextFloat() * 20f)).coerceIn(200f, 450f)
                    val newTemp = (prev.temperature + (Random.nextFloat() - 0.5f) * 0.4f).coerceIn(18f, 42f)
                    val newHumidity = (prev.humidity + (Random.nextFloat() - 0.5f) * 0.8f).coerceIn(20f, 85f)
                    val newUptime = prev.uptimeSeconds + 3L

                    val updated = TelemetryData(
                        rssi = newRssi,
                        heapBytes = newHeap,
                        voltage = newVolt,
                        powerMilliwatts = newPower,
                        temperature = newTemp,
                        humidity = newHumidity,
                        uptimeSeconds = newUptime,
                        connectedSensors = prev.connectedSensors
                    )
                    currentMap[dev.deviceId] = updated

                    // Insert to database periodically
                    if (Random.nextInt(0, 3) == 0) {
                        telemetryLogDao.insertLog(
                            TelemetryLogEntity(
                                deviceId = dev.deviceId,
                                timestamp = System.currentTimeMillis(),
                                rssi = newRssi,
                                heapBytes = newHeap,
                                voltage = newVolt,
                                powerMilliwatts = newPower,
                                temperature = newTemp,
                                humidity = newHumidity,
                                sensorReadings = mapOf("temp" to "%.1f".format(newTemp), "hum" to "%.1f".format(newHumidity))
                            )
                        )
                    }
                }
                _liveTelemetry.value = currentMap
            }
        }
    }
}
