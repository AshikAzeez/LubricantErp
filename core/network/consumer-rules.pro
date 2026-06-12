# ─── Ktor ────────────────────────────────────────────────────────────────────
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

-keep class io.ktor.client.engine.okhttp.** { *; }

# ─── OkHttp ──────────────────────────────────────────────────────────────────
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# ─── Okio ────────────────────────────────────────────────────────────────────
-dontwarn okio.**
-keep class okio.** { *; }

# ─── Kotlinx Serialization ──────────────────────────────────────────────────
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ─── Koin ────────────────────────────────────────────────────────────────────
-keep class org.koin.** { *; }
-dontwarn org.koin.**

# ─── Kotlin Coroutines ───────────────────────────────────────────────────────
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# ─── SLF4J (needed for Ktor logging) ─────────────────────────────────────────
-dontwarn org.slf4j.impl.**

# ─── Core Network Classes ────────────────────────────────────────────────────
-keep class com.havos.lubricerp.core.network.** { *; }
