# ─── Feature Reports Domain Models ───────────────────────────────────────────
-keep class com.havos.lubricerp.feature_reports.domain.model.** { *; }

# ─── Domain Use Cases (instantiated by Koin via constructor reflection) ──────
-keep class com.havos.lubricerp.feature_reports.domain.usecase.** { *; }

# ─── Domain Repository Interfaces (bound by Koin to data-layer impls) ────────
-keep interface com.havos.lubricerp.feature_reports.domain.repository.** { *; }
-keep class com.havos.lubricerp.feature_reports.domain.repository.** { *; }
