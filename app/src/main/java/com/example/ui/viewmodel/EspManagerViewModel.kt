package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiAiService
import com.example.data.local.EspDeviceEntity
import com.example.data.local.PinStateEntity
import com.example.data.local.TelemetryRecordEntity
import com.example.data.model.BoardProfiles
import com.example.data.model.EspBoardType
import com.example.data.model.JsonFrame
import com.example.data.model.TelemetryData
import com.example.data.repository.EspRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val thinkingProcess: String? = null,
    val isThinkingExpanded: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

enum class AppLanguage {
    FA, // Persian (Farsi)
    EN  // English
}

enum class MainTab {
    DASHBOARD,
    PINOUT,
    TELEMETRY,
    ASSISTANT,
    OTA
}

class EspManagerViewModel(
    private val repository: EspRepository,
    private val aiService: GeminiAiService = GeminiAiService()
) : ViewModel() {

    val allDevices: StateFlow<List<EspDeviceEntity>> = repository.allDevices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val connectedDevices: StateFlow<List<EspDeviceEntity>> = repository.connectedDevices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val liveTelemetry: StateFlow<Map<String, TelemetryData>> = repository.liveTelemetry
    val recentLogs: StateFlow<List<JsonFrame>> = repository.recentLogs

    private val _selectedDeviceId = MutableStateFlow<String?>(null)
    val selectedDeviceId: StateFlow<String?> = _selectedDeviceId.asStateFlow()

    private val _currentTab = MutableStateFlow(MainTab.DASHBOARD)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    private val _appLanguage = MutableStateFlow(AppLanguage.FA)
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    private val _useHighThinking = MutableStateFlow(true)
    val useHighThinking: StateFlow<Boolean> = _useHighThinking.asStateFlow()

    private val _promptInput = MutableStateFlow("")
    val promptInput: StateFlow<String> = _promptInput.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                isUser = false,
                text = "سلام! من دستیار هوشمند Gemini برای نظارت، برنامه‌نویسی و عیب‌یابی بردهای ESP8266 و ESP32 هستم. پین‌ها، تلمتری و مصرف انرژی به صورت زنده در شبکه محلی پایش می‌شوند. چه کمکی از دستم برمی‌آید؟",
                thinkingProcess = "سیستم آماده است. وضعیت اتصال ۵ برد در بستر LAN و پایگاه داده محلی پایدار شد.",
                isThinkingExpanded = false
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // Pins for currently selected device
    val activePins: StateFlow<List<PinStateEntity>> = _selectedDeviceId.flatMapLatest { devId ->
        if (devId != null) repository.getPinsFlow(devId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Historical telemetry for chart
    val telemetryHistory: StateFlow<List<TelemetryRecordEntity>> = _selectedDeviceId.flatMapLatest { devId ->
        if (devId != null) repository.getDeviceTelemetryHistory(devId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            allDevices.collect { devs ->
                if (_selectedDeviceId.value == null && devs.isNotEmpty()) {
                    _selectedDeviceId.value = devs.first().id
                }
            }
        }
    }

    fun selectDevice(id: String) {
        _selectedDeviceId.value = id
    }

    fun setTab(tab: MainTab) {
        _currentTab.value = tab
    }

    fun toggleLanguage() {
        _appLanguage.value = if (_appLanguage.value == AppLanguage.FA) AppLanguage.EN else AppLanguage.FA
    }

    fun toggleHighThinking() {
        _useHighThinking.value = !_useHighThinking.value
    }

    fun updatePromptInput(text: String) {
        _promptInput.value = text
    }

    fun togglePinState(gpio: Int, currentState: Int) {
        val devId = _selectedDeviceId.value ?: return
        viewModelScope.launch {
            repository.togglePin(devId, gpio, currentState)
        }
    }

    fun setPinPwm(gpio: Int, pwm: Int) {
        val devId = _selectedDeviceId.value ?: return
        viewModelScope.launch {
            repository.setPwmValue(devId, gpio, pwm)
        }
    }

    fun toggleDeviceConnection(id: String, isConnected: Boolean) {
        viewModelScope.launch {
            repository.toggleDeviceConnection(id, isConnected)
        }
    }

    fun addNewDevice(name: String, boardType: EspBoardType, ip: String) {
        viewModelScope.launch {
            repository.addDevice(name, boardType, ip)
        }
    }

    fun removeDevice(id: String) {
        viewModelScope.launch {
            repository.removeDevice(id)
            if (_selectedDeviceId.value == id) {
                _selectedDeviceId.value = allDevices.value.firstOrNull { it.id != id }?.id
            }
        }
    }

    fun sendPrompt(customPrompt: String? = null) {
        val prompt = (customPrompt ?: _promptInput.value).trim()
        if (prompt.isEmpty() || _isAiLoading.value) return

        _promptInput.value = ""
        val userMsg = ChatMessage(isUser = true, text = prompt)
        _chatMessages.value = _chatMessages.value + userMsg
        _isAiLoading.value = true

        viewModelScope.launch {
            val devId = _selectedDeviceId.value
            val activeDev = allDevices.value.find { it.id == devId }
            val telemetry = if (devId != null) repository.liveTelemetry.value[devId] else null

            val context = buildString {
                if (activeDev != null) {
                    append("برد انتخابی: ${activeDev.name} (${activeDev.boardType})، آدرس IP: ${activeDev.ipAddress}، وضعیت: ${if (activeDev.isConnected) "آنلاین" else "آفلاین"}.\n")
                }
                if (telemetry != null) {
                    append("تلمتری زنده: ولتاژ ${telemetry.voltage}V، توان ${telemetry.powerMilliwatts}mW، دما ${telemetry.temperature}°C، حافظه Heap: ${telemetry.heapBytes} بایت، کیفیت سیگنال RSSI: ${telemetry.rssi}dBm.\n")
                }
                append("تعداد بردهای متصل همزمان در شبکه: ${connectedDevices.value.size}")
            }

            val response = aiService.queryGeminiWithThinking(
                prompt = prompt,
                useHighThinking = _useHighThinking.value,
                contextInfo = context
            )

            val modelMsg = ChatMessage(
                isUser = false,
                text = response.mainText,
                thinkingProcess = response.thinkingProcess,
                isThinkingExpanded = true
            )

            _chatMessages.value = _chatMessages.value + modelMsg
            _isAiLoading.value = false
        }
    }

    fun toggleThinkingCardExpanded(messageId: String) {
        _chatMessages.value = _chatMessages.value.map { msg ->
            if (msg.id == messageId) {
                msg.copy(isThinkingExpanded = !msg.isThinkingExpanded)
            } else msg
        }
    }

    fun clearChat() {
        _chatMessages.value = emptyList()
    }
}
