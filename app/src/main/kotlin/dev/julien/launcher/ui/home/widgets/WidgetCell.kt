package dev.julien.launcher.ui.home.widgets

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import dev.julien.launcher.domain.model.GridItem

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetCellEntryPoint {
    fun appWidgetHost(): AppWidgetHost

    fun appWidgetManager(): AppWidgetManager
}

/**
 * RemoteViews cannot be rendered by Compose alone — the platform's widget system needs a
 * View-system [AppWidgetHostView]. This is the documented interop boundary.
 */
@Composable
fun WidgetCell(
    item: GridItem.Widget,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val entryPoint =
        remember(context) {
            EntryPointAccessors.fromApplication(
                context.applicationContext,
                WidgetCellEntryPoint::class.java,
            )
        }

    Box(modifier = modifier) {
        AndroidView(
            factory = { ctx ->
                val info = entryPoint.appWidgetManager().getAppWidgetInfo(item.appWidgetId)
                entryPoint.appWidgetHost().createView(ctx, item.appWidgetId, info)
            },
        )
    }
}
