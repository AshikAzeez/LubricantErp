# ─── DTOs (kotlinx.serialization) ────────────────────────────────────────────
-keep,includedescriptorclasses class com.havos.lubricerp.feature_reports.data.dto.**$$serializer { *; }
-keepclassmembers class com.havos.lubricerp.feature_reports.data.dto.** {
    *** Companion;
}
-keepclasseswithmembers class com.havos.lubricerp.feature_reports.data.dto.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep class com.havos.lubricerp.feature_reports.data.dto.** { *; }

# ─── Domain Models (used for serialization) ──────────────────────────────────
-keep,includedescriptorclasses class com.havos.lubricerp.feature_reports.domain.model.**$$serializer { *; }
-keepclassmembers class com.havos.lubricerp.feature_reports.domain.model.** {
    *** Companion;
}
-keepclasseswithmembers class com.havos.lubricerp.feature_reports.domain.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep class com.havos.lubricerp.feature_reports.domain.model.** { *; }

# ─── Data Layer (DI managed by Koin) ────────────────────────────────────────
# Remote APIs, data sources, repositories, mappers, paging sources, DI module
-keep class com.havos.lubricerp.feature_reports.data.remote.** { *; }
-keep class com.havos.lubricerp.feature_reports.data.repository.** { *; }
-keep class com.havos.lubricerp.feature_reports.data.mapper.** { *; }
-keep class com.havos.lubricerp.feature_reports.data.paging.** { *; }
