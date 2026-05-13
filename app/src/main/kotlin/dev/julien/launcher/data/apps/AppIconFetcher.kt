package dev.julien.launcher.data.apps

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.graphics.createBitmap
import coil.ImageLoader
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.request.Options

/**
 * Coil [Fetcher] that resolves a [ComponentName] to its activity icon drawable from the
 * PackageManager. Falls back to the application icon if the activity icon is missing.
 */
class AppIconFetcher(
    private val data: ComponentName,
    private val context: Context,
) : Fetcher {
    override suspend fun fetch(): FetchResult {
        val pm = context.packageManager
        val drawable: Drawable =
            runCatching { pm.getActivityIcon(data) }
                .recoverCatching { pm.getApplicationIcon(data.packageName) }
                .getOrElse { pm.defaultActivityIcon }
        return DrawableResult(
            drawable = drawable.toBitmapDrawable(context),
            isSampled = false,
            dataSource = DataSource.DISK,
        )
    }

    class Factory(
        private val context: Context,
    ) : Fetcher.Factory<ComponentName> {
        override fun create(
            data: ComponentName,
            options: Options,
            imageLoader: ImageLoader,
        ): Fetcher = AppIconFetcher(data, context)
    }
}

private fun Drawable.toBitmapDrawable(context: Context): BitmapDrawable {
    if (this is BitmapDrawable) return this
    val width = if (intrinsicWidth > 0) intrinsicWidth else 96
    val height = if (intrinsicHeight > 0) intrinsicHeight else 96
    val bitmap: Bitmap = createBitmap(width, height)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, width, height)
    draw(canvas)
    return BitmapDrawable(context.resources, bitmap)
}
