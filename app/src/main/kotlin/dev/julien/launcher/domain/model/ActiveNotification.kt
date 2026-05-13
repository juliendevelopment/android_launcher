package dev.julien.launcher.domain.model

import android.app.PendingIntent

data class ActiveNotification(
    val key: String,
    val packageName: String,
    val postedAt: Long,
    val title: String?,
    val text: String?,
    val contentIntent: PendingIntent?,
    val isClearable: Boolean,
)
