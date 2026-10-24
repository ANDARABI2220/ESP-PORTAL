package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.EspDeviceEntity
import com.example.data.local.PinStateEntity
import com.example.data.model.BoardProfiles
import com.example.data.model.EspBoardType
import com.example.data.model.PinMode
import com.example.ui.theme.GeminiBlue
import com.example.ui.theme.GeminiBorder
import com.example.ui.theme.GeminiGreen
import com.example.ui.theme.GeminiPurple
import com.example.ui.theme.GeminiSurface
import com.example.ui.theme.GeminiSurfaceVariant
import com.example.ui.theme.GeminiTextPrimary
import com.example.ui.theme.GeminiTextSecondary
import com.example.ui.theme.GeminiYellow
import com.example.ui.viewmodel.AppLanguage

@Composable
fun PinoutMatrixView(
    activeDevice: EspDeviceEntity?,
    pins: List<PinStateEntity>,
    language: AppLanguage,
    onTogglePin: (gpio: Int, currentState: Int) -> Unit,
    onSetPwm: (gpio: Int, pwm: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (activeDevice == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (language == AppLanguage.FA) "هیچ بردی انتخاب نشده است" else "No board selected",
                color = GeminiTextSecondary
            )
        }
        return
    }

    val boardEnum = runCatching { EspBoardType.valueOf(activeDevice.boardType) }
        .getOrDefault(EspBoardType.ESP_12F)
    val pinDefinitions = BoardProfiles.getPinsForBoard(boardEnum)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Board Header
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
                    Text(
                        text = if (language == AppLanguage.FA) "ماتریس پایه‌ها و پیکربندی سخت‌افزار"
                        else "Pinout Matrix & Hardware Configuration",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = GeminiTextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (language == AppLanguage.FA)
                            "تغییر مستقیم سطح منطقی پایه‌ها (HIGH / LOW) و تنظیم PWM خروجی با نظارت بر پایه‌های راه‌اندازی (Boot Strapping)."
                        else
                            "Real-time logic level switching (HIGH/LOW) & PWM tuning with boot-strapping pin safety guard.",
                        fontSize = 12.sp,
                        color = GeminiTextSecondary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(GeminiGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (language == AppLanguage.FA) "پایه‌های امن برای رله و سنسور" else "Safe general GPIOs",
                            fontSize = 11.sp,
                            color = GeminiTextSecondary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(GeminiYellow)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (language == AppLanguage.FA) "پایه‌های دارای قید بوت" else "Boot-strap pins",
                            fontSize = 11.sp,
                            color = GeminiTextSecondary
                        )
                    }
                }
            }
        }

        // Pin List
        items(pinDefinitions, key = { it.gpio }) { def ->
            val pinState = pins.find { it.gpio == def.gpio }
            val isHigh = (pinState?.digitalState ?: 0) == 1
            val currentPwm = pinState?.pwmValue ?: 0

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pin_card_${def.gpio}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = GeminiSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, GeminiBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            def.isAdc -> GeminiPurple
                                            def.isBootPin -> GeminiYellow
                                            else -> GeminiGreen
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = def.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = GeminiTextPrimary
                                )
                                Text(
                                    text = "GPIO ${def.gpio} • ${def.supportedModes.joinToString(", ") { it.name }}",
                                    fontSize = 11.sp,
                                    color = GeminiTextSecondary
                                )
                            }
                        }

                        // Right control: Switch for digital or ADC label
                        if (def.isAdc) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GeminiPurple.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "0.78 V (ADC)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GeminiPurple
                                )
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isHigh) "HIGH (3.3V)" else "LOW (0V)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isHigh) GeminiGreen else GeminiTextSecondary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Switch(
                                    checked = isHigh,
                                    onCheckedChange = { onTogglePin(def.gpio, if (isHigh) 1 else 0) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = GeminiGreen,
                                        uncheckedThumbColor = Color.LightGray,
                                        uncheckedTrackColor = GeminiSurfaceVariant
                                    ),
                                    modifier = Modifier.testTag("switch_gpio_${def.gpio}")
                                )
                            }
                        }
                    }

                    // Boot Pin Warning Note if applicable
                    if (def.isBootPin && def.bootConditionNote.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(GeminiYellow.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Boot warning",
                                tint = GeminiYellow,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = def.bootConditionNote,
                                fontSize = 10.sp,
                                color = GeminiYellow
                            )
                        }
                    }

                    // PWM Slider if PWM is supported and not ADC
                    if (def.supportedModes.contains(PinMode.PWM) && !def.isAdc) {
                        Spacer(modifier = Modifier.height(8.dp))
                        var sliderValue by remember(currentPwm) { mutableFloatStateOf(currentPwm.toFloat()) }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (language == AppLanguage.FA) "تنظیم پالس PWM:" else "PWM Duty Cycle:",
                                fontSize = 11.sp,
                                color = GeminiTextSecondary
                            )
                            Text(
                                text = "${sliderValue.toInt()} / 1023",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GeminiBlue
                            )
                        }

                        Slider(
                            value = sliderValue,
                            onValueChange = { sliderValue = it },
                            onValueChangeFinished = { onSetPwm(def.gpio, sliderValue.toInt()) },
                            valueRange = 0f..1023f,
                            colors = SliderDefaults.colors(
                                thumbColor = GeminiBlue,
                                activeTrackColor = GeminiBlue,
                                inactiveTrackColor = GeminiSurfaceVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp)
                                .testTag("slider_pwm_${def.gpio}")
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
