package dev.julien.launcher.notifications

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import dagger.hilt.android.AndroidEntryPoint
import dev.julien.launcher.data.notifications.NotificationRepository
import dev.julien.launcher.domain.model.ActiveNotification
import javax.inject.Inject

@AndroidEntryPoint
class LauncherNotificationListener : NotificationListenerService() {

    @Inject lateinit var repository: NotificationRepository

    override fun onListenerConnected() {
        super.onListenerConnected()
        repository.registerDismisser { key -> runCatching { cancelNotification(key) } }
        refresh()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) { refresh() }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) { refresh() }

    private fun refresh() {
        val current = runCatching { activeNotifications.orEmpty() }.getOrDefault(emptyArray())
        repository.publish(current.map { it.toDomain() })
    }
}

private fun StatusBarNotification.toDomain(): ActiveNotification {
    val extras = notification?.extras
    val title = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString()
    val text = extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString()
    return ActiveNotification(
        key = key,
        packageName = packageName,
        postedAt = postTime,
        title = title,
        text = text,
        contentIntent = notification?.contentIntent,
        isClearable = isClearable,
    )
}
