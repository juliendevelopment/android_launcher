package dev.julien.launcher

import android.app.Application
import android.appwidget.AppWidgetHost
import dagger.hilt.android.HiltAndroidApp
import dev.julien.launcher.widgets.LAUNCHER_WIDGET_HOST_ID
import javax.inject.Inject

@HiltAndroidApp
class LauncherApplication : Application() {

    @Inject lateinit var appWidgetHost: AppWidgetHost

    override fun onCreate() {
        super.onCreate()
        // The host id MUST be stable across process restarts; otherwise the system
        // discards bound widget ids. See widgets/WidgetModule for the bound instance.
        check(LAUNCHER_WIDGET_HOST_ID == 1024)
    }
}
