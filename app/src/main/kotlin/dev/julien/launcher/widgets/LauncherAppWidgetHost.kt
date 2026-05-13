package dev.julien.launcher.widgets

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetProviderInfo
import android.content.Context

const val LAUNCHER_WIDGET_HOST_ID: Int = 1024

/**
 * Subclass exists so we can later swap [onCreateView] for a custom host view that
 * intercepts touches for the drag overlay. v0.1.0 keeps the default.
 */
class LauncherAppWidgetHost(context: Context, hostId: Int) : AppWidgetHost(context, hostId) {
    override fun onCreateView(
        context: Context,
        appWidgetId: Int,
        appWidget: AppWidgetProviderInfo?,
    ): AppWidgetHostView = AppWidgetHostView(context)
}
