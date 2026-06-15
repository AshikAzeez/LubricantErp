# ─── ViewModels ──────────────────────────────────────────────────────────────
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# ─── Presentation Layer (DI managed by Koin) ────────────────────────────────
# Screens, contracts, reducers, navigation, DI module. ViewModels covered above.
-keep class com.havos.lubricerp.feature_reports.presentation.navigation.** { *; }
-keep class com.havos.lubricerp.feature_reports.presentation.di.** { *; }

# ─── Compose stability markers ───────────────────────────────────────────────
-keep class com.havos.lubricerp.feature_reports.domain.model.** { *; }
-keep class com.havos.lubricerp.core.common.** { *; }
