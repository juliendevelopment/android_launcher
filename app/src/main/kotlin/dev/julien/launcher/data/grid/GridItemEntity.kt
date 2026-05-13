package dev.julien.launcher.data.grid

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class GridItemType { APP, FOLDER, WIDGET }

@Entity(tableName = "grid_items")
data class GridItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cellX: Int,
    val cellY: Int,
    val spanX: Int,
    val spanY: Int,
    val type: GridItemType,
    @ColumnInfo(name = "component_flattened") val componentFlattened: String?,
    @ColumnInfo(name = "folder_id") val folderId: Long?,
    @ColumnInfo(name = "app_widget_id") val appWidgetId: Int?,
)

@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
)

@Entity(
    tableName = "folder_items",
    primaryKeys = ["folder_id", "component_flattened"],
    foreignKeys = [
        ForeignKey(
            entity = FolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folder_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("folder_id")],
)
data class FolderItemEntity(
    @ColumnInfo(name = "folder_id") val folderId: Long,
    @ColumnInfo(name = "component_flattened") val componentFlattened: String,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
)

@Entity(tableName = "widgets")
data class WidgetEntity(
    @PrimaryKey @ColumnInfo(name = "app_widget_id") val appWidgetId: Int,
    @ColumnInfo(name = "provider_flattened") val providerFlattened: String,
)
