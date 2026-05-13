package dev.julien.launcher.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GridLayoutTest {

    private val app = AppEntry("com.example", "com.example.Main", "Example")

    @Test fun `canPlace allows non-overlapping cells`() {
        val layout = GridLayout(
            listOf(GridItem.AppShortcut(1, GridCoords(0, 0), app)),
        )
        assertTrue(layout.canPlace(GridCoords(1, 0), GridSpan.ONE_BY_ONE))
    }

    @Test fun `canPlace rejects overlap`() {
        val layout = GridLayout(
            listOf(GridItem.AppShortcut(1, GridCoords(0, 0), app)),
        )
        assertFalse(layout.canPlace(GridCoords(0, 0), GridSpan.ONE_BY_ONE))
    }

    @Test fun `canPlace ignores self when moving`() {
        val layout = GridLayout(
            listOf(GridItem.AppShortcut(1, GridCoords(0, 0), app)),
        )
        assertTrue(layout.canPlace(GridCoords(0, 0), GridSpan.ONE_BY_ONE, ignoreId = 1L))
    }

    @Test fun `canPlace rejects out-of-bounds widget`() {
        val layout = GridLayout(emptyList())
        assertFalse(layout.canPlace(GridCoords(GRID_COLUMNS - 1, 0), GridSpan(2, 1)))
    }

    @Test fun `itemAt locates widget area`() {
        val layout = GridLayout(
            listOf(GridItem.Widget(7, GridCoords(1, 1), GridSpan(2, 2), 100, "com.x/A")),
        )
        val item = layout.itemAt(GridCoords(2, 2))
        assert(item is GridItem.Widget && item.id == 7L)
    }
}
