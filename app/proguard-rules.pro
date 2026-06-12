# ─── Kotlin ──────────────────────────────────────────────────────────────────
-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature, Exceptions
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-dontnote kotlin.Unit
-dontnote kotlin.collections.*
-dontwarn kotlin.**

-dontwarn java.lang.invoke.StringConcatFactory
-dontwarn java.lang.invoke.VarHandle
-dontwarn org.jetbrains.annotations.**

# Keep Kotlin metadata for reflection (used by Koin, Moshi, etc.)
-keep class kotlin.Metadata { *; }

# ─── Kotlinx Serialization ──────────────────────────────────────────────────
# Keep @Serializable annotation so R8 doesn't strip generated serializers
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, RuntimeInvisibleAnnotations

# Keep generated serializer companions
-keep,includedescriptorclasses class com.havos.lubricerp.**$$serializer { *; }
-keepclassmembers class com.havos.lubricerp.** {
    *** Companion;
}
-keepclasseswithmembers class com.havos.lubricerp.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep kotlinx.serialization itself
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ─── Ktor ────────────────────────────────────────────────────────────────────
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# Ktor OkHttp engine
-keep class io.ktor.client.engine.okhttp.** { *; }
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okio.**
-keep class okio.** { *; }

# Ktor content negotiation & serialization plugin
-keep class io.ktor.serialization.** { *; }
-keep class io.ktor.client.plugins.contentnegotiation.** { *; }

# ─── Koin ────────────────────────────────────────────────────────────────────
-keep class org.koin.** { *; }
-dontwarn org.koin.**
-keep class * extends org.koin.core.module.Module { *; }

# ─── Room ────────────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-dontwarn androidx.room.paging.**

# ─── Coil ────────────────────────────────────────────────────────────────────
-keep class coil.** { *; }
-dontwarn coil.**

# ─── AndroidX DataStore ──────────────────────────────────────────────────────
-keep class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite { *; }
-keep class androidx.datastore.** { *; }

# ─── AndroidX Security / Crypto ──────────────────────────────────────────────
-keep class androidx.security.crypto.** { *; }

# ─── AndroidX Lifecycle ──────────────────────────────────────────────────────
-keep class androidx.lifecycle.** { *; }
-keepclassmembers class * implements androidx.lifecycle.LifecycleObserver {
    <methods>;
}

# ─── Jetpack Compose ─────────────────────────────────────────────────────────
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
# Keep compose stability markers from being stripped
-keep class * {
    @androidx.compose.runtime.Stable <fields>;
    @androidx.compose.runtime.Immutable <fields>;
}
-keepclassmembers class * {
    @androidx.compose.runtime.Stable *;
    @androidx.compose.runtime.Immutable *;
}

# ─── Kotlin Coroutines ───────────────────────────────────────────────────────
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# ─── Application-wide Keep Rules ─────────────────────────────────────────────
# Note: Feature-specific DTOs, models, viewmodels, network, and common classes
# are kept via their respective library module consumer-rules.pro configurations.


# Keep all activity/fragment classes
-keep class * extends androidx.activity.ComponentActivity { *; }
-keep class * extends androidx.fragment.app.Fragment { *; }
