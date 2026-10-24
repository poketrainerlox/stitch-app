package com.stitch.app.ui.screens.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stitch.app.data.model.FocusSchedule
import com.stitch.app.data.model.InstalledAppInfo
import com.stitch.app.data.repository.ScheduleRepository
import com.stitch.app.data.repository.UsageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScheduleUiState(
    val schedules: List<FocusSchedule> = emptyList(),
    val installedApps: List<InstalledAppInfo> = emptyList(),
    val isAutoScheduleEnabled: Boolean = true,
    val isLoading: Boolean = true,
    val actionMessage: String? = null
)

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val usageRepository: UsageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    init {
        observeSchedules()
        loadInstalledApps()
    }

    private fun observeSchedules() {
        viewModelScope.launch {
            scheduleRepository.observeSchedules().collect { list ->
                _uiState.value = _uiState.value.copy(schedules = list, isLoading = false)
            }
        }
    }

    private fun loadInstalledApps() {
        viewModelScope.launch {
            val apps = usageRepository.getInstalledLaunchableApps()
            _uiState.value = _uiState.value.copy(installedApps = apps)
        }
    }

    fun toggleSchedule(scheduleId: String, isEnabled: Boolean) {
        viewModelScope.launch {
            scheduleRepository.toggleScheduleEnabled(scheduleId, isEnabled)
        }
    }

    fun deleteSchedule(scheduleId: String) {
        viewModelScope.launch {
            val res = scheduleRepository.deleteSchedule(scheduleId)
            if (res.isSuccess) {
                _uiState.value = _uiState.value.copy(actionMessage = "Schedule removed.")
            }
        }
    }

    /**
     * Executes the Auto-Scheduling intelligence:
     * Analyzes peak usage and automatically configures standard smart shields.
     */
    fun applySmartRoutines() {
        viewModelScope.launch {
            val socialApps = _uiState.value.installedApps.filter {
                it.category == "Social" || it.category == "Entertainment"
            }
            val pkgs = socialApps.map { it.packageName }.ifEmpty {
                listOf("com.instagram.android", "com.zhiliaoapp.musically", "com.google.android.youtube")
            }
            val names = socialApps.map { it.appName }.ifEmpty {
                listOf("Instagram", "TikTok", "YouTube")
            }

            val res = scheduleRepository.generateAndApplySmartSchedules(pkgs, names)
            if (res.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    actionMessage = "Auto-scheduled ${res.getOrDefault(3)} smart focus routines!"
                )
            }
        }
    }

    fun addCustomSchedule(
        title: String,
        category: String,
        startHour: Int,
        startMin: Int,
        endHour: Int,
        endMin: Int,
        days: List<Int>,
        apps: List<String>,
        appNames: List<String>
    ) {
        viewModelScope.launch {
            val schedule = FocusSchedule(
                title = title,
                category = category,
                startHour = startHour,
                startMinute = startMin,
                endHour = endHour,
                endMinute = endMin,
                daysOfWeek = days,
                blockedApps = apps,
                blockedAppNames = appNames,
                isEnabled = true
            )
            val res = scheduleRepository.saveSchedule(schedule)
            if (res.isSuccess) {
                _uiState.value = _uiState.value.copy(actionMessage = "Focus block '$title' scheduled.")
            }
        }
    }

    fun clearActionMessage() {
        _uiState.value = _uiState.value.copy(actionMessage = null)
    }
}
