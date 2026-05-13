package dev.julien.launcher.ui.drawer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.julien.launcher.domain.model.AppEntry
import dev.julien.launcher.domain.usecase.GetInstalledAppsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class DrawerUiState(
    val isOpen: Boolean = false,
    val query: String = "",
    val apps: List<AppEntry> = emptyList(),
)

@HiltViewModel
class DrawerViewModel
    @Inject
    constructor(
        getApps: GetInstalledAppsUseCase,
    ) : ViewModel() {
        private val query = MutableStateFlow("")
        private val isOpen = MutableStateFlow(false)
        private val apps: StateFlow<List<AppEntry>> =
            getApps(query)
                .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

        val uiState: StateFlow<DrawerUiState> =
            combine(isOpen, query, apps) { open, q, list ->
                DrawerUiState(isOpen = open, query = q, apps = list)
            }.stateIn(viewModelScope, SharingStarted.Eagerly, DrawerUiState())

        fun setQuery(value: String) {
            query.value = value
        }

        fun open() {
            isOpen.value = true
        }

        fun close() {
            isOpen.value = false
        }
    }
