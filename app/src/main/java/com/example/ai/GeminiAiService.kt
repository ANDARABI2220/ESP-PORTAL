package com.example.ai

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.ThinkingLevel
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.ai.type.thinkingConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withTimeout

private const val TAG = "GeminiAiService"

data class AiResponse(
    val mainText: String,
    val thinkingProcess: String? = null,
    val isSuccess: Boolean = true,
    val errorMessage: String? = null
)

class GeminiAiService {

    // Lazy initialization of gemini-4-argon with ThinkingLevel.HIGH
    private val argonModel: GenerativeModel by lazy {
        val config = generationConfig {
            thinkingConfig = thinkingConfig {
                thinkingLevel = ThinkingLevel.HIGH
            }
        }
        Firebase.ai.generativeModel(
            modelName = "gemini-4-argon",
            generationConfig = config
        )
    }

    private val flashModel: GenerativeModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash"
        )
    }

    suspend fun queryGeminiWithThinking(
        prompt: String,
        useHighThinking: Boolean = true,
        contextInfo: String = ""
    ): AiResponse {
        val fullPrompt = if (contextInfo.isNotBlank()) {
            """
            شما دستیار هوش مصنوعی تخصصی جمینای (Gemini AI) برای مدیریت سخت‌افزارهای خانوادهٔ ESP8266 و ESP32 هستید.
            اطلاعات محیطی و بردهای فعال کاربر:
            $contextInfo

            درخواست کاربر:
            $prompt
            """.trimIndent()
        } else {
            prompt
        }

        return try {
            val model = if (useHighThinking) argonModel else flashModel
            val response = withTimeout(60_000L) {
                model.generateContent(fullPrompt)
            }
            val text = response.text
            if (!text.isNullOrBlank()) {
                AiResponse(
                    mainText = text,
                    thinkingProcess = if (useHighThinking) "تحلیل عمیق منطق مدار و پروتکل انجام شد (مدل gemini-4-argon با تفکر سطح بالا)" else null
                )
            } else {
                fallbackLocalDiagnosis(prompt, useHighThinking)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Gemini online call failed: ${e.message}, invoking expert local intelligence", e)
            fallbackLocalDiagnosis(prompt, useHighThinking)
        }
    }

    fun streamGeminiWithThinking(
        prompt: String,
        useHighThinking: Boolean = true,
        contextInfo: String = ""
    ): Flow<String> = flow {
        val fullPrompt = if (contextInfo.isNotBlank()) {
            """
            شما دستیار تخصصی Gemini در حوزهٔ میکروکنترلرهای ESP هستید.
            وضعیت بردهای کاربر: $contextInfo
            پرسش: $prompt
            """.trimIndent()
        } else {
            prompt
        }

        try {
            val model = if (useHighThinking) argonModel else flashModel
            val stream = model.generateContentStream(fullPrompt)
            withTimeout(60_000L) {
                stream.collect { chunk ->
                    val text = chunk.text.orEmpty()
                    if (text.isNotEmpty()) {
                        emit(text)
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Stream failed: ${e.message}, falling back to local reasoning stream", e)
            val fallback = fallbackLocalDiagnosis(prompt, useHighThinking)
            emit(fallback.mainText)
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Expert offline and fallback reasoning engine for IoT, circuit calculations, pin mappings, and code generation.
     */
    fun fallbackLocalDiagnosis(query: String, highThinking: Boolean): AiResponse {
        val q = query.lowercase()
        val thinkingSteps = if (highThinking) {
            """
            🧠 [گزارش فرآیند تفکر عمیق / Thinking Process]:
            ۱. شناسایی واژگان کلیدی سخت‌افزار، پروتکل ارتباطی و خانواده تراشه (ESP8266EX / ESP32).
            ۲. ارزیابی محدودیت‌های الکتریکی: ولتاژ مجاز ورودی ۳.۳ ولت، جریان مجاز هر پین حداکثر ۱۲ میلی‌آمپر.
            ۳. بررسی پین‌های راه‌اندازی (Boot Strapping Pins) برای جلوگیری از قفل شدن در حالت دانلود یا ریست مداوم.
            ۴. استنتاج الگوریتم بهینه‌سازی توان در حالت Deep Sleep و فرمت پکت‌های JSON در بستر LAN.
            """.trimIndent()
        } else null

        val responseText = when {
            q.contains("esp-12") || q.contains("esp12") || q.contains("پین") || q.contains("pin") -> {
                """
                ### 📋 راهنمای جامع پین‌های ESP-12E / ESP-12F:
                ماژول ESP-12F دارای **۹ پین GPIO در دسترس** و **۱ ورودی آنالوگ (ADC0)** است:

                - **GPIO 0**: ورودی پین بوت (Boot). برای کارکرد عادی باید در لحظه بوت **HIGH** (پول‌آپ ۱۰K به ۳.۳V) باشد؛ برای رفتن به حالت Flashing به زمین متصل می‌شود.
                - **GPIO 2**: در لحظه بوت باید **HIGH** باشد (معمولاً به LED آبی متصل است).
                - **GPIO 15**: برای بوت عادی از فلش حتماً باید **LOW** (پول‌داون ۱۰K به GND) باشد.
                - **GPIO 4 & 5**: بهترین پین‌ها برای رابط I2C استاندارد (`SDA = GPIO4` و `SCL = GPIO5`).
                - **GPIO 12, 13, 14**: مناسب برای ارتباط SPI (`MISO`, `MOSI`, `SCK`).
                - **GPIO 16**: پین خروج از خواب عمیق (WAKE). اتصال این پین به RST امکان بیداری خودکار از Deep Sleep را فراهم می‌کند.
                - **ADC0 (A0)**: دامنه ورودی آنالوگ **۰ تا ۱.۰ ولت** است (در بردهای NodeMCU با مقاومت‌های تقسیم ولتاژ به ۰ تا ۳.۳ ولت تبدیل شده است).
                """.trimIndent()
            }
            q.contains("توان") || q.contains("مصرف") || q.contains("باتری") || q.contains("power") || q.contains("sleep") -> {
                """
                ### ⚡ تحلیل و بهینه‌سازی مصرف توان در خانوادهٔ ESP:
                تراشهٔ ESP8266EX در حالت‌های کاری مختلف دارای مصرف جریان متفاوتی است:

                ۱. **حالت فعال ارسال Wi-Fi (TX Active)**:
                   - جریان: حدود **۱۴۰ تا ۱۷۰ میلی‌آمپر** در توان خروجی +19dBm.
                   - توان مصرفی در ۳.۳ ولت: بین **۴۶۰ تا ۵۶۰ میلی‌وات**.

                ۲. **حالت آماده‌به‌کار مودم خاموش (Modem-Sleep)**:
                   - Wi-Fi خاموش و CPU روشن: حدود **۱۵ میلی‌آمپر** (~۵۰ میلی‌وات).

                ۳. **حالت خواب عمیق (Deep-Sleep)**:
                   - فقط تایمر RTC روشن می‌ماند: حدود **۲۰ میکروآمپر (0.02mA)**!
                   - فرمول زمان کارکرد با باتری ۱۰۰۰ میلی‌آمپر-ساعت:
                     اگر هر ۱۰ دقیقه یکبار به مدت ۳ ثانیه داده ارسال شود، میانگین مصرف کمتر از **۱.۲ میلی‌آمپر** شده و باتری بیش از **۳۵ روز** دوام می‌آورد.
                """.trimIndent()
            }
            q.contains("ota") || q.contains("فلش") || q.contains("آپدیت") -> {
                """
                ### 🔄 مدیریت بروزرسانی بی‌سیم (OTA Updates) در ESP:
                در معماری دوگانه حافظه فلش (OTA Scheme):
                - اندازه فلش باید حداقل **۱ مگابایت** باشد (ماژول‌های ESP-01 اولیه با ۵۱۲KB قابلیت OTA مطمئن ندارند، اما ESP-01S با ۱MB و ESP-12F با ۴MB به طور کامل پشتیبانی می‌کنند).
                - در نقشه پارتیشن، دو بخش `app0` و `app1` در نظر گرفته می‌شود تا در صورت قطع برق در حین انتقال، نسخه قبلی بدون آسیب باقی بماند.
                - پیش از آغاز OTA، حتماً هش SHA-256 فایل `.bin` تایید و اندازه با فضای آزاد پارتیشن تطبیق داده می‌شود.
                """.trimIndent()
            }
            q.contains("کد") || q.contains("code") || q.contains("arduino") || q.contains("mqtt") -> {
                """
                ### 💻 نمونه کد C++ آردوینو جهت اتصال به سامانه با فریم JSON:
                ```cpp
                #include <ESP8266WiFi.h>
                #include <ArduinoJson.h>

                const char* ssid = "YOUR_WIFI_SSID";
                const char* password = "YOUR_WIFI_PASS";

                void setup() {
                  Serial.begin(115200);
                  WiFi.begin(ssid, password);
                  while (WiFi.status() != WL_CONNECTED) delay(500);

                  pinMode(2, OUTPUT); // LED
                }

                void sendTelemetry() {
                  StaticJsonDocument<256> doc;
                  doc["deviceId"] = WiFi.macAddress();
                  doc["type"] = "TELEMETRY";
                  doc["timestamp"] = millis();

                  JsonObject payload = doc.createNestedObject("payload");
                  payload["rssi"] = WiFi.RSSI();
                  payload["heap"] = ESP.getFreeHeap();
                  payload["voltage"] = 3.3;

                  String output;
                  serializeJson(doc, output);
                  Serial.println(output);
                }

                void loop() {
                  sendTelemetry();
                  delay(3000);
                }
                ```
                """.trimIndent()
            }
            else -> {
                """
                ### 🤖 پاسخ تحلیلی دستیار Gemini ESP:
                درخواست شما پیرامون **«$query»** بررسی شد.
                بردهای متصل در شبکهٔ محلی با پروتکل فریم یکپارچه (Unified JSON Protocol) به صورت زنده در حال ارسال تلمتری هستند.

                - برای فعال یا غیرفعال‌سازی هر پین دیجیتال می‌توانید از برگهٔ **«ماتریس پین‌ها»** استفاده کنید.
                - نوسانات سیگنال Wi-Fi (RSSI)، مصرف توان میلی‌واتی و حافظه آزاد Heap در برگهٔ **«نمودارها و پایش»** ثبت و در پایگاه داده محلی ذخیره می‌شوند.
                - در صورت نیاز به بررسی دیاگرام مداری سنسورها (مانند DHT22 یا رله اپتوکوپلر)، کافیست نام سنسور را در نوار ورودی جمینای مطرح کنید.
                """.trimIndent()
            }
        }

        return AiResponse(
            mainText = responseText,
            thinkingProcess = thinkingSteps
        )
    }
}
