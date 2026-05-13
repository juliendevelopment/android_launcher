package dev.julien.launcher.ui.common

import android.content.ComponentName
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.julien.launcher.domain.model.AppEntry

@Composable
fun AppIcon(
    app: AppEntry,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val component = ComponentName(app.packageName, app.className)
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        AsyncImage(
            model = ImageRequest.Builder(context).data(component).build(),
            contentDescription = app.label,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
