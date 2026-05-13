package dev.julien.launcher.data.apps

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.UserHandle
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.julien.launcher.di.ApplicationScope
import dev.julien.launcher.di.IoDispatcher
import dev.julien.launcher.domain.model.AppEntry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PackageManagerRepository
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val packageManager: PackageManager,
        @IoDispatcher private val io: CoroutineDispatcher,
        @ApplicationScope private val appScope: CoroutineScope,
    ) {
        private val packageChanges: Flow<Unit> =
            callbackFlow {
                val receiver =
                    object : BroadcastReceiver() {
                        override fun onReceive(
                            ctx: Context?,
                            intent: Intent?,
                        ) {
                            trySend(Unit)
                        }
                    }
                val filter =
                    IntentFilter().apply {
                        addAction(Intent.ACTION_PACKAGE_ADDED)
                        addAction(Intent.ACTION_PACKAGE_REMOVED)
                        addAction(Intent.ACTION_PACKAGE_CHANGED)
                        addAction(Intent.ACTION_PACKAGE_REPLACED)
                        addDataScheme("package")
                    }
                context.registerReceiver(receiver, filter)
                trySend(Unit)
                awaitClose { context.unregisterReceiver(receiver) }
            }

        val installedApps: StateFlow<List<AppEntry>> =
            packageChanges
                .map { queryLauncherActivities() }
                .onStart { emit(queryLauncherActivities()) }
                .flowOn(io)
                .stateIn(appScope, SharingStarted.Eagerly, emptyList())

        private suspend fun queryLauncherActivities(): List<AppEntry> =
            withContext(io) {
                val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
                val resolved: List<ResolveInfo> = packageManager.queryIntentActivities(intent, 0)
                resolved
                    .filter { it.activityInfo?.packageName != context.packageName }
                    .map { ri ->
                        AppEntry(
                            packageName = ri.activityInfo.packageName,
                            className = ri.activityInfo.name,
                            label = ri.loadLabel(packageManager).toString(),
                        )
                    }.distinctBy { it.componentFlattened }
            }

        fun launchAppIntent(app: AppEntry): Intent =
            Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .setComponent(ComponentName(app.packageName, app.className))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)

        fun uninstallIntent(packageName: String): Intent =
            Intent(Intent.ACTION_DELETE, Uri.fromParts("package", packageName, null))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        @Suppress("UNUSED_PARAMETER")
        fun appIconLoadable(
            app: AppEntry,
            user: UserHandle? = null,
        ): Any = ComponentName(app.packageName, app.className)
    }
