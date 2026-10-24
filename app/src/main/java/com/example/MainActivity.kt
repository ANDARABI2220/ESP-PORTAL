package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.repository.EspRepository
import com.example.ui.components.AddDeviceDialog
import com.example.ui.components.DeviceSelectorRow
import com.example.ui.components.GeminiPromptBar
import com.example.ui.components.GeminiTopBar
import com.example.ui.components.SettingsDialog
import com.example.ui.screens.DashboardView
import com.example.ui.screens.FirmwareOtaView
import com.example.ui.screens.GeminiAssistantView
import com.example.ui.screens.PinoutMatrixView
import com.example.ui.screens.TelemetryChartsView
import com.example.ui.theme.EspManagerTheme
import com.example.ui.theme.GeminiBackground
import com.example.ui.theme.GeminiBlue
import com.example.ui.theme.GeminiBorder
import com.example.ui.theme.GeminiPurple
import com.example.ui.theme.GeminiSurface
import com.example.ui.theme.GeminiSurfaceVariant
import com.example.ui.theme.GeminiTextPrimary
import com.example.ui.theme.GeminiTextSecondary
import com.example.ui.viewmodel.AppLanguage
import com.example.ui.viewmodel.EspManagerViewModel
import com.example.ui.viewmodel.MainTab
import com.example.util.configureAppCheck

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mandatory App Check configuration with launch intent before any UI or Firebase operation
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        val database = AppDatabase.getInstance(applicationContext)
        val mdnsService = com.example.network.mdns.MdnsDiscoveryService(applicationContext)
        val repository = EspRepository(database, mdnsService)

        setContent {
            val viewModel: EspManagerViewModel = viewModel {
                EspManagerViewModel(repository)
            }

            EspApp(viewModel = viewModel)
        }
    }
}

@Composable
fun EspApp(viewModel: EspManagerViewModel) {
    val language by viewModel.appLanguage.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val allDevices by viewModel.allDevices.collectAsStateWithLifecycle()
    val connectedDevices by viewModel.connectedDevices.collectAsStateWithLifecycle()
    val selectedDeviceId by viewModel.selectedDeviceId.collectAsStateWithLifecycle()
    val activePins by viewModel.activePins.collectAsStateWithLifecycle()
    val liveTelemetry by viewModel.liveTelemetry.collectAsStateWithLifecycle()
    val telemetryHistory by viewModel.telemetryHistory.collectAsStateWithLifecycle()
    val recentLogs by viewModel.recentLogs.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    val useHighThinking by viewModel.useHighThinking.collectAsStateWithLifecycle()
    val promptInput by viewModel.promptInput.collectAsStateWithLifecycle()

    var showAddDeviceDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val activeDevice = allDevices.find { it.id == selectedDeviceId } ?: allDevices.firstOrNull()
    val activeTelemetry = if (activeDevice != null) liveTelemetry[activeDevice.id] else null

    val layoutDirection = if (language == AppLanguage.FA) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        EspManagerTheme(darkTheme = true) {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .background(GeminiBackground),
                containerColor = GeminiBackground,
                topBar = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(GeminiBackground)
                    ) {
                        GeminiTopBar(
                            language = language,
                            connectedDeviceCount = connectedDevices.size,
                            onLanguageToggle = { viewModel.toggleLanguage() },
                            onAddDeviceClick = { showAddDeviceDialog = true },
                            onSettingsClick = { showSettingsDialog = true }
                        )
                        DeviceSelectorRow(
                            devices = allDevices,
                            selectedDeviceId = selectedDeviceId,
                            onDeviceSelected = { viewModel.selectDevice(it) },
                            language = language
                        )
                    }
                },
                bottomBar = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(GeminiBackground)
                            .windowInsetsPadding(WindowInsets.navigationBars)
                    ) {
                        // Floating Gemini Prompt Bar (visible on Dashboard and Assistant tabs)
                        if (currentTab == MainTab.DASHBOARD || currentTab == MainTab.ASSISTANT) {
                            GeminiPromptBar(
                                promptText = promptInput,
                                onPromptChange = { viewModel.updatePromptInput(it) },
                                onSend = {
                                    viewModel.sendPrompt()
                                    if (currentTab != MainTab.ASSISTANT) {
                                        viewModel.setTab(MainTab.ASSISTANT)
                                    }
                                },
                                useHighThinking = useHighThinking,
                                onToggleHighThinking = { viewModel.toggleHighThinking() },
                                isLoading = isAiLoading,
                                language = language
                            )
                        }

                        // Navigation Bar
                        NavigationBar(
                            containerColor = GeminiSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("main_navigation_bar")
                        ) {
                            NavigationBarItem(
                                selected = currentTab == MainTab.DASHBOARD,
                                onClick = { viewModel.setTab(MainTab.DASHBOARD) },
                                icon = {
                                    Icon(Icons.Default.Dashboard, contentDescription = "Dashboard")
                                },
                                label = {
                                    Text(
                                        text = if (language == AppLanguage.FA) "داشبورد" else "Dashboard",
                                        fontSize = 10.sp
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = GeminiBlue,
                                    selectedTextColor = GeminiBlue,
                                    unselectedIconColor = GeminiTextSecondary,
                                    unselectedTextColor = GeminiTextSecondary,
                                    indicatorColor = GeminiSurfaceVariant
                                ),
                                modifier = Modifier.testTag("nav_dashboard")
                            )

                            NavigationBarItem(
                                selected = currentTab == MainTab.PINOUT,
                                onClick = { viewModel.setTab(MainTab.PINOUT) },
                                icon = {
                                    Icon(Icons.Default.Power, contentDescription = "Pinout")
                                },
                                label = {
                                    Text(
                                        text = if (language == AppLanguage.FA) "پین‌ها" else "Pinout",
                                        fontSize = 10.sp
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = GeminiBlue,
                                    selectedTextColor = GeminiBlue,
                                    unselectedIconColor = GeminiTextSecondary,
                                    unselectedTextColor = GeminiTextSecondary,
                                    indicatorColor = GeminiSurfaceVariant
                                ),
                                modifier = Modifier.testTag("nav_pinout")
                            )

                            NavigationBarItem(
                                selected = currentTab == MainTab.TELEMETRY,
                                onClick = { viewModel.setTab(MainTab.TELEMETRY) },
                                icon = {
                                    Icon(Icons.Default.Timeline, contentDescription = "Telemetry")
                                },
                                label = {
                                    Text(
                                        text = if (language == AppLanguage.FA) "تلمتری" else "Telemetry",
                                        fontSize = 10.sp
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = GeminiBlue,
                                    selectedTextColor = GeminiBlue,
                                    unselectedIconColor = GeminiTextSecondary,
                                    unselectedTextColor = GeminiTextSecondary,
                                    indicatorColor = GeminiSurfaceVariant
                                ),
                                modifier = Modifier.testTag("nav_telemetry")
                            )

                            NavigationBarItem(
                                selected = currentTab == MainTab.ASSISTANT,
                                onClick = { viewModel.setTab(MainTab.ASSISTANT) },
                                icon = {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = "AI Assistant")
                                },
                                label = {
                                    Text(
                                        text = if (language == AppLanguage.FA) "جمینای AI" else "Gemini AI",
                                        fontSize = 10.sp
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = GeminiPurple,
                                    selectedTextColor = GeminiPurple,
                                    unselectedIconColor = GeminiTextSecondary,
                                    unselectedTextColor = GeminiTextSecondary,
                                    indicatorColor = GeminiSurfaceVariant
                                ),
                                modifier = Modifier.testTag("nav_assistant")
                            )

                            NavigationBarItem(
                                selected = currentTab == MainTab.OTA,
                                onClick = { viewModel.setTab(MainTab.OTA) },
                                icon = {
                                    Icon(Icons.Default.SystemUpdate, contentDescription = "OTA Updates")
                                },
                                label = {
                                    Text(
                                        text = if (language == AppLanguage.FA) "فلش OTA" else "OTA Flash",
                                        fontSize = 10.sp
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = GeminiBlue,
                                    selectedTextColor = GeminiBlue,
                                    unselectedIconColor = GeminiTextSecondary,
                                    unselectedTextColor = GeminiTextSecondary,
                                    indicatorColor = GeminiSurfaceVariant
                                ),
                                modifier = Modifier.testTag("nav_ota")
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    Crossfade(targetState = currentTab, label = "tab_crossfade") { tab ->
                        when (tab) {
                            MainTab.DASHBOARD -> DashboardView(
                                activeDevice = activeDevice,
                                telemetry = activeTelemetry,
                                pins = activePins,
                                language = language,
                                onTogglePin = { gpio, state -> viewModel.togglePinState(gpio, state) },
                                onQuickPromptClick = { prompt ->
                                    viewModel.sendPrompt(prompt)
                                    viewModel.setTab(MainTab.ASSISTANT)
                                },
                                onNavigateToPinout = { viewModel.setTab(MainTab.PINOUT) },
                                onNavigateToTelemetry = { viewModel.setTab(MainTab.TELEMETRY) }
                            )

                            MainTab.PINOUT -> PinoutMatrixView(
                                activeDevice = activeDevice,
                                pins = activePins,
                                language = language,
                                onTogglePin = { gpio, state -> viewModel.togglePinState(gpio, state) },
                                onSetPwm = { gpio, pwm -> viewModel.setPinPwm(gpio, pwm) }
                            )

                            MainTab.TELEMETRY -> TelemetryChartsView(
                                activeDevice = activeDevice,
                                telemetry = activeTelemetry,
                                history = telemetryHistory,
                                recentLogs = recentLogs,
                                language = language
                            )

                            MainTab.ASSISTANT -> GeminiAssistantView(
                                messages = chatMessages,
                                isLoading = isAiLoading,
                                useHighThinking = useHighThinking,
                                language = language,
                                onToggleThinkingExpanded = { viewModel.toggleThinkingCardExpanded(it) },
                                onClearChat = { viewModel.clearChat() },
                                onQuickPromptClick = { viewModel.sendPrompt(it) }
                            )

                            MainTab.OTA -> FirmwareOtaView(
                                activeDevice = activeDevice,
                                language = language
                            )
                        }
                    }
                }

                if (showAddDeviceDialog) {
                    AddDeviceDialog(
                        language = language,
                        onDismiss = { showAddDeviceDialog = false },
                        onAddDevice = { name, type, ip ->
                            viewModel.addNewDevice(name, type, ip)
                        }
                    )
                }

                if (showSettingsDialog) {
                    SettingsDialog(
                        language = language,
                        useHighThinking = useHighThinking,
                        onLanguageChange = {
                            if (it != language) viewModel.toggleLanguage()
                        },
                        onToggleThinking = { viewModel.toggleHighThinking() },
                        onDismiss = { showSettingsDialog = false }
                    )
                }
            }
        }
    }
}
