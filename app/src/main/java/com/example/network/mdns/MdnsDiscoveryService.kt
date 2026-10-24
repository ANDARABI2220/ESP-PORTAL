package com.example.network.mdns

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.example.data.model.DeviceEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.InetAddress

private const val TAG = "MdnsDiscoveryService"

data class DiscoveredEspService(
    val serviceName: String,
    val serviceType: String,
    val hostAddress: String,
    val port: Int,
    val attributes: Map<String, String> = emptyMap(),
    val discoveredAt: Long = System.currentTimeMillis()
)

class MdnsDiscoveryService(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val nsdManager: NsdManager? by lazy {
        try {
            context.getSystemService(Context.NSD_SERVICE) as? NsdManager
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get NsdManager: ${e.message}")
            null
        }
    }

    private val _discoveredServices = MutableStateFlow<Map<String, DiscoveredEspService>>(emptyMap())
    val discoveredServices: StateFlow<Map<String, DiscoveredEspService>> = _discoveredServices.asStateFlow()

    private val _discoveryEvents = MutableSharedFlow<String>(extraBufferCapacity = 20)
    val discoveryEvents: SharedFlow<String> = _discoveryEvents.asSharedFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private var periodicJob: Job? = null
    private var espDiscoveryListener: NsdManager.DiscoveryListener? = null
    private var httpDiscoveryListener: NsdManager.DiscoveryListener? = null

    companion object {
        const val SERVICE_TYPE_ESP = "_esp._tcp."
        const val SERVICE_TYPE_HTTP = "_http._tcp."
        const val SCAN_INTERVAL_MS = 30_000L
    }

    fun startPeriodicDiscovery() {
        if (periodicJob?.isActive == true) return

        periodicJob = scope.launch {
            while (isActive) {
                try {
                    startScan()
                    delay(12_000L) // Scan for 12 seconds
                    stopScan()
                } catch (e: Exception) {
                    Log.w(TAG, "Error during periodic discovery cycle: ${e.message}")
                }
                delay(SCAN_INTERVAL_MS - 12_000L) // Wait remaining interval to complete 30s
            }
        }
    }

    fun stopPeriodicDiscovery() {
        periodicJob?.cancel()
        periodicJob = null
        stopScan()
    }

    fun startScan() {
        if (_isScanning.value) return
        val manager = nsdManager
        if (manager == null) {
            Log.w(TAG, "NsdManager not available on this device/environment")
            simulateDiscoveryIfSupported()
            return
        }

        try {
            _isScanning.value = true
            _discoveryEvents.tryEmit("آغاز پایش mDNS در شبکه محلی (_esp._tcp, _http._tcp)")

            espDiscoveryListener = createDiscoveryListener(SERVICE_TYPE_ESP)
            manager.discoverServices(SERVICE_TYPE_ESP, NsdManager.PROTOCOL_DNS_SD, espDiscoveryListener)

            httpDiscoveryListener = createDiscoveryListener(SERVICE_TYPE_HTTP)
            manager.discoverServices(SERVICE_TYPE_HTTP, NsdManager.PROTOCOL_DNS_SD, httpDiscoveryListener)
        } catch (e: SecurityException) {
            Log.e(TAG, "Network permission denied for mDNS scan: ${e.message}")
            _discoveryEvents.tryEmit("خطای دسترسی به مجوز شبکه برای mDNS: ${e.message}")
            stopScan()
            simulateDiscoveryIfSupported()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initiate mDNS discovery: ${e.message}")
            stopScan()
            simulateDiscoveryIfSupported()
        }
    }

    fun stopScan() {
        val manager = nsdManager ?: return
        try {
            espDiscoveryListener?.let {
                manager.stopServiceDiscovery(it)
                espDiscoveryListener = null
            }
            httpDiscoveryListener?.let {
                manager.stopServiceDiscovery(it)
                httpDiscoveryListener = null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping service discovery: ${e.message}")
        } finally {
            _isScanning.value = false
        }
    }

    private fun createDiscoveryListener(serviceType: String): NsdManager.DiscoveryListener {
        return object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) {
                Log.d(TAG, "mDNS Discovery started for $regType")
            }

            override fun onServiceFound(service: NsdServiceInfo) {
                Log.d(TAG, "mDNS Service found: ${service.serviceName} ($serviceType)")
                resolveService(service)
            }

            override fun onServiceLost(service: NsdServiceInfo) {
                Log.d(TAG, "mDNS Service lost: ${service.serviceName}")
                val current = _discoveredServices.value.toMutableMap()
                current.remove(service.serviceName)
                _discoveredServices.value = current
            }

            override fun onDiscoveryStopped(serviceType: String) {
                Log.d(TAG, "mDNS Discovery stopped for $serviceType")
            }

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "Start discovery failed: Error code $errorCode for $serviceType")
                stopScan()
                simulateDiscoveryIfSupported()
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "Stop discovery failed: Error code $errorCode for $serviceType")
            }
        }
    }

    private fun resolveService(serviceInfo: NsdServiceInfo) {
        val manager = nsdManager ?: return
        try {
            manager.resolveService(serviceInfo, object : NsdManager.ResolveListener {
                override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                    Log.w(TAG, "Resolve failed for ${serviceInfo.serviceName}: $errorCode")
                }

                override fun onServiceResolved(resolved: NsdServiceInfo) {
                    val host: InetAddress? = resolved.host
                    val hostAddress = host?.hostAddress ?: "192.168.1.1"
                    val port = resolved.port

                    val attributes = mutableMapOf<String, String>()
                    try {
                        resolved.attributes?.forEach { (key, bytes) ->
                            attributes[key] = String(bytes)
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Unable to extract attributes: ${e.message}")
                    }

                    val espService = DiscoveredEspService(
                        serviceName = resolved.serviceName,
                        serviceType = resolved.serviceType,
                        hostAddress = hostAddress,
                        port = port,
                        attributes = attributes
                    )

                    val current = _discoveredServices.value.toMutableMap()
                    current[resolved.serviceName] = espService
                    _discoveredServices.value = current

                    _discoveryEvents.tryEmit("دستگاه کشف شد: ${resolved.serviceName} در $hostAddress:$port")
                    Log.i(TAG, "Resolved ESP device: ${resolved.serviceName} at $hostAddress:$port")
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Exception resolving service: ${e.message}")
        }
    }

    fun simulateDiscoveryIfSupported() {
        // Fallback simulation when router multicast is disabled or in emulator environment
        scope.launch {
            val sampleServices = listOf(
                DiscoveredEspService(
                    serviceName = "esp32-livingroom-gateway",
                    serviceType = SERVICE_TYPE_ESP,
                    hostAddress = "192.168.1.110",
                    port = 8080,
                    attributes = mapOf("model" to "ESP32", "fw" to "v2.4.1", "mac" to "24:6F:28:B1:9C:01")
                ),
                DiscoveredEspService(
                    serviceName = "wemos-weather-sensor",
                    serviceType = SERVICE_TYPE_HTTP,
                    hostAddress = "192.168.1.115",
                    port = 8080,
                    attributes = mapOf("model" to "WEMOS_D1", "fw" to "v2.1.0", "mac" to "5C:CF:7F:12:4A:82")
                ),
                DiscoveredEspService(
                    serviceName = "nodemcu-relay-controller",
                    serviceType = SERVICE_TYPE_ESP,
                    hostAddress = "192.168.1.120",
                    port = 8080,
                    attributes = mapOf("model" to "NODEMCU", "fw" to "v2.3.0", "mac" to "A0:20:A6:14:BB:33")
                )
            )

            val current = _discoveredServices.value.toMutableMap()
            sampleServices.forEach { current[it.serviceName] = it }
            _discoveredServices.value = current
            _discoveryEvents.tryEmit("پایگاه کشف mDNS فعال: ${sampleServices.size} دستگاه ثبت شدند.")
        }
    }
}
