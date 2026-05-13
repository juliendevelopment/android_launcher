package dev.julien.launcher.domain.model

const val GRID_COLUMNS = 5
const val GRID_ROWS = 6

data class GridCoords(val x: Int, val y: Int) {
    init {
        require(x in 0 until GRID_COLUMNS) { "x=$x outside $GRID_COLUMNS" }
        require(y in 0 until GRID_ROWS) { "y=$y outside $GRID_ROWS" }
    }
}

data class GridSpan(val width: Int, val height: Int) {
    init {
        require(width in 1..GRID_COLUMNS) { "width=$width outside [1,$GRID_COLUMNS]" }
        require(height in 1..GRID_ROWS) { "height=$height outside [1,$GRID_ROWS]" }
    }

    companion object {
        val ONE_BY_ONE = GridSpan(1, 1)
    }
}

sealed interface GridItem {
    val id: Long
    val coords: GridCoords
    val span: GridSpan

    data class AppShortcut(
        override val id: Long,
        override val coords: GridCoords,
        val app: AppEntry,
    ) : GridItem {
        override val span: GridSpan = GridSpan.ONE_BY_ONE
    }

    data class Folder(
        override val id: Long,
        override val coords: GridCoords,
        val label: String,
        val apps: List<AppEntry>,
    ) : GridItem {
        override val span: GridSpan = GridSpan.ONE_BY_ONE
    }

    data class Widget(
        override val id: Long,
        override val coords: GridCoords,
        override val span: GridSpan,
        val appWidgetId: Int,
        val providerFlattened: String,
    ) : GridItem
}

/** True if [span] rooted at [coords] does not overlap any element of [items] (excluding self by id). */
fun GridLayout.canPlace(coords: GridCoords, span: GridSpan, ignoreId: Long? = null): Boolean {
    if (coords.x + span.width > GRID_COLUMNS) return false
    if (coords.y + span.height > GRID_ROWS) return false
    return items.none { existing ->
        if (existing.id == ignoreId) return@none false
        val xOverlap = existing.coords.x < coords.x + span.width &&
            existing.coords.x + existing.span.width > coords.x
        val yOverlap = existing.coords.y < coords.y + span.height &&
            existing.coords.y + existing.span.height > coords.y
        xOverlap && yOverlap
    }
}

data class GridLayout(val items: List<GridItem>) {
    fun itemAt(coords: GridCoords): GridItem? = items.firstOrNull { item ->
        coords.x in item.coords.x until item.coords.x + item.span.width &&
            coords.y in item.coords.y until item.coords.y + item.span.height
    }

    companion object { val EMPTY = GridLayout(emptyList()) }
}
