# Keep Room generated classes
-keep class * extends androidx.room.RoomDatabase
-keepclassmembers class * { @androidx.room.* <fields>; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.* { *; }

# Compose
-keep class androidx.compose.runtime.** { *; }

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class dev.julien.launcher.**$$serializer { *; }
-keepclassmembers class dev.julien.launcher.** {
    *** Companion;
}
-keepclasseswithmembers class dev.julien.launcher.** {
    kotlinx.serialization.KSerializer serializer(...);
}
