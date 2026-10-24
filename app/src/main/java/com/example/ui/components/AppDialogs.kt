package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.EspBoardType
import com.example.ui.theme.GeminiBlue
import com.example.ui.theme.GeminiBorder
import com.example.ui.theme.GeminiGreen
import com.example.ui.theme.GeminiPurple
import com.example.ui.theme.GeminiSurface
import com.example.ui.theme.GeminiSurfaceVariant
import com.example.ui.theme.GeminiTextPrimary
import com.example.ui.theme.GeminiTextSecondary
import com.example.ui.theme.GeminiTextTertiary
import com.example.ui.viewmodel.AppLanguage

@Composable
fun AddDeviceDialog(
    language: AppLanguage,
    onDismiss: () -> Unit,
    onAddDevice: (name: String, boardType: EspBoardType, ip: String) -> Unit
) {
    var deviceName by remember { mutableStateOf("") }
    var selectedBoardType by remember { mutableStateOf(EspBoardType.ESP_12F) }
    var ipAddress by remember { mutableStateOf("192.168.1.145") }
    var isScanning by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("add_device_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = GeminiSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, GeminiBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == AppLanguage.FA) "افزودن برد ESP جدید به LAN" else "Add New ESP Board",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = GeminiTextPrimary
                        )
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = GeminiTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Board Type Selection Chips
                Text(
                    text = if (language == AppLanguage.FA) "انتخاب مدل برد:" else "Select Board Profile:",
                    fontSize = 11.sp,
                    color = GeminiTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(EspBoardType.values()) { board ->
                        val isSelected = board == selectedBoardType
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) GeminiBlue else GeminiSurfaceVariant)
                                .clickable { selectedBoardType = board }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = board.displayName,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else GeminiTextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = deviceName,
                    onValueChange = { deviceName = it },
                    label = { Text(if (language == AppLanguage.FA) "نام برد (اختیاری)" else "Device Name") },
                    placeholder = { Text(selectedBoardType.displayName) },
                    modifier = Modifier.fillMaxWidth().testTag("device_name_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GeminiBlue,
                        unfocusedBorderColor = GeminiBorder,
                        focusedTextColor = GeminiTextPrimary,
                        unfocusedTextColor = GeminiTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = ipAddress,
                    onValueChange = { ipAddress = it },
                    label = { Text(if (language == AppLanguage.FA) "آدرس IP در شبکه محلی" else "Local IP Address") },
                    modifier = Modifier.fillMaxWidth().testTag("device_ip_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GeminiBlue,
                        unfocusedBorderColor = GeminiBorder,
                        focusedTextColor = GeminiTextPrimary,
                        unfocusedTextColor = GeminiTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // mDNS Scan Simulation Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(GeminiSurfaceVariant)
                        .clickable {
                            isScanning = true
                            ipAddress = "192.168.1." + (150..220).random()
                            isScanning = false
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Sensors, contentDescription = null, tint = GeminiPurple, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (language == AppLanguage.FA) "کشف خودکار mDNS (_esp._tcp.local)" else "Scan with mDNS",
                            fontSize = 11.sp,
                            color = GeminiPurple,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GeminiTextSecondary)
                    ) {
                        Text(if (language == AppLanguage.FA) "انصراف" else "Cancel")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            onAddDevice(
                                deviceName.ifBlank { selectedBoardType.displayName },
                                selectedBoardType,
                                ipAddress
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GeminiBlue),
                        modifier = Modifier.testTag("confirm_add_device_button")
                    ) {
                        Text(if (language == AppLanguage.FA) "افزودن برد" else "Add Board", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsDialog(
    language: AppLanguage,
    useHighThinking: Boolean,
    onLanguageChange: (AppLanguage) -> Unit,
    onToggleThinking: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = GeminiSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, GeminiBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == AppLanguage.FA) "تنظیمات سامانه" else "Settings",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = GeminiTextPrimary
                        )
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = GeminiTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Language Selector
                Text(
                    text = if (language == AppLanguage.FA) "زبان برنامه (Language):" else "Application Language:",
                    fontSize = 12.sp,
                    color = GeminiTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (language == AppLanguage.FA) GeminiBlue else GeminiSurfaceVariant)
                            .clickable { onLanguageChange(AppLanguage.FA) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "فارسی (Persian)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (language == AppLanguage.FA) Color.White else GeminiTextPrimary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (language == AppLanguage.EN) GeminiBlue else GeminiSurfaceVariant)
                            .clickable { onLanguageChange(AppLanguage.EN) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "English",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (language == AppLanguage.EN) Color.White else GeminiTextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Thinking Mode Toggle Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(GeminiSurfaceVariant)
                        .clickable { onToggleThinking() }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (language == AppLanguage.FA) "حالت تفکر عمیق جمینای" else "Gemini High Thinking",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = GeminiTextPrimary
                            )
                            Text(
                                text = "مدل gemini-4-argon با استدلال تحلیلی",
                                fontSize = 10.sp,
                                color = GeminiPurple
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (useHighThinking) GeminiPurple else Color.Gray)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (useHighThinking) "فعال" else "عادی",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // System info
                Text(
                    text = if (language == AppLanguage.FA)
                        "نسخه اپلیکیشن: ۲.۱.۰ • پروتکل: JSON v2 • تا ۵ اتصال همزمان در شبکه محلی (LAN)"
                    else
                        "App Version: 2.1.0 • Protocol: JSON v2 • Up to 5 concurrent LAN connections",
                    fontSize = 10.sp,
                    color = GeminiTextTertiary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = GeminiBlue),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (language == AppLanguage.FA) "بستن" else "Close", color = Color.White)
                }
            }
        }
    }
}
