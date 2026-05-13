package dev.julien.launcher.widgets

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.pm.PackageManager
import android.os.UserHandle
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.julien.launcher.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class WidgetPickerEntry(
    val provider: AppWidgetProviderInfo,
    val appLabel: String,
    val widgetLabel: String,
)

@Singleton
class WidgetRepository
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val appWidgetManager: AppWidgetManager,
        private val appWidgetHost: AppWidgetHost,
        private val packageManager: PackageManager,
        @IoDispatcher private val io: CoroutineDispatcher,
    ) {
        suspend fun listProviders(): List<WidgetPickerEntry> =
            withContext(io) {
                val all: List<AppWidgetProviderInfo> = appWidgetManager.installedProviders
                all
                    .map { info ->
                        val appLabel =
                            runCatching {
                                packageManager
                                    .getApplicationInfo(info.provider.packageName, 0)
                                    .loadLabel(packageManager)
                                    .toString()
                            }.getOrDefault(info.provider.packageName)
                        val widgetLabel = runCatching { info.loadLabel(packageManager) }.getOrDefault(info.provider.shortClassName)
                        WidgetPickerEntry(info, appLabel, widgetLabel)
                    }.sortedWith(compareBy({ it.appLabel.lowercase() }, { it.widgetLabel.lowercase() }))
            }

        fun allocateAppWidgetId(): Int = appWidgetHost.allocateAppWidgetId()

        fun deleteAppWidgetId(id: Int) {
            appWidgetHost.deleteAppWidgetId(id)
        }

        fun bindIfAllowed(
            appWidgetId: Int,
            info: AppWidgetProviderInfo,
        ): Boolean = appWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, info.provider)

        fun bindIntent(
            appWidgetId: Int,
            info: AppWidgetProviderInfo,
            profile: UserHandle? = null,
        ) = android.content.Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, info.provider)
            if (profile != null) putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER_PROFILE, profile)
        }

        fun getInfo(appWidgetId: Int): AppWidgetProviderInfo? = appWidgetManager.getAppWidgetInfo(appWidgetId)
    }
