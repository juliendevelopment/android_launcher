package dev.julien.launcher.ui.drag

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import dev.julien.launcher.domain.model.AppEntry
import dev.julien.launcher.domain.model.GridItem

/** What is being dragged. */
sealed interface DragPayload {
    /** Drag of a free-standing app entry from the drawer or a folder popup. */
    data class FromDrawer(val app: AppEntry) : DragPayload

    /** Drag of an existing grid item — moving within the home grid. */
    data class FromGrid(val item: GridItem) : DragPayload
}

class DragSession {
    var payload: DragPayload? by mutableStateOf<DragPayload?>(null)
        private set
    var position: Offset by mutableStateOf(Offset.Zero)
    var hovered: HoverTarget? by mutableStateOf<HoverTarget?>(null)

    fun begin(payload: DragPayload, startAt: Offset) {
        this.payload = payload
        this.position = startAt
    }

    fun end() {
        payload = null
        hovered = null
    }

    val isActive: Boolean get() = payload != null
}

/** Drop targets the home screen can publish. The grid cell tracker uses [HoverTarget.Cell]. */
sealed interface HoverTarget {
    data class Cell(val x: Int, val y: Int) : HoverTarget
    data object RemoveZone : HoverTarget
    data object UninstallZone : HoverTarget
    data class OverItem(val itemId: Long) : HoverTarget
}

val LocalDragSession = compositionLocalOf<DragSession> {
    error("DragSession not provided. Wrap with CompositionLocalProvider(LocalDragSession provides ...)")
}

@Composable
fun rememberDragSession(): DragSession = remember { DragSession() }
