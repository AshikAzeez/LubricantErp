# ─── ViewModels ──────────────────────────────────────────────────────────────
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# ─── Compose stability markers ───────────────────────────────────────────────
-keep class com.havos.lubricerp.feature_reports.domain.model.** { *; }
-keep class com.havos.lubricerp.core.common.** { *; }
