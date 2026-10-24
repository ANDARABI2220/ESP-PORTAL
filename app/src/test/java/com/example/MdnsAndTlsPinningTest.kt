package com.example

import androidx.test.core.app.ApplicationProvider
import com.example.network.client.EspTlsHttpClient
import com.example.network.mdns.DiscoveredEspService
import com.example.network.mdns.MdnsDiscoveryService
import okhttp3.CertificatePinner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MdnsAndTlsPinningTest {

    @Test
    fun testCertificatePinnerConfiguration() {
        val pinner = EspTlsHttpClient.createCertificatePinner()
        assertNotNull(pinner)

        // Verify pins set on local patterns
        val pins = pinner.pins
        assertTrue("Certificate pinner should have configured pins", pins.isNotEmpty())

        val hasEspPrimaryPin = pins.any { pin ->
            pin.hash.base64() == "WoiWRyIOVNa9ihaBciRSC7XHjliYS9VwUGOIud4PB18="
        }
        assertTrue("Primary SHA-256 certificate pin must be configured", hasEspPrimaryPin)
    }

    @Test
    fun testOkHttpClientConfiguration() {
        val client = EspTlsHttpClient.buildOkHttpClient()
        assertNotNull(client)

        assertEquals(5000, client.connectTimeoutMillis)
        assertEquals(10000, client.readTimeoutMillis)
        assertEquals(10000, client.writeTimeoutMillis)
        assertTrue(client.retryOnConnectionFailure)
        assertNotNull(client.certificatePinner)
    }

    @Test
    fun testMdnsServiceAttributesParsing() {
        val service = DiscoveredEspService(
            serviceName = "esp32-sensor-hub",
            serviceType = "_esp._tcp.",
            hostAddress = "192.168.1.150",
            port = 8080,
            attributes = mapOf(
                "model" to "ESP32",
                "mac" to "24:6F:28:11:22:33",
                "fw" to "v2.4.2"
            )
        )

        assertEquals("esp32-sensor-hub", service.serviceName)
        assertEquals("192.168.1.150", service.hostAddress)
        assertEquals(8080, service.port)
        assertEquals("ESP32", service.attributes["model"])
        assertEquals("24:6F:28:11:22:33", service.attributes["mac"])
    }

    @Test
    fun testMdnsDiscoveryServiceInstantiation() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val service = MdnsDiscoveryService(context)
        assertNotNull(service)

        // Test fallback simulation
        service.simulateDiscoveryIfSupported()
        // Should not throw and maintain valid state flow
        assertNotNull(service.discoveredServices.value)
    }
}
