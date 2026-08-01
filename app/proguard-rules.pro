# ═══════════════════════════════════════════════════════════════════════════════
# R8 / ProGuard – Hardened configuration for sideloaded APK distribution
# ═══════════════════════════════════════════════════════════════════════════════

# ─── Aggressive Optimization ─────────────────────────────────────────────────
-optimizationpasses 5
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-allowaccessmodification
# NOTE: -overloadaggressively is intentionally OMITTED. It renames methods to
# identical names (differing only by return type), which breaks Kotlin reflection,
# Koin DI constructor resolution, kotlinx.serialization descriptors, and Ktor.
-repackageclasses ''

# Strip ALL logs in release (android.util.Log calls become no-ops)
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
    public static int wtf(...);
}

# Strip System.out / System.err print calls
-assumenosideeffects class java.io.PrintStream {
    public void println(...);
    public void print(...);
}

# ─── Attributes ──────────────────────────────────────────────────────────────
-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature, Exceptions
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, RuntimeInvisibleAnnotations
# Strip source file names and line numbers from stack traces
-renamesourcefileattribute ''
-dontnote kotlin.Unit
-dontnote kotlin.collections.*
-dontwarn kotlin.**
-dontwarn java.lang.invoke.StringConcatFactory
-dontwarn java.lang.invoke.VarHandle
-dontwarn org.jetbrains.annotations.**

# Keep Kotlin metadata for reflection (used by Koin)
-keep class kotlin.Metadata { *; }

# ─── Kotlin Reflection (required by Koin type resolution) ───────────────────
-keep class kotlin.reflect.** { *; }
-dontwarn kotlin.reflect.**

# ─── Kotlinx Serialization ──────────────────────────────────────────────────
# Keep generated serializer companions (narrow – app package only)
-keep,includedescriptorclasses class com.havos.lubricerp.**$$serializer { *; }
-keepclassmembers class com.havos.lubricerp.** {
    *** Companion;
}
-keepclasseswithmembers class com.havos.lubricerp.** {
    kotlinx.serialization.KSerializer serializer(...);
}
# Keep kotlinx.serialization core (required for runtime)
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ─── Ktor (minimal keeps – let R8 shrink the rest) ──────────────────────────
-keep class io.ktor.client.engine.okhttp.** { *; }
-keep class io.ktor.serialization.kotlinx.** { *; }
-keep class io.ktor.client.plugins.contentnegotiation.** { *; }
-dontwarn io.ktor.**

# OkHttp / OkIO (engine dependency)
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# ─── Koin (keep core + android ViewModel factory) ───────────────────────────
-keep class org.koin.core.** { *; }
-keep class org.koin.androidx.viewmodel.** { *; }
-keep class * extends org.koin.core.module.Module { *; }
-dontwarn org.koin.**

# ─── Room ────────────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-dontwarn androidx.room.paging.**

# ─── AndroidX DataStore ──────────────────────────────────────────────────────
-keep class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite { *; }

# ─── AndroidX Security / Crypto ──────────────────────────────────────────────
-keep class androidx.security.crypto.** { *; }

# ─── AndroidX Lifecycle ──────────────────────────────────────────────────────
-keepclassmembers class * implements androidx.lifecycle.LifecycleObserver {
    <methods>;
}

# ─── Jetpack Compose (minimal) ──────────────────────────────────────────────
-dontwarn androidx.compose.**
-keep class * {
    @androidx.compose.runtime.Stable <fields>;
    @androidx.compose.runtime.Immutable <fields>;
}

# ─── Enums ────────────────────────────────────────────────────────────────────
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ─── Kotlin Coroutines ───────────────────────────────────────────────────────
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
# Keep suspend function machinery (ContinuationImpl, DispatchedTask, etc.)
-keep class kotlinx.coroutines.internal.** { *; }
-keep class kotlinx.coroutines.scheduling.** { *; }
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# ─── Application classes ─────────────────────────────────────────────────────
-keep class * extends androidx.activity.ComponentActivity { *; }
-keep class * extends androidx.fragment.app.Fragment { *; }
-keep class * extends android.app.Application { *; }

# ─── App DI Module ────────────────────────────────────────────────────────────
-keep class com.havos.lubricerp.di.** { *; }

# ─── AndroidX Paging ─────────────────────────────────────────────────────────
-keep class * extends androidx.paging.PagingSource { *; }
-keep class androidx.paging.** { *; }
-dontwarn androidx.paging.**

# ─── AndroidX Navigation ──────────────────────────────────────────────────────
-keep class androidx.navigation.** { *; }

# ─── Security: keep SecurityGuard class names for reflection-free checks ─────
-keep class com.havos.lubricerp.security.** { *; }
