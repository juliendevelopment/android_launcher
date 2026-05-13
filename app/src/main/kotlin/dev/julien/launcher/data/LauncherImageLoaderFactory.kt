package dev.julien.launcher.data

import android.content.Context
import coil.ImageLoader
import coil.ImageLoaderFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.julien.launcher.data.apps.AppIconFetcher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LauncherImageLoaderFactory @Inject constructor(
    @ApplicationContext private val context: Context,
) : ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(context)
            .components { add(AppIconFetcher.Factory(context)) }
            .crossfade(false)
            .build()
}
