package dev.julien.launcher.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.julien.launcher.ui.drag.DragLayer
import dev.julien.launcher.ui.drag.LocalDragSession
import dev.julien.launcher.ui.drag.rememberDragSession
import dev.julien.launcher.ui.drawer.DrawerScreen
import dev.julien.launcher.ui.drawer.DrawerViewModel
import dev.julien.launcher.ui.home.HomeScreen
import dev.julien.launcher.ui.notifications.NotificationShade

@Composable
fun LauncherRoot() {
    val drawerVm: DrawerViewModel = hiltViewModel()
    val dragSession = rememberDragSession()

    var drawerVisible by remember { mutableStateOf(false) }
    var shadeVisible by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalDragSession provides dragSession) {
        Box(modifier = Modifier.fillMaxSize()) {
            HomeScreen(
                modifier = Modifier.fillMaxSize(),
                onOpenDrawer = { drawerVisible = true },
                onOpenShade = { shadeVisible = true },
            )

            AnimatedVisibility(
                visible = drawerVisible,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
            ) {
                DrawerScreen(
                    viewModel = drawerVm,
                    onDismiss = { drawerVisible = false },
                    modifier = Modifier.fillMaxSize().padding(top = 32.dp),
                )
            }

            AnimatedVisibility(
                visible = shadeVisible,
                enter = slideInVertically(initialOffsetY = { -it }),
                exit = slideOutVertically(targetOffsetY = { -it }),
            ) {
                NotificationShade(
                    onDismiss = { shadeVisible = false },
                    modifier = Modifier.fillMaxSize(),
                )
            }

            DragLayer(modifier = Modifier.fillMaxSize())
        }
    }
}
