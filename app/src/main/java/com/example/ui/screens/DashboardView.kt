package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.EspDeviceEntity
import com.example.data.local.PinStateEntity
import com.example.data.model.BoardProfiles
import com.example.data.model.EspBoardType
import com.example.data.model.TelemetryData
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
import com.example.ui.theme.GeminiYellow
import com.example.ui.viewmodel.AppLanguage

@Composable
fun DashboardView(
    activeDevice: EspDeviceEntity?,
    telemetry: TelemetryData?,
    pins: List<PinStateEntity>,
    language: AppLanguage,
    onTogglePin: (gpio: Int, currentState: Int) -> Unit,
    onQuickPromptClick: (String) -> Unit,
    onNavigateToPinout: () -> Unit,
    onNavigateToTelemetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val promptSuggestions = if (language == AppLanguage.FA) listOf(
        "تحلیل پین‌های ESP-12F و پین‌های بوت",
        "محاسبه زمان کارکرد باتری در خواب عمیق",
        "کد وب‌سرور آردوینو برای کنترل پین‌ها",
        "پیکربندی حسگر دما DHT22 در GPIO4",
        "بررسی خطاهای فلش و پارتیشن OTA"
    ) else listOf(
        "Analyze ESP-12F boot pins & strapping",
        "Calculate deep sleep battery runtime",
        "Arduino WebServer C++ code for GPIOs",
        "Configure DHT22 sensor on GPIO4",
        "Verify OTA flash partitioning & errors"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Gemini AI Greeting Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .testTag("gemini_greeting_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = GeminiSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, GeminiBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(GeminiSparkleBrush),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Gemini AI",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (language == AppLanguage.FA) "سلام! دستیار هوشمند جمینای آماده است"
                            else "Hello! Gemini ESP Assistant Ready",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = GeminiTextPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (language == AppLanguage.FA)
                            "مدیریت بلادرنگ بردهای ESP در شبکه محلی، پایش توان، کنترل خروجی‌ها و رفع خطاهای برنامه‌نویسی با استدلال عمیق هوش مصنوعی."
                        else
                            "Real-time LAN telemetry, GPIO pin mapping, power profiling, and deep hardware troubleshooting with Gemini AI.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = GeminiTextSecondary,
                            lineHeight = 20.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Suggestion Pills
                    Text(
                        text = if (language == AppLanguage.FA) "پرسش‌های پیشنهادی:" else "Quick Suggestions:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GeminiBlue
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(promptSuggestions) { suggestion ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(GeminiSurfaceVariant)
                                    .border(1.dp, GeminiBorder, RoundedCornerShape(14.dp))
                                    .clickable { onQuickPromptClick(suggestion) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .testTag("suggestion_chip")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = GeminiPurple
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = suggestion,
                                        fontSize = 12.sp,
                                        color = GeminiTextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Active Board Hardware Specs Card
        if (activeDevice != null) {
            item {
                val boardEnum = runCatching { EspBoardType.valueOf(activeDevice.boardType) }
                    .getOrDefault(EspBoardType.ESP_12F)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = GeminiSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GeminiBorder)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = activeDevice.name,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = GeminiTextPrimary
                                    )
                                )
                                Text(
                                    text = if (language == AppLanguage.FA) boardEnum.persianName else boardEnum.displayName,
                                    fontSize = 12.sp,
                                    color = GeminiBlue,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GeminiGreen.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(GeminiGreen)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (activeDevice.isConnected) "ONLINE LAN" else "OFFLINE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GeminiGreen
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Specs Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SpecBadge(icon = Icons.Default.Router, label = "IP", value = activeDevice.ipAddress)
                            SpecBadge(icon = Icons.Default.Memory, label = "Flash", value = "${activeDevice.flashMb} MB")
                            SpecBadge(icon = Icons.Default.Speed, label = "CPU", value = "${boardEnum.cpuMhz} MHz")
                            SpecBadge(icon = Icons.Default.Wifi, label = "RSSI", value = "${telemetry?.rssi ?: -55} dBm")
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = boardEnum.descriptionFa,
                            fontSize = 11.sp,
                            color = GeminiTextSecondary,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        // 3. Live Telemetry Metric Gauges
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToTelemetry() },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = GeminiSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, GeminiBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (language == AppLanguage.FA) "پایش زندهٔ توان و سنسورها" else "Live Telemetry & Power",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = GeminiTextPrimary
                            )
                        )
                        Text(
                            text = if (language == AppLanguage.FA) "مشاهده نمودار ➔" else "View Charts ➔",
                            fontSize = 11.sp,
                            color = GeminiBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricItem(
                            icon = Icons.Default.Bolt,
                            title = if (language == AppLanguage.FA) "ولتاژ" else "Voltage",
                            value = "${"%.2f".format(telemetry?.voltage ?: 3.28f)} V",
                            accentColor = GeminiBlue
                        )
                        MetricItem(
                            icon = Icons.Default.Power,
                            title = if (language == AppLanguage.FA) "مصرف توان" else "Power",
                            value = "${(telemetry?.powerMilliwatts ?: 260f).toInt()} mW",
                            accentColor = GeminiCoral
                        )
                        MetricItem(
                            icon = Icons.Default.DeviceThermostat,
                            title = if (language == AppLanguage.FA) "دما" else "Temp",
                            value = "${"%.1f".format(telemetry?.temperature ?: 24.5f)} °C",
                            accentColor = GeminiYellow
                        )
                        MetricItem(
                            icon = Icons.Default.WaterDrop,
                            title = if (language == AppLanguage.FA) "رطوبت" else "Humidity",
                            value = "${(telemetry?.humidity ?: 45f).toInt()} %",
                            accentColor = GeminiGreen
                        )
                    }
                }
            }
        }

        // 4. Quick GPIO Control Switches
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = GeminiSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, GeminiBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (language == AppLanguage.FA) "کنترل سریع پین‌های خروجی" else "Quick Output Controls",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = GeminiTextPrimary
                            )
                        )
                        Text(
                            text = if (language == AppLanguage.FA) "تمام پین‌ها ➔" else "All Pins ➔",
                            fontSize = 11.sp,
                            color = GeminiBlue,
                            modifier = Modifier.clickable { onNavigateToPinout() }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (pins.isEmpty()) {
                        Text(
                            text = if (language == AppLanguage.FA) "در حال بارگذاری پین‌ها..." else "Loading pin states...",
                            fontSize = 12.sp,
                            color = GeminiTextSecondary
                        )
                    } else {
                        // Display first 3 controllable pins
                        pins.take(4).forEach { pin ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(if (pin.digitalState == 1) GeminiGreen else Color.Gray)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "GPIO ${pin.gpio}",
                                        fontWeight = FontWeight.Medium,
                                        color = GeminiTextPrimary,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = pin.mode,
                                        fontSize = 10.sp,
                                        color = GeminiTextSecondary
                                    )
                                }

                                Switch(
                                    checked = pin.digitalState == 1,
                                    onCheckedChange = { onTogglePin(pin.gpio, pin.digitalState) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = GeminiGreen,
                                        uncheckedThumbColor = Color.LightGray,
                                        uncheckedTrackColor = GeminiSurfaceVariant
                                    ),
                                    modifier = Modifier.testTag("quick_switch_gpio_${pin.gpio}")
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun SpecBadge(icon: ImageVector, label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(GeminiSurfaceVariant)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Icon(icon, contentDescription = null, tint = GeminiBlue, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, fontSize = 9.sp, color = GeminiTextSecondary)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GeminiTextPrimary)
    }
}

@Composable
private fun MetricItem(icon: ImageVector, title: String, value: String, accentColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(GeminiSurfaceVariant)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = title, fontSize = 10.sp, color = GeminiTextSecondary)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GeminiTextPrimary)
    }
}
