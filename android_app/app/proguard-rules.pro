# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep data models
-keep class jp.hsbl.bakusui.model.** { *; }

# Keep JSON serialization
-keep class com.google.gson.** { *; }

# Keep Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Media3
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

