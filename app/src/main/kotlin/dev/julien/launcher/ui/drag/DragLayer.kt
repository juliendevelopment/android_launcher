package dev.julien.launcher.ui.drag

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.julien.launcher.domain.model.GridItem
import dev.julien.launcher.ui.common.AppIcon
import kotlin.math.roundToInt

/**
 * Renders the floating drag preview at the session's current pointer position.
 * Placed on top of the rest of the launcher UI so it floats over the drawer/shade.
 */
@Composable
fun DragLayer(modifier: Modifier = Modifier) {
    val session = LocalDragSession.current
    val payload = session.payload ?: return

    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopStart,
    ) {
        Box(
            modifier = Modifier
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    layout(placeable.width, placeable.height) {
                        val xPx = (session.position.x - placeable.width / 2f).roundToInt()
                        val yPx = (session.position.y - placeable.height / 2f).roundToInt()
                        placeable.place(IntOffset(xPx, yPx))
                    }
                }
                .alpha(0.85f)
                .shadow(8.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .size(72.dp),
        ) {
            when (val p = payload) {
                is DragPayload.FromDrawer -> AppIcon(app = p.app, modifier = Modifier.size(72.dp))
                is DragPayload.FromGrid -> when (val item = p.item) {
                    is GridItem.AppShortcut -> AppIcon(app = item.app, modifier = Modifier.size(72.dp))
                    is GridItem.Folder -> Box(
                        modifier = Modifier.size(72.dp),
                        contentAlignment = Alignment.Center,
                    ) { Text(item.label.take(2), color = Color.White) }
                    is GridItem.Widget -> Box(
                        modifier = Modifier.size(72.dp),
                        contentAlignment = Alignment.Center,
                    ) { Text("[widget]", color = Color.White) }
                }
            }
        }
    }
}
