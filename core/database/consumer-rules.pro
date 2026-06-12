# ─── Room ────────────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-dontwarn androidx.room.paging.**

# ─── AndroidX DataStore ──────────────────────────────────────────────────────
-keep class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite { *; }
-keep class androidx.datastore.** { *; }

# ─── AndroidX Security ───────────────────────────────────────────────────────
-keep class androidx.security.crypto.** { *; }

# ─── Google Tink (needed for AndroidX Security Crypto) ────────────────────────
-dontwarn com.google.api.client.http.**
-dontwarn com.google.errorprone.annotations.**
-dontwarn org.joda.time.**
