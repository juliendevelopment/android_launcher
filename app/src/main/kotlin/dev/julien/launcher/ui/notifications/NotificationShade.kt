package dev.julien.launcher.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.julien.launcher.R
import dev.julien.launcher.domain.model.ActiveNotification
import kotlinx.coroutines.launch

@Composable
fun NotificationShade(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificationShadeViewModel = hiltViewModel(),
) {
    val items by viewModel.notifications.collectAsState()
    val accessGranted by viewModel.accessGranted.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Surface(
        modifier = modifier,
        color = Color(0xF00D0D10),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 16.dp),
        ) {
            Text(
                "Notifications",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 8.dp),
            )

            if (!accessGranted) {
                AccessPrompt(
                    onOpenSettings = {
                        runCatching { context.startActivity(viewModel.openSettingsIntent()) }
                    },
                    onDismiss = onDismiss,
                )
            } else if (items.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.notifications_empty), color = Color.LightGray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(items = items, key = { it.key }) { n ->
                        NotificationRow(
                            n = n,
                            onTap = {
                                runCatching { n.contentIntent?.send() }
                                onDismiss()
                            },
                            onSwipe = {
                                scope.launch { viewModel.dismiss(n.key) }
                            },
                        )
                    }
                }
            }

            // Tap below to dismiss.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .clickable(onClick = onDismiss)
                    .padding(8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("Close", color = Color.LightGray)
            }
        }
    }
}

@Composable
private fun NotificationRow(n: ActiveNotification, onTap: () -> Unit, onSwipe: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onTap),
        color = Color(0x33FFFFFF),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                n.title ?: n.packageName,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!n.text.isNullOrBlank()) {
                Text(
                    n.text,
                    color = Color.LightGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            if (n.isClearable) {
                OutlinedButton(
                    onClick = onSwipe,
                    modifier = Modifier.padding(top = 6.dp),
                ) { Text("Dismiss") }
            }
        }
    }
}

@Composable
private fun AccessPrompt(onOpenSettings: () -> Unit, onDismiss: () -> Unit) {
    Surface(
        color = Color(0x33FFFFFF),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .padding(top = 8.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.notifications_grant_title), color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(
                stringResource(R.string.notifications_grant_message),
                color = Color.LightGray,
                modifier = Modifier.padding(vertical = 8.dp),
            )
            Row(onOpenSettings, onDismiss)
        }
    }
}

@Composable
private fun Row(onOpenSettings: () -> Unit, onDismiss: () -> Unit) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Button(onClick = onOpenSettings) {
            Text(stringResource(R.string.notifications_grant_open_settings))
        }
        OutlinedButton(onClick = onDismiss) {
            Text(stringResource(R.string.notifications_grant_dismiss))
        }
    }
}
