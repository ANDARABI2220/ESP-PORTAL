package com.example.data.model

enum class PinMode {
    INPUT,
    OUTPUT,
    PWM,
    ADC
}

enum class PinState(val value: Int) {
    LOW(0),
    HIGH(1)
}

data class PinDefinition(
    val gpio: Int,
    val name: String,
    val supportedModes: List<PinMode> = listOf(PinMode.INPUT, PinMode.OUTPUT),
    val isBootPin: Boolean = false,
    val bootConditionNote: String = "",
    val isAdc: Boolean = false,
    val defaultMode: PinMode = PinMode.OUTPUT,
    val maxPwm: Int = 1023
)

enum class EspBoardType(
    val code: String,
    val displayName: String,
    val persianName: String,
    val family: String,
    val defaultFlashMb: Int,
    val cpuMhz: Int,
    val hasWifi: Boolean = true,
    val hasBluetooth: Boolean = false,
    val descriptionFa: String
) {
    ESP_01("ESP-01", "ESP-01 / 01S", "ماژول ESP-01 / 01S", "ESP8266EX", 1, 80, true, false,
        "ماژول جمع‌وجور ۸ پین با ۲ پین GPIO قابل استفاده. مناسب برای رله و پروژه‌های کم‌حجم."),
    ESP_12F("ESP-12F", "ESP-12E / 12F", "ماژول ESP-12E / 12F", "ESP8266EX", 4, 80, true, false,
        "ماژول محبوب و پایدار با حافظه فلش ۴ مگابایت و ۹ پین عمومی GPIO و یک ورودی آنالوگ."),
    ESP_07("ESP-07", "ESP-07", "ماژول ESP-07", "ESP8266EX", 1, 80, true, false,
        "ماژول دارای آنتن سرامیکی داخلی و سوکت آنتن خارجی IPEX برای بردهای طولانی‌برد."),
    ESP_14("ESP-14", "ESP-14", "ماژول ترکیبی ESP-14", "ESP8266 + STM8", 2, 80, true, false,
        "ترکیب ESP8266 و میکروکنترلر جانبی STM8S003 برای مدیریت پین‌ها با مصرف بسیار پایین."),
    ESP_201("ESP-201", "ESP-201", "برد آزمایشگاهی ESP-201", "ESP8266EX", 1, 80, true, false,
        "برد توسعه با پین‌هدرهای دو ردیفه ۲.۵۴ میلی‌متری و دسترسی به تمام پین‌های تراشه."),
    ESP_210("ESP-210", "ESP-210 (Generic)", "پروفایل عمومی ESP-210", "ESP8266EX", 2, 80, true, false,
        "پروفایل عمومی تراشه ESP8266EX سازگار با بردهای توسعه قدیمی‌تر."),
    WEMOS_D1_MINI("WEMOS_D1", "Wemos D1 Mini", "ویموس دی‌وان مینی", "ESP8266EX", 4, 80, true, false,
        "برد توسعه محبوب با تراشه CH340، ورودی MicroUSB و چینش کاربرپسند پین‌های D0 تا D8."),
    NODEMCU("NODEMCU", "NodeMCU v2/v3", "نود‌ام‌سی‌یو NodeMCU", "ESP8266EX", 4, 80, true, false,
        "پلتفرم استاندارد IoT با تنظیم‌کننده ولتاژ ۳.۳ ولت، مبدل USB-UART و پایه‌های کامل."),
    ESP32("ESP32", "ESP32 DevKit v1", "برد ESP32 دو هسته‌ای", "ESP32", 4, 240, true, true,
        "تراشه پرقدرت دو هسته‌ای ۲۴۰ مگاهرتز با بلوتوث، وای‌فای، حسگر اثر هال و ۲۸ پایه GPIO.")
}

object BoardProfiles {
    fun getPinsForBoard(board: EspBoardType): List<PinDefinition> {
        return when (board) {
            EspBoardType.ESP_01 -> listOf(
                PinDefinition(0, "GPIO 0", listOf(PinMode.INPUT, PinMode.OUTPUT, PinMode.PWM), isBootPin = true, bootConditionNote = "برای بوت عادی باید HIGH باشد; برای فلش LOW"),
                PinDefinition(2, "GPIO 2", listOf(PinMode.INPUT, PinMode.OUTPUT, PinMode.PWM), isBootPin = true, bootConditionNote = "در لحظه بوت باید HIGH باشد (پول‌آپ داخلی)"),
                PinDefinition(1, "TXD / GPIO 1", listOf(PinMode.INPUT, PinMode.OUTPUT)),
                PinDefinition(3, "RXD / GPIO 3", listOf(PinMode.INPUT, PinMode.OUTPUT))
            )
            EspBoardType.ESP_12F, EspBoardType.ESP_07, EspBoardType.ESP_201, EspBoardType.ESP_210 -> listOf(
                PinDefinition(0, "GPIO 0", listOf(PinMode.INPUT, PinMode.OUTPUT, PinMode.PWM), isBootPin = true, bootConditionNote = "بوت: HIGH / فلش: LOW"),
                PinDefinition(2, "GPIO 2", listOf(PinMode.INPUT, PinMode.OUTPUT, PinMode.PWM), isBootPin = true, bootConditionNote = "بوت: HIGH"),
                PinDefinition(4, "GPIO 4 (SDA)", listOf(PinMode.INPUT, PinMode.OUTPUT, PinMode.PWM)),
                PinDefinition(5, "GPIO 5 (SCL)", listOf(PinMode.INPUT, PinMode.OUTPUT, PinMode.PWM)),
                PinDefinition(12, "GPIO 12", listOf(PinMode.INPUT, PinMode.OUTPUT, PinMode.PWM)),
                PinDefinition(13, "GPIO 13", listOf(PinMode.INPUT, PinMode.OUTPUT, PinMode.PWM)),
                PinDefinition(14, "GPIO 14", listOf(PinMode.INPUT, PinMode.OUTPUT, PinMode.PWM)),
                PinDefinition(15, "GPIO 15", listOf(PinMode.INPUT, PinMode.OUTPUT, PinMode.PWM), isBootPin = true, bootConditionNote = "بوت: باید پول‌داون به GND باشد"),
                PinDefinition(16, "GPIO 16 (WAKE)", listOf(PinMode.INPUT, PinMode.OUTPUT), bootConditionNote = "اتصال به RST برای بیدار شدن از Deep Sleep"),
                PinDefinition(17, "ADC0 (A0)", listOf(PinMode.ADC), isAdc = true)
            )
            EspBoardType.WEMOS_D1_MINI -> listOf(
                PinDefinition(16, "D0 (GPIO16)", listOf(PinMode.INPUT, PinMode.OUTPUT), bootConditionNote = "سیگنال WAKE خواب عمیق"),
                PinDefinition(5, "D1 (GPIO5 / SCL)", listOf(PinMode.INPUT, PinMode.OUTPUT, PinMode.PWM)),
                PinDefinition(4, "D2 (GPIO4 / SDA)", listOf(PinMode.INPUT, PinMode.OUTPUT, PinMode.PWM)),
                PinDefinition(0, "D3 (GPIO0)", listOf(PinMode.INPUT, PinMode.OUTPUT, PinMode.PWM), isBootPin = true, bootConditionNote = "پول‌آپ ۱۰K"),
                PinDefinition(2, "D4 (GPIO2 / LED)", listOf(PinMode.INPUT, PinMode.OUTPUT, PinMode.PWM), isBootPin = true, bootConditionNote = "LED داخلی برد"),
                PinDefinition(14, "D5 (GPIO14 / SCK)", listOf(PinMode.INPUT, PinMode.OUTPUT, PinMode.PWM)),
                PinDefinition(12, "D6 (GPIO12 / MISO)", listOf(PinMode.INPUT, PinMode.OUTPUT, PinMode.PWM)),
                PinDefinition(13, "D7 (GPIO13 / MOSI)", listOf(PinMode.INPUT, PinMode.OUTPUT, PinMode.PWM)),
                PinDefinition(15, "D8 (GPIO15 / CS)", listOf(PinMode.INPUT, PinMode.OUTPUT, PinMode.PWM), isBootPin = true, bootConditionNote = "پول‌داون ۱۰K"),
                PinDefinition(17, "A0 (ADC0)", listOf(PinMode.ADC), isAdc = true)
            )
            EspBoardType.NODEMCU -> listOf(
                PinDefinition(16, "D0 / GPIO16", listOf(PinMode.INPUT, PinMode.OUTPUT)),
                PinDefinition(5, "D1 / GPIO5", listOf(PinMode.INPUT, PinMode.OUTPUT, PinMode.PWM)),
                PinDefinition(4, "D2 / GPIO4", listOf(PinMode.INPUT, PinMode.OUTPUT, PinMode.PWM)),
                PinDefinition(0, "D3 / GPIO0", listOf(PinMode.INPUT, PinMode.OUTPUT, PinMode.PWM), isBootPin = true),
                PinDefinition(2, "D4 / GPIO2", listOf(PinMode.INPUT, PinMode.OUTPUT, PinMode.PWM), isBootPin = true),
                PinDefinition(14, "D5 / GPIO14", listOf(PinMode.INPUT, PinMode.OUTPUT, PinMode.PWM)),
                PinDefinition(12, "D6 / GPIO12", listOf(PinMode.INPUT, PinMode.OUTPUT, PinMode.PWM)),
                PinDefinition(13, "D7 / GPIO13", listOf(PinMode.INPUT, PinMode.OUTPUT, PinMode.PWM)),
                PinDefinition(15, "D8 / GPIO15", listOf(PinMode.INPUT, PinMode.OUTPUT, PinMode.PWM), isBootPin = true),
                PinDefinition(3, "RX / GPIO3", listOf(PinMode.INPUT, PinMode.OUTPUT)),
                PinDefinition(1, "TX / GPIO1", listOf(PinMode.INPUT, PinMode.OUTPUT)),
                PinDefinition(17, "A0 / ADC", listOf(PinMode.ADC), isAdc = true)
            )
            EspBoardType.ESP32 -> listOf(
                PinDefinition(2, "GPIO 2 (LED)", listOf(PinMode.INPUT, PinMode.OUTPUT, PinMode.PWM), isBootPin = true, bootConditionNote = "در هنگام بوت باید شناور یا LOW باشد"),
                PinDefinition(4, "GPIO 4", listOf(PinMode.INPUT, PinMode.OUTPUT, PinMode.PWM, PinMode.ADC)),
                PinDefinition(5, "GPIO 5 (VSPI CS)", listOf(PinMode.INPUT, PinMode.OUTPUT, PinMode.PWM), isBootPin = true),
                PinDefinition(12, "GPIO 12 (MTDI)", listOf(PinMode.INPUT, PinMode.OUTPUT, PinMode.PWM, PinMode.ADC), isBootPin = true, bootConditionNote = "تنظیم ولتاژ فلش"),
                PinDefinition(13, "GPIO 13 (MTCK)", listOf(PinMode.INPUT, PinMode.OUTPUT, PinMode.PWM, PinMode.ADC)),
                PinDefinition(14, "GPIO 14 (MTMS)", listOf(PinMode.INPUT, PinMode.OUTPUT, PinMode.PWM, PinMode.ADC)),
                PinDefinition(15, "GPIO 15 (MTDO)", listOf(PinMode.INPUT, PinMode.OUTPUT, PinMode.PWM, PinMode.ADC), isBootPin = true),
                PinDefinition(18, "GPIO 18 (SCK)", listOf(PinMode.INPUT, PinMode.OUTPUT, PinMode.PWM)),
                PinDefinition(19, "GPIO 19 (MISO)", listOf(PinMode.INPUT, PinMode.OUTPUT, PinMode.PWM)),
                PinDefinition(21, "GPIO 21 (SDA)", listOf(PinMode.INPUT, PinMode.OUTPUT, PinMode.PWM)),
                PinDefinition(22, "GPIO 22 (SCL)", listOf(PinMode.INPUT, PinMode.OUTPUT, PinMode.PWM)),
                PinDefinition(23, "GPIO 23 (MOSI)", listOf(PinMode.INPUT, PinMode.OUTPUT, PinMode.PWM)),
                PinDefinition(25, "GPIO 25 (DAC1)", listOf(PinMode.INPUT, PinMode.OUTPUT, PinMode.PWM, PinMode.ADC)),
                PinDefinition(26, "GPIO 26 (DAC2)", listOf(PinMode.INPUT, PinMode.OUTPUT, PinMode.PWM, PinMode.ADC)),
                PinDefinition(27, "GPIO 27", listOf(PinMode.INPUT, PinMode.OUTPUT, PinMode.PWM, PinMode.ADC)),
                PinDefinition(32, "GPIO 32", listOf(PinMode.INPUT, PinMode.OUTPUT, PinMode.PWM, PinMode.ADC)),
                PinDefinition(33, "GPIO 33", listOf(PinMode.INPUT, PinMode.OUTPUT, PinMode.PWM, PinMode.ADC)),
                PinDefinition(34, "GPIO 34 (IN ONLY)", listOf(PinMode.INPUT, PinMode.ADC), isAdc = true),
                PinDefinition(35, "GPIO 35 (IN ONLY)", listOf(PinMode.INPUT, PinMode.ADC), isAdc = true),
                PinDefinition(36, "GPIO 36 (VP)", listOf(PinMode.INPUT, PinMode.ADC), isAdc = true),
                PinDefinition(39, "GPIO 39 (VN)", listOf(PinMode.INPUT, PinMode.ADC), isAdc = true)
            )
            EspBoardType.ESP_14 -> listOf(
                PinDefinition(0, "GPIO 0", listOf(PinMode.INPUT, PinMode.OUTPUT)),
                PinDefinition(2, "GPIO 2", listOf(PinMode.INPUT, PinMode.OUTPUT)),
                PinDefinition(4, "STM8 PD4", listOf(PinMode.INPUT, PinMode.OUTPUT, PinMode.PWM)),
                PinDefinition(5, "STM8 PD5", listOf(PinMode.INPUT, PinMode.OUTPUT, PinMode.PWM)),
                PinDefinition(6, "STM8 PD6", listOf(PinMode.INPUT, PinMode.OUTPUT, PinMode.ADC), isAdc = true)
            )
        }
    }
}

/**
 * Unified JSON Frame Protocol (v2)
 */
data class JsonFrame(
    val deviceId: String,
    val messageId: String,
    val sequence: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String, // DISCOVERY, GPIO_SET, TELEMETRY, OTA_QUERY, CONFIG
    val payload: Map<String, Any?> = emptyMap()
)

data class TelemetryData(
    val rssi: Int = -55,
    val heapBytes: Int = 42800,
    val voltage: Float = 3.28f,
    val powerMilliwatts: Float = 262.4f,
    val temperature: Float = 25.4f,
    val humidity: Float = 44.0f,
    val uptimeSeconds: Long = 3600L,
    val connectedSensors: List<String> = listOf("DHT22", "INA219", "Relay 2CH")
)
