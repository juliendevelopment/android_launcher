package dev.julien.launcher.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import dev.julien.launcher.data.grid.FolderEntity
import dev.julien.launcher.data.grid.FolderItemEntity
import dev.julien.launcher.data.grid.GridItemDao
import dev.julien.launcher.data.grid.GridItemEntity
import dev.julien.launcher.data.grid.GridItemType
import dev.julien.launcher.data.grid.WidgetEntity

@Database(
    entities = [
        GridItemEntity::class,
        FolderEntity::class,
        FolderItemEntity::class,
        WidgetEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(GridConverters::class)
abstract class LauncherDatabase : RoomDatabase() {
    abstract fun gridDao(): GridItemDao
}

class GridConverters {
    @TypeConverter fun gridItemTypeToString(value: GridItemType): String = value.name
    @TypeConverter fun stringToGridItemType(value: String): GridItemType = GridItemType.valueOf(value)
}
