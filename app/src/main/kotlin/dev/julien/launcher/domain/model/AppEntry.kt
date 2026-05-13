package dev.julien.launcher.domain.model

/**
 * A launchable activity reported by [android.content.pm.PackageManager].
 */
data class AppEntry(
    val packageName: String,
    val className: String,
    val label: String,
) {
    val componentFlattened: String get() = "$packageName/$className"

    companion object {
        fun fromFlattened(flat: String): Pair<String, String> {
            val parts = flat.split('/', limit = 2)
            return parts[0] to parts.getOrElse(1) { "" }
        }
    }
}
