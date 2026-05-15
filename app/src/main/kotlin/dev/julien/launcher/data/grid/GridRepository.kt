package dev.julien.launcher.data.grid

import dev.julien.launcher.data.apps.PackageManagerRepository
import dev.julien.launcher.di.ApplicationScope
import dev.julien.launcher.domain.model.AppEntry
import dev.julien.launcher.domain.model.GridCoords
import dev.julien.launcher.domain.model.GridItem
import dev.julien.launcher.domain.model.GridLayout
import dev.julien.launcher.domain.model.GridSpan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GridRepository
    @Inject
    constructor(
        private val dao: GridItemDao,
        private val packageManager: PackageManagerRepository,
        @ApplicationScope private val scope: CoroutineScope,
    ) {
        val layout: Flow<GridLayout> =
            combine(
                dao.observeItems(),
                dao.observeFolders(),
                dao.observeAllFolderItems(),
                dao.observeWidgets(),
                packageManager.installedApps,
            ) { items, folders, folderItems, widgets, apps ->
                val byComponent: Map<String, AppEntry> = apps.associateBy { it.componentFlattened }
                val itemsByFolderId = folderItems.groupBy { it.folderId }
                val widgetById = widgets.associateBy { it.appWidgetId }

                val mapped: List<GridItem> =
                    items.mapNotNull { entity ->
                        val coords =
                            runCatching { GridCoords(entity.cellX, entity.cellY) }.getOrNull()
                                ?: return@mapNotNull null
                        when (entity.type) {
                            GridItemType.APP -> {
                                val app = entity.componentFlattened?.let(byComponent::get) ?: return@mapNotNull null
                                GridItem.AppShortcut(entity.id, coords, app)
                            }
                            GridItemType.FOLDER -> {
                                val folderId = entity.folderId ?: return@mapNotNull null
                                val folder = folders.firstOrNull { it.id == folderId } ?: return@mapNotNull null
                                val contained =
                                    itemsByFolderId[folderId]
                                        .orEmpty()
                                        .mapNotNull { byComponent[it.componentFlattened] }
                                GridItem.Folder(entity.id, coords, folder.label, contained)
                            }
                            GridItemType.WIDGET -> {
                                val appWidgetId = entity.appWidgetId ?: return@mapNotNull null
                                val provider = widgetById[appWidgetId]?.providerFlattened ?: return@mapNotNull null
                                val span =
                                    runCatching { GridSpan(entity.spanX, entity.spanY) }.getOrNull()
                                        ?: return@mapNotNull null
                                GridItem.Widget(entity.id, coords, span, appWidgetId, provider)
                            }
                        }
                    }
                GridLayout(mapped)
            }

        private val cached = layout.stateIn(scope, SharingStarted.Eagerly, GridLayout.EMPTY)

        suspend fun snapshot(): GridLayout = cached.value

        suspend fun placeApp(
            app: AppEntry,
            target: GridCoords,
        ): Long = dao.insertAppShortcut(app.componentFlattened, target.x, target.y)

        suspend fun placeWidget(
            appWidgetId: Int,
            providerFlattened: String,
            target: GridCoords,
            span: GridSpan,
        ): Long {
            dao.insertWidget(WidgetEntity(appWidgetId, providerFlattened))
            return dao.insertItem(
                GridItemEntity(
                    cellX = target.x,
                    cellY = target.y,
                    spanX = span.width,
                    spanY = span.height,
                    type = GridItemType.WIDGET,
                    componentFlattened = null,
                    folderId = null,
                    appWidgetId = appWidgetId,
                ),
            )
        }

        suspend fun createFolder(
            target: GridCoords,
            contents: List<AppEntry>,
        ): Long {
            val folderId = dao.insertFolder(FolderEntity(label = "Folder"))
            contents.forEachIndexed { idx, app ->
                dao.insertFolderItem(FolderItemEntity(folderId, app.componentFlattened, idx))
            }
            return dao.insertItem(
                GridItemEntity(
                    cellX = target.x,
                    cellY = target.y,
                    spanX = 1,
                    spanY = 1,
                    type = GridItemType.FOLDER,
                    componentFlattened = null,
                    folderId = folderId,
                    appWidgetId = null,
                ),
            )
        }

        suspend fun addAppToFolder(
            folderItemId: Long,
            app: AppEntry,
        ) {
            val item = dao.itemById(folderItemId) ?: return
            val folderId = item.folderId ?: return
            val nextOrder = dao.folderItemCount(folderId)
            dao.insertFolderItem(FolderItemEntity(folderId, app.componentFlattened, nextOrder))
        }

        suspend fun removeAppFromFolder(
            folderItemId: Long,
            app: AppEntry,
        ) {
            val item = dao.itemById(folderItemId) ?: return
            val folderId = item.folderId ?: return
            dao.removeFolderItem(folderId, app.componentFlattened)
            // Collapse empty folder.
            if (dao.folderItemCount(folderId) == 0) {
                dao.deleteFolder(folderId)
                dao.deleteItem(item)
            }
        }

        suspend fun moveItem(
            itemId: Long,
            target: GridCoords,
        ) {
            val item = dao.itemById(itemId) ?: return
            dao.updateItem(item.copy(cellX = target.x, cellY = target.y))
        }

        suspend fun remove(itemId: Long) {
            val item = dao.itemById(itemId) ?: return
            // Cannot pass `dao::deleteFolder` to `let` because it is a suspend function
            // and let() only accepts non-suspending function references.
            if (item.type == GridItemType.FOLDER) item.folderId?.let { dao.deleteFolder(it) }
            if (item.type == GridItemType.WIDGET) item.appWidgetId?.let { dao.deleteWidget(it) }
            dao.deleteItem(item)
        }
    }
