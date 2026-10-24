package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GeminiBlue
import com.example.ui.theme.GeminiBorder
import com.example.ui.theme.GeminiCoral
import com.example.ui.theme.GeminiPurple
import com.example.ui.theme.GeminiSparkleBrush
import com.example.ui.theme.GeminiSurface
import com.example.ui.theme.GeminiSurfaceVariant
import com.example.ui.theme.GeminiTextPrimary
import com.example.ui.theme.GeminiTextSecondary
import com.example.ui.theme.GeminiTextTertiary
import com.example.ui.viewmodel.AppLanguage
import com.example.ui.viewmodel.ChatMessage

@Composable
fun GeminiAssistantView(
    messages: List<ChatMessage>,
    isLoading: Boolean,
    useHighThinking: Boolean,
    language: AppLanguage,
    onToggleThinkingExpanded: (String) -> Unit,
    onClearChat: () -> Unit,
    onQuickPromptClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Chat Header with status & clear
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(GeminiSparkleBrush),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = if (language == AppLanguage.FA) "دستیار مهندسی Gemini" else "Gemini Engineering Assistant",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = GeminiTextPrimary
                    )
                    Text(
                        text = if (useHighThinking) "مدل gemini-4-argon با تفکر عمیق" else "مدل gemini-3.8-flash استاندارد",
                        fontSize = 10.sp,
                        color = GeminiPurple
                    )
                }
            }

            IconButton(
                onClick = onClearChat,
                modifier = Modifier.size(32.dp).testTag("clear_chat_button")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Clear Chat",
                    tint = GeminiTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Messages List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                if (msg.isUser) {
                    UserMessageBubble(message = msg)
                } else {
                    ModelMessageCard(
                        message = msg,
                        language = language,
                        onToggleThinking = { onToggleThinkingExpanded(msg.id) },
                        onCopyCode = { code ->
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("code", code))
                            Toast.makeText(context, if (language == AppLanguage.FA) "کد کپی شد" else "Code copied", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            if (isLoading) {
                item {
                    LoadingShimmerCard(language = language, useHighThinking = useHighThinking)
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun UserMessageBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp))
                .background(GeminiSurfaceVariant)
                .border(1.dp, GeminiBorder, RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.text,
                fontSize = 13.sp,
                color = GeminiTextPrimary,
                lineHeight = 19.sp
            )
        }
    }
}

@Composable
private fun ModelMessageCard(
    message: ChatMessage,
    language: AppLanguage,
    onToggleThinking: () -> Unit,
    onCopyCode: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = GeminiSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, GeminiBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Thinking Card (if present)
            if (!message.thinkingProcess.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1B1B26))
                        .border(1.dp, GeminiPurple.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .clickable { onToggleThinking() }
                        .padding(10.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = GeminiPurple,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == AppLanguage.FA) "فرآیند تفکر عمیق (High Thinking)" else "Thinking Process (High)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GeminiPurple
                                )
                            }
                            Icon(
                                imageVector = if (message.isThinkingExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Expand",
                                tint = GeminiPurple,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        AnimatedVisibility(visible = message.isThinkingExpanded) {
                            Column(modifier = Modifier.padding(top = 8.dp)) {
                                Text(
                                    text = message.thinkingProcess,
                                    fontSize = 11.sp,
                                    color = Color(0xFFD4C2F0),
                                    lineHeight = 17.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            // Main Message Content with Code block formatting
            val text = message.text
            if (text.contains("```")) {
                val parts = text.split("```")
                parts.forEachIndexed { index, part ->
                    if (index % 2 == 1) {
                        // Code block
                        val codeLines = part.trim()
                        val codeOnly = if (codeLines.startsWith("cpp") || codeLines.startsWith("c")) {
                            codeLines.substringAfter("\n")
                        } else codeLines

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F0F12))
                                .border(1.dp, GeminiBorder, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "C++ / Arduino",
                                        fontSize = 10.sp,
                                        color = GeminiBlue,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    IconButton(
                                        onClick = { onCopyCode(codeOnly) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy",
                                            tint = GeminiTextSecondary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = codeOnly,
                                    fontSize = 11.sp,
                                    color = Color(0xFFE0E0E0),
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    } else {
                        // Regular text
                        if (part.trim().isNotEmpty()) {
                            Text(
                                text = part.trim(),
                                fontSize = 13.sp,
                                color = GeminiTextPrimary,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = text,
                    fontSize = 13.sp,
                    color = GeminiTextPrimary,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun LoadingShimmerCard(language: AppLanguage, useHighThinking: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = GeminiSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, GeminiBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = GeminiPurple,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = if (language == AppLanguage.FA) {
                        if (useHighThinking) "جمینای در حال تفکر عمیق و تحلیل دیاگرام سخت‌افزار..."
                        else "جمینای در حال پردازش..."
                    } else {
                        if (useHighThinking) "Gemini is reasoning deeply (gemini-4-argon)..."
                        else "Gemini is processing..."
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = GeminiPurple
                )
                Text(
                    text = if (language == AppLanguage.FA) "پروتکل شبکه محلی • تفکر سطح بالا" else "LAN protocol • High thinking",
                    fontSize = 10.sp,
                    color = GeminiTextTertiary
                )
            }
        }
    }
}
