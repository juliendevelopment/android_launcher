package dev.julien.launcher.data.grid

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GridItemDao {
    @Query("SELECT * FROM grid_items") fun observeItems(): Flow<List<GridItemEntity>>

    @Query("SELECT * FROM grid_items") suspend fun listItems(): List<GridItemEntity>

    @Query("SELECT * FROM grid_items WHERE id = :id LIMIT 1")
    suspend fun itemById(id: Long): GridItemEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertItem(item: GridItemEntity): Long

    @Update suspend fun updateItem(item: GridItemEntity)

    @Delete suspend fun deleteItem(item: GridItemEntity)

    @Query("DELETE FROM grid_items WHERE id = :id") suspend fun deleteItemById(id: Long)

    @Query("SELECT * FROM folders") fun observeFolders(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE id = :id LIMIT 1")
    suspend fun folderById(id: Long): FolderEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertFolder(folder: FolderEntity): Long

    @Update suspend fun updateFolder(folder: FolderEntity)

    @Query("DELETE FROM folders WHERE id = :id") suspend fun deleteFolder(id: Long)

    @Query("SELECT * FROM folder_items WHERE folder_id = :folderId ORDER BY sort_order ASC")
    suspend fun folderItems(folderId: Long): List<FolderItemEntity>

    @Query("SELECT * FROM folder_items") fun observeAllFolderItems(): Flow<List<FolderItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolderItem(item: FolderItemEntity)

    @Query("DELETE FROM folder_items WHERE folder_id = :folderId AND component_flattened = :component")
    suspend fun removeFolderItem(folderId: Long, component: String)

    @Query("SELECT COUNT(*) FROM folder_items WHERE folder_id = :folderId")
    suspend fun folderItemCount(folderId: Long): Int

    @Query("SELECT * FROM widgets") fun observeWidgets(): Flow<List<WidgetEntity>>

    @Query("SELECT * FROM widgets WHERE app_widget_id = :id LIMIT 1")
    suspend fun widgetById(id: Int): WidgetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWidget(widget: WidgetEntity)

    @Query("DELETE FROM widgets WHERE app_widget_id = :id")
    suspend fun deleteWidget(id: Int)

    /**
     * Insert an app shortcut row. Returns its generated id.
     */
    @Transaction
    suspend fun insertAppShortcut(component: String, cellX: Int, cellY: Int): Long =
        insertItem(
            GridItemEntity(
                cellX = cellX, cellY = cellY, spanX = 1, spanY = 1,
                type = GridItemType.APP,
                componentFlattened = component,
                folderId = null, appWidgetId = null,
            ),
        )
}
