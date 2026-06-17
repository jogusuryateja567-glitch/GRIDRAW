# ProGuard rules for GRIDRAW

# Keep Room entities
-keep class com.gridraw.app.data.** { *; }

# Keep Compose-related classes
-keep class androidx.compose.** { *; }

# Palette
-keep class androidx.palette.** { *; }

# CameraX
-keep class androidx.camera.** { *; }

# General Android
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn okhttp3.**
-dontwarn okio.**
