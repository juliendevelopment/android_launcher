package dev.julien.launcher.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.julien.launcher.data.notifications.NotificationRepository
import dev.julien.launcher.domain.model.ActiveNotification
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class NotificationShadeViewModel @Inject constructor(
    private val repository: NotificationRepository,
) : ViewModel() {

    val notifications: StateFlow<List<ActiveNotification>> = repository.active
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _accessGranted = MutableStateFlow(repository.isAccessGranted())
    val accessGranted: StateFlow<Boolean> = _accessGranted.asStateFlow()

    fun refreshAccess() { _accessGranted.value = repository.isAccessGranted() }

    fun openSettingsIntent() = repository.openSettingsIntent()

    fun dismiss(key: String) {
        viewModelScope.launch { repository.dismiss(key) }
    }
}
