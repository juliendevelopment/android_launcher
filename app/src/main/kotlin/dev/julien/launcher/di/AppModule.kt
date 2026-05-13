package dev.julien.launcher.di

import android.app.WallpaperManager
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.julien.launcher.data.LauncherDatabase
import dev.julien.launcher.data.grid.GridItemDao
import dev.julien.launcher.widgets.LAUNCHER_WIDGET_HOST_ID
import dev.julien.launcher.widgets.LauncherAppWidgetHost
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton
    fun providePackageManager(
        @ApplicationContext context: Context,
    ): PackageManager = context.packageManager

    @Provides @Singleton
    fun provideAppWidgetManager(
        @ApplicationContext context: Context,
    ): AppWidgetManager = AppWidgetManager.getInstance(context)

    @Provides @Singleton
    fun provideAppWidgetHost(
        @ApplicationContext context: Context,
    ): AppWidgetHost = LauncherAppWidgetHost(context, LAUNCHER_WIDGET_HOST_ID)

    @Provides @Singleton
    fun provideWallpaperManager(
        @ApplicationContext context: Context,
    ): WallpaperManager = WallpaperManager.getInstance(context)

    @Provides @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): LauncherDatabase =
        Room
            .databaseBuilder(context, LauncherDatabase::class.java, "launcher.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideGridDao(db: LauncherDatabase): GridItemDao = db.gridDao()

    @Provides @Singleton @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides @Singleton @ApplicationScope
    fun provideAppScope(
        @IoDispatcher dispatcher: CoroutineDispatcher,
    ): CoroutineScope = CoroutineScope(SupervisorJob() + dispatcher)
}
