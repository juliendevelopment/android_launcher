# android-launcher

A minimal but functional Android home launcher, written from scratch in Kotlin + Jetpack Compose.

This is not a fork of AOSP Launcher3. The home grid, drag-and-drop, drawer, folder, widget host, and notification shade are implemented directly against the Android platform APIs.

> v0.1.0 ships an APK signed with the Android debug keystore. **This APK is not Play-Store-ready.** See [Signing for distribution](#signing-for-distribution) for the path to a release keystore.

## Features (v0.1.0)

- Registers as an Android HOME launcher (`android.intent.category.HOME`, `singleTask`).
- **App drawer**: alphabetical, locale-aware, with a search field that filters as you type. Long-press an icon to start a drag onto the home grid. Updates reactively on `PACKAGE_ADDED` / `PACKAGE_REMOVED` / `PACKAGE_CHANGED`.
- **Home grid**: 5 × 6 cells with three kinds of cell occupants — app shortcuts, folders, and widgets. Persisted in Room.
- **Drag-and-drop**:
  - app on empty → place
  - app on app → create a folder containing both
  - app on folder → add to folder
  - drop on the top strip → remove from home (does **not** uninstall)
  - drop on the bottom strip (apps only) → trigger `ACTION_DELETE` (system uninstall)
  - rejected drops snap back
- **Widgets**: stable `AppWidgetHost` (id `1024`), proper `startListening` / `stopListening` lifecycle hooks, `AppWidgetHostView` rendered through Compose `AndroidView` interop (RemoteViews require the View system — Compose alone cannot host them).
- **Notifications**: `NotificationListenerService` exposes active notifications as a `StateFlow`. Tap → fire `contentIntent`; press *Dismiss* on a clearable notification → `cancelNotification(key)`. A one-tap deep link to `Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS` walks the user through granting access.
- **Folders**: tap a folder to open a popup, tap an entry to launch.

## Roadmap (what v0.1.0 does **not** include)

- Widget picker UI (you can long-press an empty cell to bring up the picker is implemented under the hood — the *visible picker screen* is the gap; widgets bound through a future release flow will render correctly today).
- Icon packs / themed icons.
- Multi-page home, dock row, page indicator.
- Wallpaper picker.
- Gestures beyond swipe-up (drawer) and swipe-down (shade).
- Search providers (web/contacts/files).
- Work profile / multi-user support.
- Backup and restore beyond the platform auto-backup of the Room database.
- Play-Store distribution (debug-signed APK only).

## Build

Requirements:
- JDK 21
- Android SDK with API 35 platform installed
- macOS / Linux / Windows

```bash
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`.

Also useful:
```bash
./gradlew spotlessApply         # format all Kotlin files
./gradlew spotlessCheck         # CI uses this to enforce formatting
./gradlew :app:lintDebug        # Android lint
./gradlew :app:testDebugUnitTest
```

## Install and run

1. Enable *Install from unknown sources* on your device for whichever app you're using to sideload (e.g. Files, Drive).
2. Download the APK from the [Releases page](../../releases).
3. Install: `adb install -r launcher-v0.1.0.apk` or open the APK in the file picker.
4. Press the HOME button — Android will show a launcher chooser. Select **Launcher** and tap *Always* (or *Just once*).
5. Granting notification access:
   - First time the shade is opened the launcher detects access is missing and shows a prompt.
   - Tap *Open settings* → toggle **Launcher** on in *Notification access*.
   - Return to the home screen; notifications appear immediately.

## Cutting a release

```bash
git tag v0.1.0
git push --tags
```

This triggers `.github/workflows/release.yml`, which:
1. Sets up JDK 21 + Android SDK + Gradle.
2. Runs `./gradlew :app:assembleRelease`.
3. Renames the APK to `launcher-v0.1.0.apk`.
4. Creates a GitHub Release with auto-generated notes and attaches the APK.

## Signing for distribution

v0.1.0 release builds are signed with the debug keystore. Replace with a real keystore by:

1. Generate a release keystore locally:
   ```bash
   keytool -genkey -v -keystore release.keystore -alias launcher \
       -keyalg RSA -keysize 2048 -validity 10000
   ```
2. Base64 it:
   ```bash
   base64 -w 0 release.keystore > release.keystore.b64
   ```
3. Add four GitHub Actions secrets to the repository:
   - `ANDROID_KEYSTORE_BASE64` (contents of `release.keystore.b64`)
   - `KEYSTORE_PASSWORD`
   - `KEY_ALIAS`
   - `KEY_PASSWORD`
4. Uncomment the *Decode release keystore* step in `.github/workflows/release.yml` and the `env:` block on *Assemble release*.

The `release` `signingConfig` in `app/build.gradle.kts` already reads `ANDROID_KEYSTORE_PATH` / `KEYSTORE_PASSWORD` / `KEY_ALIAS` / `KEY_PASSWORD` from the environment.

## Architecture

Single-module hexagonal-lite:

```
dev.julien.launcher
├── domain/        pure-Kotlin models + use cases (no Android types)
├── data/          repositories (PackageManager, Room grid, NotificationListener bridge, AppWidget)
├── notifications/ NotificationListenerService implementation
├── widgets/       AppWidgetHost subclass + widget repository
├── ui/
│   ├── drag/      drag session state + floating preview layer
│   ├── drawer/    drawer screen + ViewModel
│   ├── home/      home grid + folder popup + widget cell
│   └── notifications/ shade screen + ViewModel
└── di/            Hilt singletons
```

ViewModels expose `StateFlow<UiState>`. No `runBlocking` in production code. No `GlobalScope`.

## Platform constraints worth flagging

- **Widget hosting requires `AndroidView` interop.** RemoteViews can't be rendered by Compose alone; the launcher hosts widgets via `AppWidgetHostView`.
- **Notification access isn't a runtime permission.** Users must toggle the launcher in *Settings → Notifications → Notification access*. The launcher deep-links there, but cannot grant access programmatically.
- **`QUERY_ALL_PACKAGES`** is declared so the launcher can list installable apps. For a launcher this is the legitimate use case, but on Play Store this requires justification.
- **`REQUEST_DELETE_PACKAGES`** is declared so the "Uninstall" drop zone can fire `ACTION_DELETE`. The system still asks the user to confirm each uninstall.

## License

MIT. See [LICENSE](LICENSE).
