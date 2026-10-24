package com.stitch.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stitch.app.data.model.AppUsageInfo
import com.stitch.app.data.model.Pact
import com.stitch.app.data.model.StitchUser
import com.stitch.app.data.repository.AuthRepository
import com.stitch.app.data.repository.PactsRepository
import com.stitch.app.data.repository.UsageRepository
import com.stitch.app.service.StitchAccessibilityService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val user: StitchUser? = null,
    val totalScreenTimeMinutes: Long = 0L,
    val dailyGoalMinutes: Int = 240,
    val topApps: List<AppUsageInfo> = emptyList(),
    val activePact: Pact? = null,
    val hasUsageAccess: Boolean = false,
    val hasOverlayAccess: Boolean = false,
    val hasAccessibilityAccess: Boolean = false,
    val isLockdownEngaged: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val pactsRepository: PactsRepository,
    private val usageRepository: UsageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
        observeData()
    }

    fun loadData() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            val stats = usageRepository.getTodayScreenTimeStats()
            val totalMinutes = stats.sumOf { it.usageMinutes }
            val hasUsage = usageRepository.hasUsageStatsPermission()
            val hasOverlay = usageRepository.hasOverlayPermission()
            val hasAccessibility = usageRepository.isAccessibilityServiceEnabled()

            _uiState.value = _uiState.value.copy(
                user = user,
                totalScreenTimeMinutes = totalMinutes,
                dailyGoalMinutes = user?.dailyGoalMinutes ?: 240,
                topApps = stats.take(5),
                hasUsageAccess = hasUsage,
                hasOverlayAccess = hasOverlay,
                hasAccessibilityAccess = hasAccessibility,
                isLoading = false
            )
        }
    }

    private fun observeData() {
        viewModelScope.launch {
            authRepository.observeCurrentUser().collect { user ->
                _uiState.value = _uiState.value.copy(
                    user = user,
                    dailyGoalMinutes = user?.dailyGoalMinutes ?: 240
                )
            }
        }

        viewModelScope.launch {
            pactsRepository.observeUserPacts().collect { pacts ->
                val active = pacts.firstOrNull { it.isCurrentlyActive() }
                _uiState.value = _uiState.value.copy(activePact = active)

                if (active != null) {
                    val appNameMap = active.blockedApps.zip(active.blockedAppNames).toMap()
                    StitchAccessibilityService.updateActivePactState(
                        packages = active.blockedApps.toSet(),
                        appNames = appNameMap,
                        guardian = active.partnerName,
                        pactId = active.id,
                        remainingSeconds = active.remainingSeconds()
                    )
                } else {
                    StitchAccessibilityService.clearPactState()
                }
            }
        }
    }

    fun engageInstantLockdown() {
        viewModelScope.launch {
            val user = _uiState.value.user ?: return@launch
            // Instant 25-minute lockdown on top social apps
            val socialPackages = listOf(
                "com.instagram.android",
                "com.zhiliaoapp.musically",
                "com.twitter.android",
                "com.reddit.frontpage",
                "com.google.android.youtube"
            )
            val socialNames = listOf("Instagram", "TikTok", "X", "Reddit", "YouTube")

            pactsRepository.createPact(
                partnerUid = "self_guardian",
                partnerName = "Focus Protocol",
                title = "Emergency Solo Lockdown",
                blockedApps = socialPackages,
                blockedAppNames = socialNames,
                durationMinutes = 25
            )
            _uiState.value = _uiState.value.copy(isLockdownEngaged = true)
        }
    }
}
