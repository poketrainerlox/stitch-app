package com.stitch.app.ui.screens.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stitch.app.data.model.Friendship
import com.stitch.app.data.model.StitchUser
import com.stitch.app.data.repository.AuthRepository
import com.stitch.app.data.repository.FriendsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FriendCardData(
    val friendshipId: String,
    val friendUid: String,
    val displayName: String,
    val avatarUrl: String,
    val isOnline: Boolean,
    val currentStreak: Int,
    val totalPactsCompleted: Int,
    val currentActivity: String
)

data class FriendsUiState(
    val currentUser: StitchUser? = null,
    val friends: List<FriendCardData> = emptyList(),
    val isLoading: Boolean = true,
    val actionMessage: String? = null
)

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val friendsRepository: FriendsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    init {
        observeFriends()
    }

    private fun observeFriends() {
        viewModelScope.launch {
            authRepository.observeCurrentUser().collect { user ->
                _uiState.value = _uiState.value.copy(currentUser = user)
            }
        }

        viewModelScope.launch {
            friendsRepository.observeFriendships().collect { friendships ->
                val currentUid = authRepository.currentFirebaseUser?.uid ?: return@collect
                val accepted = friendships.filter { it.status == "accepted" }

                val friendCards = accepted.map { friendship ->
                    val friendUid = friendship.getFriendUid(currentUid)
                    val friendProfile = friendsRepository.getUserProfile(friendUid)

                    FriendCardData(
                        friendshipId = friendship.id,
                        friendUid = friendUid,
                        displayName = friendProfile?.displayName ?: friendship.getFriendName(currentUid),
                        avatarUrl = friendProfile?.avatarUrl ?: friendship.getFriendAvatar(currentUid),
                        isOnline = friendProfile?.isOnline ?: false,
                        currentStreak = friendProfile?.currentStreak ?: 0,
                        totalPactsCompleted = friendProfile?.totalPactsCompleted ?: 0,
                        currentActivity = friendProfile?.currentActivity ?: "Idle"
                    )
                }

                _uiState.value = _uiState.value.copy(friends = friendCards, isLoading = false)
            }
        }
    }

    fun sendNudge(friend: FriendCardData) {
        viewModelScope.launch {
            val res = friendsRepository.sendNudge(friend.friendUid, friend.displayName)
            if (res.isSuccess) {
                _uiState.value = _uiState.value.copy(actionMessage = "Nudge beamed to ${friend.displayName} ⚡")
            }
        }
    }

    fun enforceLock(friend: FriendCardData) {
        viewModelScope.launch {
            val res = friendsRepository.enforceEmergencyLock(friend.friendUid, friend.displayName, 30)
            if (res.isSuccess) {
                _uiState.value = _uiState.value.copy(actionMessage = "Enforced 30m Lock on ${friend.displayName} 🔒")
            }
        }
    }

    fun clearActionMessage() {
        _uiState.value = _uiState.value.copy(actionMessage = null)
    }
}
