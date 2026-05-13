package dev.julien.launcher

import android.app.Application
import android.appwidget.AppWidgetHost
import coil.ImageLoader
import coil.ImageLoaderFactory
import dagger.hilt.android.HiltAndroidApp
import dev.julien.launcher.data.LauncherImageLoaderFactory
import dev.julien.launcher.widgets.LAUNCHER_WIDGET_HOST_ID
import javax.inject.Inject

@HiltAndroidApp
class LauncherApplication :
    Application(),
    ImageLoaderFactory {
    @Inject lateinit var appWidgetHost: AppWidgetHost

    @Inject lateinit var imageLoaderFactory: LauncherImageLoaderFactory

    override fun onCreate() {
        super.onCreate()
        check(LAUNCHER_WIDGET_HOST_ID == 1024)
    }

    override fun newImageLoader(): ImageLoader = imageLoaderFactory.newImageLoader()
}
