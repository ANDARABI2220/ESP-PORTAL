package com.example.network.client

import android.util.Log
import okhttp3.CertificatePinner
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.io.IOException
import java.util.concurrent.TimeUnit

private const val TAG = "EspTlsHttpClient"

data class EspTelemetryResponse(
    val deviceId: String,
    val voltage: Float,
    val powerMilliwatts: Float,
    val rssi: Int,
    val heapBytes: Int,
    val temperature: Float,
    val humidity: Float,
    val uptimeSeconds: Long,
    val timestamp: Long = System.currentTimeMillis()
)

data class EspGpioRequest(
    val gpio: Int,
    val mode: String, // OUTPUT, PWM, INPUT
    val state: Int,   // 0 or 1
    val pwmValue: Int = 0
)

data class EspGpioResponse(
    val status: String,
    val gpio: Int,
    val state: Int,
    val message: String? = null
)

data class EspConfigResponse(
    val deviceId: String,
    val model: String,
    val firmwareVersion: String,
    val flashSizeMb: Int,
    val sensorList: List<String> = emptyList()
)

interface EspApiService {
    @GET("/api/v2/telemetry")
    suspend fun getTelemetry(): EspTelemetryResponse

    @POST("/api/v2/gpio")
    suspend fun setGpio(@Body request: EspGpioRequest): EspGpioResponse

    @GET("/api/v2/config")
    suspend fun getConfig(): EspConfigResponse
}

class RetryWithBackoffInterceptor(
    private val maxRetries: Int = 3,
    private val initialDelayMs: Long = 500L
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        var response: Response? = null
        var exception: IOException? = null
        var tryCount = 0

        while (tryCount < maxRetries) {
            try {
                response = chain.proceed(request)
                if (response.isSuccessful) {
                    return response
                }
                // If 5xx error, retry
                if (response.code in 500..599) {
                    response.close()
                    throw IOException("Server error ${response.code}")
                }
                return response
            } catch (e: IOException) {
                exception = e
                tryCount++
                if (tryCount >= maxRetries) break
                val backoffMs = initialDelayMs * (1 shl (tryCount - 1))
                try {
                    Thread.sleep(backoffMs)
                } catch (ignored: InterruptedException) {
                    Thread.currentThread().interrupt()
                    break
                }
            }
        }
        throw exception ?: IOException("Request failed after $maxRetries retries")
    }
}

object EspTlsHttpClient {

    // Known SHA-256 certificate hashes for ESP device TLS endpoints
    const val ESP_DEV_PIN_PRIMARY = "sha256/WoiWRyIOVNa9ihaBciRSC7XHjliYS9VwUGOIud4PB18="
    const val ESP_DEV_PIN_BACKUP = "sha256/r/mIkG3eEpVdm+u/ko/cwxzOMo1bk4TyHIlByibiA5E="

    fun createCertificatePinner(customPins: List<String> = emptyList()): CertificatePinner {
        val builder = CertificatePinner.Builder()
        val pins = if (customPins.isNotEmpty()) customPins else listOf(ESP_DEV_PIN_PRIMARY, ESP_DEV_PIN_BACKUP)
        pins.forEach { pin ->
            builder.add("*.local", pin)
            builder.add("*.lan", pin)
            builder.add("esp32.local", pin)
            builder.add("esp8266.local", pin)
        }
        return builder.build()
    }

    fun buildOkHttpClient(
        certificatePinner: CertificatePinner = createCertificatePinner(),
        connectTimeoutSec: Long = 5L,
        readTimeoutSec: Long = 10L,
        writeTimeoutSec: Long = 10L
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectionPool(ConnectionPool(10, 5, TimeUnit.MINUTES))
            .connectTimeout(connectTimeoutSec, TimeUnit.SECONDS)
            .readTimeout(readTimeoutSec, TimeUnit.SECONDS)
            .writeTimeout(writeTimeoutSec, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(RetryWithBackoffInterceptor(maxRetries = 3, initialDelayMs = 400L))
            .certificatePinner(certificatePinner)
            .build()
    }

    fun createRetrofitClient(baseUrl: String, okHttpClient: OkHttpClient = buildOkHttpClient()): EspApiService {
        val formattedUrl = if (!baseUrl.endsWith("/")) "$baseUrl/" else baseUrl
        return Retrofit.Builder()
            .baseUrl(formattedUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(EspApiService::class.java)
    }
}
