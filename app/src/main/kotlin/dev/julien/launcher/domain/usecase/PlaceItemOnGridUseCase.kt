package dev.julien.launcher.domain.usecase

import dev.julien.launcher.data.grid.GridRepository
import dev.julien.launcher.domain.model.AppEntry
import dev.julien.launcher.domain.model.GridCoords
import dev.julien.launcher.domain.model.GridItem
import dev.julien.launcher.domain.model.GridSpan
import dev.julien.launcher.domain.model.canPlace
import javax.inject.Inject

/**
 * Top-level placement: app/folder/widget into a target cell on the home grid.
 * Returns false (and no-ops) if the target overlaps existing items.
 */
class PlaceItemOnGridUseCase
    @Inject
    constructor(
        private val gridRepository: GridRepository,
    ) {
        suspend fun placeApp(
            app: AppEntry,
            target: GridCoords,
        ): Boolean {
            val layout = gridRepository.snapshot()
            if (!layout.canPlace(target, GridSpan.ONE_BY_ONE)) return false
            gridRepository.placeApp(app, target)
            return true
        }

        suspend fun moveItem(
            itemId: Long,
            target: GridCoords,
        ): Boolean {
            val layout = gridRepository.snapshot()
            val item = layout.items.firstOrNull { it.id == itemId } ?: return false
            if (!layout.canPlace(target, item.span, ignoreId = itemId)) return false
            gridRepository.moveItem(itemId, target)
            return true
        }

        suspend fun placeWidget(
            widget: GridItem.Widget,
            target: GridCoords,
        ): Boolean {
            val layout = gridRepository.snapshot()
            if (!layout.canPlace(target, widget.span)) return false
            gridRepository.placeWidget(widget.appWidgetId, widget.providerFlattened, target, widget.span)
            return true
        }
    }
