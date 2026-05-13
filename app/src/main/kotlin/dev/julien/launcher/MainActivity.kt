package dev.julien.launcher

import android.appwidget.AppWidgetHost
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import dev.julien.launcher.ui.LauncherRoot
import dev.julien.launcher.ui.theme.LauncherTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var appWidgetHost: AppWidgetHost

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            LauncherTheme {
                LauncherRoot()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        appWidgetHost.startListening()
    }

    override fun onStop() {
        super.onStop()
        // Tolerate already-stopped host (e.g. process death races).
        runCatching { appWidgetHost.stopListening() }
    }

    override fun onBackPressed() {
        // A launcher swallows BACK from its home screen — leaving here would expose
        // the previous task or system UI.
    }
}
