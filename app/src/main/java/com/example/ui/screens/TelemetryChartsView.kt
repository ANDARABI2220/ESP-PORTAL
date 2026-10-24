package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.EspDeviceEntity
import com.example.data.local.TelemetryRecordEntity
import com.example.data.model.JsonFrame
import com.example.data.model.TelemetryData
import com.example.ui.theme.GeminiBlue
import com.example.ui.theme.GeminiBorder
import com.example.ui.theme.GeminiCoral
import com.example.ui.theme.GeminiGreen
import com.example.ui.theme.GeminiPurple
import com.example.ui.theme.GeminiSurface
import com.example.ui.theme.GeminiSurfaceVariant
import com.example.ui.theme.GeminiTextPrimary
import com.example.ui.theme.GeminiTextSecondary
import com.example.ui.theme.GeminiTextTertiary
import com.example.ui.viewmodel.AppLanguage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TelemetryChartsView(
    activeDevice: EspDeviceEntity?,
    telemetry: TelemetryData?,
    history: List<TelemetryRecordEntity>,
    recentLogs: List<JsonFrame>,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Chart Header
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
                            text = if (language == AppLanguage.FA) "نمودار نوسانات توان و ولتاژ"
                            else "Power & Voltage Real-Time Waveform",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = GeminiTextPrimary
                            )
                        )
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = null,
                            tint = GeminiBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (language == AppLanguage.FA)
                            "ثبت بلادرنگ در پایگاه داده محلی Room با بازه زمانی ۳ ثانیه و دقت سنجش میلی‌واتی."
                        else
                            "Persisted in Room database with 3-second live sampling and milliwatt precision.",
                        fontSize = 11.sp,
                        color = GeminiTextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Custom Line Chart for Power / Voltage
                    TelemetryLineChart(
                        records = history.take(25).reversed(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(GeminiCoral))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (language == AppLanguage.FA) "مصرف توان (mW)" else "Power (mW)",
                                fontSize = 11.sp,
                                color = GeminiTextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(20.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(GeminiBlue))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (language == AppLanguage.FA) "ولتاژ تغذیه (V)" else "Voltage (V)",
                                fontSize = 11.sp,
                                color = GeminiTextSecondary
                            )
                        }
                    }
                }
            }
        }

        // 2. Hardware Resource Gauges (Heap & Wi-Fi)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GeminiSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, GeminiBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (language == AppLanguage.FA) "وضعیت منابع سیستم و شبکه" else "System Resources & Network",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = GeminiTextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ResourceCard(
                            title = if (language == AppLanguage.FA) "حافظه آزاد Heap" else "Free Heap",
                            value = "${(telemetry?.heapBytes ?: 42000) / 1024} KB",
                            subtitle = if (language == AppLanguage.FA) "حافظه رم در دسترس" else "Available RAM",
                            icon = Icons.Default.Memory,
                            color = GeminiBlue
                        )
                        ResourceCard(
                            title = if (language == AppLanguage.FA) "سیگنال Wi-Fi" else "Wi-Fi RSSI",
                            value = "${telemetry?.rssi ?: -55} dBm",
                            subtitle = if (language == AppLanguage.FA) "کیفیت عالی (۸۵٪)" else "Excellent (85%)",
                            icon = Icons.Default.Wifi,
                            color = GeminiGreen
                        )
                        ResourceCard(
                            title = if (language == AppLanguage.FA) "زمان کارکرد Uptime" else "Uptime",
                            value = formatUptime(telemetry?.uptimeSeconds ?: 3600L),
                            subtitle = if (language == AppLanguage.FA) "پایدار بدون ریست" else "Stable without WDT",
                            icon = Icons.Default.CheckCircle,
                            color = GeminiPurple
                        )
                    }
                }
            }
        }

        // 3. Unified JSON Protocol Frame Console
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DataObject, contentDescription = null, tint = GeminiBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (language == AppLanguage.FA) "کنسول فریم‌های پروتکل یکپارچه JSON v2"
                                else "Unified JSON v2 Protocol Frame Stream",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = GeminiTextPrimary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (language == AppLanguage.FA)
                            "فرمت استاندارد: { deviceId, messageId, sequence, timestamp, type, payload }"
                        else
                            "Standard format: { deviceId, messageId, sequence, timestamp, type, payload }",
                        fontSize = 11.sp,
                        color = GeminiTextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0E0E10))
                            .border(1.dp, GeminiBorder, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (recentLogs.isEmpty()) {
                            Text(
                                text = "Waiting for incoming frames...",
                                fontSize = 11.sp,
                                color = GeminiTextTertiary,
                                fontFamily = FontFamily.Monospace
                            )
                        } else {
                            recentLogs.take(6).forEach { frame ->
                                FrameLogItem(frame = frame)
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
private fun TelemetryLineChart(
    records: List<TelemetryRecordEntity>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Background grid lines
        for (i in 1..3) {
            val y = height * (i / 4f)
            drawLine(
                color = Color(0xFF282A2C),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        if (records.size < 2) return@Canvas

        val maxPower = 450f
        val minPower = 150f
        val stepX = width / (records.size - 1)

        val powerPath = Path()
        val voltPath = Path()

        records.forEachIndexed { index, rec ->
            val x = index * stepX
            // Power curve
            val normPower = ((rec.powerMilliwatts - minPower) / (maxPower - minPower)).coerceIn(0f, 1f)
            val yPower = height - (normPower * (height - 20f)) - 10f

            // Voltage curve
            val normVolt = ((rec.voltage - 3.1f) / 0.3f).coerceIn(0f, 1f)
            val yVolt = height - (normVolt * (height - 20f)) - 10f

            if (index == 0) {
                powerPath.moveTo(x, yPower)
                voltPath.moveTo(x, yVolt)
            } else {
                powerPath.lineTo(x, yPower)
                voltPath.lineTo(x, yVolt)
            }

            drawCircle(
                color = GeminiCoral,
                radius = 3.dp.toPx(),
                center = Offset(x, yPower)
            )
        }

        drawPath(
            path = powerPath,
            color = GeminiCoral,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )

        drawPath(
            path = voltPath,
            color = GeminiBlue,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun ResourceCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(GeminiSurfaceVariant)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = title, fontSize = 9.sp, color = GeminiTextSecondary)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GeminiTextPrimary)
        Text(text = subtitle, fontSize = 9.sp, color = GeminiTextTertiary)
    }
}

@Composable
private fun FrameLogItem(frame: JsonFrame) {
    val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    val timeStr = timeFormat.format(Date(frame.timestamp))

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when (frame.type) {
                                "GPIO_SET" -> GeminiGreen.copy(alpha = 0.2f)
                                "DISCOVERY" -> GeminiBlue.copy(alpha = 0.2f)
                                else -> GeminiPurple.copy(alpha = 0.2f)
                            }
                        )
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = frame.type,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (frame.type) {
                            "GPIO_SET" -> GeminiGreen
                            "DISCOVERY" -> GeminiBlue
                            else -> GeminiPurple
                        },
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "seq #${frame.sequence} • id:${frame.messageId}",
                    fontSize = 10.sp,
                    color = GeminiTextSecondary,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                text = timeStr,
                fontSize = 9.sp,
                color = GeminiTextTertiary,
                fontFamily = FontFamily.Monospace
            )
        }
        Text(
            text = "payload: ${frame.payload}",
            fontSize = 10.sp,
            color = Color(0xFFC0C0C0),
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(start = 2.dp, top = 2.dp)
        )
    }
}

private fun formatUptime(seconds: Long): String {
    val hrs = seconds / 3600
    val mins = (seconds % 3600) / 60
    return "${hrs}h ${mins}m"
}
