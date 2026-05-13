package dev.julien.launcher.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.julien.launcher.data.grid.GridRepository
import dev.julien.launcher.domain.model.AppEntry
import dev.julien.launcher.domain.model.GridCoords
import dev.julien.launcher.domain.model.GridItem
import dev.julien.launcher.domain.model.GridLayout
import dev.julien.launcher.domain.usecase.CreateFolderUseCase
import dev.julien.launcher.domain.usecase.PlaceItemOnGridUseCase
import dev.julien.launcher.domain.usecase.RemoveItemUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val gridRepository: GridRepository,
    private val placeItem: PlaceItemOnGridUseCase,
    private val createFolder: CreateFolderUseCase,
    private val removeItem: RemoveItemUseCase,
) : ViewModel() {

    val layout: StateFlow<GridLayout> = gridRepository.layout
        .stateIn(viewModelScope, SharingStarted.Eagerly, GridLayout.EMPTY)

    fun dropAppOnCell(app: AppEntry, cell: GridCoords) {
        viewModelScope.launch {
            val layout = layout.value
            val existing = layout.itemAt(cell)
            when (existing) {
                null -> placeItem.placeApp(app, cell)
                is GridItem.AppShortcut -> {
                    removeItem(existing.id)
                    createFolder(cell, existing.app, app)
                }
                is GridItem.Folder -> gridRepository.addAppToFolder(existing.id, app)
                is GridItem.Widget -> Unit // can't drop apps on widgets — ignore
            }
        }
    }

    fun moveItem(itemId: Long, target: GridCoords) {
        viewModelScope.launch { placeItem.moveItem(itemId, target) }
    }

    fun remove(itemId: Long) {
        viewModelScope.launch { removeItem(itemId) }
    }
}
