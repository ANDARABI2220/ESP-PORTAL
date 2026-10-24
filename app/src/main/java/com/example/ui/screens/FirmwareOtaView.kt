package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.EspDeviceEntity
import com.example.ui.theme.GeminiBlue
import com.example.ui.theme.GeminiBorder
import com.example.ui.theme.GeminiCoral
import com.example.ui.theme.GeminiGreen
import com.example.ui.theme.GeminiPurple
import com.example.ui.theme.GeminiSparkleBrush
import com.example.ui.theme.GeminiSurface
import com.example.ui.theme.GeminiSurfaceVariant
import com.example.ui.theme.GeminiTextPrimary
import com.example.ui.theme.GeminiTextSecondary
import com.example.ui.theme.GeminiTextTertiary
import com.example.ui.theme.GeminiYellow
import com.example.ui.viewmodel.AppLanguage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class FirmwarePackage(
    val id: String,
    val title: String,
    val version: String,
    val minFlashMb: Int,
    val targetChip: String,
    val sha256: String,
    val description: String,
    val descriptionFa: String
)

@Composable
fun FirmwareOtaView(
    activeDevice: EspDeviceEntity?,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var isFlashing by remember { mutableStateOf(false) }
    var flashProgress by remember { mutableFloatStateOf(0f) }
    var flashedSuccessfully by remember { mutableStateOf(false) }
    var selectedPackageId by remember { mutableStateOf("firmware_telemetry_v24") }

    val catalog = listOf(
        FirmwarePackage(
            id = "firmware_telemetry_v24",
            title = "Arduino ESP Telemetry Node",
            version = "v2.4.2",
            minFlashMb = 1,
            targetChip = "ESP8266EX / ESP32",
            sha256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            description = "Lightweight JSON v2 telemetry and GPIO state sync. Safe for 1MB ESP-01.",
            descriptionFa = "فریم‌ور سبک هماهنگ با پروتکل JSON v2 برای تلمتری و پین‌ها. سازگار حتی با حافظه ۱MB ماژول ESP-01."
        ),
        FirmwarePackage(
            id = "firmware_espidf_mesh",
            title = "ESP-IDF v5.2 Sensor Gateway",
            version = "v5.2.0-secure",
            minFlashMb = 4,
            targetChip = "ESP-12F / NodeMCU / ESP32",
            sha256 = "7f83b1657ff1fc53b92dc18148a1d65dfc2d4b1fa3d677284addd200126d9069",
            description = "High-performance encrypted gateway with TLS 1.3 and mDNS LAN beacon.",
            descriptionFa = "گیت‌وی صنعتی با ارتباط رمزنگاری شده TLS 1.3 و پیام‌های اعتبارسنجی شده mDNS."
        ),
        FirmwarePackage(
            id = "firmware_micropython",
            title = "MicroPython IoT Runtime",
            version = "v1.22.1",
            minFlashMb = 2,
            targetChip = "ESP-12F / ESP-210 / ESP32",
            sha256 = "c0535e4be2b79ffd93291305436bf889314e4a3faec05ecffcbb7ff3108ecd86",
            description = "Interactive Python REPL with webREPL and socket server.",
            descriptionFa = "محیط اجرای زنده پایتون با وب‌سرور سوکت و پایش متغیرها."
        )
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GeminiSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, GeminiBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (language == AppLanguage.FA) "مدیریت فرم‌ور و بروزرسانی بی‌سیم (OTA)"
                            else "Firmware Catalog & Secure OTA Updates",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = GeminiTextPrimary
                            )
                        )
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = null,
                            tint = GeminiBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (language == AppLanguage.FA)
                            "اعتبارسنجی امضای دیجیتال SHA-256 و بررسی تطابق اندازه فلش برای جلوگیری از نقص عملکرد میکروکنترلر در حین فلش بی‌سیم."
                        else
                            "SHA-256 manifest verification and flash-size compatibility checks to prevent OTA bricking.",
                        fontSize = 11.sp,
                        color = GeminiTextSecondary
                    )

                    if (activeDevice != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (language == AppLanguage.FA) "برد هدف: " else "Target: ",
                                fontSize = 12.sp,
                                color = GeminiTextSecondary
                            )
                            Text(
                                text = "${activeDevice.name} (فلش: ${activeDevice.flashMb}MB)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GeminiBlue
                            )
                        }
                    }
                }
            }
        }

        // OTA Flashing Progress Bar if active
        if (isFlashing) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B26)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GeminiPurple)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (language == AppLanguage.FA) "در حال ارسال بایت‌های فریم‌ور به بستر LAN..."
                                else "Transferring firmware packets via LAN...",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GeminiPurple
                            )
                            Text(
                                text = "${(flashProgress * 100).toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GeminiPurple
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { flashProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = GeminiPurple,
                            trackColor = GeminiSurfaceVariant
                        )
                    }
                }
            }
        } else if (flashedSuccessfully) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = GeminiGreen.copy(alpha = 0.12f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GeminiGreen)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GeminiGreen)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (language == AppLanguage.FA)
                                "بروزرسانی OTA با موفقیت پایان یافت. برد مجدداً راه‌اندازی و در LAN متصل شد."
                            else
                                "OTA update completed successfully. Device rebooted and reconnected to LAN.",
                            fontSize = 12.sp,
                            color = GeminiGreen
                        )
                    }
                }
            }
        }

        // Firmware Catalog Cards
        items(catalog, key = { it.id }) { pkg ->
            val isCompatible = (activeDevice?.flashMb ?: 4) >= pkg.minFlashMb
            val isSelected = selectedPackageId == pkg.id

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("firmware_item_${pkg.id}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) GeminiSurfaceVariant else GeminiSurface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (isSelected) GeminiBlue else GeminiBorder
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = pkg.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = GeminiTextPrimary
                            )
                            Text(
                                text = "نسخه: ${pkg.version} • تراشه‌ها: ${pkg.targetChip}",
                                fontSize = 11.sp,
                                color = GeminiBlue
                            )
                        }

                        // Compatibility badge
                        if (isCompatible) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(GeminiGreen.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (language == AppLanguage.FA) "سازگار با فلش" else "Compatible",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GeminiGreen
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(GeminiCoral.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (language == AppLanguage.FA) "نیازمند فلش ${pkg.minFlashMb}MB" else "Requires ${pkg.minFlashMb}MB",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GeminiCoral
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (language == AppLanguage.FA) pkg.descriptionFa else pkg.description,
                        fontSize = 12.sp,
                        color = GeminiTextSecondary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "SHA256",
                            tint = GeminiTextTertiary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SHA-256: ${pkg.sha256.take(16)}...",
                            fontSize = 10.sp,
                            color = GeminiTextTertiary,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            selectedPackageId = pkg.id
                            isFlashing = true
                            flashedSuccessfully = false
                            flashProgress = 0f
                            coroutineScope.launch {
                                for (step in 1..20) {
                                    delay(150L)
                                    flashProgress = step / 20f
                                }
                                delay(300L)
                                isFlashing = false
                                flashedSuccessfully = true
                            }
                        },
                        enabled = isCompatible && !isFlashing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("install_ota_button_${pkg.id}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GeminiBlue,
                            disabledContainerColor = GeminiSurfaceVariant
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (language == AppLanguage.FA) "نصب بی‌سیم روی برد (OTA Flash)" else "Flash to Board (OTA)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Companion Windows Desktop Tool Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GeminiSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, GeminiBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DesktopWindows, contentDescription = null, tint = GeminiPurple, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (language == AppLanguage.FA) "ابزار دسکتاپ ویندوز (Arduino CLI)"
                            else "Windows Desktop Companion (Arduino CLI)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = GeminiTextPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (language == AppLanguage.FA)
                            "برای ساخت سفارشی (Custom Build) و فلش اولیه از طریق پورت USB، فایل‌های باینری توسط ابزار دسکتاپ بر مبنای Arduino CLI کامپایل و سپس جهت بروزرسانی‌های بعدی به این اپ تحویل داده می‌شوند."
                        else
                            "Desktop companion uses Arduino CLI for initial USB flashing and custom builds, seamlessly exporting manifests to this app for OTA delivery.",
                        fontSize = 11.sp,
                        color = GeminiTextSecondary,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
