package com.stitch.app.ui.screens.pacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stitch.app.data.model.InstalledAppInfo
import com.stitch.app.data.model.Pact
import com.stitch.app.data.repository.AuthRepository
import com.stitch.app.data.repository.FriendsRepository
import com.stitch.app.data.repository.PactsRepository
import com.stitch.app.data.repository.UsageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PactsUiState(
    val pacts: List<Pact> = emptyList(),
    val installedApps: List<InstalledAppInfo> = emptyList(),
    val currentUid: String = "",
    val isLoading: Boolean = true,
    val message: String? = null
)

@HiltViewModel
class PactsViewModel @Inject constructor(
    private val pactsRepository: PactsRepository,
    private val authRepository: AuthRepository,
    private val usageRepository: UsageRepository,
    private val friendsRepository: FriendsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PactsUiState())
    val uiState: StateFlow<PactsUiState> = _uiState.asStateFlow()

    init {
        val uid = authRepository.currentFirebaseUser?.uid ?: ""
        _uiState.value = _uiState.value.copy(currentUid = uid)

        observePacts()
        loadInstalledApps()
    }

    private fun observePacts() {
        viewModelScope.launch {
            pactsRepository.observeUserPacts().collect { list ->
                _uiState.value = _uiState.value.copy(pacts = list, isLoading = false)
            }
        }
    }

    private fun loadInstalledApps() {
        viewModelScope.launch {
            val apps = usageRepository.getInstalledLaunchableApps()
            _uiState.value = _uiState.value.copy(installedApps = apps)
        }
    }

    fun approveEmergencyPass(pactId: String, approve: Boolean) {
        viewModelScope.launch {
            val res = pactsRepository.approveEmergencyPass(pactId, approve)
            if (res.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    message = if (approve) "5-Min Pass Approved!" else "Pass Denied. Discipline enforced."
                )
            }
        }
    }

    fun surrenderPact(pactId: String) {
        viewModelScope.launch {
            val res = pactsRepository.surrenderPact(pactId)
            if (res.isSuccess) {
                _uiState.value = _uiState.value.copy(message = "Pact surrendered. Focus streak reset.")
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
