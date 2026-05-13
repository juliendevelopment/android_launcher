package dev.julien.launcher.data.notifications

import android.content.Context
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.julien.launcher.domain.model.ActiveNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Process-singleton bridge between the [LauncherNotificationListener] system service and
 * the launcher UI. The service writes; the UI reads.
 */
@Singleton
class NotificationRepository
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val _active = MutableStateFlow<List<ActiveNotification>>(emptyList())
        val active: StateFlow<List<ActiveNotification>> = _active.asStateFlow()

        private var dismisser: (suspend (String) -> Unit)? = null

        fun publish(items: List<ActiveNotification>) {
            _active.value = items
        }

        fun registerDismisser(fn: suspend (String) -> Unit) {
            dismisser = fn
        }

        suspend fun dismiss(key: String) {
            dismisser?.invoke(key)
        }

        fun isAccessGranted(): Boolean = NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)

        fun openSettingsIntent() =
            android.content
                .Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
    }
