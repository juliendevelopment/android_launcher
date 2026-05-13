package dev.julien.launcher.ui.home.folder

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.julien.launcher.domain.model.AppEntry
import dev.julien.launcher.domain.model.GridItem
import dev.julien.launcher.ui.common.AppIcon

@Composable
fun FolderPopup(folder: GridItem.Folder, onDismiss: () -> Unit) {
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xF0181820))
                .padding(16.dp)
                .width(320.dp),
        ) {
            Column {
                Text(folder.label, color = Color.White, modifier = Modifier.padding(bottom = 8.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(items = folder.apps, key = { it.componentFlattened }) { app ->
                        FolderEntry(app = app, onLaunch = { launchApp(context, app); onDismiss() })
                    }
                }
            }
        }
    }
}

@Composable
private fun FolderEntry(app: AppEntry, onLaunch: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(4.dp)
            .pointerInput(app.componentFlattened) {
                detectTapGestures(onTap = { onLaunch() })
            },
    ) {
        AppIcon(app = app, modifier = Modifier.size(48.dp))
        Text(
            text = app.label,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun launchApp(context: android.content.Context, app: AppEntry) {
    val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
        ?: android.content.Intent(android.content.Intent.ACTION_MAIN)
            .addCategory(android.content.Intent.CATEGORY_LAUNCHER)
            .setComponent(android.content.ComponentName(app.packageName, app.className))
    intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
}
