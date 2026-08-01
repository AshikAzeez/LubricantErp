# ─── ViewModels (Koin instantiates via constructor reflection) ────────────────
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# ─── Koin ViewModel Factory (androidx.viewmodel DSL) ─────────────────────────
-keep class org.koin.androidx.viewmodel.** { *; }
-keep class org.koin.androidx.compose.** { *; }

# ─── Presentation Layer (DI managed by Koin) ────────────────────────────────
# Screens, contracts, reducers, navigation, DI module. ViewModels covered above.
-keep class com.havos.lubricerp.feature_reports.presentation.navigation.** { *; }
-keep class com.havos.lubricerp.feature_reports.presentation.di.** { *; }

# ─── Compose stability markers & shared state classes ────────────────────────
-keep class com.havos.lubricerp.feature_reports.domain.model.** { *; }
-keep class com.havos.lubricerp.core.common.** { *; }

# ─── Kotlinx Serialization (presentation may serialize nav args) ─────────────
-keep,includedescriptorclasses class com.havos.lubricerp.feature_reports.presentation.**$$serializer { *; }
-keepclassmembers class com.havos.lubricerp.feature_reports.presentation.** {
    *** Companion;
}
-keepclasseswithmembers class com.havos.lubricerp.feature_reports.presentation.** {
    kotlinx.serialization.KSerializer serializer(...);
}
