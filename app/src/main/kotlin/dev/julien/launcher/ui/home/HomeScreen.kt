package dev.julien.launcher.ui.home

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.julien.launcher.domain.model.GRID_COLUMNS
import dev.julien.launcher.domain.model.GRID_ROWS
import dev.julien.launcher.domain.model.GridCoords
import dev.julien.launcher.domain.model.GridItem
import dev.julien.launcher.ui.common.AppIcon
import dev.julien.launcher.ui.drag.DragPayload
import dev.julien.launcher.ui.drag.HoverTarget
import dev.julien.launcher.ui.drag.LocalDragSession
import dev.julien.launcher.ui.home.folder.FolderPopup
import dev.julien.launcher.ui.home.widgets.WidgetCell

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onOpenDrawer: () -> Unit,
    onOpenShade: () -> Unit,
) {
    val layout by viewModel.layout.collectAsState()
    val dragSession = LocalDragSession.current
    val context = LocalContext.current
    val density = LocalDensity.current

    var gridBounds by remember { mutableStateOf(Rect.Zero) }
    var removeZoneBounds by remember { mutableStateOf(Rect.Zero) }
    var uninstallZoneBounds by remember { mutableStateOf(Rect.Zero) }
    var openedFolderId by remember { mutableStateOf<Long?>(null) }

    fun cellAt(offset: Offset): GridCoords? {
        if (gridBounds == Rect.Zero || gridBounds.isEmpty) return null
        if (!gridBounds.contains(offset)) return null
        val localX = offset.x - gridBounds.left
        val localY = offset.y - gridBounds.top
        val cellW = gridBounds.width / GRID_COLUMNS
        val cellH = gridBounds.height / GRID_ROWS
        val x = (localX / cellW).toInt().coerceIn(0, GRID_COLUMNS - 1)
        val y = (localY / cellH).toInt().coerceIn(0, GRID_ROWS - 1)
        return runCatching { GridCoords(x, y) }.getOrNull()
    }

    // Once a drag session is active (started by a child long-press, or by the drawer
    // before it dismissed), this pointer input tracks moves and finalises the drop.
    Box(
        modifier = modifier.pointerInput(dragSession.isActive) {
            if (!dragSession.isActive) return@pointerInput
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    val pointer = event.changes.firstOrNull() ?: continue
                    dragSession.position = pointer.position
                    dragSession.hovered = when {
                        removeZoneBounds.contains(pointer.position) -> HoverTarget.RemoveZone
                        uninstallZoneBounds.contains(pointer.position) -> HoverTarget.UninstallZone
                        else -> cellAt(pointer.position)?.let { HoverTarget.Cell(it.x, it.y) }
                    }
                    if (!pointer.pressed) {
                        val pos = pointer.position
                        when {
                            removeZoneBounds.contains(pos) -> {
                                (dragSession.payload as? DragPayload.FromGrid)?.let { viewModel.remove(it.item.id) }
                            }
                            uninstallZoneBounds.contains(pos) -> {
                                val grid = dragSession.payload as? DragPayload.FromGrid
                                val app = (grid?.item as? GridItem.AppShortcut)?.app
                                if (app != null) {
                                    val intent = Intent(Intent.ACTION_DELETE, Uri.fromParts("package", app.packageName, null))
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    runCatching { context.startActivity(intent) }
                                    viewModel.remove(grid.item.id)
                                }
                            }
                            else -> {
                                val target = cellAt(pos)
                                if (target != null) {
                                    when (val p = dragSession.payload) {
                                        is DragPayload.FromDrawer -> viewModel.dropAppOnCell(p.app, target)
                                        is DragPayload.FromGrid -> viewModel.moveItem(p.item.id, target)
                                        null -> Unit
                                    }
                                }
                            }
                        }
                        dragSession.end()
                        return@awaitPointerEventScope
                    }
                }
            }
        },
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 12.dp)) {
            DropStrip(
                label = "Remove",
                active = dragSession.isActive && dragSession.hovered is HoverTarget.RemoveZone,
                visible = dragSession.isActive,
                onPositioned = { removeZoneBounds = it },
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .onGloballyPositioned { gridBounds = it.boundsInRoot() },
            ) {
                if (gridBounds != Rect.Zero) {
                    with(density) {
                        val cellWDp = gridBounds.width.toDp() / GRID_COLUMNS
                        val cellHDp = gridBounds.height.toDp() / GRID_ROWS

                        layout.items.forEach { item ->
                            Box(
                                modifier = Modifier
                                    .padding(
                                        start = cellWDp * item.coords.x,
                                        top = cellHDp * item.coords.y,
                                    )
                                    .width(cellWDp * item.span.width)
                                    .height(cellHDp * item.span.height),
                            ) {
                                GridItemCell(
                                    item = item,
                                    onTap = {
                                        when (item) {
                                            is GridItem.AppShortcut -> launchApp(context, item.app.packageName, item.app.className)
                                            is GridItem.Folder -> openedFolderId = item.id
                                            is GridItem.Widget -> Unit
                                        }
                                    },
                                    onLongPress = { offset ->
                                        dragSession.begin(DragPayload.FromGrid(item), offset)
                                    },
                                )
                            }
                        }
                    }
                }
            }

            DropStrip(
                label = "Uninstall",
                active = dragSession.isActive && dragSession.hovered is HoverTarget.UninstallZone,
                visible = dragSession.isActive,
                onPositioned = { uninstallZoneBounds = it },
            )
        }

        // Hidden double-tap zones to open drawer/shade without a swipe (used by tests).
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(24.dp)
                    .pointerInput("drawer-handle") {
                        detectTapGestures(onDoubleTap = { onOpenDrawer() })
                    },
            )
            Box(
                Modifier.align(Alignment.TopCenter).fillMaxWidth().height(24.dp)
                    .pointerInput("shade-handle") {
                        detectTapGestures(onDoubleTap = { onOpenShade() })
                    },
            )
        }

        openedFolderId?.let { id ->
            val folder = layout.items.firstOrNull { it.id == id } as? GridItem.Folder
            if (folder != null) {
                FolderPopup(folder = folder, onDismiss = { openedFolderId = null })
            } else {
                openedFolderId = null
            }
        }
    }
}

@Composable
private fun GridItemCell(item: GridItem, onTap: () -> Unit, onLongPress: (Offset) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(item.id) {
                detectTapGestures(
                    onTap = { onTap() },
                    onLongPress = { offset -> onLongPress(offset) },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        when (item) {
            is GridItem.AppShortcut -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AppIcon(app = item.app, modifier = Modifier.size(48.dp))
                Text(
                    text = item.app.label,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            is GridItem.Folder -> Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x66FFFFFF))
                    .padding(8.dp),
                contentAlignment = Alignment.Center,
            ) { Text(item.label, color = Color.White) }
            is GridItem.Widget -> WidgetCell(item = item, modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun DropStrip(
    label: String,
    active: Boolean,
    visible: Boolean,
    onPositioned: (Rect) -> Unit,
) {
    val target = if (visible) 56.dp else 0.dp
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(target)
            .padding(4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (active) Color(0xCCFF5252) else Color(0x33FFFFFF))
            .onGloballyPositioned { onPositioned(it.boundsInRoot()) },
        contentAlignment = Alignment.Center,
    ) {
        if (visible) Text(label, color = Color.White)
    }
}

private fun launchApp(context: android.content.Context, packageName: String, className: String) {
    val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        ?: Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setComponent(ComponentName(packageName, className))
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
}
