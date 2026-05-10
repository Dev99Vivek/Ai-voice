# SHADOW ProGuard Rules

# Keep all application code
-keep class com.shadow.ai.** { *; }

# Keep accessibility service
-keep class com.shadow.ai.services.ShadowAccessibilityService { *; }

# Keep Room DB entities
-keep class com.shadow.ai.data.** { *; }
-keepclassmembers class com.shadow.ai.data.** { *; }

# Room generated code
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keepclassmembers @androidx.room.Entity class * { *; }

# Keep Gson models
-keep class com.shadow.ai.models.** { *; }
-keepclassmembers class com.shadow.ai.models.** { *; }
-keep enum com.shadow.ai.models.** { *; }

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory { *; }
-keep class * implements com.google.gson.JsonSerializer { *; }
-keep class * implements com.google.gson.JsonDeserializer { *; }

# ML Kit
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }

# Compose
-keep class androidx.compose.** { *; }

# Lifecycle
-keep class androidx.lifecycle.** { *; }
-keepclassmembers class androidx.lifecycle.** { *; }

# SpeechRecognizer
-keep class android.speech.** { *; }

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}
