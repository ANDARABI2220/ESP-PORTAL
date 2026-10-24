package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.EspDeviceEntity
import com.example.ui.theme.GeminiBackground
import com.example.ui.theme.GeminiBlue
import com.example.ui.theme.GeminiBorder
import com.example.ui.theme.GeminiBorderLight
import com.example.ui.theme.GeminiCoral
import com.example.ui.theme.GeminiGreen
import com.example.ui.theme.GeminiPurple
import com.example.ui.theme.GeminiSparkleBrush
import com.example.ui.theme.GeminiSurface
import com.example.ui.theme.GeminiSurfaceVariant
import com.example.ui.theme.GeminiTextPrimary
import com.example.ui.theme.GeminiTextSecondary
import com.example.ui.theme.GeminiTextTertiary
import com.example.ui.viewmodel.AppLanguage

@Composable
fun GeminiTopBar(
    language: AppLanguage,
    connectedDeviceCount: Int,
    onLanguageToggle: () -> Unit,
    onAddDeviceClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sparkleRotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(24000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = GeminiBackground
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Gemini Sparkle Icon & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.testTag("gemini_brand_header")
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(GeminiSurfaceVariant)
                        .border(1.dp, GeminiBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Gemini Sparkle",
                        modifier = Modifier
                            .size(22.dp)
                            .rotate(rotation),
                        tint = GeminiBlue
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (language == AppLanguage.FA) "جمینای ESP" else "Gemini ESP",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = GeminiTextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(GeminiPurple.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "IoT AI",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GeminiPurple
                            )
                        }
                    }
                    Text(
                        text = if (language == AppLanguage.FA) "$connectedDeviceCount دستگاه آنلاین در LAN"
                        else "$connectedDeviceCount devices online in LAN",
                        fontSize = 11.sp,
                        color = GeminiTextSecondary
                    )
                }
            }

            // Right: Language switch & Action buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Language toggle pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(GeminiSurfaceVariant)
                        .border(1.dp, GeminiBorder, RoundedCornerShape(16.dp))
                        .clickable { onLanguageToggle() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("language_toggle_button")
                ) {
                    Text(
                        text = if (language == AppLanguage.FA) "فا | EN" else "EN | فا",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = GeminiBlue
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onAddDeviceClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(GeminiSurfaceVariant)
                        .testTag("add_device_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Device",
                        tint = GeminiTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(GeminiSurfaceVariant)
                        .testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = GeminiTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DeviceSelectorRow(
    devices: List<EspDeviceEntity>,
    selectedDeviceId: String?,
    onDeviceSelected: (String) -> Unit,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    if (devices.isEmpty()) return

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(devices, key = { it.id }) { dev ->
            val isSelected = dev.id == selectedDeviceId
            val borderModifier = if (isSelected) {
                Modifier.border(1.5.dp, GeminiBlue, RoundedCornerShape(20.dp))
            } else {
                Modifier.border(1.dp, GeminiBorder, RoundedCornerShape(20.dp))
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .then(borderModifier)
                    .background(if (isSelected) GeminiSurfaceVariant else GeminiSurface)
                    .clickable { onDeviceSelected(dev.id) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .testTag("device_pill_${dev.id}")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Pulsing / Status dot
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (dev.isConnected) GeminiGreen else Color.Gray)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dev.name,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) GeminiTextPrimary else GeminiTextSecondary
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = dev.boardType,
                        fontSize = 10.sp,
                        color = GeminiBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun GeminiPromptBar(
    promptText: String,
    onPromptChange: (String) -> Unit,
    onSend: () -> Unit,
    useHighThinking: Boolean,
    onToggleHighThinking: () -> Unit,
    isLoading: Boolean,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(GeminiBackground)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // High Thinking Pill Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (useHighThinking) GeminiPurple.copy(alpha = 0.18f) else GeminiSurfaceVariant)
                    .border(
                        1.dp,
                        if (useHighThinking) GeminiPurple.copy(alpha = 0.5f) else GeminiBorder,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onToggleHighThinking() }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                    .testTag("toggle_thinking_pill")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "Thinking Mode",
                        modifier = Modifier.size(16.dp),
                        tint = if (useHighThinking) GeminiPurple else GeminiTextSecondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (language == AppLanguage.FA) {
                            if (useHighThinking) "حالت تفکر عمیق: فعال (gemini-4-argon)" else "حالت تفکر: استاندارد"
                        } else {
                            if (useHighThinking) "High Thinking: Active (gemini-4-argon)" else "Thinking: Standard"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (useHighThinking) GeminiPurple else GeminiTextSecondary
                    )
                }
            }

            Text(
                text = if (language == AppLanguage.FA) "پروتکل JSON v2" else "JSON v2 Protocol",
                fontSize = 10.sp,
                color = GeminiTextTertiary
            )
        }

        // Gemini Floating Capsule
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(GeminiSurfaceVariant)
                .border(1.dp, GeminiBorder, RoundedCornerShape(26.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Attach file / image icon
                IconButton(
                    onClick = { /* File attachment simulated */ },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AttachFile,
                        contentDescription = "Attach",
                        tint = GeminiTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Text Field
                BasicTextField(
                    value = promptText,
                    onValueChange = onPromptChange,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .testTag("gemini_prompt_input"),
                    textStyle = TextStyle(
                        color = GeminiTextPrimary,
                        fontSize = 14.sp
                    ),
                    cursorBrush = SolidColor(GeminiBlue),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (promptText.isEmpty()) {
                                Text(
                                    text = if (language == AppLanguage.FA)
                                        "از جمینای درباره پین‌ها، مصرف توان و کد بپرسید..."
                                    else
                                        "Ask Gemini about pins, power, or code...",
                                    color = GeminiTextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                // Mic or Send Button
                if (promptText.isBlank()) {
                    IconButton(
                        onClick = {
                            onPromptChange(
                                if (language == AppLanguage.FA)
                                    "تحلیل وضعیت پین‌های برد فعال و بهینه‌سازی توان"
                                else
                                    "Analyze active board pins and power consumption"
                            )
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Input",
                            tint = GeminiTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GeminiSparkleBrush)
                            .clickable(enabled = !isLoading) { onSend() }
                            .testTag("send_prompt_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
