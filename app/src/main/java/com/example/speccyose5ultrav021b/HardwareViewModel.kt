package com.example.speccyose5ultrav021b

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HardwareViewModel(
    private val hardwareManager: HardwareControlManagerBeta,
    private val settingsManager: SettingsManager,
    private val application: Application
) : AndroidViewModel(application) {

    private val powerManager = PowerManager(application)
    private val _uiState = MutableStateFlow(HardwareUiState())
    val uiState = _uiState.asStateFlow()
    
    private val _powerStats = MutableStateFlow<PowerStats?>(null)
    val powerStats = _powerStats.asStateFlow()

    private var isFanManualOverride = false

    init {
        startMonitoring()
    }

    private fun startMonitoring() {
        viewModelScope.launch {
            while (true) {
                val currentTemp = hardwareManager.getCpuTemperature()
                val currentRamUsageStr = hardwareManager.getRamUsage()
                // Convertir "4.2GB / 12GB" a Float aproximado para la barra de progreso
                val currentRamUsage = try {
                    val used = currentRamUsageStr.substringBefore("GB").trim().toFloat()
                    val total = currentRamUsageStr.substringAfter("/").substringBefore("GB").trim().toFloat()
                    used / total
                } catch (e: Exception) { 0.35f }

                val currentPower = powerManager.getPowerStats()
                
                _powerStats.value = currentPower

                val cpuLoad = when (settingsManager.manualProfile) {
                    "ECO" -> 0.3f
                    "BALANCED" -> 0.5f
                    "PERFORMANCE" -> 0.8f
                    "EXTREME" -> 0.95f
                    else -> 0.5f
                }
                
                var fanSpeedLevel = _uiState.value.fanSpeedLevel
                if (!isFanManualOverride) {
                    fanSpeedLevel = when (settingsManager.manualProfile) {
                        "ECO" -> 0.0f  
                        "BALANCED" -> 0.33f 
                        "PERFORMANCE" -> 0.66f 
                        "EXTREME" -> 1.0f 
                        else -> 0.33f
                    }
                }

                val estimatedFps = when (settingsManager.manualProfile) {
                    "ECO" -> 30 
                    "BALANCED" -> 45
                    "PERFORMANCE" -> 60
                    "EXTREME" -> 75 
                    else -> 45
                }

                _uiState.value = _uiState.value.copy(
                    temperature = currentTemp,
                    ramUsage = currentRamUsage,
                    isManualMode = settingsManager.isManualPerformanceMode,
                    currentProfile = settingsManager.manualProfile,
                    isPro = settingsManager.isProUser,
                    cpuLoad = cpuLoad,
                    fanSpeedLevel = fanSpeedLevel,
                    estimatedFps = estimatedFps
                )
                
                if (!isFanManualOverride) {
                    val level = (fanSpeedLevel * 3).toInt()
                    hardwareManager.setFanSpeed(level)
                }
                
                hardwareManager.runSafetyCheck()
                delay(2000)
            }
        }
    }

    fun setManualProfile(profileId: String): Boolean {
        if (profileId == "EXTREME" && !settingsManager.isProUser) {
            return false
        }
        settingsManager.manualProfile = profileId
        isFanManualOverride = false 
        _uiState.value = _uiState.value.copy(currentProfile = profileId)
        hardwareManager.setPerformanceMode(profileId == "PERFORMANCE" || profileId == "EXTREME")
        return true
    }
    
    fun setManualFanSpeed(level: Int) {
        isFanManualOverride = true
        val progress = level / 3f
        _uiState.value = _uiState.value.copy(fanSpeedLevel = progress)
        hardwareManager.setFanSpeed(level)
    }
}

data class HardwareUiState(
    val temperature: Float = 0f,
    val ramUsage: Float = 0f,
    val cpuLoad: Float = 0f,
    val fanSpeedLevel: Float = 0f,
    val estimatedFps: Int = 0,
    val isManualMode: Boolean = false,
    val currentProfile: String = "BALANCED",
    val isPro: Boolean = false
)
